# Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at

#    http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

#!/bin/bash
echo Enter properties file location -
read fileLocation

#Variable to hold the Property Value
prop_value=""

getProperty()
{
    prop_key=$1
    prop_value=`cat ${fileLocation} | grep ${prop_key} | cut -d'=' -f2`
}

getProperty "THREAD_COUNT"
threadCount=${prop_value}
getProperty "LOOP_COUNT"
loopCount=${prop_value}
getProperty "RAMP_TIME"
rampTime=${prop_value}
getProperty "JNDI_FILE_LOCATION"
jndiProperties=${prop_value}
getProperty "JMX_FILE_LOCATION"
jmxLocation=${prop_value}
getProperty "JMETER_HOME"
jmeterHome=${prop_value}
getProperty "MESSAGE"
message=`cat ${prop_value}`
newMessage='"'."$message".'"'

## Summarizing inputs
echo The test plan is set to loop count - $loopCount, thread count - $threadCount and ramp time to $rampTime.
echo testplan.jmx file location is set to - $jmxLocation.
echo jndi.properties location is set to - $jndiProperties.
echo Jmeter home is set to - $jmeterHome.
$jmeterHome/jmeter -n -t $jmxLocation -DTHREAD_COUNT=$threadCount -DRAMP_TIME=$rampTime -DLOOP_COUNT=$loopCount -DJNDI_URL=$jndiProperties -DMESSAGE="$message"