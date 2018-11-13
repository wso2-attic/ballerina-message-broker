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
package io.ballerina.messaging.broker.client.resources;

/**
 * Representation of logger of log4j.
 */
public class Logger {

    public static final String NAME_TAG = "Logger Name";
    public static final String LEVEL_TAG = "Level";
    public static final String COLUMN_SEPERATOR_BEGIN = "| ";
    public static final String COLUMN_SEPERATOR_END = " |";
    public static final String CORNER_SIGN_BEGIN = "+ ";
    public static final String CORNER_SIGN_END = " +";
    public static final String BLOCK_SEPERATOR = "-";
    public static final String BLOCK_SEPERATOR_FOR_LEVEL = "---------------";

    private String name;
    private String level;

    public Logger(String name, String level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

}
