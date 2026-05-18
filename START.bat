@echo off

echo Starting Backend...
cd backend
start cmd /k "mvnw.cmd spring-boot:run"

timeout /t 10

echo Opening Website...
start http://localhost:8080/pages/index.html

exit