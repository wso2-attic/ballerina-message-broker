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

package io.ballerina.messaging.broker.core;

import java.util.UUID;

/**
 * Unique message id generator class.
 */
public class UniqueIdGenerator {

    private static final long REFERENCE_START = 41L * 365L * 24L * 60L * 60L * 1000L; //this is 2011

    private final int instanceId = UUID.randomUUID().hashCode();

    private long previousTimestamp;

    private long previousId;

    private int offset;

    public synchronized long getNextId() {
        long ts = System.currentTimeMillis();
        if (ts == previousTimestamp) {
            offset++;
        } else {
            offset = 0;
        }
        previousTimestamp = ts;
        long id = (ts - REFERENCE_START) * 256L * 1024L + instanceId * 1024L + offset;
        if (previousId == id) {
            throw new RuntimeException("Duplicate ids detected. This should never happen");
        }
        previousId = id;
        return id;
    }
}
