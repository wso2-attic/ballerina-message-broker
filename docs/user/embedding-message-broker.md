# Embedding Message Broker

The ballerina message broker could embed into your program and manage life cycle. There are certain APIs expose to 
configure broker as it created. 

1. Add maven dependency into your project.

    ```xml
            <dependency>
                <groupId>io.ballerina.messaging</groupId>
                <artifactId>broker-launcher</artifactId>
                <version>${version}</version>
            </dependency>
    ```

2. Create an instance of **EmbeddedBroker** class as shown in below example and simply start.

    ```java
       EmbeddedBroker broker = new EmbeddedBroker();
       broker.start();
    ``` 
    
3. Call stop method to shutdown the broker.

    ```java
       broker.stop();   
    ``` 
    
4. Above code snippet will start a broker and listen to default AMQP port **5672**. A user can build 
   **EmbeddedBrokerConfiguration** object and set broker port. 

     ```java
        EmbeddedBrokerConfiguration configuration = new EmbeddedBrokerConfiguration();
        configuration.setPort("5675"); 
     ``` 
     
5. To enable SSL transport, provide necessary values for following setters in **EmbeddedBrokerConfiguration**. The 
   broker will listen on the default SSL port **8672**.

     ```java
        configuration.setSslPort("8675")
                     .setSslResources("/home/user/security/keystore.jks", "password",
                                "/home/user/security/client-truststore.jks", "password");
     ``` 

6. The embedded broker starts without authentication and authorization. A user can provide necessary implementations of 
   authentication and authorization to enable the functionality.

    ```java
       System.setProperty(BrokerAuthConstants.SYSTEM_PARAM_USERS_CONFIG, "/home/user/data/users.yaml");
       HashMap<String, Object> properties = new HashMap<String, Object>();
               properties.put(BrokerAuthConstants.CONFIG_PROPERTY_JAAS_LOGIN_MODULE,
                       UserStoreLoginModule.class.getCanonicalName());
               Authenticator authenticator = new JaasAuthenticator();   
       configuration.setAuthentication(authenticator).setAuthenticatorProperties(properties);
    ```
    
The above code snippet uses default Authenticator and UserStore implementation. A user could write his own 
implementation and plug when starting the broker. The authorization functionality can be enabled same way.

Please find the complete sample.

```java
package org.sample.messaging;

import io.ballerina.messaging.broker.EmbeddedBroker;
import io.ballerina.messaging.broker.EmbeddedBrokerConfiguration;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator;
import io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule;

import java.util.HashMap;

public class Test {

    public static void main(String[] args) throws Exception {

        System.setProperty(BrokerAuthConstants.SYSTEM_PARAM_USERS_CONFIG, "/home/user/data/users.yaml");
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(BrokerAuthConstants.CONFIG_PROPERTY_JAAS_LOGIN_MODULE,
                UserStoreLoginModule.class.getCanonicalName());
        Authenticator authenticator = new JaasAuthenticator();
        EmbeddedBrokerConfiguration configuration = new EmbeddedBrokerConfiguration();
        configuration.setPort("5675").setSslPort("8675")
                .setSslResources("/home/user/security/keystore.jks", "password",
                        "/home/user/security/client-truststore.jks", "password")
                .setAuthentication(authenticator).setAuthenticatorProperties(properties);

        EmbeddedBroker broker = new EmbeddedBroker(configuration);

        broker.start();
    }
}
```