# WordWeb

AI 기반 개인 맞춤형 영어 단어 학습 플랫폼

---

## 프로젝트 소개

**WordWeb**은 단순 암기 중심의 영어 단어 학습에서 벗어나,
사용자가 **틀린 단어(오답)**를 다시 학습 경험으로 전환할 수 있도록 설계된
AI 기반 영어 단어 학습 웹 서비스입니다.

퀴즈에서 틀린 단어는 버려지지 않고,
AI가 생성한 **스토리 속 맥락**에서 다시 등장하며
자연스럽게 기억에 남도록 돕습니다.

---

## 문제 인식

* 단어 암기는 반복 위주의 학습으로 쉽게 지루해짐
* 오답은 다시 외워도 오래 기억되지 않음
* 개인별 약점(오답 패턴)을 반영한 학습이 어려움

 **WordWeb은 오답을 ‘실수’가 아닌 ‘학습 자산’으로 재활용**합니다.

---

## 핵심 기능

### 1. 단어 학습 & 관리

* Oxford 5000 기반 단어 데이터
* 즐겨찾기 / 학습 완료 관리
* 품사, 의미, 예문 제공

### 2. 퀴즈 학습

* 랜덤 / 범위 기반 퀴즈
* 즉각적인 정답 피드백
* 오답 자동 기록

### 3. AI 스토리 생성 (핵심 기능)

* 퀴즈 오답 단어 자동 수집
* AI가 오답 단어를 모두 포함한 영어 스토리 생성
* 한글 번역 제공
* 단어가 문맥 속에서 재등장 → 기억 강화

### 4. 스토리 아카이브

* 생성된 스토리 저장
* 스토리별 사용 단어 확인
* 다시 읽기 및 복습

### 5. 마이페이지 & 학습 현황

* 학습 기록 확인
* 즐겨찾기 / 완료 단어 관리

---

## 서비스 흐름

1. 단어 학습
2. 퀴즈 진행
3. 오답 발생
4. 오답 자동 수집
5. AI 스토리 생성
6. 스토리 기반 재학습

---

## 시스템 아키텍처

* **Frontend**: React (Vite), React Query, Zustand
* **Backend**: Spring Boot, JPA, JWT
* **AI**: DeepSeek API (Embedding & Story Generation)
* **Database**: MySQL / Oracle (학습용)

---

## 기술 스택

### Frontend

* React (Vite)
* React Router
* React Query
* Zustand
* CSS (Custom)
* Lucide Icons

### Backend

* Java 17
* Spring Boot
* Spring Security + JWT
* JPA / Hibernate
* Gradle

### AI / Data

* DeepSeek API
* Word Embedding
* Oxford 5000 Dataset

### Database

* MySQL
* Oracle (학습/테스트)
### Deployment
* AWS EC2
* Nginx

---

## 팀 구성

### Frontend

* 황수지
* 이채은

### Backend

* 신상훈
* 최종혁(팀장)

---

## 향후 확장 계획

* 스토리 장르 / 난이도 선택
* 개인별 학습 추천 강화
* 통계 기반 대시보드 고도화
* 모바일 UX 개선

---

## 한 줄 요약

> **WordWeb은 오답을 이야기로 바꿔, 기억에 남는 영어 학습 경험을 제공합니다.**
