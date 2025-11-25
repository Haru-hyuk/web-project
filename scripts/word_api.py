"""
FastAPI를 사용한 영단어 사전 데이터 수집 API
- raw_words 폴더의 JSON/TXT 파일에서 단어를 읽어옴
- 무료 번역 API로 영어 단어의 뜻을 한국어로 번역
- dictionaryapi.dev API를 사용하여 품사, 예문, 유의어, 반의어를 수집
- processed_words 폴더에 JSON 파일로 저장
"""

from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import List, Dict, Optional
import json
import time
import requests
from pathlib import Path
import asyncio
from datetime import datetime

app = FastAPI(title="Word Dictionary API", version="1.0.0")

# 전역 변수
words_cache = None
processed_words_cache = []
processing_status = {
    'is_processing': False,
    'current_word': '',
    'total_words': 0,
    'processed': 0,
    'saved': 0,
    'start_time': None
}


class WordData(BaseModel):
    """단어 데이터 모델"""
    word: str
    meaning: str  # 한국어로 번역된 뜻
    part_of_speech: str
    example_sentence: str
    synonyms: List[str]
    antonyms: List[str]


class WordProcessor:
    """단어 처리 클래스"""
    
    DICTIONARY_API = "https://api.dictionaryapi.dev/api/v2/entries/en/"
    TRANSLATE_API_LIBRE = "https://libretranslate.com/translate"
    TRANSLATE_API_MYMEMORY = "https://api.mymemory.translated.net/get"
    
    def __init__(self, delay: float = 0.1, use_mymemory: bool = True):
        self.delay = delay
        self.use_mymemory = use_mymemory  # True: MyMemory, False: LibreTranslate
        self.stats = {
            'total_words': 0,
            'successful': 0,
            'failed': 0,
            'no_data': 0,
            'translation_errors': 0
        }
    
    def translate_to_korean(self, text: str) -> Optional[str]:
        """영어 텍스트를 한국어로 번역"""
        try:
            if self.use_mymemory:
                # MyMemory API 사용
                url = self.TRANSLATE_API_MYMEMORY
                params = {"q": text, "langpair": "en|ko"}
                response = requests.get(url, params=params, timeout=10)
                
                if response.status_code == 200:
                    data = response.json()
                    if "responseData" in data and "translatedText" in data["responseData"]:
                        return data["responseData"]["translatedText"]
            else:
                # LibreTranslate API 사용
                url = self.TRANSLATE_API_LIBRE
                data = {
                    "q": text,
                    "source": "en",
                    "target": "ko",
                    "format": "text"
                }
                response = requests.post(url, json=data, timeout=10)
                
                if response.status_code == 200:
                    result = response.json()
                    if "translatedText" in result:
                        return result["translatedText"]
            
            return None
        except Exception as e:
            print(f"Translation error for '{text}': {e}")
            self.stats['translation_errors'] += 1
            return None
    
    def fetch_word_data(self, word: str) -> Optional[Dict]:
        """Dictionary API에서 단어 데이터 가져오기"""
        try:
            url = f"{self.DICTIONARY_API}{word.lower()}"
            response = requests.get(url, timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, list) and len(data) > 0:
                    return data[0]
            return None
        except Exception as e:
            print(f"Error fetching '{word}': {e}")
            return None
    
    def extract_word_info(self, word: str, api_data: Dict) -> Optional[Dict]:
        """API 데이터에서 단어 정보 추출 및 한국어 번역"""
        meanings = api_data.get('meanings', [])
        
        if not meanings:
            return None
        
        first_meaning = meanings[0]
        definitions = first_meaning.get('definitions', [])
        
        if not definitions:
            return None
        
        # 영어 뜻 가져오기
        english_meaning = definitions[0].get('definition', '').strip()
        if not english_meaning:
            return None
        
        # 한국어로 번역
        korean_meaning = self.translate_to_korean(english_meaning)
        if not korean_meaning:
            # 번역 실패 시 저장하지 않음
            return None
        
        # 한국어가 포함되어 있는지 확인 (한글 유니코드 범위: AC00-D7AF)
        has_korean = any('\uAC00' <= char <= '\uD7AF' for char in korean_meaning)
        if not has_korean:
            # 한국어가 없으면 저장하지 않음 (영어만 있는 경우)
            return None
        
        # 예문 가져오기
        example = definitions[0].get('example', '').strip()
        if example.startswith('"') and example.endswith('"'):
            example = example[1:-1]
        
        # 유의어와 반의어 수집 (모든 meanings에서)
        synonyms = []
        antonyms = []
        
        for meaning in meanings:
            meaning_synonyms = meaning.get('synonyms', [])
            if meaning_synonyms:
                synonyms.extend(meaning_synonyms)
            
            meaning_antonyms = meaning.get('antonyms', [])
            if meaning_antonyms:
                antonyms.extend(meaning_antonyms)
        
        synonyms = sorted(list(set(synonyms)))
        antonyms = sorted(list(set(antonyms)))
        
        # 필수 필드 검증: 단어, 뜻(한국어), 품사, 예문
        if not word.lower() or not korean_meaning or not first_meaning.get('partOfSpeech') or not example:
            return None
        
        return {
            'word': word.lower(),
            'meaning': korean_meaning,  # 한국어로 번역된 뜻
            'part_of_speech': first_meaning.get('partOfSpeech', ''),
            'example_sentence': example,
            'synonyms': synonyms,
            'antonyms': antonyms
        }


# 전역 프로세서 인스턴스
processor = WordProcessor(delay=0.1)


def load_words_from_file(file_path: str) -> List[str]:
    """파일에서 단어 목록 로드 (JSON 또는 TXT)"""
    file_path_obj = Path(file_path)
    if file_path_obj.suffix.lower() == '.txt':
        # TXT 파일: 한 줄에 하나씩 단어
        with open(file_path, 'r', encoding='utf-8') as f:
            words = [line.strip() for line in f if line.strip()]
        return words
    else:
        # JSON 파일
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        return list(data.keys())


@app.on_event("startup")
async def startup_event():
    """서버 시작 시 단어 목록 로드"""
    global words_cache
    # 기본 파일 경로들 확인
    words_file = Path("scripts/words_dictionary.json")
    if not words_file.exists():
        words_file = Path("scripts/google-10000-english-usa-no-swears-medium.txt")
    
    if words_file.exists():
        words_cache = load_words_from_file(str(words_file))
        print(f"Loaded {len(words_cache)} words from {words_file.name}")


@app.get("/")
async def root():
    """API 루트 엔드포인트"""
    return {
        "message": "Word Dictionary API",
        "version": "1.0.0",
        "endpoints": {
            "/word/{word}": "Get word data by word",
            "/words/batch": "Get multiple words data",
            "/words/process": "Process words from dictionary (background task)",
            "/stats": "Get processing statistics"
        }
    }


@app.get("/word/{word}", response_model=WordData)
async def get_word(word: str):
    """
    단일 단어의 데이터를 가져옵니다.
    뜻이 없는 경우 404를 반환합니다.
    """
    api_data = processor.fetch_word_data(word)
    
    if not api_data:
        raise HTTPException(status_code=404, detail=f"Word '{word}' not found in dictionary API")
    
    word_info = processor.extract_word_info(word, api_data)
    
    # 뜻이 없으면 404 반환
    if not word_info['meaning']:
        raise HTTPException(
            status_code=404, 
            detail=f"Word '{word}' found but has no meaning"
        )
    
    return word_info


@app.post("/words/batch")
async def get_words_batch(words: List[str], max_results: Optional[int] = None):
    """
    여러 단어의 데이터를 한 번에 가져옵니다.
    뜻이 있는 단어만 반환합니다.
    """
    results = []
    processed_words_set = set()  # 중복 체크용
    
    for word in words[:max_results] if max_results else words:
        word_lower = word.lower()
        
        # 중복 체크
        if word_lower in processed_words_set:
            continue
        
        api_data = processor.fetch_word_data(word)
        
        if api_data:
            word_info = processor.extract_word_info(word, api_data)
            # word_info가 None이 아니고 뜻이 있는 경우만 추가
            if word_info is not None:
                # 중복 체크: 이미 추가된 단어인지 확인
                if word_info['word'] not in processed_words_set:
                    results.append(word_info)
                    processed_words_set.add(word_info['word'])
        
        # Rate limiting
        await asyncio.sleep(processor.delay)
    
    return {
        "total_requested": len(words),
        "total_found": len(results),
        "words": results
    }


@app.post("/words/process")
async def process_words(
    background_tasks: BackgroundTasks,
    max_words: Optional[int] = None,
    start_index: int = 0,
    output_file: str = "word_data.json",
    input_file: Optional[str] = None
):
    """
    단어 파일에서 단어를 처리합니다.
    뜻이 있는 단어만 저장합니다.
    백그라운드 작업으로 실행됩니다.
    
    Args:
        input_file: 입력 파일 경로 (JSON 또는 TXT), 없으면 기본 파일 사용
    """
    global words_cache, processed_words_cache, processing_status
    
    # 이미 처리 중이면 에러
    if processing_status['is_processing']:
        raise HTTPException(
            status_code=409, 
            detail="Another processing task is already running. Please wait for it to complete."
        )
    
    # 입력 파일 지정 시 해당 파일 사용
    if input_file:
        words_to_process = load_words_from_file(input_file)
    elif words_cache:
        words_to_process = words_cache[start_index:]
    else:
        raise HTTPException(status_code=500, detail="Words dictionary not loaded and no input file provided")
    
    if max_words:
        words_to_process = words_to_process[:max_words]
    
    # 처리 상태 초기화
    processing_status['is_processing'] = True
    processing_status['total_words'] = len(words_to_process)
    processing_status['processed'] = 0
    processing_status['saved'] = 0
    processing_status['current_word'] = ''
    processing_status['start_time'] = datetime.now().isoformat()
    
    background_tasks.add_task(
        process_words_background,
        words_to_process,
        output_file
    )
    
    return {
        "message": "Processing started in background",
        "total_words": len(words_to_process),
        "output_file": output_file,
        "input_file": input_file or "default",
        "status_url": "/words/process/status"
    }


async def process_words_background(words: List[str], output_file: str):
    """백그라운드에서 단어 처리"""
    global processed_words_cache, processing_status
    
    # processed_words 폴더에 저장
    output_dir = Path("scripts/processed_words")
    output_dir.mkdir(exist_ok=True)
    
    # 파일명이 경로를 포함하지 않으면 processed_words 폴더에 저장
    if "/" not in output_file and "\\" not in output_file:
        output_path = output_dir / output_file
    else:
        output_path = Path(f"scripts/{output_file}")
    
    print(f"Output file path: {output_path.absolute()}")
    
    # 기존 파일에서 이미 처리된 단어 로드
    existing_words = set()
    processed_words = []
    
    if output_path.exists():
        try:
            with open(output_path, 'r', encoding='utf-8') as f:
                existing_data = json.load(f)
                existing_words = {item['word'].lower() for item in existing_data if item.get('word')}
                processed_words = existing_data
                print(f"Found {len(existing_words)} existing words, will skip duplicates.")
        except Exception as e:
            print(f"Error loading existing file: {e}")
            pass
    
    # 중복 체크를 위한 집합 (현재 세션에서 처리된 단어)
    processed_in_session = set(existing_words)
    
    print(f"\n{'='*70}")
    print(f"{'Progress':<10} {'Word':<20} {'Status':<15} {'Total Saved':<15} {'%':<10}")
    print(f"{'='*70}")
    
    start_time = time.time()
    
    for i, word in enumerate(words, 1):
        word_lower = word.lower()
        
        # 이미 처리된 단어 건너뛰기
        if word_lower in processed_in_session:
            processor.stats['no_data'] += 1
            if i % 5 == 0 or i == 1 or i == len(words):
                progress = (i / len(words)) * 100
                print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Skipped':<15} {len(processed_words):<15} {progress:>5.1f}%")
            continue
        
        # 처리 상태 업데이트
        processing_status['current_word'] = word_lower
        processing_status['processed'] = i
        
        if i % 5 == 0 or i == 1 or i == len(words):
            progress = (i / len(words)) * 100
            print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Processing...':<15} {len(processed_words):<15} {progress:>5.1f}%", end='\r')
        
        processor.stats['total_words'] += 1
        
        api_data = processor.fetch_word_data(word)
        
        if api_data:
            word_info = processor.extract_word_info(word, api_data)
            # word_info가 None이면 필수 데이터가 없는 것이므로 저장하지 않음
            if word_info is not None:
                # 중복 체크: 이미 저장된 단어인지 확인 (대소문자 구분 없이)
                word_key = word_info['word'].lower()
                if word_key not in processed_in_session:
                    processed_words.append(word_info)
                    processed_in_session.add(word_key)
                    processor.stats['successful'] += 1
                    processing_status['saved'] = len(processed_words)
                    if i % 5 == 0 or i == 1 or i == len(words):
                        progress = (i / len(words)) * 100
                        print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Saved':<15} {len(processed_words):<15} {progress:>5.1f}%")
                else:
                    # 중복 단어는 저장하지 않음
                    processor.stats['no_data'] += 1
                    if i % 5 == 0 or i == 1 or i == len(words):
                        progress = (i / len(words)) * 100
                        print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Duplicate':<15} {len(processed_words):<15} {progress:>5.1f}%")
            else:
                processor.stats['no_data'] += 1
                if i % 5 == 0 or i == 1 or i == len(words):
                    progress = (i / len(words)) * 100
                    print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Missing data':<15} {len(processed_words):<15} {progress:>5.1f}%")
        else:
            processor.stats['no_data'] += 1
            if i % 5 == 0 or i == 1 or i == len(words):
                progress = (i / len(words)) * 100
                print(f"{i:>6}/{len(words):<4} {word_lower:<20} {'Not found':<15} {len(processed_words):<15} {progress:>5.1f}%")
        
        # Rate limiting (번역 API와 dictionary API 모두 고려)
        time.sleep(processor.delay * 2)  # 번역 API 호출도 있으므로 딜레이 증가
    
    # 최종 중복 제거
    seen_words = set()
    unique_words = []
    for word_data in processed_words:
        word_key = word_data['word'].lower()
        if word_key not in seen_words:
            seen_words.add(word_key)
            unique_words.append(word_data)
    
    if len(unique_words) < len(processed_words):
        print(f"\nRemoved {len(processed_words) - len(unique_words)} duplicate words from final output")
        processed_words = unique_words
    
    # 파일 저장
    try:
        elapsed_time = time.time() - start_time
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(processed_words, f, ensure_ascii=False, indent=2)
        print(f"\n{'='*70}")
        print(f"Processing completed!")
        print(f"{'='*70}")
        print(f"Total words processed: {processor.stats['total_words']}")
        if processor.stats['total_words'] > 0:
            print(f"Successfully saved: {processor.stats['successful']} ({processor.stats['successful']/processor.stats['total_words']*100:.1f}%)")
            print(f"Skipped/No data: {processor.stats['no_data']} ({processor.stats['no_data']/processor.stats['total_words']*100:.1f}%)")
        print(f"Translation errors: {processor.stats['translation_errors']}")
        print(f"Final unique words in file: {len(processed_words)}")
        print(f"Time elapsed: {elapsed_time:.1f} seconds")
        print(f"Saved to: {output_path.absolute()}")
        print(f"{'='*70}")
        
        processed_words_cache = processed_words
    except Exception as e:
        print(f"\nERROR: Failed to save file: {e}")
        print(f"Output path: {output_path.absolute()}")
        raise
    
    # 처리 완료 상태 업데이트
    processing_status['is_processing'] = False
    processing_status['current_word'] = ''


@app.get("/stats")
async def get_stats():
    """처리 통계 조회"""
    return {
        "stats": processor.stats,
        "cached_words_count": len(processed_words_cache) if processed_words_cache else 0,
        "total_dictionary_words": len(words_cache) if words_cache else 0
    }


@app.get("/words/process/status")
async def get_processing_status():
    """현재 처리 중인 작업의 상태 조회"""
    global processing_status
    
    if not processing_status['is_processing']:
        return {
            "is_processing": False,
            "message": "No processing task is currently running"
        }
    
    total = processing_status['total_words']
    processed = processing_status['processed']
    saved = processing_status['saved']
    progress_percent = (processed / total * 100) if total > 0 else 0
    
    return {
        "is_processing": True,
        "current_word": processing_status['current_word'],
        "total_words": total,
        "processed": processed,
        "saved": saved,
        "progress_percent": round(progress_percent, 1),
        "start_time": processing_status['start_time']
    }


@app.get("/words/cached")
async def get_cached_words(limit: Optional[int] = None):
    """캐시된 처리된 단어 목록 조회"""
    if not processed_words_cache:
        return {"message": "No words processed yet", "words": []}
    
    words = processed_words_cache[:limit] if limit else processed_words_cache
    return {
        "total": len(processed_words_cache),
        "returned": len(words),
        "words": words
    }


if __name__ == "__main__":
    import uvicorn
    import sys
    
    # 포트가 이미 사용 중이면 다른 포트 사용
    port = 8000
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except:
            pass
    
    print(f"Starting server on http://0.0.0.0:{port}")
    print(f"API docs: http://localhost:{port}/docs")
    uvicorn.run(app, host="0.0.0.0", port=port)

