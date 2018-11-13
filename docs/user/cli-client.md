# Broker Admin Command Line Interface (CLI)

Broker Command Line Interface is used to perform all kinds of administrative operations in the Message Broker. 

Following is the list of possible operations supported by the Broker CLI.

## Contents

1. [Initialize the Broker connection settings](#1-initialize-the-broker-connection-settings)
2. [Exchanges related administrative operations](#2-exchanges-related-administrative-operations)<br/>
    2.1. [Exchange creation](#21-exchange-creation)<br/>
    2.2. [Exchange information retrieval](#22-exchange-information-retrieval)<br/>
    2.3. [Exchange deletion](#23-exchange-deletion)
3. [Queues related administrative operations](#3-queues-related-administrative-operations)<br/>
    3.1. [Queue creation](#31-queue-creation)<br/>
    3.2. [Queue information retrieval](#32-queue-information-retrieval)<br/>
    3.3. [Queue deletion](#33-queue-deletion)
4. [Bindings related administrative operations](#4-bindings-related-administrative-operations)<br/>
    4.1. [Binding creation](#41-binding-creation)<br/>
    4.2. [Binding information retrieval](#42-binding-information-retrieval)<br/>
5. [Consumer related administrative operations](#5-consumer-related-administrative-operations)<br/>
    4.1. [Consumer information retrieval](#51-consumer-information-retrieval)
6. [Logger related administrative operations](#6-logger-related-administrative-operations)<br/>
    6.1. [Logger information retrieval](#61-logger-information-retrieval)<br/>
    6.2. [Logger log level change](#62-logger-log-level-change) 
7. [Output result formatting](#7-output-result-formatting)
8. [Global flags](#8-global-flags)
9. [Change CLI alias](#9-change-cli-alias)

[Appendix A: Specification](#appendix-a-specification)

## 1. Initialize the Broker connection settings

#### Description:
Broker's CLI connects to the the Broker's Rest APIs to perform all the administrative tasks. Therefore before using the CLI it should be initialized with connection details to the Rest API and use credentials.

There are two ways you can provide the password,<br/>
1. **Provide it with the init command at CLI client initialization**<br/>
The password will be saved in plain text when we provide it in the init command. But the advantage of this method
 is that you don't have to provide it on execution of each command, as this will be one time thing.<br/>
`./broker-admin.sh -H localhost -P 9000 -u admin -p`<br/>
(Provide the password in the next line. You can also provide the password following -p flag inline)

2. **Provide it with each command**<br/>
You can override the password given with the init command if you give it with the execution of each command. As follows,<br/>
`./broker-admin.sh list exchange -p`<br/>
(Provide the password in the next line. You can also provide the password following -p flag inline)

#### Command format:
`./broker-admin.sh init ([--hostname|-H] <host_name>)? ([--port|-P] <port>)? ([--username|-u] <username>)? ([--password|-p] <password>?)?`

#### Options:
- hostname (--hostname, -H) (default: localhost)
- port (--port, -P) (default: 9000)
- username (--username, -u) (default: admin)
- password (--password, -p) (default: admin)

## 2. Exchanges related administrative operations

Broker's CLI supports following admin operations on Broker Exchanges.

### 2.1. Exchange creation

#### Description:
New exchanges can be created in the Broker using CLI.

#### Command format:
`./broker-admin.sh create exchange <exchange_name> ([--type|-t] <ex_type>)? (--durable|-d)? (global_flags)*`

#### Options:
- type of the exchange (--type, -t) (default: direct) (allowed values: direct, topic)
- durability of the exchange (--durable, -d) (default: false/flag is not set)

#### Sample commands:
- Creating a 'direct' exchange that is 'non-durable'<br/>
`./broker-admin.sh create exchange my_direct_ex`

- Creating a 'topic' exchange that is 'durable'<br/>
`./broker-admin.sh create exchange my_topic_ex -t topic -d`

### 2.2. Exchange information retrieval

#### Description:
Information about an exchange or all the exchanges can be retrieved using the CLI.

#### Command format:
`./broker-admin.sh list exchange (exchange_name)? ([--output|-o] <output_format>)? (global_flags)*`

#### Options:
Result output format (--output, -o) (default: table) (allowed values: table, csv)

#### Sample commands:
- List all the exchanges in the Broker
`./broker-admin.sh list exchange`

- List a specific exchange info in csv format
`./broker-admin.sh list exchange my_exchange -o csv`


### 2.3. Exchange deletion

#### Description:
Exchanges can be deleted from the Broker using the CLI.

#### Command format:
`./broker-admin.sh delete exchange (exchange_name)? (--force-used|-u)? (global_flags)*`

#### Options:
Force delete exchange with bindings (--force-used, -u) (default: false/not set by default)

#### Sample commands:
Delete a specific exchange if its unused<br/>
`./broker-admin.sh delete exchange my_exchange -u`

## 3. Queues related administrative operations

Broker's CLI supports following admin operations on Broker Queues.

### 3.1. Queue creation

#### Description:
New queues can be created in the Broker using CLI.

#### Command format:
`./broker-admin.sh create queue <queue_name> (--autoDelete|-a)? (--durable|-d)? (global_flags)*`

#### Options:
- set queue to auto-delete (--autoDelete, -a) (default: false/flag is not set)
- durability of the queue (--durable, -d) (default: false/flag is not set)

#### Sample commands:
- Creating a 'non-autoDelete', 'non-durable' queue<br/>
`./broker-admin.sh create queue my_queue`
- Creating a 'autoDeletable', 'durable' queue<br/>
`./broker-admin.sh create queue my_queue -a -d`

### 3.2. Queue information retrieval

#### Description:
Information about a queue or all the queues can be retrieved using the CLI.

#### Command format:
`./broker-admin.sh list queue (queue_name)? ([--output|-o] <output_format>)? (global_flags)*`

#### Options:
Result output format (--output, -o) (default: table) (allowed values: table, csv)

#### Sample commands:
- List all queues in the Broker<br/>
`./broker-admin.sh list queue`

- List a specific queue info in csv format<br/>
`./broker-admin.sh list queue my_queue -o csv`


### 3.3. Queue deletion

#### Description:
Queues can be deleted from the Broker using the CLI.

#### Command format:
`./broker-admin.sh delete queue (queue_name)? (--force-used|-u)? (--force-non-empty|-e)? (global_flags)*`

#### Options:
- Force delete queue with consumers (--force-used, -u) (default: false/not set by default)<br/>
- Force delete non empty queue (--force-non-empty, -e) (default: false/not set by default)

#### Sample commands:
Delete a specific exchange if it's unused<br/>
`./broker-admin.sh delete exchange my_exchange -u`

## 4. Bindings related administrative operations

Broker's CLI supports following admin operations on (Exchange-Queue) Bindings.

### 4.1. Binding creation

#### Description:
New bindings can be created in the Broker using the CLI. Routing key is used to name the binding and messages will be routed to the queue based on the exchange type and the routing key. If the routing key is not provided with the command, it will use queue name as the routing key.

#### Command format:
`./broker-admin.sh create binding <routing_key>? ([--queue|-q] <queue_name>) ([--exchange|-e] <exchange_name>) ([--filter|-f <filter_expression>])? (global_flags)*`

#### Options:
- name of the queue (--queue|-q) (mandatory)
- name of the exchange (--exchange|-e) (mandatory)
- binding filters (--filter|-f) (default: EMPTY)

#### Sample commands:
Creating a binding with routing key between an exchange and a queue<br/>
`./broker-admin.sh create binding my_route -e sample_ex -q sample_q`

### 4.2. Binding information retrieval

#### Description:
List down bindings of a queue or an exchange. Name of a queue or an exchange must be provided.<br/>
Note: Listing queue bindings is still not supported yet.

#### Command format:
`./broker-admin.sh list binding (([--queue|-q] <queue_name>) | ([--exchange|-e] <exchange_name>)) ([--output|-o] <output_format>)? (global_flags)*`

#### Options:
- Name of the exchange (--exchange, -e)
- Name of the queue (--queue, -q)
- Result output format (--output, -o) (default: table) (allowed values: table, csv)

#### Sample commands:
List all bindings of an exchange in the Broker<br/>
`./broker-admin.sh list binding -e my_exchange`

## 5. Consumer related administrative operations

Message consumers are connected to queues under the AMQP architecture. Broker's CLI can be used to list down consumers on a given queue. 

### 5.1. Consumer information retrieval

#### Description:
List down message consumers of a queue. If consumer Id is not given, CLI will display all the consumers on the given queue.

#### Command format:
`./broker-admin.sh list consumer <consumer_id>? ([--queue|-q] <queue_name>) ([--output|-o] <output_format>)? (global_flags)*`

#### Options:
- Name of the queue (--queue, -q) (mandatory)<br/>
- Result output format (--output, -o) (default: table) (allowed values: table, csv)

#### Sample commands:
List all consumers of queue in the Broker<br/>
`./broker-admin.sh list consumer -q my_queue`

## 6. Logger related administrative operations

Ballerina Message Broker uses log4j framework for logging. Broker's CLI can be used to retrieve information and change log level of log4j loggers in the run time. 

### 6.1. Logger information retrieval

#### Description:
Show details of loggers in the broker at run time.

#### Command format:
`./broker-admin.sh show logger <logger-name>? ([--output|-o] <output_format>)? (global_flags)*`

#### Options:
- Logger name ( Regular expression to  filter loggers by name)<br/>
- Result output format (--output, -o) (default: table) (allowed values: table, csv)

#### Sample commands:
Show all the loggers in the Broker<br/>
`./broker-admin.sh show logger`

Filter loggers in the Broker<br/>
`./broker-admin.sh show logger *org.wso2.carbon.metrics.*`

### 6.2. Logger log level change

#### Description:
Update log level of a given logger.

#### Command format:
`./broker-admin.sh update logger [--name|-n] <logger_name> [--level|-l] <log_level>)`

#### Options:
- Name of the logger (--name, -n) (mandatory)<br/>
- Log level of the logger (--level, -l) (mandatory)

#### Sample commands:
Change log level of a logger to WARN<br/>
`./broker-admin.sh update logger -n my_logger -l WARN`

## 7. Output result formatting

Output results of the 'list' commands can be formatted and view as table or csv using the flag (--output, -o)

#### Sample commands:
List all exchanges in the broker in csv format<br/>
`./broker-admin.sh list exchange -o csv`
 
## 8. Global flags

Following are the global flags supported by the CLI commands,

- Ask for help (--help, -h)<br/>
- Set or override the password (--password, -p)

## 9. Change CLI alias

You can add a alias you prefer instead of using the ./broker-admin.sh file directly.

## Appendix A: Specification

### General command format

`./broker-admin.sh [action] [resource-type]? [resource-name]? [flag]*`

### CLI actions

- init
- create
- list
- delete
- grant
- revoke
- transfer
- show
- update
 
### CLI resource types

- exchange
- queue
- binding
- consumer
- logger
