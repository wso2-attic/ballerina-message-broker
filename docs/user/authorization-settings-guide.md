# Changing the Default Authorization

Mandatory Access Control (MAC) and Discretionary Access Control (DAC) is used for the authorization in the Message
Broker and it is disabled by default. A user could enable it and restrict the resource access. After enabling, 
authorization happen at AMQP transport and REST APIs. A user should have a relevant scope or resource action before 
connecting with the message broker. A user does not have a correct scope or resource action ends up with permission 
denied exception.

  
     # Broker authorization related configurations.
     authorization:
      # Enable the authorization
      enabled: false
      # User store used tot lookup user groups that a user belongs.
      userStore:
       # User store implementing class
       className: io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore
      # Configurations related to MAC handler.
      mandatoryAccessController:
       # MAC handler implementation.
       className: io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler
      # Configurations related to DAC handler.
      discretionaryAccessController:
       className: io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler

## User store

Implementation of the [UserStore](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/modules/broker-auth/src/main/java/io/ballerina/messaging/broker/auth/authorization/UserStore.java) 
interface that provides user details to map with user groups.

1) io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore
    - Default user store implementation of the message broker retrieve users from the local file.

## Mandatory Access Controller

Implementation of the [MandatoryAccessController](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/modules/broker-auth/src/main/java/io/ballerina/messaging/broker/auth/authorization/MandatoryAccessController.java) 
interface that authorizes user with given scope key. User group could assign any of the following scope keys.

- exchanges:create : allow user group to create exchanges
- exchanges:delete : allow user group to delete exchanges
- exchanges:get : allow user group to get exchanges
- exchanges:publish : allow user group to publish to exchanges
- queues:create : allow user group to create queues
- queues:delete : allow user group to delete queues
- queues:get : allow user group to get queues
- queues:consume : allow user group consume queues
- resources:grant : allow user group to grant resources

1) io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler
    - Default implementation of the MAC
    
## Discretionary Access Controller

Implementation of the [DiscretionaryAccessController](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/modules/broker-auth/src/main/java/io/ballerina/messaging/broker/auth/authorization/DiscretionaryAccessController.java) interface that authorizes user with dynamic resources.
Dynamic resources are created internally and registered against following actions.

- update : update queue or exchange 
- delete : delete queue or exchange
- get : get queue or exchange
- consume : consume from queue
- publish : publish to exchange
- bind : bind to exchange
- unbind : unbind from exchange
- grantPermission : grant resource to user

1) io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler
    - Default implementation of the DAC

## Custom authorizer

A user can customize authorization by implementing any of the above interfaces. For an example, user store can implement 
with a different mechanism and plug into the authorization to map with access control. Please refer to the [Developer Guide](../dev/security-architecture.md) for more implementation details.

### Configure custom authorizer

1. Copy the custom authorizer jar to <BROKER_HOME>/lib directory.
2. Change the default authorizer in the <BROKER_HOME>/conf/broker.yaml

```
     # Broker authorization related configurations.
     authorization:
      # Enable the authorization
      enabled: false
      # User store used tot lookup user groups that a user belongs.
      userStore:
       # User store implementing class
       className: <fully qualified class name of the custom user store>
      # Configurations related to MAC handler.
      mandatoryAccessController:
       # MAC handler implementation.
       className: <fully qualified class name of the custom MAC>
      # Configurations related to DAC handler.
      discretionaryAccessController:
       className: <fully qualified class name of the custom DAC>
```
