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

# variable to store the location of properties file
properties_file_location="resources/broker_test_consumer.properties"

# variable to store jms destination topic/queue.Default value is queue
jmx_file_location="test_plan/broker_test_queue_consumer.jmx"
destination=""
connection_factory_name=""
jndi_file_location=""

# get inputs from the user -p <location of the properties file> -d topic/queue
while getopts "hp:d:t:h:v" OPTION
do
     case $OPTION in
         p)
            properties_file_location=$OPTARG
            break
            ;;
         d)
            case $OPTARG in
                queue)
                    destination="QueueName"
                    connection_factory_name="QueueConnectionFactory"
                    jndi_file_location="resources/jndi_queue.properties"
                    jmx_file_location="test_plan/broker_performance_test_queue_consumer.jmx"
                ;;
                topic)
                    destination="TopicName"
                    connection_factory_name="TopicConnectionFactory"
                    jndi_file_location="resources/jndi_topic.properties"
                    jmx_file_location="test_plan/broker_test_topic_consumer.jmx"
                    ;;
                ?)
                echo $OPTARG
                    echo "$OPTARG is an invalid destination.JMS destination should be a queue or a topic"
                    exit
                ;;
            esac
            break
            ;;
         h)
            help_text="Welcome to ballerina message broker micro-benchmark tool\n\nUsage:\n\t./broker_test_consumer.sh [command].\n\nCommands\n\t-h  ask for help\n\t-p  set the location of the properties file\n\t-d  set jms destination type queue/topic\n"
            printf "$help_text"
            exit
            ;;
         ?)
            printf "Invalid command.Run ./broker_test_consumer.sh -h for usage.\n"
            exit
            ;;
     esac
done

# check for target folder
if [ ! -e target/ ];
then
    mkdir -p target/subscriber
fi

# hash-map to store user desired parameters
declare -A user_inputs=(["jmeter_home"]="" ["thread_count"]="1" ["number_of_messages"]="1000000")

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
if [ ${user_inputs["thread_count"]} == '0' ]
    then
        # Command invoked cannot execute
        echo Thread count cannot be zero
        exit
    fi

# Summarizing inputs
echo Jmeter home is set to - ${user_inputs["jmeter_home"]}
echo Starting test process with a throughput - ${user_inputs["throughput"]}

# calculate loop count
loop_count=$(echo "${user_inputs["number_of_messages"]}/${user_inputs["thread_count"]}+1" | bc)

# create folder to store report files
folder_name=$(date '+%d-%m-%Y-%H-%M-%S')
mkdir -p target/subscriber/"$folder_name"

# execute jmeter command
if [ ${user_inputs["jmeter_home"]} != '' ]
        then
            # if user specified jmeter home
            ${user_inputs["jmeter_home"]}/jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DDURABLE_SUB_COUNT=${user_inputs["thread_count"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DLOOP_COUNT=$loop_count -l target/subscriber/"$folder_name"/log/test_results.jtl -e -o target/subscriber/"$folder_name"/report/
        else
            jmeter_console_out="$(command -v jmeter)"
            if [${jmeter_console_out} == '']
                then
                    echo Please set jmeter home or include jmeter_home property in the properties file
                else
                    echo "$jmeter_console_out"
                    # if jmeter_home is already configured
                    jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DDURABLE_SUB_COUNT=${user_inputs["thread_count"]} -DTHREAD_COUNT=${user_inputs["thread_count"]} -DLOOP_COUNT=$loop_count -l target/subscriber/"$folder_name"/log/test_results.jtl -e -o target/subscriber/"$folder_name"/report/
            fi
        fi

# open report
firefox target/subscriber/"$folder_name"/report/index.html
