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


public class ExchangeMetadata   {
  
  private @Valid String name = null;
  private @Valid String type = null;
  private @Valid Boolean durable = null;

  /**
   * Name of the exchange
   **/
  public ExchangeMetadata name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the exchange")
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  /**
   * Type of exchange.  for instance amq.direct, amq.topic
   **/
  public ExchangeMetadata type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Type of exchange.  for instance amq.direct, amq.topic")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * True if the exchange is durable and false otherwise
   **/
  public ExchangeMetadata durable(Boolean durable) {
    this.durable = durable;
    return this;
  }

  
  @ApiModelProperty(value = "True if the exchange is durable and false otherwise")
  @JsonProperty("durable")
  public Boolean isDurable() {
    return durable;
  }
  public void setDurable(Boolean durable) {
    this.durable = durable;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeMetadata exchangeMetadata = (ExchangeMetadata) o;
    return Objects.equals(name, exchangeMetadata.name) &&
        Objects.equals(type, exchangeMetadata.type) &&
        Objects.equals(durable, exchangeMetadata.durable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, durable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExchangeMetadata {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    durable: ").append(toIndentedString(durable)).append("\n");
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

