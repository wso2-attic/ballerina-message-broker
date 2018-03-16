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
 */

package io.ballerina.messaging.broker.auth;

import java.security.Principal;
import java.util.Objects;
import javax.security.auth.Subject;

/**
 * A principal that is just a wrapper for a simple username
 */
public class UsernamePrincipal implements Principal {

    private final String name;

    public UsernamePrincipal(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            if (obj instanceof UsernamePrincipal) {
                UsernamePrincipal other = (UsernamePrincipal) obj;
                return name.equals(other.name);
            } else {
                return false;
            }
        }
    }

    /**
     * Create subject from the given authorization.
     *
     * @param authorizationID authorization ID
     * @return user subject
     */
    public static Subject createSubject(String authorizationID) {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePrincipal(authorizationID));
        return subject;
    }
}
