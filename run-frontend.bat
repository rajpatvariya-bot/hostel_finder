@echo off
REM ========================================
REM Start Frontend Only
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0frontend"

echo.
echo ========================================
echo   Starting Frontend Server
echo   (Python HTTP Server on port 3000)
echo ========================================
echo.

echo Serving frontend at: http://localhost:3000
echo.

python -m http.server 3000

pause
