# Changing the Default Authentication

WSO2 Message Broker provides authenticator extension to authenticate username and password. Default 
authenticator uses JAAS (Java Authentication and Authorization Service) apis to authenticate client connections. JAAS
 Authenticator will use JAAS login modules.

    authenticator:
        # Authenticator implemetation
        className: io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator
        # Optional properties
        properties:
         loginModule: io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule

## Change default authentication

Default authentication can be .

1) Write custom JAAS Login module
2) Write custom authenticator.

### 1) Write custom JAAS login module

Custom jaas module is developed by implementing javax.security.auth.spi.LoginModule interface.
Custom authentication should be implemented in login method and username and password can be retrieved by using callback
handler. Here is sample code snippet for login method.

    @override
    public boolean login() throws LoginException {
        NameCallback usernameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = { usernameCallback, passwordCallback };
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new BrokerLoginException("Callback type does not support. ", e);
        } catch (IOException e) {
            throw new BrokerLoginException("Error occurred while handling authentication data. ", e);
        }
        username = usernameCallback.getName();
        password = passwordCallback.getPassword();
        success = validateUserPassword(username, password);
        return success;
        }

#### Configure custom login module

1. Copy the custom login module build jar to <BROKER_HOME>/lib directory.
2. Change the default login module. Default login module can be changed in two ways.

##### option 1

1. Edit the broker.yaml (located at <BROKER_HOME>/conf/broker.yaml) pointing to the custom login module as follows.
Custom login module can be set using loginModule property inside authenticator. The options property is used to pass any
properties to login module as a map which is optional.
 
    ```
     authenticator:
         # Authenticator implemetation
         className: io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator
         # Optional properties
         properties:
          loginModule: <custom login module>
      ...
   ```

##### option 2

1. Create custom jaas.conf file as follows.

    ```
    BrokerSecurityConfig {
        <custom login module class path> required;
    };
    ```

2. Set the jaas config file path with java system property with name java.security.auth.login.config. As an example
property can be added to broker.sh (located at <BROKER_HOME>/bin/broker.sh) .

    -Djava.security.auth.login.config="<Path to file>/jaas.conf" \

### 2) Write custom authenticator

Developers can implement custom authenticator and plugin using broker Configuration. This can be done by implementing
 io.ballerina.messaging.broker.auth.authentication.Authenticator interface. Here is sample code snippet.
 
     
        @Override
        public void initialize(StartupContext startupContext, Map<String, Object> properties) throws Exception {
            // Initilization logic required for custom authenticator. Properties can be used to retrive any external 
            values which can be configured in broker.yaml( server url, jass login module)
        }
    
        @Override
        public AuthResult authenticate(String username, char[] password) {
            // Add custom authentication login here. 
            return new AuthResult(true, username);
        }
        
Custom authenticator can be configured in the broker.yaml as follows.
 
     ```
      authenticator:
           # Authenticator implemetation
           className: < custom authenticator class name >
           # Optional properties
           properties: 
     '''