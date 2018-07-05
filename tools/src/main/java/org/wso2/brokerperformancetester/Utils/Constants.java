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

/**
 * Constants need to execute the Application
 */
public class Constants {

    // x-paths
    public static final String XPATH_THREAD_GROUP = "/jmeterTestPlan/hashTree/hashTree/ThreadGroup";
    public static final String XPATH_PUBLISHER_SAMPLER = "/jmeterTestPlan/hashTree/hashTree/hashTree/PublisherSampler";

    // file path
    public static final String FILE_PATH = "JmsPublisher.jmx";

    // properties file constants
    public static final String JNDI_PROPERTY_FILE_LOCATION = "jndi_file";
    public static final String LOOP_COUNT = "loop_count";
    public static final String THREAD_COUNT = "thread_count";
    public static final String RAMP_TIME = "ramp_time";
    public static final String JMETER_HOME = "jmeter_home";
    public static final String MESSAGE = "message";

    // xml tag constants
    public static final String XML_STRING_PROP = "stringProp";
    public static final String XML_ELEMENT_PROP = "elementProp";
    public static final String XML_NAME = "name";
    public static final String XML_JMS_PROVIDER_URL = "jms.provider_url";
    public static final String XML_NO_OF_THREADS = "ThreadGroup.num_threads";
    public static final String XML_RAMP_TIME = "ThreadGroup.ramp_time";

}
