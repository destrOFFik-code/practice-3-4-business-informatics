@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\ru\edu\pr05\TripStatus.java src\ru\edu\pr05\BusinessTrip.java src\ru\edu\pr05\BusinessTripService.java src\ru\edu\pr05\Main.java src\ru\edu\pr05\SelfCheck.java
if errorlevel 1 exit /b 1
java -cp out ru.edu.pr05.SelfCheck
exit /b %errorlevel%
