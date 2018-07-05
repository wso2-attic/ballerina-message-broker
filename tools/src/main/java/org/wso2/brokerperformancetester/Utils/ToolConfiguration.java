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

package org.wso2.brokerperformancetester.Utils;

import java.util.Properties;

/**
 * Configuration needed to execute the file
 */
public class ToolConfiguration {

    private String jndiPropertyPath = null;
    private int loopCount = 10000;
    private int threadCount = 1;
    private int rampTime = 0;
    private String jmeterHome = "";

    public ToolConfiguration(String folderPath) {
        Properties properties = new PropertyUtils(folderPath).get();
        if (properties.getProperty(Constants.JNDI_PROPERTY_FILE_LOCATION) != null) {
            this.jndiPropertyPath = properties.getProperty(Constants.JNDI_PROPERTY_FILE_LOCATION);
        }
        if (properties.getProperty(Constants.LOOP_COUNT) != null) {
            this.loopCount = Integer.parseInt(properties.getProperty(Constants.LOOP_COUNT));
        }
        if (properties.getProperty(Constants.THREAD_COUNT) != null) {
            this.threadCount = Integer.parseInt(properties.getProperty(Constants.THREAD_COUNT));
        }
        if (properties.getProperty(Constants.RAMP_TIME) != null) {
            this.rampTime = Integer.parseInt(properties.getProperty(Constants.RAMP_TIME));
        }
        if (properties.getProperty(Constants.JMETER_HOME) != null) {
            this.jmeterHome = properties.getProperty(Constants.JMETER_HOME);
        }
    }

    public String getJndiPropertyPath() {
        return jndiPropertyPath;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getRampTime() {
        return rampTime;
    }

    public String getJmeterHome() {
        return jmeterHome;
    }
}
