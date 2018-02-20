# Changing the Default Authentication

WSO2 Message Broker uses JAAS (Java Authentication and Authorization Service) apis to authenticate username and
password of client connections. Authentication will be handled by the JAAS login modules.


    authenticator:
      loginModule: io.ballerina.messaging.broker.auth.authentication.jaas.BrokerLoginModule

## Change default Login module

### Write custom JAAS Login module

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

### Configure custom login module

1. Copy the custom login module build jar to <BROKER_HOME>/lib directory.
2. Change the default login module. Default login module can be changed in two ways.

#### option 1

1. Edit the broker.yaml (located at <BROKER_HOME>/conf/broker.yaml) pointing to the custom login module as follows.
Custom login module can be set using loginModule property inside authenticator. The options property is used to pass any
properties to login module as a map which is optional.
 
    ```
     authenticator:
      loginModule: <custom login module class path>
      options:
        key: value
      ...
   ```

#### option 2

1. Create custom jaas.conf file as follows.

    ```
    BrokerSecurityConfig {
        <custom login module class path> required;
    };
    ```

2. Set the jaas config file path with java system property with name java.security.auth.login.config. As an example
property can be added to broker.sh (located at <BROKER_HOME>/bin/broker.sh) .


    ```
    -Djava.security.auth.login.config="<Path to file>/jaas.conf" \
   ```

