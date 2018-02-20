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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.messaging.broker.core.rest.BrokerAdminService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * HTTP Client Helper class.
 */
public class HttpClientHelper {

    public static String getRestApiBasePath(String brokerHost, String port) throws URISyntaxException {
        return "http://" + brokerHost + ":" + port + BrokerAdminService.API_BASE_PATH;
    }

    public static <T> T getResponseMessage(CloseableHttpResponse response, Class<T> responseType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String responsePayloadString = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(responsePayloadString, responseType);
    }


}
