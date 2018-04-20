/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl.AuthResourceInMemoryDao;
import io.ballerina.messaging.broker.common.StartupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * This implementation used when there is no auth manager set to the startup context.
 */
public class MemoryDacHandler extends DiscretionaryAccessController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryDacHandler.class);
    private AuthResourceDao resourceDao = new AuthResourceInMemoryDao();

    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties) {
        try {
            persistDefaultAuthResources();
        } catch (AuthServerException e) {
            LOGGER.error("Error occurred while persisting auth resources.", e);
        }
    }

    @Override
    public void addResource(String resourceType, String resourceName, String owner) throws AuthServerException {
        //do nothing as authorization disabled
        resourceDao.persist(new AuthResource(resourceType, resourceName, false, owner));
    }

    @Override
    public boolean deleteResource(String resourceType, String resourceName) throws AuthServerException {
        //do nothing as authorization disabled
        return resourceDao.delete(resourceType, resourceName);
    }

    @Override
    public boolean addGroupsToResource(String resourceType, String resourceName, String action, List<String> groups)
            throws AuthServerException {
        return resourceDao.addGroups(resourceType, resourceName, action, groups);
    }

    @Override
    public boolean removeGroupFromResource(String resourceType, String resourceName, String action, String group)
            throws AuthServerException, AuthNotFoundException {
        return resourceDao.removeGroup(resourceType, resourceName, action, group);
    }

    @Override
    public boolean changeResourceOwner(String resourceType, String resourceName, String owner)
            throws AuthServerException {
        return resourceDao.updateOwner(resourceType, resourceName, owner);
    }

    @Override
    public AuthResource getAuthResource(String resourceType, String resourceName) throws AuthServerException {
        return resourceDao.read(resourceType, resourceName);
    }

    private void persistDefaultAuthResources() throws AuthServerException {
        addResource("exchange", "<<default>>", "admin");
        addResource("exchange", "amq.direct", "admin");
        addResource("exchange", "amq.topic", "admin");
        addResource("queue", "amq.dlq", "admin");
    }
}
