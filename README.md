#  WordWeb – AI 기반 맞춤형 영단어 학습 플랫폼

##  프로젝트 소개
WordWeb은 AI 임베딩·스토리 생성 기술을 활용해  
사용자에게 **개인 맞춤형 영단어 학습 경험**을 제공하는 웹 서비스입니다.

사용자가 틀린 단어를 자동으로 기록하고,  
DeepSeek 기반 스토리 생성 + 5,000개 단어 임베딩을 활용한 연관 단어 추천 기능을 제공합니다.  
카테고리 탐색, 즐겨찾기, 학습 상태 관리까지 포함된 올인원 학습 플랫폼입니다.

---

##  개발 기간
**2025.11.21 ~ 2025.12.22**

---

##  개발 환경

### **Frontend**
- figma
- React (Vite)
- Axios
- Zustand
- TailwindCSS
- React Router

### **Backend**
- Spring Boot 4.0.0
- Spring Security + JWT
- JPA / Hibernate
- Lombok
- sts 4.32.0

### **Database**
- **AWS RDS – MySQL**
- DBeaver / MySQL Workbench

### **AI & Infra**
- DeepSeek API (스토리 생성)
- 단어 임베딩(Word2Vec/유사도 기반)
- GitHub / Git

---

## 🚀 주요 기능

### 🔐 인증(Auth)
- 회원가입 / 로그인
- Access / Refresh Token 발급
- 내 정보 조회

### 📖 단어 기능
- 단어 상세 조회
- 전체 단어 조회 (페이징)
- 오늘의 단어
- 카테고리 / 레벨 필터링
- 단어 임베딩 기반 연관 단어 추천

### ⭐ 즐겨찾기
- 즐겨찾기 추가 / 삭제
- 즐겨찾기 목록 조회

### 🧠 AI Story
- 오답 단어 기반 개인 맞춤형 스토리 생성
- DeepSeek API 연동

### 🗂️ 학습 관리
- 오답 자동 기록
- 학습 상태 관리
