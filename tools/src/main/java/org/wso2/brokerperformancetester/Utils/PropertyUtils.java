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

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Util class to get properties of the test plan
 */
public class PropertyUtils {

    private static final Logger log = Logger.getLogger(PropertyUtils.class);

    private String filePath;

    PropertyUtils(String filePath) {
        this.filePath = filePath;
    }

    public Properties get() {
        Properties props = null;
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            props = new Properties();
            props.load(fileInputStream);
            fileInputStream.close();
        } catch (Exception e) {
            log.error("Exception occured while getting propeties file to design test plan: " + e.getMessage());
        }
        return props;
    }
}
