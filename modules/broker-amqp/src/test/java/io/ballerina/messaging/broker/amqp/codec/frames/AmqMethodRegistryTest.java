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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.amqp.codec.AmqFrameDecodingException;
import io.ballerina.messaging.broker.amqp.codec.auth.AuthenticationStrategy;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AmqMethodRegistryTest {

    AuthenticationStrategy authenticationStrategy;

    @BeforeTest
    public void init() {
        authenticationStrategy = Mockito.mock(AuthenticationStrategy.class);
    }
    @Test(expectedExceptions = AmqFrameDecodingException.class)
    public void testInvalidMethodValue() throws Exception {
        AmqMethodBodyFactory factory =
                new AmqMethodRegistry(authenticationStrategy).getFactory((short) 12, (short) 12);
    }

    @Test(expectedExceptions = AmqFrameDecodingException.class)
    public void testLargeMethodValue() throws Exception {
        AmqMethodBodyFactory factory =
                new AmqMethodRegistry(authenticationStrategy).getFactory((short) 10, (short) 2000);
    }

    @Test(expectedExceptions = AmqFrameDecodingException.class)
    public void testLargeClassValue() throws Exception {
        AmqMethodBodyFactory factory =
                new AmqMethodRegistry(authenticationStrategy).getFactory((short) 2000, (short) 10);
    }

    @Test(expectedExceptions = AmqFrameDecodingException.class)
    public void testInvalidMethodValueInValidClass() throws Exception {
        AmqMethodBodyFactory factory = new AmqMethodRegistry(authenticationStrategy).getFactory((short) 60, (short) 5);
    }

}
