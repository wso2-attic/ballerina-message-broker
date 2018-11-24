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

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Implementation of a boolean expression. Here we compare a expression value with pattern value
 * and evaluate to a boolean value.
 */

public class LikeComparision implements BooleanExpression {

    private final Expression<Metadata> left;

    private final String ltr1;

    private final String ltr2;

    Pattern likePattern;
    //    Character c1;
    private static final HashSet REGEXP_CONTROL_CHARS = new HashSet();

    static {
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('.'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('\\'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('['));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf(']'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('^'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('$'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('?'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('*'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('+'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('{'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('}'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('|'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('('));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf(')'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf(':'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('&'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('<'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('>'));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('='));
        LikeComparision.REGEXP_CONTROL_CHARS.add(Character.valueOf('!'));
    }



    public LikeComparision (Expression<Metadata> left , String ltr1 , String ltr2) {
        this.left = left;
        this.ltr2 = ltr2;
        this.ltr1 = ltr1;
        this.likePattern = null;


    }

    @Override
    public boolean evaluate (Metadata metadata) {
        Object leftValue = left.evaluate(metadata);


        if (leftValue == null || ltr1 == null) {
            return false;
        }

        String s = String.valueOf(ltr1);
        String s1 = String.valueOf(ltr2);
        if (!(ltr2 == null) && (s1.length() != 1)) {
            throw new RuntimeException(
                    " Litteral used: " + s1 + "is not a valid litteral it can use single character only");
        }

        int m = -1;
        if (!(ltr2 == null)) {
            m = 0xFFFF & s1.charAt(0);
        }

        StringBuffer regexp = new StringBuffer(s.length() * 2);
        regexp.append("\\A"); // The beginning of the input
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (m == (0xFFFF & c)) {
                i++;
                if (i >= s.length()) {
                    // nothing left to escape...
                    break;
                }

                char t = s.charAt(i);
                regexp.append("\\x");
                regexp.append(Integer.toHexString(0xFFFF & t));
            } else if (c == '%') {
                regexp.append(".*?"); // Do a non-greedy match
            } else if (c == '_') {
                regexp.append("."); // match one

            } else if (LikeComparision.REGEXP_CONTROL_CHARS.contains(Character.valueOf(c))) {
                regexp.append("\\x");
                regexp.append(Integer.toHexString(0xFFFF & c));
            } else {
                regexp.append(c);
            }
        }
        regexp.append("\\z"); // The end of the input

        likePattern = Pattern.compile(regexp.toString() , Pattern.DOTALL);

        if (likePattern.matcher((String) leftValue).matches()) {
             return true;
        }
          return false;
    }


}
