@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%__JAVA_HUB_DEBUG__%"=="1" @echo on

@setlocal

set MAVEN_CMD_LINE_ARGS=%*

@REM Find the project base dir, i.e. the dir that contains the folder ".mvn".
set MAVEN_PROJECT_BASEDIR=%~dp0
:findBaseDir
@if exist "%MAVEN_PROJECT_BASEDIR%\.mvn" goto baseDirFound
set MAVEN_PROJECT_BASEDIR=%MAVEN_PROJECT_BASEDIR%..
goto findBaseDir
:baseDirFound

set MAVEN_CONFIG=%MAVEN_PROJECT_BASEDIR%\.mvn

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute
echo Error: JAVA_HOME not set and 'java' command not found in PATH. 1>&2
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute
echo Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
exit /b 1

:execute
@REM Execute Maven command via Java
"%JAVA_EXE%" -jar "%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.jar" %MAVEN_CMD_LINE_ARGS%
