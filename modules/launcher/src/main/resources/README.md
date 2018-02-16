# WSO2 Message Broker 4

WSO2 Message Broker is a lightweight, easy-to-use, 100% open source message-brokering server. It uses AMQP 0-9-1 
as the messaging protocol.

## Building from the source

Please follow the steps below to build message broker from source code.

1. Clone or download the source code from this repository (https://github.com/wso2/message-broker)
2. Run the Maven command `mvn clean install` from the root directory of the repository
3. Extract the message broker distribution created at 
`message-broker/modules/launcher/target/message-broker-<version>.zip`

## Folder Structure of the distribution

```
.
├── bin
│   ├── broker.sh
│   └── broker.bat
├── conf
│   ├── broker.yaml
│   └── log4j.properties
├── lib
├── database
├── dbscripts
├── logs
│   └── broker.log
└── resources
    └── security
        ├── client-truststore.jks
        └── keystore.jks

```

- **bin/broker.sh** - executable used to run message broker
- **bin/broker.bat** - windows executable used to run message broker
- **conf/broker.yaml** - main configuration file
- **conf/log4j.properties** - logging configuration file
- **lib** - contains all the required jars
- **database** - contains files related to the embedded H2 database
- **dbscripts** - database schemas related to supported databases
- **logs/broker.log** - main logging file
- **resources/security** - contains both the keystore and the truststore used to create the SSL engine

## Documentation

Please refer the [user documentation](https://github.com/wso2/message-broker/blob/master/docs/user-doc-index.md) for information on using the product.

Please refer the [developer documentation](https://github.com/wso2/message-broker/blob/master/docs/developer-doc-index.md) for more information on the internal design.

## Licence

WSO2 Message Broker is licensed under [the Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Copyright

(c) 2017, [WSO2 Inc.](http://www.wso2.org) All Rights Reserved.