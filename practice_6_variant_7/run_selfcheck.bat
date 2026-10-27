@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\ru\edu\pr03\ApprovalRoute.java src\ru\edu\pr03\RiskLevel.java src\ru\edu\pr03\ApprovalRouter.java src\ru\edu\pr03\Request.java src\ru\edu\pr03\RequestParser.java src\ru\edu\pr03\Main.java src\ru\edu\pr03\SelfCheck.java
if errorlevel 1 exit /b 1
java -cp out ru.edu.pr03.SelfCheck
exit /b %errorlevel%
