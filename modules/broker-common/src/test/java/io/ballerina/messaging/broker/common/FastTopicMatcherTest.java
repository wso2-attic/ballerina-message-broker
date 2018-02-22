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
 * Tests for fast topic matcher.
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
    public void testTopicRemoval() throws Exception {
        String pattern1 = "aa.bb.cc";
        String pattern2 = "bb.cc.aa";
        String pattern3 = "cc.kk.ll";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);
        topicMatcher.add(pattern3);

        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings("cc.kk.ll", matchedPatterns::add);

        Assert.assertTrue(matchedPatterns.contains(pattern3), pattern3 + " didn't match.");
        Assert.assertEquals(matchedPatterns.size(), 1);
        matchedPatterns.clear();

        topicMatcher.remove(pattern2);
        // Check for other patterns
        topicMatcher.matchingBindings("aa.bb.cc", matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.contains(pattern1), pattern1 + " didn't match.");
        Assert.assertEquals(matchedPatterns.size(), 1);
        matchedPatterns.clear();

        topicMatcher.matchingBindings("cc.kk.ll", matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.contains(pattern3), pattern3 + " didn't match.");
        Assert.assertEquals(matchedPatterns.size(), 1);
        matchedPatterns.clear();

        topicMatcher.matchingBindings("bb.cc.aa", matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty(), "No patterns should match.");
    }

    @Test
    public void testRemoveItemAtTheEnd() throws Exception {
        String pattern1 = "aa.bb.cc";
        String pattern2 = "bb.cc.aa";
        String pattern3 = "cc.kk.ll";

        topicMatcher.add(pattern1);
        topicMatcher.add(pattern2);
        topicMatcher.add(pattern3);

        Set<String> matchedPatterns = new HashSet<>();
        topicMatcher.matchingBindings(pattern3, matchedPatterns::add);

        Assert.assertTrue(matchedPatterns.contains(pattern3), pattern3 + " didn't match");
        Assert.assertEquals(matchedPatterns.size(), 1);
        matchedPatterns.clear();

        topicMatcher.remove(pattern3);
        topicMatcher.matchingBindings(pattern3, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.isEmpty());

        topicMatcher.matchingBindings(pattern2, matchedPatterns::add);
        Assert.assertTrue(matchedPatterns.contains(pattern2), pattern2 + " didn't match");
    }
}
