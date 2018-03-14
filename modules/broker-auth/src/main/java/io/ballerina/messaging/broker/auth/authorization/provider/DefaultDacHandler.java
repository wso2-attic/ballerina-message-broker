package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.AuthDuplicateException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.AuthResourceStore;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResourceStoreImpl;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;

import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Default implementation of DAC handler.
 */
public class DefaultDacHandler implements DiscretionaryAccessController {
    private AuthResourceStore authResourceStore;

    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
            throws Exception {
        DataSource dataSource = startupContext.getService(DataSource.class);
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration = configProvider.getConfigurationObject(
                BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);

        authResourceStore = new AuthResourceStoreImpl(brokerAuthConfiguration, dataSource, userStore);
    }

    @Override
    public boolean authorize(String resourceType, String resource, String action, String userId, Set<String> userGroups)
            throws AuthServerException, AuthNotFoundException {
            return authResourceStore.authorize(resourceType, resource, action, userId, userGroups);
    }

    @Override
    public void addResource(String resourceType, String resourceName, String owner)
            throws AuthServerException, AuthDuplicateException {
            authResourceStore.add(new AuthResource(resourceType, resourceName, true, owner));
    }

    @Override
    public boolean deleteResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        return authResourceStore.delete(resourceType, resourceName);
    }

    @Override
    public void addGroupToResource(String resourceType, String resourceName, String action, String group)
            throws AuthNotFoundException, AuthServerException {
        authResourceStore.addGroup(resourceType, resourceName, action, group);
    }

    @Override
    public void removeGroupFromResource(String resourceType, String resourceName, String action, String group)
            throws AuthServerException, AuthNotFoundException {
        authResourceStore.removeGroup(resourceType, resourceName, action, group);
    }

    @Override
    public void changeResourceOwner(String resourceType, String resourceName, String newOwner)
            throws AuthServerException, AuthNotFoundException {
        authResourceStore.updateOwner(resourceType, resourceName, newOwner);
    }

    @Override
    public AuthResource getAuthResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        return authResourceStore.read(resourceType, resourceName);
    }
}
