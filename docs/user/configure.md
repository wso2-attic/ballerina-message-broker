# Configuring Message Broker

All the configuration files for the broker can be located under ${message.broker.home}/conf. Following is a summary 
of the all available configuration options.

## Main configuration file (broker.yaml)

The main configuration file of the broker can be located at ${message.broker.home}/conf/broker.yaml. The default 
values are sufficient to run the message broker in some environments (e.g. development, QA). This guide can be use to
 assist you in configuring the broker for other use cases.

### Broker common configurations

These configurations are defined under the namespace `ballerina.broker`.

| Config                      | Default Value                          | Description                                   |
|-----------------------------| ---------------------------------------|-----------------------------------------------|
| enableInMemoryMode          | false                                  | When the in-memory mode is enabled all message and context data including durable entities are held in memory. lease note that this can increase memory usage of the broker. Therefore it is advised to allocate more memory in the JVM which run the broker. | 
| datasource:url              | jdbc:h2:./database/MB_DB               | Database URL.                                 |
| database:user               | ballerina                              | Database username                             |
| database:password           | ballerina                              | Database password.                            |

### Broker-core configurations

These configurations are defined under the namespace `ballerina.broker.core`. 

| Config                      | Default Value                          | Description                                   |
|-----------------------------| ---------------------------------------|-----------------------------------------------|
| nonDurableQueueMaxDepth     | 10000                                  | Maximum number of messages kept in a non-durable queue. Increasing this number can increase the memory consumption. | 
| maxPersistedChunkSize     | 65500                                  | Maximum size of a chunk that is persisted. We will have to change this value depending on the underline database used. We have used the  frame size as the limit. |
| durableQueueInMemoryCacheLimit | 10000                                  | Maximum number of messages cached in-memory for faster delivery. Increasing this number can result in better throughput while increasing the memory consumption. |
| deliveryTask:workerCount    | 5                                      | Number of concurrent workers used to process the delivery tasks. |
| deliveryTask:idleTaskDelay  | 50                                     | The time that the delivery task will wait when the queue is empty or no consumers are available for message delivery in milliseconds.  |
| deliveryTask:deliveryBatchSize | 1000                                | Messages are delivered to consumers in batches by the delivery task. Following configuration changes the default message delivery batch size.
| authenticator:loginModule   | io.ballerina.messaging.broker.core .security.authentication.jaas.BrokerLoginModule | JAAS login module used to authenticate users. |

### AMQP transport configurations

These configurations are defined under the namespace `ballerina.broker.transport.amqp`. 

| Config                      | Default Value                                | Description                                                                                                  |
|-----------------------------|----------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| hostName                    | localhost                                    | Hostname configuration used in creating the server socket                                                    |
| maxRedeliveryCount          | 5                                            | Maximum number of redeliveries before publishing a message to the DLX (dead letter exchange).                |
| channelFlow:lowLimit        | 100                                          | The low limit used to enable channel flow when it is disabled. Value corresponds to the number of messages.  |
| channelFlow:highLimit       | 1000                                         | The high limit used to disable channel flow when it is enabled. Value corresponds to the number of messages. |
| plain:port                  | 5672                                         | Port used for the nonsecured transport.                                                                      |
| ssl:enabled                 | true                                         | Indicate if secured transport is enabled. Accepted values are 'true' or 'false'.                             |
| ssl:port                    | 8672                                         | Port used to bind the secured transport.                                                                     |
| ssl:protocol                | TLS                                          | Secureprotocol used to encrypt data.                                                                         |
| ssl:keyStore:type           | JKS                                          | Type of the keystore file.                                                                                   |
| ssl:keyStore:location       | resources/security/keystore.jks              | Location of the keystore file. The path can be relative to broker home or absolute path.                     |
| ssl:keyStore:password       | ballerina                                    | Keystore password.                                                                                           |
| ssl:keyStore:certType       | SunX509                                      | Cert type used in the keystore file.                                                                         |
| ssl:trustStore:type         | JKS                                          | Type of truststore file.                                                                                     |
| ssl:trustStore:location     | resources/security/client-truststore.jks     | Location of the keystore file. The path can be relative to broker home or absolute path.                     |
| ssl:trustStore:password     | ballerina                                    | Truststore password.                                                                                         |
| ssl:trustStore:certType     | SunX509                                      | Cert type used in the truststore file.                                                                       |

### Fail-over configurations

These configurations are defined under the namespace `ballerina.broker.failover`. 

| Config                                   | Default Value                 | Description                                                                              |
|------------------------------------------|-------------------------------|------------------------------------------------------------------------------------------|
| enabled                                  | false                         | Whether or not fail-over is enabled                                                      |
| strategy                                 | io.ballerina.messaging.broker.coordination. rdbms.RdbmsHaStrategy | The high availability strategy to use to provide fail-over functionality                 |
| options:heartbeatInterval                | 5000                          | The interval, in milliseconds, at which nodes in the fail-over group update the database (for RdbmsHaStrategy) |
| options:coordinatorEntryCreationWaitTime | 3000                          | The interval, in milliseconds, to wait prior to confirming election as coordinator (for RdbmsHaStrategy) |


### Auth configurations

These configurations are defined under the namespace `ballerina.broker.auth`. 

| Config                                   | Default Value                 | Description                                                                              |
|------------------------------------------|-------------------------------|------------------------------------------------------------------------------------------|
| authentication:enabled                   | true                          | Whether or not authentication is enabled |
| authentication:authenticator:className   | io.ballerina.messaging.broker.auth. authentication.authenticator. JaasAuthenticator | The authenticator strategy to use to provide authentication functionality                 |
| authentication:authenticator:properties  |   loginModule: io.ballerina.messaging.broker.auth. authentication.jaas.UserStoreLoginModule  | Set of properties required for authenticator. ex. serverUrl, Jaas login module . |
| authorization:enabled                    | false                         | Set this value tro true to enable authorization |
| authorization:userstore:className        | io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore | full qualified class name of the User store implementation |
| authorization:userstore:properties       |   - | Optional parameters given to the user store implementation |
| authorization:mandatoryAccessController:className       | io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler | full qualified name of the MAC implementing class |
| authorization:mandatoryAccessController:properties       |   - | Optional parameters given to the MAC implementation |
| authorization:discretionaryAccessController:className       | io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler | full qualified name of the DAC implementing class |
| authorization:discretionaryAccessController:properties       |   - | Optional parameters given to the DAC implementation |
| cache:timeout                            | 15 | Auth cache timeout in Minutes |
| cache:size                               | 5000 | Maximum Cache size to hold user id |

## Admin service related configurations (admin-service-transports.yaml)

### Admin service listener configurations

These configurations are defined under the namespace `listenerConfigurations`. We can define a list of listener 
configurations under this section. 

| Config         | Default Value | Description                                           |
|----------------|---------------|-------------------------------------------------------|
| id | default     | listener identifier used internally in MSF4j. |
| host     | 0.0.0.0          | Hostname used for the admin service.           |
| port     | 8080          | Port used for the admin service.           |
| scheme     | http          | Scheme used for the admin service. Can be http or https.          |
| keyStoreFile     | -          | Keystore file used for the admin service when https is used.           |
| keyStorePassword     | -          | Password of the used keystore file.           |
| certPass     | -          | Certificate password used in the keystore file.           |
