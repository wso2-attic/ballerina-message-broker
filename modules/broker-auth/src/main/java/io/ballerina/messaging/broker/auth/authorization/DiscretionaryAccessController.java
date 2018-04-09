package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Used to manage permissions for dynamic resources.
 */
public abstract class DiscretionaryAccessController {
    /**
     * Initialize authorization controller based on given auth configuration, user store.
     *
     * @param startupContext the startup context provides registered services in broker
     * @param userStore      user store
     * @param properties     properties
     */
    public abstract void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
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
     * @throws AuthServerException throws if an server error occurred
     * @throws AuthNotFoundException throws if the resource is not found
     */
    public final boolean authorize(String resourceType, String resource, String action, String userId,
                                   Set<String> userGroups) throws AuthServerException, AuthNotFoundException {
        AuthResource authResource = getAuthResource(resourceType, resource);

        if (Objects.isNull(authResource)) {
            return false;
        }

        if (authResource.getOwner().equals(userId)) {
            return true;
        }
        Set<String> userGroupsForAction = authResource.getActionsUserGroupsMap().get(action);
        return Objects.nonNull(userGroupsForAction) && userGroupsForAction.stream().anyMatch(userGroups::contains);

    }

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        resource owner
     * @throws AuthServerException throws if an server error occurred
     */
    public abstract void addResource(String resourceType, String resourceName, String owner) throws AuthServerException;

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws AuthServerException throws if an server error occurred
     * @throws AuthNotFoundException throws if the resource is not found
     */
    public abstract boolean deleteResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Allow given groups to access the given resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param groups       list of group
     * @return true if group list add successfully
     * @throws AuthServerException throws if server error occurred
     */
    public abstract boolean addGroupsToResource(String resourceType, String resourceName, String action,
                                                List<String> groups) throws AuthServerException;

    /**
     * Revoke access from the given group.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group
     * @throws AuthServerException throws if an server error occurred
     * @throws AuthNotFoundException throws if the resource is not found
     */
    public abstract boolean removeGroupFromResource(String resourceType, String resourceName,
                                                    String action, String group) throws AuthServerException,
                                                                                        AuthNotFoundException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param owner        newOwner
     * @throws AuthServerException throws if an server error occurred
     * @throws AuthNotFoundException throws if the resource is not found
     */
    public abstract boolean changeResourceOwner(String resourceType, String resourceName, String owner)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Get auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @return permission data of the resource
     * @throws AuthServerException throws if an server error occurred
     * @throws AuthNotFoundException throws if the resource is not found
     */
    public abstract AuthResource getAuthResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;

}
