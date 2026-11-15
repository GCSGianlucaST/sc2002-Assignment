@echo off
rem Build and play:          run.cmd
setlocal enabledelayedexpansion
cd /d "%~dp0"
if exist out rmdir /s /q out
if exist sources.txt del sources.txt
for /r src %%f in (*.java) do (
  set "p=%%f"
  echo "!p:\=/!">> sources.txt
)
javac --release 17 -d out @sources.txt
set err=%errorlevel%
del sources.txt
if not %err%==0 exit /b %err%
java -cp out restaurantrush.app.RestaurantRushApp %*
