# Configuring High Availability

The Message Broker supports high availability (HA), allowing configuration of failover nodes.

The configuration for HA - whether or not enabled and the strategy to be used, needs to be included in the broker.yaml 
file, where strategy is any implementation of the HaStrategy interface. Any custom parameters required can be specified 
as options.

The default implementation [RdbmsHaStrategy](../../modules/broker-coordination/src/main/java/io/ballerina/messaging/broker/coordination/rdbms/RdbmsHaStrategy.java),
based on an RDBMS based leader election mechanism, will be used if HA is enabled but no strategy is specified. 

The default values used in the RDBMS based leader election approach, upon which the RDBMS based HA strategy is based, 
could be overridden by specifying the values as options in the broker.yaml file.

```yaml
failover:
 enabled: true
 strategy: io.ballerina.messaging.broker.coordination.rdbms.RdbmsHaStrategy
 options:
  heartbeatInterval: 5000
  coordinatorEntryCreationWaitTime: 3000
```

The sample [broker.yaml](ha-enabled-sample-broker.yaml) configuration file includes the required configuration to enable
HA and use the default RDBMS coordinator election based HA strategy.

## The default fail-over strategy

The default implementation providing fail-over functionality, the [RdbmsHaStrategy](../../modules/broker-coordination/src/main/java/io/ballerina/messaging/broker/coordination/rdbms/RdbmsHaStrategy.java), 
is based on an RDBMS based leader election mechanism.

All nodes in the fail-over group start up in passive mode, and at any given instance only the node elected as the leader
 will be considered the active node. Any and all other nodes in the fail-over group will be considered passive nodes.
 
If the current leader node loses leader/coordinator state (going in to election state), this node which would have been 
marked as the active node will be marked as passive, and the new leader/coordinator node, once elected, will be marked 
as the active node.
 


