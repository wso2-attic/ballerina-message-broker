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
package io.ballerina.messaging.broker.auth.ldap;

/**
 * Constants related to ldap authentication/authorization.
 */
public class LdapConstants {

    private LdapConstants() {
    }

    public static final String JNDI_LDAP_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String LDAP_AUTHENTICATION_SIMPLE = "simple";
    public static final String ENV_KEY_LDAP_SOCKET_FACTORY = "java.naming.ldap.factory.socket";
    public static final String SECURITY_PROTOCOL_SSL = "ssl";

}
