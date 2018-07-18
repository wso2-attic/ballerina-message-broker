# Microbenchmark for Ballerina Message Broker

This tool allows ballerina message broker developers to get an idea about the performance of the message broker after a modification is done in the code. Following criteria is used in evaluating the performance.

- Connection Load  - The number of message producers , or the number of concurrent connections a system can support.
- Message throughput - The number of messages or message bytes that can be pumped through a messaging system per second.

The tool allows developers to publish/consume messages to/from queues and topics and test the performance and get a report of the result.

# How to use

## Prerequisite

1. Create two queues ```micro_benchmark_queue1``` and ```micro_benchmark_queue2```.The script uses these queues separately to study the performance of queue and topic operations.

2. Copy following jars to JMETER_HOME/lib.

    - andes-client-0.13.wso2v8.jar
    - geronimo-jms_1.1_spec-1.1.0.wso2v1.jar
    - slf4j-1.5.10.wso2v1.jar

### Publish messages to queues/topics

1. Include the following details in broker_performance_test_publisher.properties file which is located at resources/.
```properties
broker_url=<broker_url>
jmeter_home = <jmeter_home>
thread_count = <number_of_threads>
message_size = <size_of_the_message>
number_of_messages = <number_of_messages_need_to_be_published>
throughput= <throughput_need_to_be_maintained>
```
2.Run ```./broker_test_publisher.sh -p <location_of_properties_file> -d queue/topic```

- -p is an optional paramter.If not provided ```resources/broker_test_publisher.properties``` is set as the properties file location 

Upon completion of the test, you will be directed to a web interface which contains the summary of the results obtained by the test.

### Consume messages from queues/topics

1. Include the following details in broker_performance_test_consumer.properties file which is located at resources/.
```properties
jmeter_home = <jmeter_home>
thread_count = <number_of_threads>
number_of_messages = <number_of_messages_need_to_be_consumed>
```
2.Run ```./broker_test_consumer.sh -p <location_of_properties_file> -d queue/topic```

- -p is an optional paramter.If not provided ```resources/broker_test_consumer.properties``` is set as the properties file location 

Upon completion of the test, you will be directed to a web interface which contains the summary of the results obtained by the test.

### Test scenario - Publish messages to queues/topics and consume them

1.Include the following details in broker_performance_test_publisher.properties file which is located at resources/.
```properties
jmeter_home = <jmeter_home>
thread_count = <number_of_threads>
message_size = <size_of_the_message>
number_of_messages = <number_of_messages_need_to_be_published>
throughput= <throughput_need_to_be_maintained>
```

2.Include the following details in broker_performance_test_consumer.properties file which is located at resources/.
```properties
jmeter_home = <jmeter_home>
thread_count = <number_of_threads>
number_of_messages = <number_of_messages_need_to_be_published>
```
3.Run ```./broker_test_consumer.sh -p <location_of_properties_file> -d queue/topic```

## Special Notes

- Use 1KB , 10KB , 100KB , 1MB as inputs to the message_size paramter in properties file.
    - 1KB = 1 KB message
    - 10KB = 10 KB message
    - 100KB = 100KB message
    - 1MB = 1MB message 
  
- Following values are used as default values for some of the above mentioned parameters.
    - thread_count = 1
    - message_size = 10
    - number_of_messages = 1000000
    - throughput = 5000 (5000 messages/seconds)
