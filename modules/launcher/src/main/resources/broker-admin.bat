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

goto runCli

rem ----------------- Execute The Requested Command ----------------------------
:runCli

set CMD_LINE_ARGS= -Dmessage.broker.home="%MESSAGE_BROKER_HOME%"  -Dbroker.config="%MESSAGE_BROKER_HOME%\conf\cli-config.yaml"
"%JAVA_HOME%\bin\java" -classpath %MESSAGE_BROKER_HOME%\lib\* %CMD_LINE_ARGS% io.ballerina.messaging.broker.client.Main %0 %*

:end
goto endlocal

:endlocal

:END