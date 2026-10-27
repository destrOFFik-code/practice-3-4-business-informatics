@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\ru\edu\pr02\TimesheetAnalyzer.java src\ru\edu\pr02\Main.java src\ru\edu\pr02\SelfCheck.java
if errorlevel 1 exit /b 1
java -cp out ru.edu.pr02.SelfCheck
exit /b %errorlevel%
