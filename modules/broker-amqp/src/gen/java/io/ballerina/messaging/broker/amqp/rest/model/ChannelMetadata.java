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


import javax.validation.constraints.*;
import javax.validation.Valid;
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ChannelMetadata   {

    private @Valid Integer id = null;
    private @Valid Integer consumerCount = null;
    private @Valid Long createdTime = null;
    private @Valid Boolean isClosed = null;
    private @Valid Integer unackedMessageCount = null;
    private @Valid Boolean isFlowEnabled = null;
    private @Valid Integer deliveryPendingMessageCount = null;
    private @Valid String transactionType = null;
    private @Valid Integer prefetchCount = null;

    /**
     * Channel identifier
     **/
    public ChannelMetadata id(Integer id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(required = true, value = "Channel identifier")
    @JsonProperty("id")
    @NotNull
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Number of active consumers created on the channel
     **/
    public ChannelMetadata consumerCount(Integer consumerCount) {
        this.consumerCount = consumerCount;
        return this;
    }


    @ApiModelProperty(value = "Number of active consumers created on the channel")
    @JsonProperty("consumerCount")
    public Integer getConsumerCount() {
        return consumerCount;
    }
    public void setConsumerCount(Integer consumerCount) {
        this.consumerCount = consumerCount;
    }

    /**
     * The time at which the channel was created
     **/
    public ChannelMetadata createdTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }


    @ApiModelProperty(value = "The time at which the channel was created")
    @JsonProperty("createdTime")
    public Long getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * Is the channel closed
     **/
    public ChannelMetadata isClosed(Boolean isClosed) {
        this.isClosed = isClosed;
        return this;
    }


    @ApiModelProperty(value = "Is the channel closed")
    @JsonProperty("isClosed")
    public Boolean isIsClosed() {
        return isClosed;
    }
    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    /**
     * Unacknowledged message count
     **/
    public ChannelMetadata unackedMessageCount(Integer unackedMessageCount) {
        this.unackedMessageCount = unackedMessageCount;
        return this;
    }


    @ApiModelProperty(value = "Unacknowledged message count")
    @JsonProperty("unackedMessageCount")
    public Integer getUnackedMessageCount() {
        return unackedMessageCount;
    }
    public void setUnackedMessageCount(Integer unackedMessageCount) {
        this.unackedMessageCount = unackedMessageCount;
    }

    /**
     * Is flow enabled in the channel
     **/
    public ChannelMetadata isFlowEnabled(Boolean isFlowEnabled) {
        this.isFlowEnabled = isFlowEnabled;
        return this;
    }


    @ApiModelProperty(value = "Is flow enabled in the channel")
    @JsonProperty("isFlowEnabled")
    public Boolean isIsFlowEnabled() {
        return isFlowEnabled;
    }
    public void setIsFlowEnabled(Boolean isFlowEnabled) {
        this.isFlowEnabled = isFlowEnabled;
    }

    /**
     * Number of messages to be delivered
     **/
    public ChannelMetadata deliveryPendingMessageCount(Integer deliveryPendingMessageCount) {
        this.deliveryPendingMessageCount = deliveryPendingMessageCount;
        return this;
    }


    @ApiModelProperty(value = "Number of messages to be delivered")
    @JsonProperty("deliveryPendingMessageCount")
    public Integer getDeliveryPendingMessageCount() {
        return deliveryPendingMessageCount;
    }
    public void setDeliveryPendingMessageCount(Integer deliveryPendingMessageCount) {
        this.deliveryPendingMessageCount = deliveryPendingMessageCount;
    }

    /**
     * Underlying transaction type that is being used
     **/
    public ChannelMetadata transactionType(String transactionType) {
        this.transactionType = transactionType;
        return this;
    }


    @ApiModelProperty(value = "Underlying transaction type that is being used")
    @JsonProperty("transactionType")
    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * The number of messages that will be prefetched
     **/
    public ChannelMetadata prefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
        return this;
    }


    @ApiModelProperty(value = "The number of messages that will be prefetched")
    @JsonProperty("prefetchCount")
    public Integer getPrefetchCount() {
        return prefetchCount;
    }
    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelMetadata channelMetadata = (ChannelMetadata) o;
        return Objects.equals(id, channelMetadata.id) &&
               Objects.equals(consumerCount, channelMetadata.consumerCount) &&
               Objects.equals(createdTime, channelMetadata.createdTime) &&
               Objects.equals(isClosed, channelMetadata.isClosed) &&
               Objects.equals(unackedMessageCount, channelMetadata.unackedMessageCount) &&
               Objects.equals(isFlowEnabled, channelMetadata.isFlowEnabled) &&
               Objects.equals(deliveryPendingMessageCount, channelMetadata.deliveryPendingMessageCount) &&
               Objects.equals(transactionType, channelMetadata.transactionType) &&
               Objects.equals(prefetchCount, channelMetadata.prefetchCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, consumerCount, createdTime, isClosed, unackedMessageCount, isFlowEnabled, deliveryPendingMessageCount, transactionType, prefetchCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChannelMetadata {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    consumerCount: ").append(toIndentedString(consumerCount)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    isClosed: ").append(toIndentedString(isClosed)).append("\n");
        sb.append("    unackedMessageCount: ").append(toIndentedString(unackedMessageCount)).append("\n");
        sb.append("    isFlowEnabled: ").append(toIndentedString(isFlowEnabled)).append("\n");
        sb.append("    deliveryPendingMessageCount: ").append(toIndentedString(deliveryPendingMessageCount)).append("\n");
        sb.append("    transactionType: ").append(toIndentedString(transactionType)).append("\n");
        sb.append("    prefetchCount: ").append(toIndentedString(prefetchCount)).append("\n");
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
