@echo off

set JAVA_HOME=C:\software\jdk\zulu21.30.15-ca-jdk21.0.1-win_x64
set PATH=%JAVA_HOME%/bin;%PATH%

set MAVEN_HOME=C:/software/maven/apache-maven-3.9.4
set PATH=%MAVEN_HOME%\bin;%PATH%

call mvn clean package -DskipTests=true

SET MAIN_JAR_NAME=asktask-run.jar

SET iconFilePath=%~dp0%src/main/resources/favicon.ico

SET sourceFolder=%~dp0target\jpackage%

mkdir %sourceFolder%

echo %~dp0target\%MAIN_JAR_NAME%

copy %~dp0target\%MAIN_JAR_NAME%  %sourceFolder%

REM 调用jdeps --print-module-deps --ignore-missing-deps %~dp0target\%MAIN_JAR_NAME%并将结果赋值给modules
REM
REM
REM
REM

REM for /f %%i in ('call jdeps --print-module-deps --ignore-missing-deps %~dp0target\%MAIN_JAR_NAME%') do set modules=%%i

set modules=java.base,java.compiler,java.desktop,java.management,java.naming,java.rmi,java.scripting,java.sql,jdk.httpserver,java.sql,java.security.sasl,java.security.jgss,java.xml.crypto,jdk.charsets,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.crypto.mscapi
echo %modules%
@REM VirtualAlloc2 Error occurred during initialization of VM ZGC requires Windows version 1803 or later
jpackage.exe --type app-image -i "%sourceFolder%" -n asktask --java-options "-Djava.net.preferIPv4Stack=true -Xmx100m" --main-jar %MAIN_JAR_NAME% --icon %iconFilePath% --add-modules %modules% -d "%~dp0%target" --name "asktask" --copyright "gpai.net" --vendor "gpai.net"
