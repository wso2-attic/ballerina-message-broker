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

package io.ballerina.messaging.broker.core.selector.generated;

import io.ballerina.messaging.broker.core.selector.BooleanExpression;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * {@link MessageFilter} parsing and expression evaluation tests
 */
public class MessageFilterTestArithmeticoperators {

    @Test (dataProvider = "positive-filter-strings", description = "Test parsing correct filter strings")
    private void testPositiveFilterStringParsing(String filterString) throws Exception {
        MessageFilter filter = new MessageFilter(filterString);
        BooleanExpression expression = filter.parse();
        Assert.assertNotNull(expression, "Expression shouldn't be null for " + filterString);
    }

    @Test (dataProvider = "negative-filter-strings", description = "Test parsing incorrect filter strings",
            expectedExceptions = {ParseException.class, TokenMgrError.class})
    private void testNegativeFilterStringParsing(String filterString) throws Exception {
        MessageFilter filter = new MessageFilter(filterString);
        filter.parse();
    }

    @DataProvider(name = "positive-filter-strings")
    public Object[] positiveFilterStrings() {
        return new String[] {
                // unary operations
                "MyProperty + -1 = -5",
                "-MyProperty * 8 = 23.4",
                "-(MyProperty + 1*6) = 345",
                "MyProperty + -34  < 4455",
                "MyProperty + (-300)  =< 44.5",
                "MyProperty * (+3.56)  >= 4",
                "MyProperty + -(234 + (+45) - (-4))  <> 4455",
                //multiplication ,division , addition and subtraction
                "MyProperty + 1 = 222",
                "MyProperty - 10 = 0",
                "MyProperty * 2 <> 88",
                "MyProperty / 5 = 89",
                "MyProperty + (1+1) = 6.6",
                "(MyProperty +1) * 2.6 > 765",
                "MyProperty * (20+100) = 500",
                "(MyProperty*8.1) =< 20.2",
                "(MyProperty/8) + 12 = 67",
                "MyProperty * (78/3) >= 675.90",
                "(MyProperty + (34*6) +1) + 3  =  3000",
                "MyProperty - +345 = 45.90",
                "-MyProperty + (45.9+ 345) = 567",
                "Age * 3 * 9 / 5 <> 10.2",
                // values with Exponent
                "Age + 344 =< 40E0",
                "Age + 67.E10 >= 45L",
                "Age * (4E5 + 67.E10) >= 45L",

        };
    }

    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "",
                // invalid arithmetic operations
                "MyProperty ++ 2 = 'abcde'f'",
                "MyProperty+34+ = 8",

                "MyProperty = 3.E",
                "MyProperty = 3.EE",
                "Property ++ = 89",
                "--Property >= Age",
                "Property +  =< 8",
               "myProperty *()() = 123LL",
                "myProperty + () 5= 123ll",
                "myProperty+(5)(345) = 123lLl",
        };
    }
}
