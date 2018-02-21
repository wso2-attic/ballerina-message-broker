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


public class BindingCreateRequest {
  
  private @Valid String bindingPattern = null;
  private @Valid String exchangeName = null;
  private @Valid String filterExpression = null;

  /**
   * Binding pattern that should routing keys
   **/
  public BindingCreateRequest bindingPattern(String bindingPattern) {
    this.bindingPattern = bindingPattern;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Binding pattern that should routing keys")
  @JsonProperty("bindingPattern")
  @NotNull
  public String getBindingPattern() {
    return bindingPattern;
  }
  public void setBindingPattern(String bindingPattern) {
    this.bindingPattern = bindingPattern;
  }

  /**
   * Name of the exchange to bind to
   **/
  public BindingCreateRequest exchangeName(String exchangeName) {
    this.exchangeName = exchangeName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Name of the exchange to bind to")
  @JsonProperty("exchangeName")
  @NotNull
  public String getExchangeName() {
    return exchangeName;
  }
  public void setExchangeName(String exchangeName) {
    this.exchangeName = exchangeName;
  }

  /**
   * JMS selector relater message filter pattern
   **/
  public BindingCreateRequest filterExpression(String filterExpression) {
    this.filterExpression = filterExpression;
    return this;
  }

  
  @ApiModelProperty(value = "JMS selector relater message filter pattern")
  @JsonProperty("filterExpression")
  public String getFilterExpression() {
    return filterExpression;
  }
  public void setFilterExpression(String filterExpression) {
    this.filterExpression = filterExpression;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BindingCreateRequest bindingCreateRequest = (BindingCreateRequest) o;
    return Objects.equals(bindingPattern, bindingCreateRequest.bindingPattern) &&
        Objects.equals(exchangeName, bindingCreateRequest.exchangeName) &&
        Objects.equals(filterExpression, bindingCreateRequest.filterExpression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bindingPattern, exchangeName, filterExpression);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BindingCreateRequest {\n");

    sb.append("    bindingPattern: ").append(toIndentedString(bindingPattern)).append("\n");
    sb.append("    exchangeName: ").append(toIndentedString(exchangeName)).append("\n");
    sb.append("    filterExpression: ").append(toIndentedString(filterExpression)).append("\n");
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

