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
package io.ballerina.messaging.broker.core.rest;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.core.rest.model.LoggerMetadata;
import io.ballerina.messaging.broker.core.rest.model.ResponseMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.security.auth.Subject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles Loggers REST API requests.
 */
public class LoggersApiDelegate {

    private AuthorizationHandler authHandler;

    public LoggersApiDelegate(Authorizer authorizer) {
        this.authHandler = new AuthorizationHandler(authorizer);
    }

    public Response updateLogger(Subject subject, LoggerMetadata requestLogger) {

        try {
            authHandler.handle(ResourceAuthScope.LOGGERS_UPDATE, subject);
            Logger logger = LogManager.exists(requestLogger.getName());
            String responseMessage;
            if (logger == null) {
                responseMessage = "Logger not found";
            } else if (!isLogLevelValid(requestLogger.getLevel())) {
                responseMessage = "'" + requestLogger.getLevel() + "' is not a valid log level.\n Valid log levels : " +
                                  Level.OFF.toString() + " , " + Level.TRACE.toString() + " , " +
                                  Level.DEBUG.toString() + " , " + Level.INFO.toString() + " , " +
                                  Level.WARN.toString() + " , " + Level.ERROR.toString() + " , " +
                                  Level.FATAL.toString();
            } else {
                String oldLevel = logger.getEffectiveLevel().toString();
                logger.setLevel(Level.toLevel(requestLogger.getLevel()));
                responseMessage = "Logger : " + requestLogger.getName() + "\nChanged log level : " + oldLevel + " -> " +
                                  logger
                                          .getEffectiveLevel().toString();
            }
            return Response.ok().entity(new ResponseMessage().message(responseMessage)).build();
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }

    }

    public Response getLoggers(Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.LOGGERS_GET, subject);
            List<LoggerMetadata> loggerArray = new ArrayList<>();
            Enumeration loggers = LogManager.getCurrentLoggers();
            while (loggers.hasMoreElements()) {
                loggerArray.add(toLoggerMetadata((Logger) loggers.nextElement()));
            }
            return Response.ok().entity(loggerArray).build();
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    /**
     * This methods checks whether the given string represents a valid log level.
     *
     * @param logLevel the string that needs to be validated
     * @return validity of the string
     */
    private boolean isLogLevelValid(String logLevel) {
        // when the given log level is not a valid one, it is set to the default log level (DEBUG)
        if (!Level.DEBUG.toString().equals(logLevel) &&
            Level.toLevel(logLevel).toString().equals(Level.DEBUG.toString())) {
            return false;
        }
        return true;
    }

    /**
     * This method returns a LoggerMetaData object containing details of the given Log4j logger object.
     *
     * @param logger log4j logger
     * @return LoggerMetaData object
     */
    private LoggerMetadata toLoggerMetadata(Logger logger) {
        LoggerMetadata loggerMetadata = new LoggerMetadata();
        loggerMetadata.name(logger.getName()).level(logger.getEffectiveLevel().toString());
        return loggerMetadata;
    }

}
