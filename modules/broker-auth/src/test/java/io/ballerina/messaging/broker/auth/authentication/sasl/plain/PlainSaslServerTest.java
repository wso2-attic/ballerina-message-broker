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

import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator;
import io.ballerina.messaging.broker.auth.authentication.jaas.PlainSaslCallbackHandler;
import io.ballerina.messaging.broker.auth.authentication.sasl.plain.jaas.TestLoginModule;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.sasl.SaslException;

/**
 * Class for testing @{@link PlainSaslServer} response evaluation.
 */
public class PlainSaslServerTest {

    private PlainSaslServer plainSaslServer;
    private Authenticator authenticator;

    @BeforeMethod
    public void setUp() throws Exception {
        authenticator = new JaasAuthenticator();
        plainSaslServer = new PlainSaslServer(authenticator);
        // create test login module and set in in the configuration
        AppConfigurationEntry[] entries = {
                new AppConfigurationEntry(TestLoginModule.class.getCanonicalName(),
                                          AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, new HashMap<>())
        };
        Configuration.setConfiguration(new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entries;
            }
        });
    }

    @Test(expectedExceptions = { SaslException.class },
          description = "Test evaluate empty response",
          expectedExceptionsMessageRegExp = ".*Invalid SASL/PLAIN response due to authzid null separator not found.*")
    public void testEvaluateEmptyResponse() throws Exception {
        plainSaslServer.evaluateResponse(new byte[0]);
    }

    @Test(expectedExceptions = SaslException.class,
          description = "Test evaluate response without authorization separator",
          expectedExceptionsMessageRegExp = ".*Invalid SASL/PLAIN response due to authzid null separator not found.*")
    public void testEvaluateResponseWithoutAuthorizationSeparator() throws Exception {
        plainSaslServer.evaluateResponse(new byte[] { (byte) 'a', (byte) 'u', (byte) 't', (byte) 'h' });
    }

    @Test(expectedExceptions = SaslException.class,
          description = "Test evaluate response without username separator",
          expectedExceptionsMessageRegExp = ".*Invalid SASL/PLAIN response due to authcid null separator not found.*")
    public void testEvaluateResponseWithoutUsernameSeparator() throws Exception {
        plainSaslServer
                .evaluateResponse(new byte[] { (byte) 'a', (byte) 0, (byte) 'u', (byte) 's', (byte) 'e', (byte) 'r' });
    }

    @Test(description = "Test evaluate correct response")
    public void testEvaluateCorrectResponse() throws Exception {
        plainSaslServer.evaluateResponse(new byte[] {
                (byte) 'a', (byte) 0, (byte) 'u', (byte) 's', (byte) 'e', (byte) 'r', (byte) 0, (byte) 'p', (byte) 'a',
                (byte) 's', (byte) 's'
        });
        Assert.assertEquals(plainSaslServer.isComplete(), true, "Sasl handshake has not been completed.");
        Assert.assertEquals(plainSaslServer.getAuthorizationID(), "user", "Invalid Plain server evaluation");
    }

    @Test(description = "Test evaluate correct response with special characters")
    public void testEvaluateResponseWithSpecialCharacters() throws Exception {
        plainSaslServer.evaluateResponse(new byte[] {
                (byte) '(', (byte) 0, (byte) 'u', (byte) '#', (byte) '@', (byte) 'a', (byte) '.', (byte) 'c', (byte)
                'o', (byte) 'm', (byte) 0, (byte) 'P', (byte) '1', (byte) '@', (byte) '$', (byte) '&', (byte) '#'
        });
        Assert.assertEquals(plainSaslServer.isComplete(), true, "Sasl handshake has not been completed.");
        Assert.assertEquals(plainSaslServer.getAuthorizationID(), "u#@a.com", "Invalid Plain server evaluation");
    }

    @Test(expectedExceptions = SaslException.class,
          description = "Test evaluate invalid password",
          expectedExceptionsMessageRegExp = ".*Error while authenticating user with authenticator.*")
    public void testEvaluateInvalidPassword() throws Exception {
        plainSaslServer.evaluateResponse(new byte[] {
                (byte) 'a', (byte) 0, (byte) 'u', (byte) 's', (byte) 'e', (byte) 'r', (byte) 0, (byte) 'P', (byte) 'a',
                (byte) 's', (byte) 's'
        });
        Assert.assertEquals(plainSaslServer.isComplete(), false,
                            "Sasl mechanism should not be completed for invalid " + "password.");

    }

    @Test(expectedExceptions = SaslException.class,
          description = "Test evaluate invalid callback handler",
          expectedExceptionsMessageRegExp = ".*Error while authenticating user with authenticator.*")
    public void testInvalidCallbackHandler() throws Exception {
        final PlainSaslCallbackHandler handler = Mockito.mock(PlainSaslCallbackHandler.class);
        Mockito.doThrow(UnsupportedCallbackException.class).when(handler).handle(Mockito.any());
        plainSaslServer = new PlainSaslServer(authenticator);
        plainSaslServer.evaluateResponse(new byte[] {
                (byte) 'a', (byte) 0, (byte) 'u', (byte) 's', (byte) 'e', (byte) 'r', (byte) 0, (byte) 'P', (byte) 'a',
                (byte) 's', (byte) 's'
        });
        Assert.assertEquals(plainSaslServer.isComplete(), false,
                            "Sasl mechanism should not be completed for invalid password.");

    }
}
