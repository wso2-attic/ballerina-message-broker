package io.ballerina.messaging.broker.core.rest.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class UserGroupList   {
  
  private @Valid List<String> userGroups = new ArrayList<String>();

  /**
   * Set of user groups.
   **/
  public UserGroupList userGroups(List<String> userGroups) {
    this.userGroups = userGroups;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Set of user groups.")
  @JsonProperty("userGroups")
  @NotNull
  public List<String> getUserGroups() {
    return userGroups;
  }
  public void setUserGroups(List<String> userGroups) {
    this.userGroups = userGroups;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGroupList userGroupList = (UserGroupList) o;
    return Objects.equals(userGroups, userGroupList.userGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userGroups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserGroupList {\n");
    
    sb.append("    userGroups: ").append(toIndentedString(userGroups)).append("\n");
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

