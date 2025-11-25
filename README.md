WordWeb Backend — Progress Summary

(2025.11.24 기준)

본 문서는 WordWeb 서비스 백엔드 개발 현황을 정리한 것입니다.
Spring Boot 기반 인증/인가, DB 세팅, AI 스토리 기능 준비까지 완료된 상태입니다.

✅ 1. 개발 환경 구성 완료
Backend 기술 스택

Spring Boot 4.0

Java 17

Spring Security (JWT 기반)

Spring Data JPA

Oracle Database 23c FREE

Lombok / Hibernate / HikariCP

Postman API 테스트 환경 구축

AI 기능 → OpenAI → DeepSeek 등 확장 가능 구조

✅ 2. 데이터베이스 구성 완료
연결

Oracle FREEPDB1 연결 완료

application.yml 설정 완료

HikariCP로 connection pool 정상 동작

테이블
1) USERS
컬럼	설명
USER_ID (PK)	회원 고유 키
EMAIL	이메일
USER_PW	비밀번호(BCrypt 암호화)
NICKNAME	닉네임
USER_NAME	이름
USER_BIRTH	생년월일
PREFERENCE	관심 분야
GOAL	학습 목표
DAILY_WORD_GOAL	일일 학습 목표
CREATED_AT	생성일
UPDATED_AT	업데이트일
2) REFRESH_TOKEN
컬럼	설명
USER_EMAIL (PK)	이메일
REFRESH_TOKEN	발급된 리프레시 토큰
✅ 3. 인증/인가(JWT) 시스템 구축
구성 요소

JwtTokenProvider (발급/검증)

JwtAuthenticationFilter (요청 필터링)

JwtAuthenticationEntryPoint (401 처리)

JwtAccessDeniedHandler (403 처리)

SecurityConfig (permitAll / 인증 설정)

.env → JWT_SECRET 환경변수 시스템 적용

지원 기능

✔ 회원가입
✔ 로그인
✔ AccessToken + RefreshToken 발급
✔ 토큰 저장
✔ 로그인 후 보호 API 접근
✔ JWT 만료/유효성 예외 처리 완료

✅ 4. 주요 API 개발 완료
1) 회원가입
POST /api/auth/signup


비밀번호 BCrypt 암호화

USERS DB 저장

중복 이메일 예외 처리

2) 로그인
POST /api/auth/login


이메일/비밀번호 검증

AccessToken + RefreshToken 발급

REFRESH_TOKEN 테이블 저장

3) Refresh Token 재발급
POST /api/auth/refresh


리프레시 토큰 유효성 확인

새 AccessToken 재발급

4) 로그아웃
POST /api/auth/logout


해당 이메일의 refresh-token 삭제

✅ 5. 예외처리 글로벌 Handler 구축

GlobalExceptionHandler

JWT 오류 처리

IllegalArgumentException 처리

RuntimeException 처리

서버 내부 오류 처리

✅ 6. Postman 테스트 전체 성공

회원가입 → 성공

로그인 → 토큰 발급 정상

Refresh → 정상

보호 API 접근 시 JWT 없으면 401

BODY/Headers 세팅 문제 해결
