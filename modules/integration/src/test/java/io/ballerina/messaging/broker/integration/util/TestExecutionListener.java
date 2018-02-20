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

package io.ballerina.messaging.broker.integration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestExecutionListener implements ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info(
                "Running the test method " + result.getTestClass().getName()
                        + "#" + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info(
                "Test successful " + result.getTestClass().getName() + "#" + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.info(
                "Test failed " + result.getTestClass().getName() + "#" + result.getMethod().getMethodName());

    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.info(
                "Test skipped " + result.getTestClass().getName() + "#" + result.getMethod().getMethodName());

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        LOGGER.info(
                "Test failed within success percentage " + result.getTestClass().getName()
                        + "#" + result.getMethod().getMethodName());

    }

    @Override
    public void onStart(ITestContext context) { }

    @Override
    public void onFinish(ITestContext context) { }
}
