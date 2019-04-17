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
public class MessageFilterTestComparisonoperator {

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
                "MyProperty <> 'abcdef'",
                "MyProperty <> 'abcde''f'",
                "MyProperty <> 'ABCDEF'",
                "MyProperty <> 'aBCDEF'",
                "MyProperty <> 'aB$CDEF'",
                "MyProperty <> 'aB_CDEF'",
                "MyProperty <> 'aB1CDEF'",
                "MyProperty <> 'aBCdef$'",
                "MyProperty <> 'aBCdef_'",
                "MyProperty <> 'aBCdef2'",
                "MyProperty <> '_aBCdef2'",
                "MyProperty <> '$aBCdef2'",
                "MyProperty <> '2aBCdef2'",
                "MyProperty <> 'aB$C1d_ef2'",
                "MyProperty LIKE 'abcd%'",
                "MyProperty LIKE '%wer'",
                "MyProperty LIKE 'er_d'",
                "MyProperty LIKE '_ghj'",
                "MyProperty LIKE '123_'",
                "MyProperty LIKE '\\_%' ESCAPE '\\'",
                "MyProperty NOT LIKE  'ab%de'",
                "MyProperty NOT LIKE '%fahs'",
                "MyProperty NOT LIKE '_ert'",
                "MyProperty NOT LIKE '1234_'",
                "MyProperty NOT LIKE '34_4'",
                "MyProperty NOT LIKE '\\_%' ESCAPE '\\'",
                "MyProperty IS NULL ",
                "MyProperty IS NOT NULL",
                "MyProperty BETWEEN 10 AND 20",
                "MyProperty BETWEEN 0.3E3 AND 0.2",
                "MyProperty BETWEEN 07654 AND 77777 ",
                "MyProperty NOT BETWEEN 2. AND 0.8E4",
                "MyProperty NOT BETWEEN .345 AND 780",
                "Age NOT BETWEEN 0x45578 AND 0X34567",
                "MyProperty IN ('a' , 'b' , 'c')",
                "MyProperty IN ('11' , '2.90' , '.90')",
                "MyProperty NOT IN ('aett' , 'bawdrt' , 'cfgfhgf')",
                "MyProperty NOT IN ('a1234' , 'fgyFr$' , '$2fh')",



                // valid identifiers
                "abCDEF = 'abcdef'",
                "ab9cdef = 'abcdef'",
                "ab$cdef = 'abcdef'",
                "ab_cdef = 'abcdef'",
                "abcdef2 = 'abcdef'",
                "abcdef$ = 'abcdef'",
                "abcdef_ = 'abcdef'",
                // numeric literals
                "Age > 10",
               "Age = 0x10A",
                "Age <= 40l",
                "Age >= 45L"
        };
    }

    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "",
                // invalid string literals
                "myProperty = 'abcde'f'",
                "myProperty LIKE abcd%",
                // invalid identifiers
                "$yProperty = 'abcdef'",
                "_yProperty = 'abcdef'",
                "1yProperty = 'abcdef'",
                // invalid numeric literals
                "myProperty = 123LL",
                "myProperty = 123ll",
                "myProperty = 123lLl",
                "myProperty = 24567A",
                "myProperty = 2x345",
                "myProperty = 0123F",
                "myProperty = 0xX23355F",
                "myProperty = 123lLl",
                "myProperty >= ..799",
                "myProperty = 56..",
                "myProperty = 0x345.89f",
                "myProperty <= 4557.E",
                "myProperty = 78897543F",
                "myProperty <> 5647Fa",
                "myProperty = 54737F547",
                "myProperty < 1EE",
                "myProperty > 56Eh5",
                "myProperty = 855E56E",
        };
    }
}
