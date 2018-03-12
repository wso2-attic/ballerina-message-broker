package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Used to manage static resource permissions.
 */
public interface MandatoryAccessController {
    /**
     * Initialize authorization controller based on given auth configuration, user store.
     *
     * @param startupContext the startup context provides registered services in broker
     * @param properties properties
     */
    void initialize(StartupContext startupContext, Map<String, String> properties) throws Exception;

    /**
     * Authorize user with given scope key.
     *
     * @param userId    an user identifier
     * @param scopeName a scope key
     * @return if authorised or not
     * @throws BrokerAuthException throws if error occur during authorization
     */
    boolean authorize(String userId, String scopeName) throws BrokerAuthException;
}
