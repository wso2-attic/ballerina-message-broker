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

import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Rest api client class to for testing purposes
 */
public class BrokerRestApiClient {

    private final String userName;

    private final String password;

    private final String apiBasePath;

    private final CloseableHttpClient httpClient;

    public BrokerRestApiClient(String userName, String password,
                               String restPort, String hostname) throws URISyntaxException {
        this.userName = userName;
        this.password = password;
        this.apiBasePath = HttpClientHelper.getRestApiBasePath(hostname, restPort);
        this.httpClient = HttpClients.createDefault();
    }

    public QueueMetadata getQueueMetadata(String queueName) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpGet, userName, password);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return HttpClientHelper.getResponseMessage(response, QueueMetadata.class);
    }

    public void close() throws IOException {
        httpClient.close();
    }
}
