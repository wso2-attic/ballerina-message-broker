package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Default implementation of DAC handler.
 */
public class DefaultDacHandler implements DiscretionaryAccessController {
    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
            throws Exception {
        // TODO Implement logic
    }

    @Override
    public boolean authorize(String resourceType, String resource, String action, String userId)
            throws BrokerAuthException {
        return true;
    }

    @Override
    public void addResource(String resourceType, String resourceName, String owner) throws BrokerAuthException {
        // TODO Implement logic
    }

    @Override
    public void deleteResource(String resourceType, String resourceName) throws BrokerAuthException {
        // TODO Implement logic
    }

    @Override
    public void addGroupToResource(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthException {
        // TODO Implement logic
    }

    @Override
    public void changeResourceOwner(String resourceType, String resourceName, String owner) throws BrokerAuthException {
        // TODO Implement logic
    }
}
