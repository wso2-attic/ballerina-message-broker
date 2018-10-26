package io.ballerina.messaging.broker.eventing;

import java.util.Map;
/**
 * Interface used by event publishers.
 */
public interface EventSync {


    String getName();
    int getPublisherID();
    void publish(int id, Map<String, String> properties); //string
    void activate();
    void deactivate();

}
