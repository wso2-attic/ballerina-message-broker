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


public class BindingSetInfo   {
  
  private @Valid String bindingPattern = null;
  private @Valid List<BindingSetInfoBindings> bindings = new ArrayList<BindingSetInfoBindings>();

  /**
   * Binding pattern used for routing messages
   **/
  public BindingSetInfo bindingPattern(String bindingPattern) {
    this.bindingPattern = bindingPattern;
    return this;
  }

  
  @ApiModelProperty(value = "Binding pattern used for routing messages")
  @JsonProperty("bindingPattern")
  public String getBindingPattern() {
    return bindingPattern;
  }
  public void setBindingPattern(String bindingPattern) {
    this.bindingPattern = bindingPattern;
  }

  /**
   * Set of bindings for a given routing key
   **/
  public BindingSetInfo bindings(List<BindingSetInfoBindings> bindings) {
    this.bindings = bindings;
    return this;
  }

  
  @ApiModelProperty(value = "Set of bindings for a given routing key")
  @JsonProperty("bindings")
  public List<BindingSetInfoBindings> getBindings() {
    return bindings;
  }
  public void setBindings(List<BindingSetInfoBindings> bindings) {
    this.bindings = bindings;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BindingSetInfo bindingSetInfo = (BindingSetInfo) o;
    return Objects.equals(bindingPattern, bindingSetInfo.bindingPattern) &&
        Objects.equals(bindings, bindingSetInfo.bindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bindingPattern, bindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BindingSetInfo {\n");

    sb.append("    bindingPattern: ").append(toIndentedString(bindingPattern)).append("\n");
    sb.append("    bindings: ").append(toIndentedString(bindings)).append("\n");
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

