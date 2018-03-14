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
package io.ballerina.messaging.broker.auth.authentication.sasl.plain;

import com.google.common.primitives.Bytes;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * This class implements @{@link SaslServer} for Plain text mechanism.
 * Response data will be in following format.
 * message   = [authzid] UTF8NUL authcid UTF8NUL passwd
 *
 * authzid = authorization identity
 * authcid = authentication identity
 * passwd = password
 */
class PlainSaslServer implements SaslServer {

    private Authenticator authenticator;

    private boolean isComplete = false;

    private String authenticationId;

    private char[] password;

    static final String PLAIN_MECHANISM = "PLAIN";

    PlainSaslServer(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public String getMechanismName() {
        return PLAIN_MECHANISM;
    }

    @Override
    public byte[] evaluateResponse(byte[] response) throws SaslException {
        int authzidNullPosition = Bytes.indexOf(response, (byte) 0);
        if (authzidNullPosition < 0) {
            throw new SaslException("Invalid SASL/PLAIN response due to authzid null separator not found");
        }
        int authcidNullPosition = Bytes
                .indexOf(Arrays.copyOfRange(response, authzidNullPosition + 1, response.length), (byte) 0);
        if (authcidNullPosition < 0) {
            throw new SaslException("Invalid SASL/PLAIN response due to authcid null separator not found");
        }
        String userName = new String(response, authzidNullPosition + 1, authcidNullPosition, StandardCharsets.UTF_8);
        int counter = authzidNullPosition + authcidNullPosition + 1;
        int passwordLen = response.length - counter - 1;
        password = new char[passwordLen];
        for (int i = 0; i < passwordLen; i++) {
            password[i] = (char) response[++counter];
        }
        try {
            AuthResult authResult = authenticator.authenticate(userName, password);
            authenticationId = authResult.getUserId();
            isComplete = authResult.isAuthenticated();
            return new byte[0];
        } catch (AuthException e) {
            throw new SaslException("Error while authenticating user with authenticator", e);
        } finally {
            clearCredentials();
        }
    }

    /**
     * Clear the credentials after handling data.
     */
    private void clearCredentials() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public String getAuthorizationID() {
        return authenticationId;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        return null;
    }

    @Override
    public void dispose() throws SaslException {
        authenticator = null;
    }
}
