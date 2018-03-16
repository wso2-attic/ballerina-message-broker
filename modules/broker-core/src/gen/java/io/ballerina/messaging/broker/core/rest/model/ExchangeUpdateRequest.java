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


public class ExchangeUpdateRequest   {
  
  private @Valid String owner = null;
  private @Valid List<ActionUserGroupsMapping> authorizedUserGroups = new ArrayList<ActionUserGroupsMapping>();

  /**
   * Owner of exchange
   **/
  public ExchangeUpdateRequest owner(String owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(value = "Owner of exchange")
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Exchange action User groups mappings
   **/
  public ExchangeUpdateRequest authorizedUserGroups(List<ActionUserGroupsMapping> authorizedUserGroups) {
    this.authorizedUserGroups = authorizedUserGroups;
    return this;
  }

  
  @ApiModelProperty(value = "Exchange action User groups mappings")
  @JsonProperty("authorizedUserGroups")
  public List<ActionUserGroupsMapping> getAuthorizedUserGroups() {
    return authorizedUserGroups;
  }
  public void setAuthorizedUserGroups(List<ActionUserGroupsMapping> authorizedUserGroups) {
    this.authorizedUserGroups = authorizedUserGroups;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeUpdateRequest exchangeUpdateRequest = (ExchangeUpdateRequest) o;
    return Objects.equals(owner, exchangeUpdateRequest.owner) &&
        Objects.equals(authorizedUserGroups, exchangeUpdateRequest.authorizedUserGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, authorizedUserGroups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExchangeUpdateRequest {\n");
    
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    authorizedUserGroups: ").append(toIndentedString(authorizedUserGroups)).append("\n");
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

