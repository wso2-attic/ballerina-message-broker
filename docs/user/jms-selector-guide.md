# JMS Selectors

This broker is written based on AMQP broker semantics. In addition, to AMQP broker semantics 
following JMS selector functionalities are supported as well.

1. __x-filter-jms-selector__ header is used to define the selector expression.
    When binding a queue to an exchange above additional header can be provided with a
    valid JMS selector expression to enable message filtering.  
2. Only the topic exchange supports JMS message selectors. 
3. Subset of JMS selector grammar is supported. At the moment JMS equality
   operator with string, integer and float values are supported.
   
   Eg:- Following selector expression will match the messages with **CorrelationId** header value **a234df34**
    
    ```iso92-sql
    CorrelationId = 'a234df34'
    ```
    
    Following selector expression will match the messages with **eventName** custom property's value **logging**
    ```iso92-sql
    eventName = 'logging'
    ```
    
 
