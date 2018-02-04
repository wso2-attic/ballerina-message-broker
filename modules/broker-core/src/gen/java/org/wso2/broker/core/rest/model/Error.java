package org.wso2.broker.core.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


public class Error   {
  
  private @Valid Long code = null;
  private @Valid String message = null;

  /**
   **/
  public Error code(Long code) {
    this.code = code;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("code")
  @NotNull
  public Long getCode() {
    return code;
  }
  public void setCode(Long code) {
    this.code = code;
  }

  /**
   * Error message.
   **/
  public Error message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Error message.")
  @JsonProperty("message")
  @NotNull
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(code, error.code) &&
        Objects.equals(message, error.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

