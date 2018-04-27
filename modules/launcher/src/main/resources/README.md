# Ballerina Message Broker

WSO2 Message Broker is a lightweight, easy-to-use, 100% open source message-brokering server. It uses AMQP 0-9-1
as the messaging protocol.

## Building from the source

Please follow the steps below to build message broker from source code.

1. Clone or download the source code from this repository (https://github.com/ballerina-platform/ballerina-message-broker)
2. Run the Maven command `mvn clean install` from the root directory of the repository
3. Extract the message broker distribution created at
`message-broker/modules/launcher/target/message-broker-<version>.zip`

## Folder Structure of the distribution

```
.
├── bin
│   ├── broker-admin.sh
│   ├── broker-admin.bat
│   ├── broker.bat
│   ├── broker
│   ├── ciphertool.bat
│   └── ciphertool.sh
├── conf
│   ├── admin-service-transports.yaml
│   ├── broker.yaml
│   ├── log4j.properties
│   ├── master-keys.yaml
│   └── security
│       ├── secrets.properties
│       └── users.yaml
├── database
├── dbscripts
├── lib
└── resources
    └── security
        ├── client-truststore.jks
        └── keystore.jks

```

- **bin/broker** - executable used to run message broker
- **bin/broker.bat** - windows executable used to run message broker
- **bin/broker-admin.sh** - executable used to start message broker cli client
- **bin/broker-admin.bat** - windows executable used to start message broker cli client
- **bin/ciphertool.sh** - executable used to encrypt plain text passwords define in configuration file
- **bin/ciphertool.bat** - windows executable used to encrypt plain text passwords define in configuration file
- **conf/admin-service-transports.yaml** - admin rest service related configuration file
- **conf/broker.yaml** - main configuration file
- **conf/log4j.properties** - logging configuration file
- **conf/master-keys.yaml** - store keystore password and private key password to read by the secure vault
- **conf/security/secrets.properties** - store password with alias such as the database, keystore etc. to be encrypted by the cipher tool
- **conf/security/users.yaml** - file based user store
- **lib** - contains all the required jars
- **database** - contains files related to the embedded H2 database
- **dbscripts** - database schemas related to supported databases
- **logs/broker.log** - main logging file
- **logs/broker-trace.log** - message trace log file
- **resources/security** - contains both the keystore and the truststore used to create the SSL engine

## Documentation

Please refer the [user documentation](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/docs/user-doc-index.md) for information on using the product.

Please refer the [developer documentation](https://github.com/ballerina-platform/ballerina-message-broker/blob/master/docs/developer-doc-index.md) for more information on the internal design.

## Licence

Ballerina Message Broker is licensed under [the Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Copyright

(c) 2017, [WSO2 Inc.](http://www.wso2.org) All Rights Reserved.