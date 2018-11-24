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
package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression. Here we compare two booleanexpressions and evaluate to a boolean value.
 * */


public class ANDOperation implements BooleanExpression {

    private  final BooleanExpression value;
    private final BooleanExpression value1;

    public ANDOperation (BooleanExpression value2 , BooleanExpression value3) {
        this.value = value2;
        this.value1 = value3;
    }

    @Override
    public boolean evaluate (Metadata metadata) {
      Object x = value.evaluate(metadata);
        Object y = value1.evaluate(metadata);
        ConvertAndCompare con = new ConvertAndCompare();
        boolean b = con.convertToBoolean(x);
        boolean b1 = con.convertToBoolean(y);


       if (b == true && b1 == true) {
           return true;
       }

        return false;
    }
}
