@echo off
setlocal
cd /d "%~dp0"
set "DOCKER_BIN=%LOCALAPPDATA%\Programs\DockerDesktop\resources\bin\docker.exe"
if not exist "%DOCKER_BIN%" set "DOCKER_BIN=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
if not exist "%DOCKER_BIN%" (
  echo Docker Desktop could not be found.
  pause
  exit /b 1
)
"%DOCKER_BIN%" compose stop
if errorlevel 1 (
  echo The backend could not be stopped. Check the error above.
) else (
  echo Notes backend stopped. Your notes remain saved.
)
pause
