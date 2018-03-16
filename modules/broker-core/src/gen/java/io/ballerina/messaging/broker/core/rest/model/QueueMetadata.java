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

package io.ballerina.messaging.broker.core.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


public class QueueMetadata   {
  
  private @Valid String name = null;
  private @Valid Integer consumerCount = null;
  private @Valid Boolean durable = null;
  private @Valid Integer capacity = null;
  private @Valid Integer size = null;
  private @Valid Boolean autoDelete = null;
  private @Valid String owner = null;
  private @Valid List<ActionUserGroupsMapping> permissions = new ArrayList<ActionUserGroupsMapping>();

  /**
   * Name of the queue. This is a unique value
   **/
  public QueueMetadata name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Name of the queue. This is a unique value")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * number of active consumers assigned to the queue. Messages will be dispatched in a round robing manner to the consumers.
   **/
  public QueueMetadata consumerCount(Integer consumerCount) {
    this.consumerCount = consumerCount;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "number of active consumers assigned to the queue. Messages will be dispatched in a round robing manner to the consumers.")
  @JsonProperty("consumerCount")
  @NotNull
  public Integer getConsumerCount() {
    return consumerCount;
  }
  public void setConsumerCount(Integer consumerCount) {
    this.consumerCount = consumerCount;
  }

  /**
   * durability of the queue. True if the queue is durable and false otherwise. Durable queues will survive node failures.
   **/
  public QueueMetadata durable(Boolean durable) {
    this.durable = durable;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "durability of the queue. True if the queue is durable and false otherwise. Durable queues will survive node failures.")
  @JsonProperty("durable")
  @NotNull
  public Boolean isDurable() {
    return durable;
  }
  public void setDurable(Boolean durable) {
    this.durable = durable;
  }

  /**
   * Maximum number of messages the queue can hold. Durable queues are unbounded and will specify the integer max value (2,147,483,647) as the capacity.
   **/
  public QueueMetadata capacity(Integer capacity) {
    this.capacity = capacity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Maximum number of messages the queue can hold. Durable queues are unbounded and will specify the integer max value (2,147,483,647) as the capacity.")
  @JsonProperty("capacity")
  @NotNull
  public Integer getCapacity() {
    return capacity;
  }
  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  /**
   * Number of messages in the queue.
   **/
  public QueueMetadata size(Integer size) {
    this.size = size;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Number of messages in the queue.")
  @JsonProperty("size")
  @NotNull
  public Integer getSize() {
    return size;
  }
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * If set to true queue will get deleted once all the consumers of the queue get disconnected.
   **/
  public QueueMetadata autoDelete(Boolean autoDelete) {
    this.autoDelete = autoDelete;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "If set to true queue will get deleted once all the consumers of the queue get disconnected.")
  @JsonProperty("autoDelete")
  @NotNull
  public Boolean isAutoDelete() {
    return autoDelete;
  }
  public void setAutoDelete(Boolean autoDelete) {
    this.autoDelete = autoDelete;
  }

  /**
   * Queue owner
   **/
  public QueueMetadata owner(String owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(value = "Queue owner")
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Queue action User groups permissions
   **/
  public QueueMetadata permissions(List<ActionUserGroupsMapping> permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "Queue action User groups permissions")
  @JsonProperty("permissions")
  public List<ActionUserGroupsMapping> getPermissions() {
    return permissions;
  }
  public void setPermissions(List<ActionUserGroupsMapping> permissions) {
    this.permissions = permissions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueueMetadata queueMetadata = (QueueMetadata) o;
    return Objects.equals(name, queueMetadata.name) &&
        Objects.equals(consumerCount, queueMetadata.consumerCount) &&
        Objects.equals(durable, queueMetadata.durable) &&
        Objects.equals(capacity, queueMetadata.capacity) &&
        Objects.equals(size, queueMetadata.size) &&
        Objects.equals(autoDelete, queueMetadata.autoDelete) &&
        Objects.equals(owner, queueMetadata.owner) &&
        Objects.equals(permissions, queueMetadata.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, consumerCount, durable, capacity, size, autoDelete, owner, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueueMetadata {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    consumerCount: ").append(toIndentedString(consumerCount)).append("\n");
    sb.append("    durable: ").append(toIndentedString(durable)).append("\n");
    sb.append("    capacity: ").append(toIndentedString(capacity)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    autoDelete: ").append(toIndentedString(autoDelete)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

