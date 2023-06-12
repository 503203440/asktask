@echo off

set JAVA_HOME=D:\software\jdk\openjdk-20.0.1_windows-x64_bin\jdk-20.0.1
set PATH=%JAVA_HOME%/bin;%PATH%

set MAVEN_HOME=D:/software/Maven/apache-maven-3.8.3
set PATH=%MAVEN_HOME%\bin;%PATH%

call mvn clean package -DskipTests=true

SET MAIN_JAR_NAME=asktask-run.jar

SET iconFilePath=%~dp0%src/main/resources/favicon.ico

SET sourceFolder=%~dp0target\jpackage%

mkdir %sourceFolder%

echo %~dp0target\%MAIN_JAR_NAME%

copy %~dp0target\%MAIN_JAR_NAME%  %sourceFolder%

REM 调用jdeps --print-module-deps --ignore-missing-deps %~dp0target\%MAIN_JAR_NAME%并将结果赋值给modules
for /f %%i in ('call jdeps --print-module-deps --ignore-missing-deps %~dp0target\%MAIN_JAR_NAME%') do set modules=%%i

echo %modules%

jpackage.exe --type app-image -i "%sourceFolder%" -n asktask --java-options "--enable-preview -XX:+UseZGC -Xmx100m" --main-jar %MAIN_JAR_NAME% --icon %iconFilePath% --add-modules %modules% -d "%~dp0%target"
