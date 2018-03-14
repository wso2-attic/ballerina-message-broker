package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.AuthScopeStoreImpl;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;

import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Default implementation class for MAC handler.
 */
public class DefaultMacHandler implements MandatoryAccessController {

    private AuthScopeStoreImpl authScopeStore;

    @Override
    public void initialize(StartupContext startupContext,
                           UserStore userStore,
                           Map<String, String> properties) throws Exception {
        DataSource dataSource = startupContext.getService(DataSource.class);
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration = configProvider.getConfigurationObject(
                BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);

        authScopeStore = new AuthScopeStoreImpl(brokerAuthConfiguration, dataSource);

    }

    @Override
    public boolean authorize(String scopeName, Set<String> userGroups)
            throws AuthNotFoundException {
        return authScopeStore.authorize(scopeName, userGroups);
    }
}
