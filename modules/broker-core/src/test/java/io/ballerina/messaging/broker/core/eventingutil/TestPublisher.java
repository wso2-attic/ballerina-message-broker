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

package io.ballerina.messaging.broker.core.eventingutil;

import io.ballerina.messaging.broker.common.EventSync;
import org.testng.Assert;

import java.util.Map;

public class TestPublisher implements EventSync {
    public String id;
    public Map<String, String> properties;

    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    void checkProperty(String propertyName, String value) {
        Assert.assertEquals(properties.get(propertyName), value);
    }

    void testEvent(Object[][] properties) {
        for (Object[] combination:properties) {
            String propertyName = combination[0].toString();
            String value = combination[1].toString();
            Assert.assertEquals(this.properties.get(propertyName), value);
        }
    }

    @Override
    public void publish(String id, Map<String, String> properties) {

        this.properties = properties;
        this.id = id;
    }
    @Override
    public void activate() {
        //No implementation
    }

    @Override
    public void deactivate() {
        //No implementation
    }

}
