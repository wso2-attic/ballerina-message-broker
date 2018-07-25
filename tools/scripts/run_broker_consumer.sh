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
infrastructure_file_location="resources/broker_test_infrastructure.properties"
testplan_file_location="resources/broker_test_plan.properties"

# variable to store jms destination topic/queue.Default value is queue
jmx_file_location=""
destination=""
connection_factory_name=""
jndi_file_location=""
is_given_destination=false
queue_name=""
exchange_name=""
base_file_location="consumer"

# get inputs from the user -p <location of the properties file> -d topic/queue
while getopts "hi:d:s:t:h:v" OPTION
do
     case $OPTION in
         d)
            case $OPTARG in
                queue)
                    is_given_destination=true
                    destination="QueueName"
                    connection_factory_name="QueueConnectionFactory"
                    jndi_file_location="resources/jndi_queue.properties"
                    queue_name="micro_benchmark_queue1"
                    exchange_name="amq.direct"
                    jmx_file_location="test_plan/broker_test_queue_consumer.jmx"
                ;;
                topic)
                    is_given_destination=true
                    destination="TopicName"
                    connection_factory_name="TopicConnectionFactory"
                    jndi_file_location="resources/jndi_topic.properties"
                    queue_name="micro_benchmark_queue2"
                    exchange_name="amq.topic"
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
            help_text="Welcome to ballerina message broker micro-benchmark tool\n\nUsage:\n\t./run_broker_consumer.sh [command].\n\nCommands\n\t-h  ask for help\n\t-i  set the location of the infrastructure properties file\n\t-t  set the location of the testplan properties file\n\t-d  set jms destination type queue/topic\n"
            printf "$help_text"
            exit
            ;;
         i)
            infrastructure_file_location=$OPTARG
            break
            ;;
         t)
            testplan_file_location=$OPTARG
         ;;
         s)
            base_file_location="$OPTARG/consumer"
            ;;
         ?)
            printf "Invalid command.Run ./run_broker_consumer.sh -h for usage.\n"
            exit
            ;;
     esac
done

if [ $is_given_destination == false ];
    then
        printf 'A JMS destination should be provided.\nRun ./run_broker_consumer.sh -h for usage.\n'
        exit
    fi

# Method to extract values from property file
getProperty()
{
    local property_key=$1
    local file_location=$2
    local user_value=`cat ${file_location} | grep ${property_key} | cut -d'=' -f2`
    echo "$user_value"
}

# hash-map to store user test plan inputs
declare -A testplan_user_inputs=(["JmeterHome"]="" ["BrokerPort"]="9000" ["AMQPListenerPort"]="5672" ["NumberOfMessages"]="1000000" ["ThreadCount"]="5000")

for parameter in "${!testplan_user_inputs[@]}";
do
    value=$(getProperty $parameter $testplan_file_location)
    if [ "$value" != '' ]
        then
            testplan_user_inputs["$parameter"]="$value"
    fi
done

host_url=$(getProperty HostURL $infrastructure_file_location)

# validate inputs
if [ "$host_url" == '' ]
    then
        echo "HostURL parameter in broker_test_infrastructure.properties cannot be empty."
        exit
    elif  [ ${testplan_user_inputs["ThreadCount"]} == '0' ]
        then
            echo "ThreadCount parameter in broker_test_plan.properties cannot be zero."
            exit
    elif  [ ${testplan_user_inputs["NumberOfMessages"]} == '0' ]
        then
            echo "NumberOfMessages parameter in broker_test_plan.properties cannot be zero."
            exit
    fi

case $queue_name in
    micro_benchmark_queue1)
        if [ -e resources/jndi_queue.properties ];
            then
                 rm resources/jndi_queue.properties
            fi
            printf "connectionfactory.QueueConnectionFactory=amqp://admin:admin@clientID/carbon?brokerlist='tcp://"$host_url":${testplan_user_inputs["AMQPListenerPort"]}'\nqueue.QueueName=micro_benchmark_queue1" >> resources/jndi_queue.properties
        ;;
    micro_benchmark_queue2)
        if [ -e resources/jndi_topic.properties ];
            then
                 rm resources/jndi_topic.properties
            fi
            printf "connectionfactory.TopicConnectionFactory=amqp://admin:admin@clientID/carbon?brokerlist='tcp://"$host_url":${testplan_user_inputs["AMQPListenerPort"]}'\ntopic.TopicName=micro_benchmark_queue2" >> resources/jndi_topic.properties
        ;;
esac

# create target folder if not exist
if [ ! -e target/"$base_file_location" ];
    then
        mkdir -p target/"$base_file_location"
    fi

# create queues and bindings to execute tests
queue_available_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" "$host_url":${testplan_user_inputs["BrokerPort"]}/broker/v1.0/queues/"$queue_name")
# if queue is not available create queue
if [ "$queue_available_response" == 404 ]
    then
        json_payload='{"name":"'"$queue_name"'", "durable":"true","autoDelete":"true"}'
        queue_create_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" -d "$json_payload" -H "Content-Type: application/json" -X POST "$host_url":${testplan_user_inputs["BrokerPort"]}/broker/v1.0/queues)
        if [ "$queue_create_response" == "201" ]
            then
                echo $queue_name created sucessfully.
                json_payload='{"bindingPattern":"'"$queue_name"'","exchangeName":"'"$exchange_name"'","filterExpression":""}'
                queue_bind_response=$(curl -k -u admin:admin -o /dev/null -s -w "%{http_code}\n" -d "$json_payload"  -H "Content-Type: application/json" -X POST "$host_url":${testplan_user_inputs["BrokerPort"]}/broker/v1.0/queues/"$queue_name"/bindings)
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
echo Host is set to "$host_url"
echo Broker port is set to ${testplan_user_inputs["BrokerPort"]} and AMQP listener port is set to ${testplan_user_inputs["AMQPListenerPort"]}
echo Jmeter home is set to - ${testplan_user_inputs["JmeterHome"]}

# create folder to store report files
folder_name=$(date '+%d-%m-%Y-%H-%M-%S')
mkdir -p target/"$base_file_location"/"$folder_name"

# calculate loop count
loop_count=$(echo "${testplan_user_inputs["NumberOfMessages"]}/${testplan_user_inputs["ThreadCount"]}" | bc)

# execute jmeter command
if [ ${testplan_user_inputs["JmeterHome"]} != '' ]
        then
            # if user specified jmeter home
            ${testplan_user_inputs["JmeterHome"]}/bin/jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DTHREAD_COUNT=${testplan_user_inputs["ThreadCount"]} -DLOOP_COUNT=$loop_count -l target/"$base_file_location"/"$folder_name"/log/test_results.jtl -e -o target/"$base_file_location"/"$folder_name"/report/
        else
            jmeter_console_out="$(command -v jmeter)"
            if [${jmeter_console_out} == '']
                then
                    echo Please set jmeter home or include JmeterHome property in the properties file
                    exit
                else
                    echo "$jmeter_console_out"
                    # if JmeterHome is already configured
                    jmeter -n -t "$jmx_file_location" -DJNDI_URL="$jndi_file_location" -DCONNECTION_FACTORY="$connection_factory_name" -DDESTINATION="$destination" -DTHREAD_COUNT=${testplan_user_inputs["ThreadCount"]} -DLOOP_COUNT=$loop_count -DTHROUGHPUT=${testplan_user_inputs["Throughput"]} -l target/"$base_file_location"/"$folder_name"/log/test_results.jtl -e -o target/"$base_file_location"/"$folder_name"/report/
            fi
        fi

# log report location
echo A report of the test results is generated at target/"$base_file_location"/"$folder_name"/report
