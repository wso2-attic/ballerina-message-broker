/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.amqp.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Representation of an AMQP connection.
 */
public class ConnectionMetadata   {

    private @Valid Integer id = null;
    private @Valid String remoteAddress = null;
    private @Valid Integer channelCount = null;
    private @Valid Long connectedTime = null;

    /**
     * Connection identifier
     **/
    public ConnectionMetadata id(Integer id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(required = true, value = "Connection identifier")
    @JsonProperty("id")
    @NotNull
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The ip address of the connected client. This could be the same for multiple connections.
     **/
    public ConnectionMetadata remoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }


    @ApiModelProperty(required = true, value = "The ip address of the connected client. This could be the same for multiple connections.")
    @JsonProperty("remoteAddress")
    @NotNull
    public String getRemoteAddress() {
        return remoteAddress;
    }
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Number of active channels registered for each connection
     **/
    public ConnectionMetadata channelCount(Integer channelCount) {
        this.channelCount = channelCount;
        return this;
    }


    @ApiModelProperty(required = true, value = "Number of active channels registered for each connection")
    @JsonProperty("channelCount")
    @NotNull
    public Integer getChannelCount() {
        return channelCount;
    }
    public void setChannelCount(Integer channelCount) {
        this.channelCount = channelCount;
    }

    /**
     * The time at which the connection was established
     **/
    public ConnectionMetadata connectedTime(Long connectedTime) {
        this.connectedTime = connectedTime;
        return this;
    }


    @ApiModelProperty(required = true, value = "The time at which the connection was established")
    @JsonProperty("connectedTime")
    @NotNull
    public Long getConnectedTime() {
        return connectedTime;
    }
    public void setConnectedTime(Long connectedTime) {
        this.connectedTime = connectedTime;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionMetadata connectionMetadata = (ConnectionMetadata) o;
        return Objects.equals(id, connectionMetadata.id) &&
               Objects.equals(remoteAddress, connectionMetadata.remoteAddress) &&
               Objects.equals(channelCount, connectionMetadata.channelCount) &&
               Objects.equals(connectedTime, connectionMetadata.connectedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, remoteAddress, channelCount, connectedTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConnectionMetadata {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    remoteAddress: ").append(toIndentedString(remoteAddress)).append("\n");
        sb.append("    channelCount: ").append(toIndentedString(channelCount)).append("\n");
        sb.append("    connectedTime: ").append(toIndentedString(connectedTime)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
