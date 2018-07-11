# Performance testing tool for ballerina-message-broker 

This tool allows ballerina message broker developers to get an idea about the performance of the message broker after a modification is done in the code. Following criteria is going to be used in evaluating the performance.

- Connection Load  - The number of message producers, or message consumers, or the number of concurrent connections a system can support.
- Message throughput - The number of messages or message bytes that can be pumped through a messaging system per second.
- Latency - The time it takes a particular message to be delivered from message producer to message consumer.

## How to use 

1.Copy following jars to JMETER_HOME/lib.

- andes-client-0.13.wso2v8.jar
- geronimo-jms_1.1_spec-1.1.0.wso2v1.jar
- slf4j-1.5.10.wso2v1.jar

2.Create jndi.properties file

```properties
# register some connection factories
# connectionfactory.[jndiname] = [ConnectionURL]
connectionfactory.QueueConnectionFactory = amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:<port>'
 
# register some queues in JNDI using the form
# queue.[jndiName] = [physicalName]
queue.QueueName = <queue_name>
```

3.Download the jmx file in test_plan folder to your local machine

4.Create a properties files including following details.
```properties
jmeter_home = <jmeter_home>
jmx_file_location = <jmx_file_location>
jndi_file_location = <jndi_file_location>
thread_count = <number_of_threads>
ramp_time = <ramp_time>
message_size = <size_of_the_message>
duration_of_the_test = <test_time_in_seconds>
throughput= <throughput_need_to_be_maintained>
```
4.Run ```./broker_performance_test.sh <location_of_properties_file>```

Upon completion of the test,you'll be directed to a web interface which contains the summary of the results obtained by the test.

![statistics.png](scripts/images/Statistics.png)
![throughput.png](scripts/images/Throughput.png)

## Special Notes

- Use 1,10,100,1000 as inputs to the message_size paramter in properties file.
    - 1 = 1 KB message
    - 10 = 10 KB message
    - 100 = 100KB message
    - 1000 = 1MB message 
  
- Following values are used as default values for some of the above mentioned parameters.
    - jmx_file_location = test_plan/ballerina_message_broker_performance_test.jmx
    - jndi_file_location = resources/jndi.properties
    - thread_count = 1
    - ramp_time = 0
    - message_size = 10
    - duration_of_the_test = 900 (900 seconds = 15 minutes)
    - throughput = 5000 (5000 messages/seconds)
    



