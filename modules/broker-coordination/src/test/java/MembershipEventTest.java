/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.broker.coordination.CoordinationException;
import org.wso2.broker.coordination.rdbms.MembershipEvent;
import org.wso2.broker.coordination.rdbms.MembershipEventType;

/**
 * Test for membership event type.
 */
public class MembershipEventTest {

    @Test(dataProvider = "membershipEventInfo", description = "Test MembershipEvent initialization and value retrieval")
    public void testMembershipEventInitializationAndRetrieval(MembershipEventType membershipEventType, String member) {
        MembershipEvent membershipEvent = new MembershipEvent(membershipEventType, member);
        Assert.assertEquals(membershipEvent.getMembershipEventType(), membershipEventType, "Membership Event Type"
                + " not set correctly in MembershipEvent");
        Assert.assertEquals(membershipEvent.getMember(), member, "Member not set correctly in MembershipEvent");
    }

    @Test(description = "Test retrieval of MembershipEventType by corresponding valid integer representations")
    public void testGetTypeFromIntForValidTypes() throws Exception {
        Assert.assertEquals(MembershipEventType.getTypeFromInt(1), MembershipEventType.MEMBER_ADDED, "Incorrect "
                + "event type retrieved for typeInInt 1");
        Assert.assertEquals(MembershipEventType.getTypeFromInt(2), MembershipEventType.MEMBER_REMOVED, "Incorrect "
                + "event type retrieved for typeInInt 2");
        Assert.assertEquals(MembershipEventType.getTypeFromInt(3), MembershipEventType.COORDINATOR_CHANGED
                , "Incorrect event type retrieved for typeInInt 3");
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test retrieval of MembershipEventType for invalid integer representation")
    public void testGetTypeFromIntForInvalidTypes() throws Exception {
        MembershipEventType.getTypeFromInt(100);
    }

    @DataProvider(name = "membershipEventInfo")
    public Object[][] membershipEventInfo() {
        return new Object[][] {
                { MembershipEventType.MEMBER_ADDED, "7a473f76-e22b-11e7-80c1-9a214cf093ae" },
                { MembershipEventType.MEMBER_REMOVED, "bc993ca8-e22b-11e7-80c1-9a214cf093ae" },
                { MembershipEventType.COORDINATOR_CHANGED, "cd3b554e-e3c7-11e7-80c1-9a214cf093ae"  }
        };
    }

}
