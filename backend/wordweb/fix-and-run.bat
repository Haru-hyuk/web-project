@echo off
chcp 65001 >nul
echo ========================================
echo Java 환경 확인 및 수정
echo ========================================
echo.

REM 잘못된 JAVA_HOME 환경 변수 제거
if defined JAVA_HOME (
    echo 현재 JAVA_HOME: %JAVA_HOME%
    set JAVA_HOME=
    echo JAVA_HOME 환경 변수를 초기화했습니다.
    echo.
)

REM 시스템 Java 확인
echo Java 버전 확인:
java -version
if %errorlevel% neq 0 (
    echo.
    echo [오류] Java를 찾을 수 없습니다!
    echo Java가 PATH에 있는지 확인하세요.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Gradle 실행
echo ========================================
echo.

REM GRADLE_OPTS와 JAVA_OPTS 초기화
set GRADLE_OPTS=
set JAVA_OPTS=

REM Gradle 실행
gradlew.bat %*

pause

