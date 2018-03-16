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

package io.ballerina.messaging.broker.core.rest;

import com.google.common.base.Strings;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.core.Binding;
import io.ballerina.messaging.broker.core.BindingSet;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.BindingSetInfo;
import io.ballerina.messaging.broker.core.rest.model.BindingSetInfoBindings;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;

/**
 * Delegate class to handle {@link Binding} related REST requests.
 */
public class BindingsApiDelegate {

    private final BrokerFactory brokerFactory;

    public BindingsApiDelegate(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    public Response createBinding(String queueName, BindingCreateRequest requestBody, Subject subject) {
        FieldTable fieldTable = new FieldTable();
        String filter = requestBody.getFilterExpression();
        if (Objects.nonNull(filter)) {
            fieldTable.add(Binding.JMS_SELECTOR_ARGUMENT, FieldValue.parseLongString(filter));
        }
        if (Objects.isNull(requestBody.getBindingPattern()) || Objects.isNull(requestBody.getExchangeName())) {
            throw new BadRequestException("Exchange name and the binding pattern should be set");
        }

        try {
            brokerFactory.getBroker(subject).bind(queueName, requestBody.getExchangeName(),
                    requestBody.getBindingPattern(), fieldTable);
            BindingCreateResponse responsePayload = new BindingCreateResponse().message("Binding created.");
            return Response.created(generateLocationHeader(queueName, requestBody))
                           .entity(responsePayload)
                           .build();
        } catch (BrokerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new InternalServerErrorException("Error occurred while creating location header for the "
                                                           + "created binding.", e);
        }
    }

    private URI generateLocationHeader(String queueName, BindingCreateRequest requestBody)
            throws URISyntaxException, UnsupportedEncodingException {
        StringBuilder locationBuilder = new StringBuilder();
        locationBuilder.append(BrokerAdminService.API_BASE_PATH).append("/").append(queueName)
                       .append("/bindings/").append(requestBody.getBindingPattern());

        if (!Strings.isNullOrEmpty(requestBody.getFilterExpression())) {
            locationBuilder.append("?filterExpression=")
                           .append(URLEncoder.encode(requestBody.getFilterExpression(),
                                                     StandardCharsets.UTF_8.toString()));
        }
        return new URI(locationBuilder.toString());
    }

    public Response getAllBindingsForExchange(String exchangeName, Subject subject) {
        try {
            Map<String, BindingSet> bindingSets =
                    brokerFactory.getBroker(subject).getAllBindingsForExchange(exchangeName);
            List<BindingSetInfo> responsePayload = new ArrayList<>();
            for (Map.Entry<String, BindingSet> entry : bindingSets.entrySet()) {
                BindingSetInfo responseBindingSet = new BindingSetInfo().bindingPattern(entry.getKey());
                BindingSet bindingSet = entry.getValue();
                ArrayList<BindingSetInfoBindings> responseBindingList = new ArrayList<>();

                addUnfilteredBindings(bindingSet, responseBindingList);
                addFilteredBindings(bindingSet, responseBindingList);
                responseBindingSet.bindings(responseBindingList);
                responsePayload.add(responseBindingSet);
            }
            return Response.ok().entity(responsePayload).build();
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }

    }

    private void addUnfilteredBindings(BindingSet bindingSet, ArrayList<BindingSetInfoBindings> responseBindingList) {
        for (Binding binding : bindingSet.getUnfilteredBindings()) {
            BindingSetInfoBindings responseBinding =
                    new BindingSetInfoBindings().queueName(binding.getQueue().getName());
            responseBindingList.add(responseBinding);
        }
    }

    private void addFilteredBindings(BindingSet bindingSet, ArrayList<BindingSetInfoBindings> responseBindingList) {
        for (Binding binding : bindingSet.getFilteredBindings()) {
            String filter = binding.getArgument(Binding.JMS_SELECTOR_ARGUMENT).getValue().toString();
            BindingSetInfoBindings bindingInfo =
                    new BindingSetInfoBindings().queueName(binding.getQueue().getName())
                                                .filterExpression(filter);
            responseBindingList.add(bindingInfo);
        }
    }

    public Response deleteBinding(String queueName, String bindingPattern, String filterExpression, Subject subject) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    public Response getBinding(String queueName, String bindingPattern, String filterExpression, Subject subject) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
