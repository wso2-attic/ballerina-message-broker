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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource;

import com.google.common.base.Objects;

/**
 * Class represents cache key for auth resource cache.
 */
public class ResourceCacheKey {

    private static final String AUTH_RESOURCE_TYPE_SEPARATOR = "_";

    private final String resourceType;

    private final String resourceName;

    public ResourceCacheKey(String resourceType, String resourceName) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resourceType, resourceName);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ResourceCacheKey &&
                (((ResourceCacheKey) obj).resourceName.equals(resourceName)
                        && ((ResourceCacheKey) obj).resourceType.equals(resourceType)));
    }

    @Override
    public String toString() {
        return resourceType + AUTH_RESOURCE_TYPE_SEPARATOR + resourceName;
    }
}
