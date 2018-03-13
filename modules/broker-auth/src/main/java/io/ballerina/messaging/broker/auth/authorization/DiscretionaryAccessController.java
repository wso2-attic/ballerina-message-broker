package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthDuplicateException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;
import java.util.Set;

/**
 * Used to manage permissions for dynamic resources.
 */
public interface DiscretionaryAccessController {
    /**
     * Initialize authorization controller based on given auth configuration, user store.
     *
     * @param startupContext the startup context provides registered services in broker
     * @param userStore      user store
     * @param properties     properties
     */
    void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
            throws Exception;

    /**
     * Authorize resource with given resource and action.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @param action       action
     * @param userId       user identifier of owner
     * @param userGroups   user groups of the user
     * @return true if authorised, false otherwise
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    boolean authorize(String resourceType, String resource, String action, String userId, Set<String> userGroups)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        resource owner
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthDuplicateException throws if the resource already exists
     */
    void addResource(String resourceType, String resourceName, String owner)
            throws BrokerAuthServerException, BrokerAuthDuplicateException;

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    boolean deleteResource(String resourceType, String resourceName)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Allow given group to access the given resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group
     * @throws BrokerAuthException throws if error occur during updating resource
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    void addGroupToResource(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthException, BrokerAuthNotFoundException, BrokerAuthServerException;

    /**
     * Revoke access from the given group.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    void removeGroupFromResource(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        newOwner
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    void changeResourceOwner(String resourceType, String resourceName, String owner)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Get auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @return permission data of the resource
     * @throws BrokerAuthServerException throws if an server error occurred
     * @throws BrokerAuthNotFoundException throws if the resource is not found
     */
    AuthResource getAuthResource(String resourceType, String resourceName)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

}
