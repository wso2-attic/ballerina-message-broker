package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Default implementation class for MAC handler.
 */
public class DefaultMacHandler implements MandatoryAccessController {
    @Override
    public void initialize(StartupContext startupContext, Map<String, String> properties) throws Exception {
        // TODO implement logic
    }

    @Override
    public boolean authorize(String userId, String scopeName) throws BrokerAuthException {
        // TODO implement logic
        return true;
    }
}
