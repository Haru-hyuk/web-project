import cx_Oracle
import json
import time
import os
import sys
from datetime import datetime

# Windows 콘솔 인코딩 설정
if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except AttributeError:
        # Python 3.6 이하 호환성
        import io
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 설정 파일 import 시도
try:
    from db_config import DB_CONFIG, BATCH_SIZE, JSON_FILE_PATH
    print("✓ db_config.py에서 설정 로드 완료")
except ImportError:
    print("경고: db_config.py 파일을 찾을 수 없습니다.")
    print("db_config.example.py를 db_config.py로 복사하고 설정을 수정하세요.")
    print("\n명령어: cp db_config.example.py db_config.py")
    sys.exit(1)
except Exception as e:
    print(f"설정 파일 로드 오류: {e}")
    sys.exit(1)

def connect_to_database():
    """오라클 데이터베이스 연결"""
    try:
        connection = cx_Oracle.connect(
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password'],
            dsn=DB_CONFIG['dsn'],
            encoding="UTF-8"
        )
        print(f"✓ 데이터베이스 연결 성공: {DB_CONFIG['user']}@{DB_CONFIG['dsn']}")
        return connection
    except cx_Oracle.Error as error:
        print(f"✗ 데이터베이스 연결 실패: {error}")
        raise

def load_json_data(file_path):
    """JSON 파일에서 데이터 로드"""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            data = json.load(file)
        print(f"✓ JSON 파일 로드 성공: {len(data)}개의 단어")
        return data
    except Exception as error:
        print(f"✗ JSON 파일 로드 실패: {error}")
        raise

def clean_meaning(meaning):
    """meaning 필드에서 마크다운 형식 및 불필요한 따옴표 제거"""
    if not meaning or not isinstance(meaning, str):
        return meaning
    
    # 마크다운 볼드 형식 제거 (**텍스트** -> 텍스트)
    meaning = meaning.replace('**', '')
    
    # 따옴표 제거 ("" 또는 ''로 감싸진 경우)
    meaning = meaning.strip()
    if (meaning.startswith('"') and meaning.endswith('"')) or \
       (meaning.startswith("'") and meaning.endswith("'")):
        meaning = meaning[1:-1]
    
    # 앞뒤 공백 제거
    meaning = meaning.strip()
    
    return meaning

def prepare_batch_data(word_data):
    """단어 데이터를 오라클 INSERT용 튜플로 변환"""
    # embedding을 JSON 문자열로 변환
    embedding_json = json.dumps(word_data.get('embedding', []))

    # exampleSentence 처리
    example_sentence = word_data.get('exampleSentence', {})
    example_en = example_sentence.get('en', '') if example_sentence else ''
    example_ko = example_sentence.get('ko', '') if example_sentence else ''

    # meaning 필드 정리 (마크다운 형식 제거)
    meaning = clean_meaning(word_data.get('meaning', ''))

    # level을 숫자로 변환 (문자열인 경우 처리)
    level_value = word_data.get('level', None)
    if level_value is not None:
        try:
            level_value = int(level_value) if level_value != '' else None
        except (ValueError, TypeError):
            level_value = None

    return (
        word_data.get('word', ''),
        meaning,
        word_data.get('partOfSpeech', ''),
        example_en,
        example_ko,
        word_data.get('category', ''),
        level_value,
        embedding_json
    )

def insert_words_batch(connection, words_batch):
    """배치 단위로 단어 삽입"""
    cursor = connection.cursor()

    # INSERT 쿼리 (WORD_ID는 트리거가 자동 생성)
    # LEVEL은 Oracle 예약어이므로 큰따옴표로 감싸야 함
    insert_query = """
        INSERT INTO WORD (
            WORD, MEANING, PART_OF_SPEECH,
            EXAMPLE_SENTENCE_EN, EXAMPLE_SENTENCE_KO,
            CATEGORY, "LEVEL", EMBEDDING
        ) VALUES (
            :1, :2, :3, :4, :5, :6, :7, :8
        )
    """

    try:
        # 배치 데이터 준비
        batch_data = [prepare_batch_data(word) for word in words_batch]

        # executemany로 배치 삽입
        cursor.executemany(insert_query, batch_data)
        connection.commit()

        return len(batch_data)
    except cx_Oracle.IntegrityError as error:
        # 중복 키 에러 처리 (UNIQUE 제약조건 위반)
        print(f"  경고: 중복 단어 발견, 개별 처리로 전환...")
        connection.rollback()
        return insert_words_individually(connection, words_batch)
    except Exception as error:
        print(f"  ✗ 배치 삽입 실패: {error}")
        connection.rollback()
        raise
    finally:
        cursor.close()

def insert_words_individually(connection, words_batch):
    """개별 단위로 단어 삽입 (중복 체크)"""
    cursor = connection.cursor()
    # LEVEL은 Oracle 예약어이므로 큰따옴표로 감싸야 함
    insert_query = """
        INSERT INTO WORD (
            WORD, MEANING, PART_OF_SPEECH,
            EXAMPLE_SENTENCE_EN, EXAMPLE_SENTENCE_KO,
            CATEGORY, "LEVEL", EMBEDDING
        ) VALUES (
            :1, :2, :3, :4, :5, :6, :7, :8
        )
    """

    inserted_count = 0
    skipped_count = 0

    try:
        for word in words_batch:
            try:
                word_data = prepare_batch_data(word)
                cursor.execute(insert_query, word_data)
                connection.commit()
                inserted_count += 1
            except cx_Oracle.IntegrityError:
                # 중복 단어는 스킵
                skipped_count += 1
                connection.rollback()
                continue
            except Exception as error:
                print(f"  ✗ 단어 '{word.get('word', 'unknown')}' 삽입 실패: {error}")
                connection.rollback()

        if skipped_count > 0:
            print(f"    - {skipped_count}개 중복 단어 스킵됨")

        return inserted_count
    finally:
        cursor.close()

def check_file_exists(file_path):
    """파일 존재 여부 확인"""
    if not os.path.exists(file_path):
        print(f"✗ 파일을 찾을 수 없습니다: {file_path}")
        return False
    return True

def get_table_count(connection):
    """현재 WORD 테이블의 레코드 수 조회"""
    try:
        cursor = connection.cursor()
        cursor.execute("SELECT COUNT(*) FROM WORD")
        count = cursor.fetchone()[0]
        cursor.close()
        return count
    except Exception as error:
        print(f"  경고: 테이블 카운트 조회 실패: {error}")
        return None

def get_existing_words(connection):
    """데이터베이스에 이미 존재하는 단어 목록 조회"""
    try:
        cursor = connection.cursor()
        cursor.execute("SELECT WORD FROM WORD")
        existing_words = {row[0].lower() for row in cursor.fetchall()}
        cursor.close()
        return existing_words
    except Exception as error:
        print(f"  경고: 기존 단어 목록 조회 실패: {error}")
        return set()

def main():
    """메인 함수"""
    start_time = time.time()

    print("=" * 70)
    print("오라클 데이터베이스 단어 데이터 삽입 프로그램")
    print("=" * 70)

    # JSON 파일 경로
    json_file_path = JSON_FILE_PATH

    try:
        # 0. 파일 존재 확인
        if not check_file_exists(json_file_path):
            print(f"\n현재 디렉토리: {os.getcwd()}")
            print(f"찾으려는 파일: {json_file_path}")
            return 1

        # 1. JSON 데이터 로드
        print("\n[1/4] JSON 데이터 로딩...")
        words_data = load_json_data(json_file_path)
        total_words = len(words_data)

        # 2. 데이터베이스 연결
        print("\n[2/4] 데이터베이스 연결...")
        connection = connect_to_database()

        # 현재 테이블 레코드 수 확인
        initial_count = get_table_count(connection)
        if initial_count is not None:
            print(f"  현재 WORD 테이블 레코드 수: {initial_count}개")

        # 기존 단어 목록 조회 (중복 방지)
        print("  기존 단어 목록 조회 중...")
        existing_words = get_existing_words(connection)
        print(f"  기존 단어 수: {len(existing_words)}개")

        # 중복 제거: 이미 존재하는 단어 필터링
        print("\n[2.5/4] 중복 단어 필터링...")
        new_words_data = []
        skipped_words = []
        for word_data in words_data:
            word = word_data.get('word', '').lower()
            if word in existing_words:
                skipped_words.append(word_data.get('word', ''))
            else:
                new_words_data.append(word_data)
        
        print(f"  - 전체 단어: {total_words}개")
        print(f"  - 중복 제외: {len(skipped_words)}개")
        print(f"  - 삽입 대상: {len(new_words_data)}개")
        
        if skipped_words:
            print(f"  중복 단어 예시 (처음 10개): {', '.join(skipped_words[:10])}")
            if len(skipped_words) > 10:
                print(f"  ... 외 {len(skipped_words) - 10}개")

        # 3. 배치 단위로 데이터 삽입
        print(f"\n[3/4] 데이터 삽입 중... (배치 크기: {BATCH_SIZE})")
        print("-" * 70)

        total_inserted = 0
        batch_number = 0
        words_to_insert = new_words_data

        for i in range(0, len(words_to_insert), BATCH_SIZE):
            batch = words_to_insert[i:i + BATCH_SIZE]
            batch_number += 1

            print(f"배치 {batch_number}: {i+1}~{min(i+BATCH_SIZE, len(words_to_insert))} 처리 중...", end=" ")

            inserted = insert_words_batch(connection, batch)
            total_inserted += inserted

            print(f"✓ {inserted}개 삽입 완료")

            # 진행률 표시
            progress = (min(i + BATCH_SIZE, len(words_to_insert)) / len(words_to_insert)) * 100
            print(f"  진행률: {progress:.1f}% ({min(i+BATCH_SIZE, len(words_to_insert))}/{len(words_to_insert)})")

        # 4. 최종 검증 및 결과 출력
        print("\n[4/4] 최종 검증 중...")
        final_count = get_table_count(connection)
        if final_count is not None:
            print(f"  최종 WORD 테이블 레코드 수: {final_count}개")
            if initial_count is not None:
                actual_inserted = final_count - initial_count
                print(f"  실제 삽입된 레코드: {actual_inserted}개")

        elapsed_time = time.time() - start_time
        print("-" * 70)
        print("\n✓ 작업 완료!")
        print(f"  - 전체 단어: {total_words}개")
        print(f"  - 중복 제외: {len(skipped_words)}개")
        print(f"  - 삽입 대상: {len(new_words_data)}개")
        print(f"  - 삽입 성공: {total_inserted}개")
        print(f"  - 소요 시간: {elapsed_time:.2f}초")
        if total_inserted > 0:
            print(f"  - 초당 처리량: {total_inserted/elapsed_time:.2f}개/초")

        # 5. 데이터베이스 연결 종료
        connection.close()
        print("\n✓ 데이터베이스 연결 종료")

    except Exception as error:
        print(f"\n✗ 오류 발생: {error}")
        import traceback
        traceback.print_exc()
        return 1

    print("=" * 70)
    return 0

if __name__ == "__main__":
    exit(main())
