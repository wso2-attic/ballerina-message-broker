package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl;

import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;

import javax.sql.DataSource;

/**
 * Instantiates DAO objects required to access different storage mediums.
 */
public class AuthResourceDaoFactory {

    private AuthResourceDao inMemoryDao;

    private AuthResourceDao durableAuthResourceDao;

    public AuthResourceDaoFactory(DataSource dataSource) {
        this.inMemoryDao = new AuthResourceInMemoryDao();
        this.durableAuthResourceDao = new AuthResourceRdbmsDao(dataSource);
    }

    /**
     * Provide instance of @{@link AuthResourceDao} based on storage condition.
     * @param isDurableResource is durable
     * @return dao instance
     */
    public  AuthResourceDao getAuthResourceDao(boolean isDurableResource) {
        if (isDurableResource) {
            return durableAuthResourceDao;
        } else {
            return inMemoryDao;
        }
    }
}
