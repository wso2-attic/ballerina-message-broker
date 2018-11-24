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
 * Implementation of a boolean expression. Here we get the boolean value and convert it to a opposite boolean value .
 */

public class NOTOperation implements BooleanExpression {

    private  final BooleanExpression value;

    public NOTOperation (BooleanExpression value) {
        this.value = value;
    }

    @Override
    public boolean evaluate (Metadata metadata) {

        Object x = value.evaluate(metadata);

        ConvertAndCompare con = new ConvertAndCompare();
        boolean b = con.convertToBoolean(x);

        if (b == true) {
            return false;
        }
        return true;
    }
}
