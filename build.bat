@echo off
rem Build, test and package UC2 "Find a Match" on Windows – needs only a JDK 17+.
rem   build.bat        compile + run tests + create out\matching-uc2.jar
rem   build.bat run    same, then start the web app on http://localhost:8080
setlocal
cd /d "%~dp0"
set TESTS=com.sangreyoro.matching.StrategyTest com.sangreyoro.matching.MatchingServiceTest com.sangreyoro.matching.PatternTest com.sangreyoro.matching.ApiTest
if exist out rmdir /s /q out
mkdir out\classes\data out\test-classes
dir /s /b src\main\java\*.java > out\main-sources.txt
dir /s /b src\test\java\*.java > out\test-sources.txt
echo == Compiling sources
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d out\classes @out\main-sources.txt || exit /b 1
xcopy /e /q /y src\main\resources out\classes\ > nul
copy /y data\students.csv out\classes\data\students.csv > nul
echo == Compiling tests
javac --release 17 -encoding UTF-8 -cp out\classes -d out\test-classes @out\test-sources.txt || exit /b 1
echo == Running tests
java -Dstdout.encoding=UTF-8 -cp out\classes;out\test-classes com.sangreyoro.matching.testkit.TestRunner %TESTS% || exit /b 1
echo == Packaging
jar --create --file out\matching-uc2.jar --main-class com.sangreyoro.matching.MatchingApplication -C out\classes .
echo Built out\matching-uc2.jar
if "%1"=="run" java -jar out\matching-uc2.jar 8080
