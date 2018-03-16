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

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


public class MessageDeleteResponse   {
  
  private @Valid Integer numberOfMessagesDeleted = null;

  /**
   * Response message with number of messages deleted.
   **/
  public MessageDeleteResponse numberOfMessagesDeleted(Integer numberOfMessagesDeleted) {
    this.numberOfMessagesDeleted = numberOfMessagesDeleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Response message with number of messages deleted.")
  @JsonProperty("numberOfMessagesDeleted")
  @NotNull
  public Integer getNumberOfMessagesDeleted() {
    return numberOfMessagesDeleted;
  }
  public void setNumberOfMessagesDeleted(Integer numberOfMessagesDeleted) {
    this.numberOfMessagesDeleted = numberOfMessagesDeleted;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessageDeleteResponse messageDeleteResponse = (MessageDeleteResponse) o;
    return Objects.equals(numberOfMessagesDeleted, messageDeleteResponse.numberOfMessagesDeleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numberOfMessagesDeleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessageDeleteResponse {\n");
    
    sb.append("    numberOfMessagesDeleted: ").append(toIndentedString(numberOfMessagesDeleted)).append("\n");
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

