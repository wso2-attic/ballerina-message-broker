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
package io.ballerina.messaging.broker.rest.auth;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.UsernamePrincipal;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.security.auth.Subject;

/**
 * Class implements  @{@link RequestInterceptor} to authenticate requests with basic authentication.
 */
public class BasicAuthSecurityInterceptor implements RequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthSecurityInterceptor.class);

    private static final String AUTH_TYPE_BASIC = "Basic";

    private static final int AUTH_TYPE_BASIC_LENGTH = AUTH_TYPE_BASIC.length();

    private AuthenticateFunction<AuthException> authenticateFunction;

    public BasicAuthSecurityInterceptor(AuthenticateFunction<AuthException> authenticateFunction) {
        this.authenticateFunction = authenticateFunction;
    }

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        String authHeader = request.getHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            String authType = authHeader.substring(0, AUTH_TYPE_BASIC_LENGTH);
            String authEncoded = authHeader.substring(AUTH_TYPE_BASIC_LENGTH).trim();
            if (AUTH_TYPE_BASIC.equals(authType) && !authEncoded.isEmpty()) {

                // Read the Basic auth header and extract the username and password from base 64 encoded string.
                byte[] decodedByte = Base64.getDecoder().decode(authEncoded.getBytes(StandardCharsets.UTF_8));
                char[] array = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decodedByte)).array();
                int separatorIndex = findIndex(array, ':');
                String userName = new String(Arrays.copyOfRange(array, 0, separatorIndex));
                char[] password = Arrays.copyOfRange(array, separatorIndex + 1, array.length);
                if (authenticate(userName, password)) {
                    Subject subject = new Subject();
                    subject.getPrincipals().add(new UsernamePrincipal(userName));
                    request.getSession().setAttribute(BrokerAuthConstants.AUTHENTICATION_ID, subject);
                    return true;
                }
            }
        }
        response.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
        response.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE_BASIC);
        return false;
    }

    private int findIndex(char[] array, char value) {
        int startIndex = 0;

        if (array == null) {
            return -1;
        } else {
            for (int i = startIndex; i < array.length; ++i) {
                if (value == array[i]) {
                    return i;
                }
            }
            return -1;
        }
    }

    private boolean authenticate(String userName, char... password) {
        try {
            return userName != null && authenticateFunction.authenticate(userName, password).isAuthenticated();
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error occurred while authenticating user", e);
            }
            return false;
        }
    }

    /**
     * Function to authenticate user on given username and credentials.
     *
     * @param <E> the type of the exception thrown
     */
    @FunctionalInterface
    public interface AuthenticateFunction<E extends Exception> {
        /**
         * Authenticate based on given username and credentials.
         *
         * @param username    username
         * @param credentials user credentials
         * @throws E if unable to authenticate the user
         */
        AuthResult authenticate(String username, char... credentials) throws E;
    }
}
