# Broker User Interface

Broker User Interface can be used to view the state of the Ballerina Message Broker at a given time. It visualizes the exchanges, queues, bindings, and their relationships in the message broker.

#### Prerequisites: A ballerina message broker instance should be up and running.

Following is the list of possible operations supported by the Broker UI.

## Contents

1. [View all exchanges in the broker](#1-View-all-exchanges-in-the-broker
)
2. [View all queues in the broker](#2-View-all-queues-in-the-broker)
   
3. [View details of a specific exchange](#3-View-details-of-a-specific-exchange)
4. [View details of a specific queue](#4-View-details-of-a-specific-queue)
5. [View binding details of an exchange
](#5-View-binding-details-of-an-exchange
)
6. [View details of consumers of a specific queue](#6-View-details-of-consumers-of-a-specific-queue)
7. [Add a new exchange to the broker](#7-Add-a-new-exchange-to-the-broker)
8. [ Add a new queue to the broker](#8-Add-a-new-queue-to-the-broker )




## 1. View all exchanges in the broker

#### Description:
In order to view all the exchanges in the broker click on the Exchanges button which on the drawer. All the exchanges in the broker will be listed in a tabular format with exchange name, type, and durability. Exchanges can be searched by any of those fields.

## 2. View all queues in the broker

#### Description:
In order to view all the queues in the broker click on the Queues button. All the queues will be displayed in a tabular format with queue name, consumer count, durability, capacity size, and auto delete capability. Queues can be searched by their name, durability or auto delete capability.

## 3. View details of a specific exchange

#### Description:

To view details of a specific exchange, click on the Exchange button. From the list of exchanges that is displayed, select the name of the exchange which you want to see the details. Then the details of that specific exchange and its binding details will be displayed.

## 4. View details of a specific queue

#### Description:

To view details of a specific queue click on the Queue button. From the list of queues that is displayed, select the name of the queue which you want to see the details. Then the details of that specific queue and its binding details will be displayed.

## 5. View binding details of an exchange

#### Description:

In order to view binding details of an exchange, click on the Exchange button. From the list of exchanges that is displayed, select the exchange name. Click on the expansion icon under bindings. Then the binding details of that exchange such as to which queue it is bound to and the binding key of each binding can be viewed.

## 6. View details of consumers of a specific queue

#### Description:

Details of a consumer of a specific queue can be viewed in two ways.	
- Click on the queue button on the drawer. From the table of queue details, select the consumer count in front of the queue name and click on it. Then the consumer details of that queue such as consumer id, whether is-exclusive, flow enabled, connection id and channel id will be displayed.

- Click on the consumer button on the drawer. Select the queue name from the drop-down list. Then the consumer details will be displayed in a tabular format.

## 7. Add a new exchange to the broker


#### Description:

In order to add a new exchange to the broker, click on the exchange button on the drawer. Click on the add icon at the right corner. Then a pop up will appear. Provide exchange name and select type and durability from the drop-down lists. Click on add exchange button to add the exchange and cancel button to cancel.

## 8. Add a new queue to the broker


#### Description:

In order to add a new queue to the broker, click on the queue button on the drawer. Click on the add icon at the right corner. Then a pop up will appear. Provide queue name and select durability and auto delete ability from the drop down lists. Click on add queue button to add the queue and cancel button to cancel.





