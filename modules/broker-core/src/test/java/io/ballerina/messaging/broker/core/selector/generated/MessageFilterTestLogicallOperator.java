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
public class MessageFilterTestLogicallOperator {

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
                // valid NOT logical operator values
                " NOT MyProperty = 'abcdef'",
                " NOT MyProperty = 'abcde''f'",
                " NOT MyProperty > 5647",
                " NOT MyProperty+1 =< 567",
                " NOT (MyProperty *6) + 34.5 = 679.89",
                "NOT MyProperty = 3.E910",
                "NOT MyProperty LIKE 'abcd%'",
                "NOT MyProperty BETWEEN 10 AND 20",

                // valid AND logical operator values
                "MyProperty =< 12 AND MyProperty >= 3",
                "MyProperty ='etrts' AND MyProperty = 3",
                "MyProperty = 12.4E7 AND MyProperty = .3",
                "MyProperty LIKE 'abcd%' AND MyProperty = 3",
                "MyProperty LIKE 'abcd%' AND MyProperty LIKE '%rtyuu'",
                "MyProperty BETWEEN 10 AND 20 AND MyProperty LIKE '12_'",
                "MyProperty IS NULL AND NOT MyProperty LIKE 'abcd%'",
                "MyProperty IN ('ab' , 'tw' , 'r') AND Age NOT IN ( '2' , '7' , '10')",
                "(MyProperty *6) + 34.5 = 679.89 AND (MyProperty / 12) * 34.5 = 679.8E9",

                // valid OR logical operator values
                "MyProperty <> 6 OR MyProperty < 90",
                "MyProperty ='etrts' OR MyProperty =< 3",
                "MyProperty LIKE 'abcd%' OR MyProperty = 3",
                "MyProperty LIKE '\\_%' ESCAPE '\\' OR MyProperty NOT LIKE '_tyuu'",
                "MyProperty BETWEEN .8 AND 2. OR MyProperty LIKE '12_12'",
                "MyProperty IS NULL OR NOT MyProperty LIKE 'abcd_'",
                "MyProperty IN ('ab' , 'tw' , 'r') OR Age NOT IN ( '2' , '7' , '10')",
                "(MyProperty *6) + 34.5 = 0x46546f OR (MyProperty / 12) * 34.5 = 679.8E9",


                // valid identifiers
                "abCDEF <> 'abcdef'",
                "ab9cdef <> 'abcdef'",
                "ab$cdef <> 'abcdef'",
                "ab_cdef <> 'abcdef'",
                "abcdef2 <> 'abcdef'",
                "abcdef$ <> 'abcdef'",
                "abcdef_ <> 'abcdef'",
                // numeric literals
                "Age <> 10",
                "Age <> 40l",
                "Age <> 45L"
        };
    }

    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "",
                // invalid logical operations
                "myProperty = 4557.E NOT",
                "NOT (Age > 7)",
                "NOT myProperty > 7)",
                "myProperty NOT 4557.E",
                "NOT myProperty = 4557.E)",
                "myProperty = 4557.E AND (myProperty = 0x56A",
                "myProperty AND 8900",
                "((myProperty *6) + 34.5 = 0x46546f) OR ((myProperty) * 34.5 = 679.8E9))",
                "myProperty OR AND (myProperty=2)",
                "myProperty NOT AND myProperty >= 'rtty'",
                "(myProperty =< 12) AND (myProperty >= 3)"
        };
    }
}
