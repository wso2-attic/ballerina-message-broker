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

package org.wso2.broker.core.transaction;

import org.wso2.broker.core.Broker;
import org.wso2.broker.core.store.StoreFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.transaction.xa.Xid;

/**
 * Creates transactional branches for the broker.
 */
public class BranchFactory {

    private final StoreFactory storeFactory;

    private final Broker broker;

    public BranchFactory(Broker broker, StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
        this.broker = broker;
    }

    public Branch createBranch() {
        // TODO: improve Xid generation logic
        Xid xid = new XidImpl(0,
                              UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8),
                              "".getBytes(StandardCharsets.UTF_8));
        return new Branch(xid, storeFactory.getSharedMessageStore(), broker);
    }
}
