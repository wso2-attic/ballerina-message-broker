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
package io.ballerina.messaging.broker.integration.standalone.cli;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *  Class holing static instance of the print stream.
 */
public class PrintStreamHandler {

    private static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static PrintStream printStream = new PrintStream(outputStream);
    private static PrintStream originalErrorStream = System.err;

    @BeforeGroups("StreamReading")
    public static void setPrintStream() {
        System.setErr(printStream);
    }

    /**
     * Clear the content of the stream. This needs to be done before each stream based test.
     *
     */
    static void resetStream() {
        outputStream.reset();
    }

    /**
     * Read the content of the stream.
     *
     * @return content of the stream as a {@link String}
     */
    static String readStream() {
        return outputStream.toString();
    }

    @AfterGroups("StreamReading")
    public void closeStreams() throws IOException {
        System.setErr(originalErrorStream);
        printStream.close();
    }
}
