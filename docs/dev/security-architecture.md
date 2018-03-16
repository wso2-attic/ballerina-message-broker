# Message Broker Security Architecture

## Authentication

AMQP specification defined the authentication mechanism and security content data is based on Simple Authentication
and Security Layer(SASL) framework. Following figure shows the authentication security architecture.


![Module Relation](../images/authentication-architecture.png)

Once client request  a connection, server will send the supported SASL mechanisms to client. Message broker supports
PLAIN text SASL mechanism.

After that client will send the selected mechanism + auth response data. Server will create SASL server based on the
client mechanism and then server will evaluate the client authentication data . Then SASL server will call the 
registered authenticator for broker. Broker will use Java Authentication and Authorization Service 
(JAAS) as default authentication implementation. Once authenticator authenticate the plain text username and 
password, it will response authentication result with authentication Id. 

 Users can defined their own JAAS Login module or write own custom authenticators as well.
  - [Changing the Default Security Settings](../user/authentication-settings-guide.md)

If authentication is success, connection will be established or else will send authentication error.