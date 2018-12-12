@echo off

REM ---------------------------------------------------------------------------
REM  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM  WSO2 Inc. licenses this file to you under the Apache License,
REM  Version 2.0 (the "License"); you may not use this file except
REM  in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing,
REM  software distributed under the License is distributed on an
REM  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM  KIND, either express or implied. See the License for the
REM  specific language governing permissions and limitations
REM  under the License.
REM ---------------------------------------------------------------------------

rem ----- if JAVA_HOME is not set we're not happy ------------------------------
:checkJava

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running the Broker"
goto end

rem ----- set MESSAGE_BROKER_HOME ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
set MESSAGE_BROKER_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET brokerDrive=%MESSAGE_BROKER_HOME:~0,1%
if not "%curDrive%" == "%brokerDrive%" %brokerDrive%:

goto updateClasspath

rem ----- update classpath -----------------------------------------------------
:updateClasspath

setlocal EnableDelayedExpansion
cd %MESSAGE_BROKER_HOME%
set MESSAGE_BROKER_CLASSPATH=
FOR %%C in ("%MESSAGE_BROKER_HOME%\lib\*.jar") DO set MESSAGE_BROKER_CLASSPATH=!MESSAGE_BROKER_CLASSPATH!;".\lib\%%~nC%%~xC"

rem ----- Process the input command -------------------------------------------

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).

:setupArgs
if ""%1""=="""" goto doneStart

if ""%1""==""debug""    goto commandDebug
if ""%1""==""-debug""   goto commandDebug
if ""%1""==""--debug""  goto commandDebug

shift
goto setupArgs


rem ----- commandDebug ---------------------------------------------------------
:commandDebug
shift
set DEBUG_PORT=%1
if "%DEBUG_PORT%"=="" goto noDebugPort
if not "%JAVA_OPTS%"=="" echo Warning !!!. User specified JAVA_OPTS will be ignored, once you give the --debug option.
set JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT%
echo Please start the remote debugging client to continue...
goto runServer

:noDebugPort
echo Please specify the debug port after the --debug option
goto end


:doneStart
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal
goto runServer


rem ----------------- Execute The Requested Command ----------------------------

:runServer
set CMD=%*

rem ---------- Add jars to classpath ----------------
set MESSAGE_BROKER_CLASSPATH=.\lib;%MESSAGE_BROKER_CLASSPATH%

rem Please use following options if you want to use JMX monitoring remotely.
rem You will have to use the machine IP instead of '127.0.0.1'.
rem -Djava.rmi.server.hostname=127.0.0.1
rem -Dcom.sun.management.jmxremote.port=9595
rem -Dcom.sun.management.jmxremote.ssl=false
rem -Dcom.sun.management.jmxremote.authenticate=false
set CMD_LINE_ARGS=-Xbootclasspath/a:%MESSAGE_BROKER_XBOOTCLASSPATH% -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="%MESSAGE_BROKER_HOME%\heap-dump.hprof" -classpath %MESSAGE_BROKER_HOME%\lib\* %JAVA_OPTS% -Dmessage.broker.home="%MESSAGE_BROKER_HOME%" -Djava.command="%JAVA_HOME%\bin\java" -Djava.opts="%JAVA_OPTS%" -Dlog4j.configuration="file:%MESSAGE_BROKER_HOME%\conf\log4j.properties" -Dbroker.config="%MESSAGE_BROKER_HOME%\conf\broker.yaml"  -Dbroker.users.config="%MESSAGE_BROKER_HOME%\conf\security\users.yaml" -Dtransports.netty.conf="%MESSAGE_BROKER_HOME%\conf\admin-service-transports.yaml" -Dbroker.classpath=%MESSAGE_BROKER_HOME%\lib\* -Dfile.encoding=UTF8

:runJava
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% io.ballerina.messaging.broker.Main %CMD%
:end
goto endlocal

:endlocal

:END