#  Dockerfile for Message Broker

Dockerfile provides the capability to the user to build the docker image for deploying the product in a containerized 
environment. This section explains step-by-step instructions to build docker image of the Ballerina Message Broker.

The Ballerina message broker docker image based on the **openjdk:8-jre-alpine**. A user can change to the desired base image 
and run with desired Java distribution.

## How to build an image and run

1. Build the ballerina-message-broker repo to generated the distribution.

2. You can find the distribution in **modules/launcher/target** folder. Extract the zip file and copy it to the 
**dockerfiles/files** folder.

3. Open the **dockerfiles/Dockerfile** and edit **BROKER_VERSION** according to the built version. The current version 
is **0.970.0.rc1**. 

4. Navigate to **dockerfiles** folder and run docker build command.
    ```
    docker build -t message-broker:0.970.0.rc1 .
    ```
5. Run Ballerina Message Broker docker image.
    ```
    docker run -d -p 5672:5672 -p 8672:8672 -p 9000:9000 message-broker:0.970.0.rc1
    ```