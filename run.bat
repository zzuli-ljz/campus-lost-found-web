@echo off
echo Starting Campus Lost Found Application...

REM Set Java version compatibility
set JAVA_OPTS=-Djava.version=17

REM Try to start with SimpleApplication (simplified version)
echo Attempting to start with SimpleApplication...
java %JAVA_OPTS% -cp "target/classes" com.campus.lostfound.SimpleApplication

pause