# Microbenchmark for Ballerina Message Broker

This tool allows ballerina message broker developers to get an idea about the performance of the message broker after a modification is done in the code. Following criteria is used in evaluating the performance.

- Connection Load  - The number of message producers , or the number of concurrent connections a system can support.
- Message throughput - The number of messages or message bytes that can be pumped through a messaging system per second.

The tool allows developers to publish/consume messages to/from queues and topics and test the performance and get a report of the result.

# How to use

## Prerequisite

1. Copy following jars to JMETER_HOME/lib.

    - andes-client-0.13.wso2v8.jar
    - geronimo-jms_1.1_spec-1.1.0.wso2v1.jar
    - slf4j-1.5.10.wso2v1.jar

### Publish messages to queues/topics

1. Include the following details in broker_test_plan.properties file which is located at resources/.
    ```properties
    
    JmeterHome = <jmeter_home>
    ThreadCount = <number_of_threads>
    MessageSize = <size_of_the_message>
    NumberOfMessages = <number_of_messages_need_to_be_published>
    Throughput= <throughput_need_to_be_maintained>
    
    ```
2. Include the following details in broker_test_infrastructure.properties file which is located at resources/.
    ```properties
    HostURL=<host_url>
    BrokerPort=<broker_port>
    AMQPListenerPort=<amqp_listener_port>
    ```

3. Run ```./run_broker_publisher.sh -i <location_of_infrastructure_properties_file> -t <location_of_testplan_properties_file> -d queue/topic```

- -i and -t are optional parameters.If not provided ```resources/broker_test_infrastructure.properties``` and ```resources/broker_test_plan.properties``` are set as default file locations

Upon completion of the test, you will be directed to a web interface which contains the summary of the results obtained by the test.

### Consume messages from queues/topics

1. Include the following details in broker_test_plan.properties file which is located at resources/.
```properties
JmeterHome = <jmeter_home>
ThreadCount = <number_of_threads>
NumberOfMessages = <number_of_messages_need_to_be_published>
```

2. Include the following details in broker_test_infrastructure.properties file which is located at resources/.
    ```properties
    HostURL=<host_url>
    BrokerPort=<broker_port>
    AMQPListenerPort=<amqp_listener_port>
    ```
Run ```./run_broker_consumer.sh -i <location_of_infrastructure_properties_file> -t <location_of_testplan_properties_file> -d queue/topic```

- -i and -t are optional parameters.If not provided ```resources/broker_test_infrastructure.properties``` and ```resources/broker_test_plan.properties``` are set as default file locations

Upon completion of the test, you will be directed to a web interface which contains the summary of the results obtained by the test.

### Test scenario - Publish messages to queues/topics and consume them

1.Run ```./run_broker_test_consumer.sh -d queue/topic```

## Special Notes

- Use 1KB , 10KB , 100KB , 1MB as inputs to the message_size paramter in properties file.
    - 1KB = 1 KB message
    - 10KB = 10 KB message
    - 100KB = 100KB message
    - 1MB = 1MB message 
  
- Following values are used as default values for some of the above mentioned parameters.
    - HostURL = https://localhost
    - BrokerPort = 9000
    - AMQPListenerPort = 5672
    - ThreadCount = 1
    - MessageSize = 10KB
    - NumberOfMessages = 1000000
    - Throughput = 5000 (5000 messages/seconds)
    
