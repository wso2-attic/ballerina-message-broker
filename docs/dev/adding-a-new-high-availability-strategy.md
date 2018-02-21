# Adding a new High Availability Strategy

Introducing a new High Availability (HA) strategy to provide HA support with the Message Broker can be done by adopting 
either of the following approaches:
 * implementing the [io.ballerina.messaging.broker.coordination.HaStrategy](../../modules/broker-coordination/src/main/java/io/ballerina/messaging/broker/coordination/HaStrategy.java) interface 
 * extending the [io.ballerina.broker.coordination.AbstractHaStrategy](../../modules/broker-coordination/src/main/java/io/ballerina/messaging/broker/coordination/AbstractHaStrategy.java) class


### Requirements

It is required that the new HA strategy implementations call the `activate()` and `deactivate()` methods of 
the HA Listeners registered to receive notifications on node state changes, when the node state changes from passive to 
active and active to passive, respectively. 

HA strategies introduced by extending the AbstractHaStrategy class can notify the listeners registered by calling the 
`notifyBecameActiveNode()` and `notifyBecamePassiveNode()` methods in AbstractHaStrategy.

The default HA strategy, [io.ballerina.messaging.broker.coordination.RdbmsHaStrategy](../../modules/broker-coordination/src/main/java/io/ballerina/messaging/broker/coordination/rdbms/RdbmsHaStrategy.java), 
extends the AbstractHaStrategy class to provide HA support.

### Using the new HA Strategy

With the custom HA strategy implemented, the following steps should be followed to utilize the new HA strategy.

1. Copy the JAR file to the <MB_HOME>/lib directory
2. Mark failover as enabled and specify the canonical name of the class implementing the HA strategy as the strategy in 
the `broker.yaml` configuration file along with the any custom parameters as options:
```yaml
 enabled: true
 strategy: com.custom.failover.CustomHaStrategy
 options:
  heartbeatInterval: 3000
```