 WordWeb 프로젝트 – 1주일 작업 계획

FE 2명 · BE 2명 팀 구성 기준

 Overview

본 문서는 WordWeb 프로젝트의 첫 주 개발 일정과 팀별 역할 분담을 정리한 문서입니다.
(업무 진행 시 참고용 기준 문서)

 Frontend Team (FE 2명)
  이번 주 목표

React 개발 환경 구성

UI 레이아웃 및 주요 페이지 구조 설계

API 연동을 위한 기반 설정

🗂 FE 공통 작업

Vite 기반 React 프로젝트 생성

React Router 초기 세팅

Axios 인터셉터 구현 (JWT 포함 대비)

👤 FE 1 — UI 구조 & 공용 컴포넌트

전체 라우팅 구조 설계

상단 네비게이션 / 레이아웃 제작

회원가입 & 로그인 페이지 UI 구현

공용 UI 컴포넌트(버튼, 카드 등) 구현

기본 테마/스타일 가이드 구성

👤 FE 2 — 화면 Flow & API 준비

전체 UI Flow 문서화 → docs/에 업로드

페이지별 API 연동 항목 정의

온보딩 / 대시보드 / 학습 페이지 UI 초안 설계

Zustand 또는 Redux 상태관리 구조 준비

API 요청 wrapper 기본 구현

  FE 주간 목표 요약

FE 환경 구성 완료

주요 페이지 UI 구조 확립

로그인/회원가입 UI 완성

학습/단어/오답 페이지 설계

 Backend Team (BE 2명)
  이번 주 목표

Spring Boot 기반 세팅

DB Entity 매핑

JWT 기반 인증 구현

단어/학습 관련 기본 API 개발

스토리 생성 파이프라인 설계

🗂 BE 공통 작업

Spring Boot 프로젝트 생성

공통 패키지 구조 정의

com.wordweb
 ├─ config
 ├─ controller
 ├─ service
 ├─ repository
 ├─ entity
 ├─ dto
 └─ scheduler


Oracle + JPA 환경 설정

Swagger(OpenAPI) 설정


👤 BE 1 — Auth & User 도메인

회원가입 API

로그인 API (JWT 발급)

BCrypt를 사용한 비밀번호 암호화

JWT Access/Refresh Token 구조 구현

Spring Security 기본 구성

User 정보 조회·수정 API 개발

👤 BE 2 — Word/Story/AI 파이프라인

DB 테이블 기반 JPA Entity 7종 생성

User

Word

UserWord

WrongWord

Story

FavoriteWord

ClusterWord

단어 조회/필터링 API 개발

학습 상태 업데이트 API

정답/오답 처리 API

오답 기반 스토리 생성 파이프라인 뼈대 구현
(이번 주는 AI 호출 제외, 구조만 완성)

  BE 주간 목표 요약

JWT 인증 구현

Entity 매핑 100% 완료

학습/단어 API 구축

스토리 생성 로직 설계

Swagger 기반 API 문서화


이 문서는 WordWeb 프로젝트의 첫 주 개발 방향성과 역할 분담을 정리한 문서이며,
앞으로 진행 상황에 따라 유연하게 업데이트됩니다.
