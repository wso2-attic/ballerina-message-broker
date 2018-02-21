# Working with broker RESTful services

This document explains how to create or modify a REST service for the broker. Our approach to building a REST
service is as follows,

1. Create a Swagger definition.
2. Generate JAX-RS service code template using Swagger codegen tool.
3. Use the generated JAX-RS code and connect it to the underlying broker services using a delegate class.
4. Deploy the service in MSF4j rest-runner.

Following are the instructions in detail for the above steps 

1. Create a Swagger definitions file for the API. For this, you can use the [online swagger editor.](https://editor.swagger.io/)
2. From the Swagger definition generate the relevant API and model classes. For this step use the swagger code gen tool
    1. Go to the [swagger-codegen repository](https://github.com/swagger-api/swagger-codegen) and follow the instructions there
    to build or download the swagger-codegen tool.
    2. We use JAX-RS to generate the REST service. Use the following command to create the JAX-RS server code.
        ```bash
        $ java -jar modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
        -i <path-to-swagger.yaml>  \ 
        -l jaxrs-spec -o <output-directory-path> \
        --model-package io.ballerina.messaging.broker.core.rest.model \ 
        --api-package io.ballerina.messaging.broker.core.rest.api
        ```
    3. Copy the created content into a src/gen directory on the relevant module.
       > NOTE: Since we are editing the generated classes we need to have
       them inside the src directory. This doesn't get generated every time we compile. 
3. Use a separate delegate class to handle the relevant logic of the REST API resources. Invoke the methods
   of the delegate class from the generated API classes.
4. Deploy the service using the `BrokerServiceRunner#deploy` method.
5. Copy the original swagger definition into the resources directory of the relevant module. 
    