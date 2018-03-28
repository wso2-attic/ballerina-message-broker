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

package io.ballerina.messaging.broker.common;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests for fast topic matcher
 */
public class FastTopicMatcherTest {

    private static FastTopicMatcher topicMatcher;

    @BeforeMethod
    public void setUp() {
        topicMatcher = new FastTopicMatcher();
    }

    @AfterMethod
    public void tearDown() {
        topicMatcher = null;
    }

    @Test
    public void testMultipleTopicMatching() {
        String pattern1 = "aa.bb.cc";
        String pattern2 = "*.bb.*";
        String pattern3 = "aa.bb.*";
        String pattern4 = "*.cc.*";
        String pattern5 = "*.dd.#";
        String pattern6 = "aa.bb.#";
        String pattern7 = "aa.#";
        String pattern8 = "aa.dd.kk.ll.*.rr.#";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);
        topicMatcher.add(pattern3);
        topicMatcher.add(pattern4);
        topicMatcher.add(pattern5);
        topicMatcher.add(pattern6);
        topicMatcher.add(pattern7);
        topicMatcher.add(pattern8);

        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings("aa.bb.cc", matchedPatterns::add);

        Assert.assertTrue(matchedPatterns.contains(pattern1), pattern1 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern2), pattern2 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern3), pattern3 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern6), pattern6 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern7), pattern7 + " didn't match");
        Assert.assertEquals(matchedPatterns.size(), 5);

        matchedPatterns.clear();
        topicMatcher.matchingBindings("aa.dd.kk.ll.kk.rr.ff.tt", matchedPatterns::add);

        Assert.assertTrue(matchedPatterns.contains(pattern5), pattern5 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern7), pattern7 + " didn't match");
        Assert.assertTrue(matchedPatterns.contains(pattern8), pattern8 + " didn't match");
        Assert.assertEquals(matchedPatterns.size(), 3);
    }

    @Test
    public void testTopicRemoval() {
        String pattern1 = "aa.bb.cc";
        String pattern2 = "bb.cc.aa";
        String pattern3 = "cc.kk.ll";
        String pattern4 = "aa.cc.ll";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);
        topicMatcher.add(pattern3);
        topicMatcher.add(pattern4);

        testForSingleMatchingPattern(pattern3);

        topicMatcher.remove(pattern2);
        // Check for other patterns
        testForSingleMatchingPattern(pattern1);
        testForSingleMatchingPattern(pattern3);
        testForSingleMatchingPattern(pattern4);

        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings("bb.cc.aa", matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "No patterns should match.");
        matchedPatterns.clear();
    }

    private void testForSingleMatchingPattern(String pattern) {
        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings(pattern, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.contains(pattern), pattern + " didn't match.");
        Assert.assertEquals(matchedPatterns.size(), 1);
    }

    @Test
    public void testRemoveItemAtTheEnd() {
        String pattern1 = "aa.bb.cc";
        String pattern2 = "bb.cc.aa";
        String pattern3 = "cc.kk.ll";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);
        topicMatcher.add(pattern3);

        Set<String> matchedPatterns = new HashSet<>();

        testForSingleMatchingPattern(pattern3);
        topicMatcher.remove(pattern3);

        topicMatcher.matchingBindings(pattern3, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty());

        testForSingleMatchingPattern(pattern2);
    }

    /**
     * Test for issue raised at
     * https://github.com/ballerina-platform/ballerina-message-broker/issues/365
     */
    @Test
    public void testPatternRemovalAndAddition() {
        String pattern1 = "Sports.cricket.100s";
        String pattern2 = "sports";

        topicMatcher.add(pattern1);

        testForSingleMatchingPattern(pattern1);
        topicMatcher.remove(pattern1);

        topicMatcher.add(pattern2);
        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings("sports.cricket", matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "Shouldn't match any pattern.");

        topicMatcher.matchingBindings(pattern1, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "Shouldn't match any pattern.");
    }

    @Test
    public void testRemovalOfAllSubscriptions() {
        String pattern1 = "aa.bb.cc.dd";
        String pattern2 = "dd.kk";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);

        topicMatcher.remove(pattern1);

        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings(pattern1, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "No pattern should match");
        matchedPatterns.clear();

        topicMatcher.remove(pattern2);
        topicMatcher.matchingBindings(pattern2, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "No pattern should match");
        matchedPatterns.clear();
    }

}
