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

-- WSO2 Message Broker MySQL Database Schema --

-- Start of RDBMS based Coordinator Election Tables  --

CREATE TABLE IF NOT EXISTS MB_COORDINATOR_HEARTBEAT (
                        ANCHOR INT NOT NULL,
                        NODE_ID VARCHAR(512) NOT NULL,
                        LAST_HEARTBEAT BIGINT NOT NULL,
                        PRIMARY KEY (ANCHOR)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS MB_NODE_HEARTBEAT (
                        NODE_ID VARCHAR(512) NOT NULL,
                        LAST_HEARTBEAT BIGINT NOT NULL,
                        IS_NEW_NODE TINYINT NOT NULL,
                        PRIMARY KEY (NODE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- End of RDBMS based Coordinator Election Tables  --