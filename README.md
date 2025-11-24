📘 WordWeb – AI 기반 영어 학습 플랫폼

AI가 단어를 분류하고, 오답으로 스토리를 생성해주는 맞춤형 어학 학습 서비스

🌟 프로젝트 소개

WordWeb은 사용자가 학습 과정에서 틀린 단어를 자동으로 분석하여
AI 기반으로 분야별 자동 분류, 오답 기반 스토리 생성,
개인 맞춤형 학습 경험을 제공하는 영어 학습 플랫폼입니다.

사용자가 학습 과정에서 틀린 단어들을 AI가 스토리로 재구성해주며,
단순 암기를 넘어 맥락 기반 학습을 경험할 수 있습니다.

🔥 주요 기능
🔹 1) AI 단어 분야 자동 분류

분야가 없는 단어를 Batch로 불러와

AI가 “정해진 카테고리 목록” 내에서 자동 분류

명확한 화이트리스트 기반 분류로 정확도 확보

🔹 2) AI 기반 오답 스토리 생성

사용자가 틀렸지만 스토리에 사용되지 않은 단어 5~10개 수집

AI가 해당 단어를 자연스럽게 포함한 영문/한글 스토리 생성

STORY 테이블에 저장

사용된 단어는 “사용됨(Y)”으로 업데이트

🔹 3) 맞춤형 개인 학습 관리

일일 학습 목표 설정

학습 상태(learned/pending) 기록

정답/오답 자동 저장

정답률/오답률/주간 학습량 분석

🔹 4) 단어 즐겨찾기 / 추천 기능

즐겨찾기 등록·해제

선호 분야 기반 학습 추천어 제공(추가 예정)

🔹 5) 대시보드

주간 학습 통계

총 학습량 시각화

오답률 그래프

스토리 히스토리

🧱 기술 스택
🟦 Frontend

React + Vite

React Router

Zustand / Redux

Axios

Tailwind or Custom CSS

🟩 Backend

Spring Boot 3

Spring Security (JWT)

Spring Data JPA

OpenAI API (GPT-4.1 / GPT-4o-mini)

🟧 Database

Oracle Database (On-Premise 또는 AWS RDS)

☁ Infrastructure

AWS EC2 (Backend)

AWS RDS (Oracle)

AWS S3 + CloudFront (Frontend Hosting)

Docker (선택)

Nginx (Reverse Proxy)

🏗 시스템 아키텍처 개요


[React Frontend (S3 + CloudFront)]
                 │
                 ▼
[Spring Boot API Server (EC2)]
                 │
                 ▼
[Oracle DB (RDS)]
                 │
                 ▼
[OpenAI API]
(단어 분류 / 스토리 생성)

📆 WordWeb 프로젝트 – 1주일 작업 계획

FE 2명 · BE 2명 팀 구성 기준

📂 Overview

본 문서는 WordWeb 프로젝트의 첫 주 개발 일정과 팀별 역할 분담을 정리한 문서로,
각 파트가 어떤 작업을 진행해야 하는지 명확하게 제시합니다.

🟦 Frontend Team (FE 2명)
🎯 이번 주 목표

React 개발환경 구축

주요 페이지 UI 골격 제작

API 연동 구조 설계

📌 FE 공통 작업

Vite 기반 React 프로젝트 생성

React Router 초기 세팅

Axios 인터셉터 구현 (JWT 대비)

👤 FE 1 — UI 구조 & 공통 컴포넌트 담당

전체 라우팅 구조 설계

상단 네비게이션 / 레이아웃 제작

회원가입 & 로그인 페이지 UI 제작

공용 컴포넌트(버튼/카드) 제작

기본 스타일 가이드 구성

👤 FE 2 — 화면 Flow & API 준비 담당

전체 UI Flow 문서화 → docs/ 업로드

페이지별 API 연동 목록 작성

온보딩 / 대시보드 / 학습 페이지 UI 초안 설계

Zustand 또는 Redux 상태관리 구조 구축

API 요청 wrapper 구현

✅ FE 주간 목표 요약

FE 개발환경 구성 완료

핵심 페이지 UI 골격 구축

로그인/회원가입 UI 개발

학습/단어/오답 페이지 구조 설계

🟩 Backend Team (BE 2명)
🎯 이번 주 목표

Spring Boot 환경 세팅

Entity 매핑

JWT 기반 인증 구현

학습·단어 관련 API 구축

AI 스토리 파이프라인 구조 설계

📌 BE 공통 작업

Spring Boot 프로젝트 생성

Oracle + JPA 환경 설정

Swagger(OpenAPI) 적용

👤 BE 1 — Auth & User 도메인

회원가입 API

로그인 API (JWT 발급)

BCrypt 비밀번호 암호화

Access/Refresh Token 구조

Spring Security 기본 설정

User 조회/수정 API 개발

👤 BE 2 — Word/Story/AI 파이프라인

JPA Entity 7종 생성

User, Word, UserWord, WrongWord, Story, FavoriteWord, ClusterWord

단어 조회/필터링 API

학습 상태 업데이트 API

정답/오답 처리 API

오답 기반 스토리 생성 파이프라인 뼈대 구현
(AI 호출은 다음 주)

✅ BE 주간 목표 요약

JWT 인증 구현

Entity 매핑 100% 완료

단어/학습 API 구축

스토리 생성 로직 설계

Swagger API 문서화

📄 마무리

본 문서는 WordWeb 프로젝트의 첫 주 개발 방향성과 역할 분담을 명확히 정리한 문서입니다.
프로젝트 진행에 따라 유연하게 업데이트되며, 팀 전체의 공통 기준으로 사용됩니다.
