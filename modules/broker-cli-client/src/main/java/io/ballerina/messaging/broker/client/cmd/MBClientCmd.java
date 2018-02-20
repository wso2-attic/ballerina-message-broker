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
package io.ballerina.messaging.broker.client.cmd;

/**
 * Interface to represent API for any command to be declared.
 */
public interface MBClientCmd {

    /**
     * Execution logic of the command.
     */
    void execute();


    /**
     * Append long description of this command to the passed string builder.
     *
     * @param out StringBuilder instance, which messages should be appended to.
     */
    void printLongDesc(StringBuilder out);

    /**
     * Append usage description of this command to the passed string builder.
     *
     * @param out StringBuilder instance, which messages should be appended to.
     */
    void printUsage(StringBuilder out);
}
