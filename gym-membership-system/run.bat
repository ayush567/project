@echo off
title Gym Membership Tracking and Management System
cd /d "%~dp0"

echo [1/2] Compiling Java source files...
if not exist bin mkdir bin
javac -cp "lib\mysql-connector-j-26.7.0.jar" -d bin src\interfaces\*.java src\exceptions\*.java src\model\*.java src\database\*.java src\service\*.java src\gui\*.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching application...
java -cp "bin;lib\mysql-connector-j-26.7.0.jar" Main
