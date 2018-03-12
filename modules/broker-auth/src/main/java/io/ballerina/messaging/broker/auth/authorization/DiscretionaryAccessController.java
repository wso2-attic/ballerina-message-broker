package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

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
     * @return true if authorised, false otherwise
     * @throws BrokerAuthException throws if error occur during authorization
     */
    boolean authorize(String resourceType, String resource, String action, String userId) throws BrokerAuthException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        resource owner
     * @throws BrokerAuthException throws if error occur during adding resource
     */
    void addResource(String resourceType, String resourceName, String owner) throws BrokerAuthException;

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws BrokerAuthException throws if error occur during deleting resource
     */
    void deleteResource(String resourceType, String resourceName) throws BrokerAuthException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group
     * @throws BrokerAuthException throws if error occur during updating resource
     */
    void addGroupToResource(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        newOwner
     * @throws BrokerAuthException throws if error occur during updating resource
     */
    void changeResourceOwner(String resourceType, String resourceName, String owner) throws BrokerAuthException;

}
