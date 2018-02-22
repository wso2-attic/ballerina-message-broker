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

    private static ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private static PrintStream errPrintStream = new PrintStream(errStream);
    private static PrintStream originalErrorStream = System.err;

    private static ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private static PrintStream outPrintStream = new PrintStream(outStream);
    private static PrintStream originalOutStream = System.out;

    @BeforeGroups("StreamReading")
    public static void setPrintStream() {
        System.setErr(errPrintStream);
        System.setOut(outPrintStream);
    }

    /**
     * Clear the content of the streams. This needs to be done before each stream based test.
     *
     */
    static void resetStreams() {
        errStream.reset();
        outStream.reset();
    }

    /**
     * Read the content of the error stream.
     *
     * @return content of the stream as a {@link String}
     */
    static String readErrStream() {
        return errStream.toString();
    }

    /**
     * Read the content of the out stream.
     *
     * @return content of the stream as a {@link String}
     */
    static String readOutStream() {
        return outStream.toString();
    }

    @AfterGroups("StreamReading")
    public void closeStreams() throws IOException {
        System.setErr(originalErrorStream);
        System.setOut(originalOutStream);
        errPrintStream.close();
        outPrintStream.close();
    }
}
