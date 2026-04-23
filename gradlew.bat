@echo off
@rem Gradle Wrapper script for Windows

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%

set JAVACMD=java.exe
if not "%JAVA_HOME%" == "" set JAVACMD="%JAVA_HOME%\bin\java.exe"

set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if exist "%WRAPPER_JAR%" goto execute

echo Downloading gradle-wrapper.jar...
if not exist "%APP_HOME%\gradle\wrapper" mkdir "%APP_HOME%\gradle\wrapper"
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -OutFile '%WRAPPER_JAR%'"

:execute
%JAVACMD% %GRADLE_OPTS% -Xmx64m -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
