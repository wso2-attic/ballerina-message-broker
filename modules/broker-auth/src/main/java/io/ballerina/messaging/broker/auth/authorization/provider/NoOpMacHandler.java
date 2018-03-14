package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;
import java.util.Set;

/**
 * {@link NoOpMacHandler} allows every request.
 */
public class NoOpMacHandler implements MandatoryAccessController {
    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties) {
        // Do nothing
    }

    @Override
    public boolean authorize(String scopeName, Set<String> userGroups) {
        // We can not do much since we don't manage MAC in memory.
        return true;
    }
}
