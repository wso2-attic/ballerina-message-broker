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

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Exchange;
import io.ballerina.messaging.broker.core.rest.model.ExchangeCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.ExchangeCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.ExchangeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles /exchanges api requests.
 */
public class ExchangesApiDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangesApiDelegate.class);

    public static final String EXCHANGES_API_PATH = "/exchanges";

    private final Broker broker;

    public ExchangesApiDelegate(Broker broker) {
        this.broker = broker;
    }

    public Response createExchange(ExchangeCreateRequest requestBody) {
        try {
            broker.createExchange(requestBody.getName(), requestBody.getType(), requestBody.isDurable());
            // TODO: exchange create message response type
            return Response.created(new URI(BrokerAdminService.API_BASE_PATH + EXCHANGES_API_PATH
                                                    + "/" + requestBody.getName()))
                           .entity(new ExchangeCreateResponse().message("Exchange created.")).build();


        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            String message = "Invalid URI syntax for the location header.";
            LOGGER.error(message, e);
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (BrokerException e) {
            String message = "Error occurred while creating exchange.";
            LOGGER.error(message, e);
            throw new InternalServerErrorException(message, e);
        }
    }

    public Response deleteExchange(String exchangeName, boolean ifUnused) {
        try {
            boolean deleted = broker.deleteExchange(exchangeName, ifUnused);
            if (!deleted) {
                throw new NotFoundException("Exchange " + exchangeName + " not found.");
            }
            return Response.ok().build();
        } catch (BrokerException e) {
            String message = "Error occurred while deleting exchange " + exchangeName + ".";
            LOGGER.error(message, e);
            throw new InternalServerErrorException(message, e);
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    public Response getAllExchanges() {
        Collection<Exchange> exchangeList = broker.getAllExchanges();
        List<ExchangeMetadata> exchangeMetadataList = new ArrayList<>(exchangeList.size());
        for (Exchange exchange : exchangeList) {
            exchangeMetadataList.add(new ExchangeMetadata().name(exchange.getName())
                                                           .type(exchange.getType().toString())
                                                           .durable(exchange.isDurable()));
        }
        return Response.ok().entity(exchangeMetadataList).build();
    }

    public Response getExchange(String exchangeName) {

        Exchange exchange = broker.getExchange(exchangeName);
        if (Objects.isNull(exchange)) {
            throw new NotFoundException("Exchange '" + exchangeName + "' not found.");
        }
        ExchangeMetadata exchangeMetadata = new ExchangeMetadata().name(exchange.getName())
                                                                  .type(exchange.getType().toString())
                                                                  .durable(exchange.isDurable());
        return Response.ok().entity(exchangeMetadata).build();

    }
}
