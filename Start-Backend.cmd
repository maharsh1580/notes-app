@echo off
setlocal
cd /d "%~dp0"
set "DOCKER_BIN=%LOCALAPPDATA%\Programs\DockerDesktop\resources\bin\docker.exe"
set "DOCKER_DESKTOP=%LOCALAPPDATA%\Programs\DockerDesktop\Docker Desktop.exe"
if not exist "%DOCKER_BIN%" (
  set "DOCKER_BIN=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
  set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
)
if not exist "%DOCKER_BIN%" (
  echo Docker Desktop is not installed in its standard location.
  pause
  exit /b 1
)
"%DOCKER_BIN%" info >nul 2>&1
if errorlevel 1 start "" "%DOCKER_DESKTOP%"
echo Waiting for Docker Desktop...
set /a ATTEMPTS=0
:waitDocker
"%DOCKER_BIN%" info >nul 2>&1
if not errorlevel 1 goto runNotes
set /a ATTEMPTS+=1
if %ATTEMPTS% geq 60 (
  echo Docker is not ready. Check its window for setup or restart instructions.
  pause
  exit /b 1
)
timeout /t 2 /nobreak >nul
goto waitDocker
:runNotes
"%DOCKER_BIN%" compose up --build -d
if errorlevel 1 (
  echo The backend could not start. Check the error above.
  pause
  exit /b 1
)
echo.
echo Notes backend started. API docs: http://localhost:8000/docs
echo Run the Notes app on your Android emulator.
pause
