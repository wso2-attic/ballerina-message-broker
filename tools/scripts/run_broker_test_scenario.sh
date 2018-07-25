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

destination=""
is_given_destination=false

# get inputs from the user -d topic/queue
while getopts "hp:d:t:h:v" OPTION
do
     case $OPTION in
         d)
            case $OPTARG in
                queue)
                     is_given_destination=true
                     destination="queue"
                ;;
                topic)
                     is_given_destination=true
                     destination="topic"
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
            help_text="Welcome to ballerina message broker micro-benchmark tool\n\nUsage:\n\t./run_broker_test_scenario.sh [command].\n\nCommands\n\t-h  ask for help\n\t-d  set jms destination type queue/topic\n"
            printf "$help_text"
            exit
            ;;
         ?)
            printf "Invalid command.Run ./run_broker_test_scenario.sh -h for usage.\n"
            exit
            ;;
     esac
done


if [ $is_given_destination == false ];
    then
        printf 'A JMS destination should be provided.\nRun ./run_broker_test_scenario.sh -h for usage.\n'
        exit
    fi

time=$(date '+%d-%m-%Y-%H-%M-%S')
broker_consumer_log=broker_consumer_"$time".log
broker_publisher_log=broker_publisher_"$time".log

# create log folder if not exist
if [ ! -e logs ];
    then
        mkdir -p logs
    fi

# execute publisher and consumer at the same time
echo "Starting message consumer."
printf $(run_broker_consumer.sh -s "test_scenario/$time" -d "$destination" &) >> logs/"$broker_consumer_log"
sleep 4
echo "Starting message pubisher"
printf $(run_broker_publisher.sh -s "test_scenario/$time" -d "$destination" & >> logs/"$broker_publisher_log")
wait
