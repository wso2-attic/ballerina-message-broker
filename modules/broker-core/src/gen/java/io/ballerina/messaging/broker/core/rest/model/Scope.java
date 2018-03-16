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


public class Scope   {
  
  private @Valid String name = null;
  private @Valid List<String> authoriedUserGroups = new ArrayList<String>();

  /**
   * Scope name
   **/
  public Scope name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Scope name")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * User groups of the scope
   **/
  public Scope authoriedUserGroups(List<String> authoriedUserGroups) {
    this.authoriedUserGroups = authoriedUserGroups;
    return this;
  }

  
  @ApiModelProperty(value = "User groups of the scope")
  @JsonProperty("authoriedUserGroups")
  public List<String> getAuthoriedUserGroups() {
    return authoriedUserGroups;
  }
  public void setAuthoriedUserGroups(List<String> authoriedUserGroups) {
    this.authoriedUserGroups = authoriedUserGroups;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Scope scope = (Scope) o;
    return Objects.equals(name, scope.name) &&
        Objects.equals(authoriedUserGroups, scope.authoriedUserGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, authoriedUserGroups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Scope {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    authoriedUserGroups: ").append(toIndentedString(authoriedUserGroups)).append("\n");
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

