# Configuring High Availability

The Message Broker supports high availability (HA), allowing configuration of failover nodes.

The configuration for HA - whether or not enabled and the strategy to be used, needs to be included in the broker.yaml 
file, where strategy is any implementation of the HaStrategy interface. Any custom parameters required can be specified 
as options.

The default implementation [RdbmsHaStrategy](../../modules/broker-coordination/src/main/java/org/wso2/broker/coordination/rdbms/RdbmsHaStrategy.java), based on an RDBMS based leader election mechanism, will be used if HA is 
enabled but no strategy is specified. 

The default values used in the RDBMS based leader election approach, upon which the RDBMS based HA strategy is based, 
could be overridden by specifying the values as options in the broker.yaml file.

```yaml
failover:
 enabled: true
 strategy: org.wso2.broker.coordination.rdbms.RdbmsHaStrategy
 options:
  heartbeatInterval: 5000
  coordinatorEntryCreationWaitTime: 3000
```

The sample [broker.yaml](ha-enabled-sample-broker.yaml) configuration file includes the required configuration to enable
HA and use the default RDBMS coordinator election based HA strategy.