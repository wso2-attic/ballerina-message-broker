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

import java.util.regex.Pattern;

/**
 * Implementation of a boolean expression. Here we compare a expression value with pattern value
 * and evaluate to a boolean value.
 * please refer the ![jms-selector-guide](../docs/user/jms-selector-guide.md).
 */
public class LikeComparisionExpression implements BooleanExpression {

    private final Expression<Metadata> left;

    private final String right;

    private final String escape;

    private Pattern likePattern;

    public LikeComparisionExpression (Expression left , String right , String escape) {
        this.left = left;
        this.right = right;
        this.escape = escape;
        this.likePattern = null;
    }
    @Override
    public boolean evaluate (Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        if (!(leftValue instanceof String) || right == null) {
            return false;
        }
        if (!(escape == null) && (escape.length() != 1)) {
            throw new RuntimeException(
                    " Litteral used: " + escape + "is not a valid litteral it can use single character only");
        }
        char q = 0;
        if (!(escape == null)) {
            q =  escape.charAt(0);
        }
        StringBuilder regexp = new StringBuilder(right.length() * 2);
        regexp.append("\\A"); //Matches the beginning of the string.
        for (int i = 0; i < right.length(); i++) {
            char c = right.charAt(i);
            if (q == c) {
                i++;
                if (i >= right.length()) {
                    // nothing left to escape...
                    break;
                }
                char t = right.charAt(i);
                regexp.append("\\x");
                regexp.append(Integer.toHexString(0xFFFF & t));
            } else if (c == '%') {
                regexp.append(".*?"); // Do a non-greedy match
            } else if (c == '_') {
                regexp.append("."); // match one
            } else {
                regexp.append(c);
            }
        }
        regexp.append("\\z"); //Matches the end of the string.

        likePattern = Pattern.compile(regexp.toString() , Pattern.DOTALL);

        return likePattern.matcher((String) leftValue).matches();
    }
}
