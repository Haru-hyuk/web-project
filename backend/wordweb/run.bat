@echo off
REM Spring Boot Application Runner
REM Uses system Java or sets JAVA_HOME if needed

echo Checking Java installation...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java is not found in PATH!
    echo Please set JAVA_HOME or add Java to PATH.
    pause
    exit /b 1
)

REM Clear problematic environment variables
set GRADLE_OPTS=
set JAVA_OPTS=

echo.
echo Starting Spring Boot Application...
echo Server will run at: http://localhost:8081
echo Press Ctrl+C to stop the server.
echo.

gradlew.bat bootRun

