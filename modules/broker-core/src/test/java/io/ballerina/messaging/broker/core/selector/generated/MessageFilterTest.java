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
public class MessageFilterTest {

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
                // valid string literal
                "MyProperty = 'abcdef'",
                "MyProperty = 'abcde''f'",
                "MyProperty = 'ABCDEF'",
                "MyProperty = 'aBCDEF'",
                "MyProperty = 'aB$CDEF'",
                "MyProperty = 'aB_CDEF'",
                "MyProperty = 'aB1CDEF'",
                "MyProperty = 'aBCdef$'",
                "MyProperty = 'aBCdef_'",
                "MyProperty = 'aBCdef2'",
                "MyProperty = '_aBCdef2'",
                "MyProperty = '$aBCdef2'",
                "MyProperty = '2aBCdef2'",
                "MyProperty = 'aB$C1d_ef2'",
                // valid identifiers
                "abCDEF = 'abcdef'",
                "ab9cdef = 'abcdef'",
                "ab$cdef = 'abcdef'",
                "ab_cdef = 'abcdef'",
                "abcdef2 = 'abcdef'",
                "abcdef$ = 'abcdef'",
                "abcdef_ = 'abcdef'",
                // numeric literals
                "Age = 10",
                "Age = 40l",
                "Age = 45L"
        };
    }

    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "",
                // invalid string literals
                "MyProperty = 'abcde'f'",
                "MyProperty = abcdef",
                // invalid identifiers
                "$yProperty = 'abcdef'",
                "_yProperty = 'abcdef'",
                "1yProperty = 'abcdef'",
                "'property' = 'abcdef'",
                "10 = Age",
                // invalid numeric literals
                "myProperty = 123LL",
                "myProperty = 123ll",
                "myProperty = 123lLl",
        };
    }
}
