# Performance testing tool for ballerina-message-broker 

This tool allows ballerina message broker developers to get an idea about the performance of the message broker after a modification is done in the code.Following criteria is going to be used in evaluating the performance.

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
3.Create a testplan.properties files including following results.
```properties
jndi_file= <jndi_file_location>
loop_count = <number_of_loops>
thread_count = <number_of_threads>
ramp_time = <ramp_time>
jmeter_home = <jmeter_home>/jmeter

```
4.Run ```mvn clean install``` on the project and obtain the ```performance-testing-tool-1.0-SNAPSHOT-jar-with-dependencies.jar```.<br><br>
5.Execute the jar using ```java -jar performance-testing-tool-1.0-SNAPSHOT-jar-with-dependencies.jar -p <testplan.properties file location>```