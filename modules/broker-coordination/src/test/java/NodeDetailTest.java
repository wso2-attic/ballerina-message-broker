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

import io.ballerina.messaging.broker.coordination.node.NodeDetail;
import io.ballerina.messaging.broker.coordination.node.NodeHeartbeatData;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test the initialization and retrieval of node details.
 */
public class NodeDetailTest {

    @Test(dataProvider = "nodeDetails", description = "Test for NodeDetail initialization and retrieval")
    public void testNodeDetails(String nodeID, boolean isCoordinator) {
        NodeDetail nodeDetail = new NodeDetail(nodeID, isCoordinator);
        Assert.assertNotNull(nodeDetail, "Node Details initialization unsuccessful");
        Assert.assertEquals(nodeDetail.getNodeId(), nodeID, "Incorrect node ID returned");
        Assert.assertEquals(nodeDetail.isCoordinator(), isCoordinator, "Incorrect coordinator status returned");
    }

    @Test(dataProvider = "nodeHeartbeat", description = "Test for NodeHeartbeatData initialization and retrieval")
    public void testNodeHeartbeatData(String nodeID, long heartbeatValue, boolean isNewNode) {
        NodeHeartbeatData nodeHeartbeatData = new NodeHeartbeatData(nodeID, heartbeatValue, isNewNode);
        Assert.assertNotNull(nodeHeartbeatData, "Node Heartbeat Details initialization unsuccessful");
        Assert.assertEquals(nodeHeartbeatData.getNodeId(), nodeID, "Incorrect node ID returned");
        Assert.assertEquals(nodeHeartbeatData.getLastHeartbeat(), heartbeatValue,
                "incorrect heartbeat value returned");
        Assert.assertEquals(nodeHeartbeatData.isNewNode(), isNewNode, "incorrect new node status returned");
    }

    @DataProvider(name = "nodeDetails")
    public Object[][] nodeDetails() {
        return new Object[][] {
                { "7a473f76-e22b-11e7-80c1-9a214cf093ae", true },
                { "bc993ca8-e22b-11e7-80c1-9a214cf093ae", false }
        };
    }

    @DataProvider(name = "nodeHeartbeat")
    public Object[][] nodeHeartbeat() {
        return new Object[][] {
                { "7a473f76-e22b-11e7-80c1-9a214cf093ae", 1513324811176L, true },
                { "bc993ca8-e22b-11e7-80c1-9a214cf093ae", 1513324832758L, false }
        };
    }
}
