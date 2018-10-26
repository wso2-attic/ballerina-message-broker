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

package io.ballerina.messaging.broker.eventing;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Represents event configuration for broker.
 */

@Configuration(
        namespace = "ballerina.broker.events",
        description = "Events Configuration Parameters"
)
public class EventConfiguration {


    @Element(
            description = "Enable Events"
    )
    private boolean enabled = true;


    @Element(
            description = "Enable publisher"
    )
    private String publisher = "brokerpublisher";


    public String getPublisher() {
        return publisher;
    }

    public boolean isEnabled() {

        return this.enabled;
    }


    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }
}
