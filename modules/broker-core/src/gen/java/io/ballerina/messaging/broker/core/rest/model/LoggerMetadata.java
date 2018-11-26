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


public class LoggerMetadata {

    private @Valid
    String name = null;
    private @Valid
    String level = null;

    /**
     * Name of the logger. This is a unique value
     **/
    public LoggerMetadata name(String name) {
        this.name = name;
        return this;
    }


    @ApiModelProperty(required = true, value = "Name of the logger. This is a unique value")
    @JsonProperty("name")
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Log level of the logger
     **/
    public LoggerMetadata level(String level) {
        this.level = level;
        return this;
    }


    @ApiModelProperty(required = true, value = "Current log level of the logger")
    @JsonProperty("level")
    @NotNull
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoggerMetadata loggerMetadata = (LoggerMetadata) o;
        return Objects.equals(name, loggerMetadata.name) &&
               Objects.equals(level, loggerMetadata.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, level);
    }

    @Override
    public String toString() {
        return "class LoggerMetadata {\n"
               + "    name: " + toIndentedString(name) + "\n"
               + "    level: " + toIndentedString(level) + "\n"
               + "}";
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
