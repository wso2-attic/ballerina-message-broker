package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;
import java.util.Set;

/**
 * Used to manage static resource permissions.
 */
public interface MandatoryAccessController {
    /**
     * Initialize authorization controller based on given auth configuration, user store.
     *
     * @param startupContext the startup context provides registered services in broker
     * @param userStore      contains the user ID to user group mapping
     * @param properties     properties
     */
    void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
            throws Exception;

    /**
     * Authorize user with given scope key.
     *
     * @param scopeName  a scope key
     * @param userGroups set of user groups to check
     * @return if authorised or not
     * @throws AuthNotFoundException throws if scope is not found
     */
    boolean authorize(String scopeName, Set<String> userGroups) throws AuthNotFoundException;
}
