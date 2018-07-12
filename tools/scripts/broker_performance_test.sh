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

#!/usr/bin/env bash

# check for target folder
if [ ! -e target/ ];
then
    mkdir target
fi

# get the properties file location
properties_file_location=$1

if [ "$properties_file_location" == '' ]
        then
            properties_file_location=resources/ballerina_message_broker_performance_test.properties
        fi

# hash-map to store user desired parameters
declare -A user_inputs=(["jmeter_home"]="" ["jndi_file_location"]="resources/jndi.properties" ["jmx_file_location"]="test_plan/ballerina_message_broker_performance_test.jmx" ["thread_count"]="1" ["number_of_messages"]="1000000" ["throughput"]="5000" ["message_size"]="10KB")

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

# validate inputs
if [ ${user_inputs["thread_count"]} == '0' ]  || [ ${user_inputs["throughput"]} == '0' ]
    then
        # Command invoked cannot execute
        echo Thread count and throughput cannot be zero
        exit 126
    fi

# Summarizing inputs
echo JMX file location is set to - ${user_inputs["jmx_file_location"]}
echo JNDI properties file location is set to - ${user_inputs["jndi_file_location"]}
echo Jmeter home is set to - ${user_inputs["jmeter_home"]}
echo Starting test process with ${user_inputs["number_of_messages"]} messages and a and throughput - ${user_inputs["throughput"]}

# calculate loop count
loop_count=$(echo "${user_inputs["number_of_messages"]}/${user_inputs["thread_count"]}+1" | bc)
duration_of_the_test=$(echo "${user_inputs["number_of_messages"]}/${user_inputs["throughput"]}" | bc)

# variable to store message math
text_message_file_location="sample_messages/1kB.json"
case ${user_inputs["message_size"]} in
	10KB)
		text_message_file_location="sample_messages/10kB.json"
		;;
	100KB)
        text_message_file_location="sample_messages/100kB.json"
		;;
	1MB)
		text_message_file_location="sample_messages/1MB.json"
		;;
esac

# create folder to store report files
folder_name=$(date '+%d-%m-%Y-%H-%M-%S')
mkdir target/"$folder_name"

# execute jmeter command
if [ ${user_inputs["jmeter_home"]} != '' ]
        then
            # if user specified jmeter home
            ${user_inputs["jmeter_home"]}/jmeter -n -t ${user_inputs["jmx_file_location"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DDURATION_OF_THE_TEST=$duration_of_the_test -DLOOP_COUNT=$loop_count -DJNDI_URL=${user_inputs["jndi_file_location"]} -DTHROUGHPUT=${user_inputs["throughput"]} -DFILE_PATH="$text_message_file_location"  -l target/"$folder_name"/log/test_results.jtl -e -o target/"$folder_name"/report/
            # open report
            firefox target/"$folder_name"/report/index.html
        else
            jmeter_console_out="$(command -v jmeter)"
            if [${jmeter_console_out} == '']
                then
                    echo Please set jmeter home or include jmeter_home property in the properties file
                else
                    # if jmeter_home is already configured
                    jmeter -n -t ${user_inputs["jmx_file_location"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DLOOP_COUNT=$loop_count -DDURATION_OF_THE_TEST=$duration_of_the_test -DJNDI_URL=${user_inputs["jndi_file_location"]} -DTHROUGHPUT=${user_inputs["throughput"]} -DFILE_PATH="$text_message_file_location"  -l target/"$folder_name"/log/test_results.jtl -e -o target/"$folder_name"/report/
                    # open report
                    firefox target/"$folder_name"/report/index.html
            fi
            # if jmeter_home is already configured
            jmeter -n -t ${user_inputs["jmx_file_location"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DLOOP_COUNT=$loop_count -DDURATION_OF_THE_TEST=$duration_of_the_test -DJNDI_URL=${user_inputs["jndi_file_location"]} -DTHROUGHPUT=${user_inputs["throughput"]} -DFILE_PATH="$text_message_file_location"  -l target/"$folder_name"/log/test_results.jtl -e -o target/"$folder_name"/report/
        fi

