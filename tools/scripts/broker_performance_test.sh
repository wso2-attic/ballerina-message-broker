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

# deleting existing folders
if [ -e logs/ ];
then
    rm -r logs/
fi

if [ -e report/ ];
then
    rm -r report/
fi

# get the properties file location
properties_file_location=$1

if [ "$properties_file_location" == '' ]
        then
            properties_file_location=resources/ballerina_message_broker_performance_test.properties
    fi

# hash-map to store user desired parameters
declare -A user_inputs=(["jmeter_home"]="" ["jndi_file_location"]="resources/jndi.properties" ["jmx_file_location"]="test_plan/ballerina_message_broker_performance_test.jmx" ["thread_count"]="10000" ["loop_count"]="1" ["ramp_time"]="0" ["duration_of_the_test"]="900" ["throughput"]="5000" ["message_size"]="10")

# Method to extract values from property file
getProperty()
{
    local property_key=$1
    local user_value=`cat ${properties_file_location} | grep ${property_key} | cut -d'=' -f2`
    echo "$user_value"
}

for parameter in "${!user_inputs[@]}";
do
    value=$(getProperty $parameter)
    if [ "$value" != '' ]
        then
            user_inputs["$parameter"]="$value"
    fi
done

# Summarizing inputs
echo The test plan is set to loop count - ${user_inputs["loop_count"]}, thread count - ${user_inputs["thread_count"]},ramp time - ${user_inputs["ramp_time"]} and throughput - ${user_inputs["throughput"]}
echo JMX file location is set to - ${user_inputs["jmx_file_location"]}
echo JNDI properties file location is set to - ${user_inputs["jndi_file_location"]}
echo Jmeter home is set to - ${user_inputs["jmeter_home"]}

# variable to store message math
text_message_file_location="sample_messages/1kB.json"
case ${user_inputs["message_size"]} in
	10)
		text_message_file_location="sample_messages/10kB.json"
		;;
	100)
        text_message_file_location="sample_messages/100kB.json"
		;;
	1000)
		text_message_file_location="sample_messages/1MB.json"
		;;
  esac

# retrieve the message
message=`cat $text_message_file_location`

if [ ${user_inputs["jmeter_home"]} != '' ]
        then
            # if user specified jmeter home
            ${user_inputs["jmeter_home"]}/jmeter -n -t ${user_inputs["jmx_file_location"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DRAMP_TIME=${user_inputs["ramp_time"]} -DDURATION_OF_THE_TEST=${user_inputs["duration_of_the_test"]} -DJNDI_URL=${user_inputs["jndi_file_location"]} -DTHROUGHPUT=${user_inputs["throughput"]} -DMESSAGE="$message"  -l logs/test_results.jtl -e -o report/
        else
            # if jmeter_home is already configured
            jmeter -n -t ${user_inputs["jmx_file_location"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DRAMP_TIME=${user_inputs["ramp_time"]} -DDURATION_OF_THE_TEST=${user_inputs["duration_of_the_test"]} -DJNDI_URL=${user_inputs["jndi_file_location"]} -DTHROUGHPUT=${user_inputs["throughput"]} -DMESSAGE="$message"  -l logs/test_results.jtl -e -o report/
    fi

# execute jmeter command

# open report
xdg-open report/index.html
