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

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Handles Ldap queries.
 */
public class LdapAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthHandler.class);
    private BrokerAuthConfiguration.LdapConfiguration ldapConfiguration;

    public LdapAuthHandler(BrokerAuthConfiguration.LdapConfiguration ldapConfiguration) throws AuthException {

        this.ldapConfiguration = ldapConfiguration;
        BrokerAuthConfiguration.LdapSslConfiguration sslConfig = ldapConfiguration.getSsl();

        if (sslConfig.isEnabled()) {

            String trustStoreLocation = Paths.get(sslConfig.getTrustStore().getLocation())
                    .toAbsolutePath().toString();
            try (FileInputStream fileInputStream = new FileInputStream(trustStoreLocation)) {

                KeyStore trustStore = KeyStore.getInstance(sslConfig.getTrustStore().getType());
                trustStore.load(fileInputStream, sslConfig.getTrustStore().getPassword().toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        sslConfig.getTrustStore().getCertType());
                trustManagerFactory.init(trustStore);
                SSLContext sslContext = SSLContext.getInstance(sslConfig.getProtocol());
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                LdapSslSocketFactory.setSslContext(sslContext);

            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
                    | KeyManagementException e) {
                throw new AuthException("Initializing ldap trust store failed", e);
            }
        }
    }

    public DirContext connectAnonymously() throws NamingException {

        return connect(new Hashtable<>());
    }

    /**
     * Connect to ldap server.
     *
     * @param env properties to be passed to InitialDirContext
     * @return InitialDirContext if the connection is successful
     * @throws NamingException if the connection failed
     */
    private DirContext connect(Hashtable<String, String> env) throws NamingException {

        DirContext dirContext = null;
        env.put(Context.INITIAL_CONTEXT_FACTORY, LdapConstants.JNDI_LDAP_CTX_FACTORY);
        env.put(Context.SECURITY_AUTHENTICATION, LdapConstants.LDAP_AUTHENTICATION_SIMPLE);

        if (ldapConfiguration.getSsl().isEnabled()) {

            env.put(Context.PROVIDER_URL,
                    "ldap://" + ldapConfiguration.getHostName() + ":" + ldapConfiguration.getSsl().getPort());
            env.put(Context.SECURITY_PROTOCOL, LdapConstants.SECURITY_PROTOCOL_SSL);
            env.put(LdapConstants.ENV_KEY_LDAP_SOCKET_FACTORY, LdapSslSocketFactory.class.getName());
            dirContext = new InitialDirContext(env);

        } else {

            env.put(Context.PROVIDER_URL,
                    "ldap://" + ldapConfiguration.getHostName() + ":" + ldapConfiguration.getPlain().getPort());
            dirContext = new InitialDirContext(env);
        }

        return dirContext;
    }

    /**
     * Fetch distinguished name (dn) of the given user.
     *
     * @param dirContext ldap connection object
     * @param username of the user
     * @return distinguished name (dn) of the given user. Null if not found.
     * @throws NamingException in case of an erroneous search query
     */
    public String searchDN(DirContext dirContext, String username) throws NamingException {

        String lookup = ldapConfiguration.getUsernameSearchFilter().replace("?", username);
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> answer
                = dirContext.search(ldapConfiguration.getBaseDN(), lookup, searchControls);

        String dn;
        if (answer.hasMore()) {
            javax.naming.directory.SearchResult result = answer.next();
            dn = result.getNameInNamespace();
            LOGGER.debug("DN: {} obtained for Username: {}", dn, username);
        } else {
            dn = null;
            LOGGER.debug("DN search failed for Username: {}", username);
        }
        answer.close();
        return dn;
    }

    /**
     * Authenticate the user's distinguished name and password.
     *
     * @param dn distinguished name of the user
     * @param password of the user
     * @return whether authenticated or not
     * @throws NamingException if the connection failed
     */
    public boolean authenticate(String dn, String password) throws NamingException {

        boolean isAuthenticated = false;

        if (dn != null && dn.length() > 0) {

            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.SECURITY_PRINCIPAL, dn);
            env.put(Context.SECURITY_CREDENTIALS, String.valueOf(password));
            DirContext authDirContext = null;
            try {
                authDirContext = connect(env);
                isAuthenticated = true;
                LOGGER.debug("DN: {} authenticated successfully", dn);
            } catch (javax.naming.AuthenticationException e) {
                LOGGER.debug("DN: {} authentication failed", dn, e);
            } finally {
                if (authDirContext != null) {
                    try {
                        authDirContext.close();
                    } catch (NamingException e) {
                        LOGGER.debug("Error closing dir context", e);
                    }
                }
            }
        } else {
            LOGGER.debug("Invalid DN");
        }

        return isAuthenticated;
    }
}
