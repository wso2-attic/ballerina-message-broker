#!/usr/bin/env sh
# ---------------------------------------------------------------------------
#  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# set MESSAGE_BROKER_HOME
MESSAGE_BROKER_HOME=`cd "$PRGDIR/.." ; pwd`

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  exit 1
fi

# if JAVA_HOME is not set we're not happy
if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running the Broker."
  exit 1
fi

echo JAVA_HOME environment variable is set to $JAVA_HOME
echo MESSAGE_BROKER_HOME environment variable is set to $MESSAGE_BROKER_HOME

MESSAGE_BROKER_CLASSPATH="$MESSAGE_BROKER_HOME/lib/*"

$JAVACMD \
    -Xms256m -Xmx1024m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath="$MESSAGE_BROKER_HOME/heap-dump.hprof" \
    -classpath "$MESSAGE_BROKER_CLASSPATH" \
    -Dmessage.broker.home="$MESSAGE_BROKER_HOME" \
    -Dbroker.config="$MESSAGE_BROKER_HOME/conf/broker.yaml" \
    io.ballerina.messaging.secvault.ciphertool.CipherToolInitializer $*

echo DONE!
