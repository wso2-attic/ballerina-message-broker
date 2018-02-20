# Changing the Default Database

WSO2 Message Broker uses JDBC based persistence to store messages of durable queues.
By default the broker is using file based H2 database as the persistence storage. 

## Supported Databases

Databases schemas related to supported database vendors can be found in the 
<BROKER_HOME>/dbscripts directory.
 
### Connecting to MySQL database

Follow the steps below to connect the broker to an existing MySQL database;

1. Download the MySQL Java connector [JAR file](https://dev.mysql.com/downloads/connector/j/), 
and copy it to the <BROKER_HOME>/lib/ directory.
    > NOTE: Be sure to use the connector version that is supported by the MySQL version you use.

2. Connect to the MySQL client console and create a database by executing the following command
    
    ```mysql-sql
    mysql> create database brokerdb;
    ```
3. Setup the database using the schama defined in mysql-mb.sql 
(located at <BROKER_HOME>/dbscripts/mysql-mb.sql)
    ```mysql-sql
    mysql> use brokerdb;
    mysql> source <path to the script>\mysql-mb.sql;
    ```

4. Edit the broker.yaml (located at <BROKER_HOME>/conf/broker.yaml) pointing to the MySQL
database you have created.
 
    ```
    broker:
     dataSource:
      url: jdbc:mysql://localhost:3306/brokerdb
      user: wso2user
      password: wso2password
      
      ...
   ```
