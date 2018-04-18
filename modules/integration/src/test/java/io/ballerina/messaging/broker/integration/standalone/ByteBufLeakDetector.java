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
 */

package io.ballerina.messaging.broker.integration.standalone;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class to detect any memory leak reports in the log file.
 */
public class ByteBufLeakDetector {

    /**
     * Ideally this file should be empty. If any memory leaks found the stack traces from the leak detector will be
     * printed to this log file.
     */
    @Test
    public void checkMemoryLeakLogs() {
        String logFilePath = System.getProperty("testng.logs.directory");
        File file = new File(logFilePath);
        Assert.assertTrue(file.exists(), "Memory leak detector log file not found [" + logFilePath + "]");
        Assert.assertEquals(file.length(), 0, "Memory leaks detected! Check for error details in "
                + file.getAbsolutePath());
    }
}
