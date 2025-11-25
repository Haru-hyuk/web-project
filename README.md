 WordWeb Backend – 기능 요약

본 프로젝트는 영어 학습 플랫폼 WordWeb의 백엔드 서버로,
회원 관리, 단어 관리, 즐겨찾기, 그리고 DeepSeek 기반 AI 스토리 생성 기능을 제공합니다.

🚀 기술 스택

Spring Boot 4.0
Java 17
Spring Security + JWT
Spring Data JPA
Oracle Database
Lombok
OkHttp (DeepSeek API 연동)

 구현된 기능
 1. 회원가입 / 로그인 / JWT 인증
이메일 기반 회원가입
비밀번호 암호화 (BCrypt)
로그인 시 JWT 토큰 발급
인증 필요한 API → JWT 검증 후 접근 허용
사용자 정보 조회 가능

 2. User 엔티티 및 회원 정보 관리
User 테이블 필드 구성:
userId
email
password
nickname
userName
userBirth
preference
goal
dailyWordGoal
createdAt
JPA 매핑 완료 및 CRUD 기반 구조 준비됨.

 3. 즐겨찾기(FavoriteWord) 기능
사용자별 단어 즐겨찾기 저장
FavoriteWord 엔티티 구성
Word 엔티티와 연관 매핑
FavoriteWordResponse DTO로 단어 전체 정보 반환
반환 데이터:
단어/뜻/품사
예문
카테고리
레벨

 4. 단어(Word) 엔티티 구조 구축
Word 엔티티 포함 필드:
wordId
word
meaning
partOfSpeech
exampleSentence
category
level
사전 데이터 저장 및 활용을 위한 기반 설계 완료.

 5. AI 스토리 생성 기능 (DeepSeek 연동)
영단어 목록을 기반으로 영어 + 한국어 스토리 자동 생성 기능.

✔ 입력
{
  "words": ["paradigm", "eloquent", "sustainability"],
  "difficulty": "intermediate",
  "style": "narrative"
}

✔ 수행 기능

AI 프롬프트 자동 생성
DeepSeek ChatCompletion API 호출
영어 스토리 / 한국어 번역 자동 분리
모든 단어 사용 여부 자동 검증
사용되지 않은 단어가 있을 경우 최대 3회 재시도

usedWords 목록 반환

✔ 반환 구조
{
  "storyEn": "...",
  "storyKo": "...",
  "success": true,
  "usedWords": [
    "paradigm",
    "eloquent",
    "sustainability"
  ]
}

✔ 안정성 기능

OkHttpClient 타임아웃 확장 (최대 120초)
DeepSeek 응답 구조 변화 대비 파싱 처리
재시도 로직 내장

6. 개발 및 설정 요소
DeepSeek API Key → application.yml에서 주입
Postman으로 API 테스트 완료
모든 주요 DTO, 엔티티, 서비스 구조화 완료

## 3. API 명세

## **1. Auth (회원 가입 / 로그인)**

---

### **POST /api/auth/signup**

회원가입

**Request**

```json
{
  "email": "test@test.com",
  "password": "1234",
  "nickname": "hyuk",
  "userName": "최종혁",
  "userBirth": "2000-01-01"
}

```

**Response**

```json
{
  "success": true,
  "message": "Signup completed"
}

```

---

### **POST /api/auth/login**

로그인 + JWT 발급

**Request**

```json
{
  "email": "test@test.com",
  "password": "1234"
}

```

**Response**

```json
{
  "token": "JWT_TOKEN_VALUE",
  "email": "test@test.com",
  "nickname": "hyuk"
}

```

---

## **2. User 정보**

(로그인 후 Authorization Bearer 필요)

---

### **GET /api/user/me**

내 정보 조회

**Headers**

```
Authorization: Bearer <JWT_TOKEN>

```

**Response**

```json
{
  "userId": 1,
  "email": "test@test.com",
  "nickname": "hyuk",
  "userName": "최종혁",
  "preference": "reading",
  "goal": "daily_study",
  "dailyWordGoal": 10
}

```

---

## **3. Favorite Words (즐겨찾기)**

---

### **POST /api/favorite/add**

단어 즐겨찾기 추가

**Request**

```json
{
  "wordId": 101
}

```

**Response**

```json
{
  "id": 33,
  "wordId": 101,
  "word": "serendipity",
  "meaning": "운 좋게 발견하는 능력",
  "partOfSpeech": "noun",
  "exampleSentence": "She found the book by pure serendipity.",
  "category": "common",
  "level": "intermediate"
}

```

---

### **GET /api/favorite/list**

사용자 즐겨찾기 리스트

**Response**

```json
[
  {
    "id": 33,
    "wordId": 101,
    "word": "serendipity",
    "meaning": "...",
    "partOfSpeech": "noun",
    "exampleSentence": "...",
    "category": "common",
    "level": "intermediate"
  }
]

```

---

## **4. Word (단어 정보)**

---

### **GET /api/word/{wordId}**

단어 상세 조회

**Response**

```json
{
  "wordId": 101,
  "word": "serendipity",
  "meaning": "...",
  "exampleSentence": "...",
  "partOfSpeech": "noun",
  "category": "common",
  "level": "intermediate"
}

```

---

## **5. AI Story Generator (DeepSeek 기반)**

---

### **POST /api/story/ai**

영단어 기반 영어/한국어 스토리 생성

**Request**

```json
{
  "words": ["paradigm", "eloquent", "sustainability"],
  "difficulty": "intermediate",
  "style": "narrative"
}

```

**Response**

```json
{
  "success": true,
  "storyEn": "A short English story...",
  "storyKo": "짧은 한국어 번역...",
  "usedWords": [
    "paradigm",
    "eloquent",
    "sustainability"
  ]
}

```

---

# 공통 인증 방식

대부분의 API는 JWT 필요.

### Headers

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

```

---

# 오류 응답 형식

```json
{
  "success": false,
  "message": "Invalid request"
}

```
