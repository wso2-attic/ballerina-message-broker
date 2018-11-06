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

package io.ballerina.messaging.broker.common;

/**
 * Contains commonly used EventNames.
 */
public final class EventConstants {

    /**
     * ID to identify Consumer Added Event.
     */
    public static final int CONSUMER_ADDED_EVENT = 1;
    /**
     * ID to identify Message Published Event.
     */
    public static final int MESSAGE_PUBLISHED_EVENT = 2;
    /**
     * ID to identify Queue Created Event.
     */
    public static final int QUEUE_CREATED = 3;
    /**
     * ID to identify Binding Created Event.
     */
    public static final int BINDING_CREATED = 4;
}
