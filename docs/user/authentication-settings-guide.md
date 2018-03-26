# Changing the Default Authentication

Message Broker provides authenticator extension to authenticate by username and password. By default, authentication 
enabled and uses JAAS (Java Authentication and Authorization Service) APIs to authenticate client connections. 
JAAS Authenticator will use JAAS login modules.


     # Broker authentication related configurations.
     authentication:
      # Enable the authentication
      enabled: true
      authenticator:
        # Authenticator implementation
        className: io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator
        # Optional properties
        properties:
         loginModule: io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule


## Available authenticators

There are two authenticator implementations available out of the box.

1) io.ballerina.messaging.broker.auth.authentication.authenticator.DefaultAuthenticator
    - User credential not verified and optional properties not required.

2) io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator
    - Provide authentication using JaaS login module. 
    - The io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule implements 
    the javax.security.auth.spi.LoginModule and set as a property.


## Custom authenticator

A user can write custom authenticator by implementing the [Authenticator](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/modules/broker-auth/src/main/java/io/ballerina/messaging/broker/auth/authentication/Authenticator.java) interface.
Supporting classes pass to the Authenticator via properties. In the default implementation, JaasAuthenticator 
required JaaS login module and it has provided with UserStoreLoginModule. Please refer to the [developer guide](../dev/security-architecture.md) for more 
implementation details.


### Configure custom authenticator

1. Copy the custom authenticator jar to <BROKER_HOME>/lib directory.
2. Change the default authenticator in the <BROKER_HOME>/conf/broker.yaml

```
     # Broker authentication related configurations.
     authentication:
      # Enable the authentication
      enabled: true
      authenticator:
        # Authenticator implementation
        className: <fully qualified class name of the custom authenticator>
```

### Configure custom login module

##### option 1

1. Copy the custom login module jar to the <BROKER_HOME>/lib directory.
2. Change the default authenticator in the <BROKER_HOME>/conf/broker.yaml.

```
     # Broker authentication related configurations.
     authentication:
      # Enable the authentication
      enabled: true
      authenticator:
        # Authenticator implementation
        className: io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator
        # Optional properties
        properties:
         loginModule: <fully qualified class name of the custom login module>
```

##### option 2

1. Create custom jaas.conf file as follows.

        BrokerSecurityConfig {
            <custom login module class path> required;
        };

2. Set the jaas config file path with java system property with name java.security.auth.login.config. As an example
property can be added to broker.sh (located at <BROKER_HOME>/bin/broker.sh) .

        -Djava.security.auth.login.config="<Path to file>/jaas.conf" \
