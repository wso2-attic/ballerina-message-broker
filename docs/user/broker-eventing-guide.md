# Event Triggers

Parts of a message broker such as consumers, connections, queues, exchanges etc generally generate events. Event 
notifications are useful when monitoring the broker. Ballerina message broker provides the functionality to consume 
these events and re-publish them to a topic exchange.  

Ballerina Message broker currently provides notifications for following events.

- Queue Created Event
- Queue Deleted Event
- Queue Message Limit Reached Event
- Exchange Created
- Exchange Deleted
- Binding Created
- Binding Deleted
- Consumer Added
- Consumer Removed

## Configuring Broker For Events

Event notifications can be enabled using ballerina.broker.events: namespace in broker.yaml file. There is a module that 
publish the events triggered to the needed medium. You should include the needed Event Publisher in publisher class as 
below (ex: Broker Publisher, Stream Processor Publisher).

Available default implementations are,
- Broker Event Publisher - io.ballerina.messaging.broker.core.eventpublisher.BrokerCoreEventPublisher

### Enabling Event Publishing Capability

Set the enabled attribute true or false according to your preference to enable events.
```yaml
# Broker event related configurations.
ballerina.broker.events:
  # Enable Events
  enabled: true
  # Publisher Implementation
  publisherClass: io.ballerina.messaging.broker.core.eventpublisher.BrokerCoreEventPublisher
```
### Enabling Event Listening Capability

```yaml
# Message limits for to trigger notifications in queues
 eventConfig:
  queueLimitEvents:
    enabled:  true
    commonLimits: [10, 6]
    queues:
      - name: test
        limits: [2,6]
      - name: test2
        limits: [2,8]
  # Enable Exchange Admin Events
  exchangeAdminEventsEnabled: true
  # Enable Queue Admin Events
  queueAdminEventsEnabled: true
  # Enable Queue External Events
  queueExternalEventsEnabled: true
```

- Queue Limit Events - `queue.publishLimitReached`, `queue.deliverLimitReached`
- Queue Admin Events - `queue.added, queue.removed`
- Queue External Events - `consumer.added, consumer.removed`, `binding.added`, `binding.removed`
- Exchange Admin Events - `exchange.added`, `exchange.removed`
## Obtaining Event notifications

To listen to specific event notifications you should declare an AMQP consumer with a queue which listen to routing keys 
of the event notifications as below. Note that queues should be binded to the topic exchange named “x-event”. Below 
shows events and their related routing key.

### Queue Related Events

- Queue Created Event -	
`queue.added`
- Queue Deleted Event -	
`queue.deleted`
- Queue Limit Reached Events -	
`queue.limitReached.*`                                                  
- Queue Limit Reached Events for a specific queue “QueueName” - 	
`queue.limiReached.”QueueName”.*`  
- Queue Limit Reached Event for queue “QueueName” for limit “limitValue” - 
`queue.limitReached.”QueueName”.”limitValue”`
- Queue Events -	
`queue.*`

### Exchange Related Events

- Exchange Created Event -	
`exchange.created`
- Exchange Deleted Event -	
`exchange,deleted`
- Exchange Events -	
`exchange.*`

### Binding Related Events

- Binding Created Event -	
`binding.created`
- Binding Deleted Event -	
`binding,deleted`
- Binding Events -	
`binding.*`

### Consumer Related Events

- Consumer Added Event - 
`consumer.added`
- Consumer Added to queue “QueueName” -	
`consumer.added.”QueueName"`
- Consumer Removed Event -	
`consumer.removed`
- Consumer Removed from queue “QueueName” -	
`consumer.removed.”QueueName"`
- Consumer Events -	
`consumer.*`

Sample RabbitMQ consumer:

```java
public class EventPublish {

    public static void main ( String[] args ) throws IOException, TimeoutException {

        String QUEUE_NAME = "testQueueCreatedEvent";
        Connection connection = ClientHelper.getAmqpConnection( "admin", "admin", "localhost", "5672" );

        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, "amq.event", "queue.created");
        channel.queueDeclare("testQueue", false, false, false,null);
        System.out.println( " [*] Waiting for messages. To exit press CTRL+C" );

        DefaultConsumer consumer = new DefaultConsumer( channel ) {
            @Override
            public void handleDelivery ( String consumerTag, Envelope envelope,
                                         AMQP.BasicProperties properties, byte[] body )
                    throws IOException {
                String message = new String( body, StandardCharsets.UTF_8 );
                System.out.println( " [x] Received '" + message + "'" );
                Map<String, Object> headers = properties.getHeaders();
                for(Map.Entry<String, Object> header: headers.entrySet()) {
                    System.out.println(header.getKey());
                    System.out.println(header.getValue());
                }
            }
        };
        channel.basicConsume( QUEUE_NAME, false, consumer);
    }

}
```

## Specifying Message Limits

Message Limits can be specified for queues to trigger queue limit reached notifications. It can be done under 
ballerina.broker.core: namespace. Specify the queue and the needed limits.

```yaml
 # Message limits for to trigger notifications in queues
  eventConfig:
   queueLimitEvents:
     enabled:  true
     commonLimits: [10, 6]
     queues:
       - name: test
         limits: [2,6]
       - name: test2
         limits: [2,8]
```
### Specifying message limits when declaring an AMQP consumer

Furthermore, You can use arguments in QueueDeclare method to add event limits to the queue. You can add a field in arguments with the 
key "x-message-limits" and the value string containing the intended limits.

```
    Map<String, Object> arguments =  new HashMap<>();
    arguments.put("x-queue-limits", "5,7");
    channel.queueDeclare ("queueName", false, false, false, arguments);
``` 