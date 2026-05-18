@echo off
REM ========================================
REM Start Backend Only
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0backend"

echo.
echo ========================================
echo   Starting Backend Server
echo   (Spring Boot on port 8080)
echo ========================================
echo.

echo Compiling and starting Spring Boot...
mvnw spring-boot:run

pause
