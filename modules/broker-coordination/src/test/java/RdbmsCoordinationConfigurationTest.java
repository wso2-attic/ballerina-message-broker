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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.broker.coordination.rdbms.CoordinationConfiguration;
import org.wso2.carbon.config.ConfigProviderFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to RDBMS coordination configuration.
 */
public class RdbmsCoordinationConfigurationTest {

    private CoordinationConfiguration coordinationConfiguration = new CoordinationConfiguration();

    private CoordinationConfiguration.RdbmsCoordinationConfiguration rdbmsCoordinationConfiguration;

    @BeforeClass
    public void setUp() throws Exception {
        String brokerFilePath = new File(this.getClass()
                .getResource("rdbms" + File.separator + "broker.yaml")
                .getFile())
                .getAbsolutePath();
        Path brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        rdbmsCoordinationConfiguration = ConfigProviderFactory.getConfigProvider(brokerYamlFile, null)
                .getConfigurationObject("rdbmsCoordinationConfig",
                        CoordinationConfiguration.RdbmsCoordinationConfiguration.class);
    }

    @Test(description = "Test configuration loading from yaml files")
    public void testRdbmsCoordinationConfigurationCreationFromYamlFile() {
        Assert.assertEquals(rdbmsCoordinationConfiguration.getNodeId(), "7a473f76-e22b-11e7-80c1-9a214cf093ae",
                "Node ID value not set correctly.");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getHeartbeatInterval(), 5000,
                "Heartbeat Interval value not set correctly");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getCoordinatorEntryCreationWaitTime(), 3000,
                "Coordinator Entry Creation Wait Time value not set correctly");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getEventPollingInterval(), 4000,
                "Event Polling Interval not set correctly");
    }

    @Test(description = "Test setting and retrieving RDBMS coordination configuration")
    public void testSetAndGetRdbmsCoordinationConfiguration() {
        String nodeId = "bc993ca8-e22b-11e7-80c1-9a214cf093ae";
        int heartbeatInterval = 8000;
        int coordinatorEntryCreationWaitTime = 6000;
        int eventPollingInterval = 7000;
        String rdbmsCoordinationConfigString = "RDBMSCoordinationConfiguration [nodeID=" + nodeId
                + ", heartbeatInterval=" + heartbeatInterval
                + ", coordinatorEntryCreationWaitTime=" + coordinatorEntryCreationWaitTime
                + ", eventPollingInterval=" + eventPollingInterval + "]";
        rdbmsCoordinationConfiguration.setNodeId(nodeId);
        rdbmsCoordinationConfiguration.setHeartbeatInterval(heartbeatInterval);
        rdbmsCoordinationConfiguration.setCoordinatorEntryCreationWaitTime(coordinatorEntryCreationWaitTime);
        rdbmsCoordinationConfiguration.setEventPollingInterval(eventPollingInterval);
        coordinationConfiguration.setRdbmsCoordinationConfig(rdbmsCoordinationConfiguration);
        Assert.assertEquals(rdbmsCoordinationConfiguration.getNodeId(), nodeId, "Node ID  value does not match the "
                + "value set");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getHeartbeatInterval(), heartbeatInterval, "Heartbeat "
                + "Interval value does not match the value set");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getCoordinatorEntryCreationWaitTime(),
                coordinatorEntryCreationWaitTime, "Coordinator Entry Creation Wait Time value does not match the "
                        + "value set");
        Assert.assertEquals(rdbmsCoordinationConfiguration.getEventPollingInterval(), eventPollingInterval,
                "Event Polling Interval value does not match the value set");
        Assert.assertEquals(rdbmsCoordinationConfiguration.toString(), rdbmsCoordinationConfigString, "Incorrect "
                + "toString() representation of RdbmsCoordinationConfiguration");
        Assert.assertEquals(coordinationConfiguration.getRdbmsCoordinationConfig(), rdbmsCoordinationConfiguration,
                "RDBMS Coordination Configuration is not set correctly");
    }

}
