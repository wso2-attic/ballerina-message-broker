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
properties_file_location="resources/broker_test_publisher.properties"

# variable to store jms destination topic/queue.Default value is queue
jmx_file_location="test_plan/broker_test_publisher.jmx"
destination=""
connection_factory_name=""
jndi_file_location=""
is_given_destination=false
queue_name=""
exchange_name=""
base_file_location="/publisher"

# get inputs from the user -p <location of the properties file> -d topic/queue
while getopts "hp:d:s:h:v:t" OPTION
do
     case $OPTION in
         p)
            properties_file_location=$OPTARG
            break
            ;;
         d)
            case $OPTARG in
                queue)
                    is_given_destination=true
                    destination="QueueName"
                    connection_factory_name="QueueConnectionFactory"
                    jndi_file_location="resources/jndi_queue.properties"
                    queue_name="micro_benchmark_queue1"
                    exchange_name="amq.direct"
                ;;
                topic)
                    is_given_destination=true
                    destination="TopicName"
                    connection_factory_name="TopicConnectionFactory"
                    jndi_file_location="resources/jndi_topic.properties"
                    queue_name="micro_benchmark_queue2"
                    exchange_name="amq.topic"
                    ;;
                ?)
                echo $OPTARG
                    echo "$OPTARG is an invalid destination.JMS destination should be a queue or a topic"
                    exit
                ;;
            esac
            break
            ;;
         s)
            base_file_location="$OPTARG/publisher"
            ;;
         h)
            help_text="Welcome to ballerina message broker micro-benchmark tool\n\nUsage:\n\t./broker_test_publisher.sh [command].\n\nCommands\n\t-h  ask for help\n\t-p  set the location of the properties file\n\t-d  set jms destination type queue/topic\n"
            printf "$help_text"
            exit
            ;;
         ?)
            printf "Invalid command.Run ./broker_test_publisher.sh -h for usage.\n"
            exit
            ;;
     esac
done

if [ $is_given_destination == false ];
    then
        printf 'A JMS destination should be provided.\nRun ./broker_test_publisher.sh -h for usage.\n'
        exit
    fi

# Method to extract values from property file
getProperty()
{
    local property_key=$1
    local user_value=`cat ${properties_file_location} | grep ${property_key} | cut -d'=' -f2`
    echo "$user_value"
}

# hash-map to store user desired parameters
declare -A user_inputs=(["jmeter_home"]="" ["host_url"]="" ["broker_port"]="9000" ["amqp_listener_port"]="5672" ["thread_count"]="1" ["number_of_messages"]="1000000" ["throughput"]="5000" ["message_size"]="10KB")

for parameter in "${!user_inputs[@]}";
do
    value=$(getProperty $parameter)
    if [ "$value" != '' ]
        then
            user_inputs["$parameter"]="$value"
    fi
done

case $queue_name in
    micro_benchmark_queue1)
        if [ -e resources/jndi_queue.properties ];
            then
                 rm resources/jndi_queue.properties
            fi
            printf "connectionfactory.QueueConnectionFactory=amqp://admin:admin@clientID/carbon?brokerlist='tcp://${user_inputs["host_url"]}:${user_inputs["amqp_listener_port"]}'\nqueue.QueueName=micro_benchmark_queue1" >> resources/jndi_queue.properties
        ;;
    micro_benchmark_queue2)
        if [ -e resources/jndi_topic.properties ];
            then
                 rm resources/jndi_topic.properties
            fi
            printf "connectionfactory.TopicConnectionFactory=amqp://admin:admin@clientID/carbon?brokerlist='tcp://${user_inputs["host_url"]}:${user_inputs["amqp_listener_port"]}'\ntopic.TopicName=micro_benchmark_queue2" >> resources/jndi_topic.properties
        ;;
esac

# validate inputs
if [ ${user_inputs["host_url"]} == '' ]
    then
        echo "broker_url parameter in broker_test_publisher.properties cannot be empty."
        exit
    elif  [ ${user_inputs["thread_count"]} == '0' ]
        then
            echo "thread_count parameter in broker_test_publisher.properties cannot be empty."
            exit
    elif  [ ${user_inputs["throughput"]} == '0' ]
        then
            echo "throughput parameter in broker_test_publisher.properties cannot be empty."
            exit
    fi

# create target folder if not exist
if [ ! -e target/"$base_file_location" ];
    then
        mkdir -p target/"$base_file_location"
    fi

# create queues and bindings to execute tests
queue_available_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" https://${user_inputs["host_url"]}:${user_inputs["broker_port"]}/broker/v1.0/queues/"$queue_name")
# if queue is not available create queue
if [ "$queue_available_response" == 404 ]
    then
        json_payload='{"name":"'"$queue_name"'", "durable":"true","autoDelete":"true"}'
        queue_create_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" -d "$json_payload" -H "Content-Type: application/json" -X POST https://${user_inputs["host_url"]}:${user_inputs["broker_port"]}/broker/v1.0/queues)
        if [ "$queue_create_response" == "201" ]
            then
                echo $queue_name created sucessfully.
                json_payload='{"bindingPattern":"'"$queue_name"'","exchangeName":"'"$exchange_name"'","filterExpression":""}'
                queue_bind_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" -d "$json_payload"  -H "Content-Type: application/json" -X POST https://${user_inputs["host_url"]}:${user_inputs["broker_port"]}/broker/v1.0/queues/"$queue_name"/bindings)
                if [ "$queue_bind_response" == "201" ]
                    then
                        echo "Binding created sucessfully with $queue_name.Exchange name - $exchange_name"
                    else
                        echo "Error occured while creating binding.Response code $queue_bind_response"
                        exit
                    fi
            else
                echo "Error occured while creating queue $queue_name.Response code $queue_create_response"
                exit
            fi
    else
        echo "$queue_name is already available."
    fi

## Summarizing inputs
echo Jmeter home is set to - ${user_inputs["jmeter_home"]}
echo Starting test process with ${user_inputs["number_of_messages"]} messages and a and throughput - ${user_inputs["throughput"]}

# calculate loop count
loop_count=$(echo "${user_inputs["number_of_messages"]}/${user_inputs["thread_count"]}" | bc)
duration_of_the_test=$(echo "${user_inputs["number_of_messages"]}/${user_inputs["throughput"]}" | bc)

# variable to store message math
text_message_file_location=""
case ${user_inputs["message_size"]} in
    1KB)
        text_message_file_location="sample_messages/1kB.json"
        ;;
	10KB)
		text_message_file_location="sample_messages/10kB.json"
		;;
	100KB)
        text_message_file_location="sample_messages/100kB.json"
		;;
	1MB)
		text_message_file_location="sample_messages/1MB.json"
		;;
	?)
	    echo message_size parameter should be one of 1KB,10KB,100KB and 1MB
	    exit
	;;
esac

# create folder to store report files
folder_name=$(date '+%d-%m-%Y-%H-%M-%S')
mkdir -p target/"$base_file_location"/"$folder_name"

# execute jmeter command
if [ ${user_inputs["jmeter_home"]} != '' ]
        then
            # if user specified jmeter home
            ${user_inputs["jmeter_home"]}/bin/jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DTHREAD_COUNT=${user_inputs["thread_count"]} -DDURATION_OF_THE_TEST=$duration_of_the_test -DLOOP_COUNT=$loop_count -DTHROUGHPUT=${user_inputs["throughput"]} -DFILE_PATH="$text_message_file_location"  -l target/"$base_file_location"/"$folder_name"/log/test_results.jtl -e -o target/"$base_file_location"/"$folder_name"/report/
        else
            jmeter_console_out="$(command -v jmeter)"
            if [${jmeter_console_out} == '']
                then
                    echo Please set jmeter home or include jmeter_home property in the properties file
                    exit
                else
                    echo "$jmeter_console_out"
                    # if jmeter_home is already configured
                    jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DTHREAD_COUNT=${user_inputs["thread_count"]} -DLOOP_COUNT=$loop_count -DDURATION_OF_THE_TEST=$duration_of_the_test -DTHROUGHPUT=${user_inputs["throughput"]} -DFILE_PATH="$text_message_file_location"  -l target/"$base_file_location"/"$folder_name"/log/test_results.jtl -e -o target/"$base_file_location"/"$folder_name"/report/
            fi
        fi

# log report location
echo A report of the test results is generated at target/"$base_file_location"/"$folder_name"/report