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

package io.ballerina.messaging.broker.core.transaction;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.store.MemBackedStoreFactory;
import io.ballerina.messaging.broker.core.store.NullMessageStore;
import io.ballerina.messaging.broker.core.store.StoreFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.transaction.xa.Xid;

/**
 * Test class for {@link DistributedTransaction} object
 */
public class DistributedTransactionValidationTest {

    private DistributedTransaction transaction;

    private Registry transactionRegistry;

    private Xid xid;

    @BeforeMethod
    public void setUp() {
        transactionRegistry = new Registry();
        transaction = new DistributedTransaction(new BranchFactory(null, new NullMessageStore()),
                                                 transactionRegistry);
        xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
    }

    @AfterMethod
    public void tearDown() {
        transaction = null;
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Cannot start a branch with both join and resume set .*")
    public void testStartWithJoinAndResumeSet() throws Exception {
        transaction.start(xid, 1, true, true);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch not found with xid .*")
    public void testJoinWithUnknownXid() throws Exception {
        transaction.start(xid, 1, true, false);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch not found with xid .*")
    public void testResumeWithUnknownXid() throws Exception {
        transaction.start(xid, 1, false, true);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Xid .* cannot be started as it is already known")
    public void testStartWithAlreadyKnownXidWithoutJoinOrResume()  throws Exception {
        StoreFactory storeFactory = new MemBackedStoreFactory(new NullBrokerMetricManager(),
                                                              new BrokerCoreConfiguration());
        Branch branch = new Branch(xid, storeFactory.getMessageStore(), null);
        transactionRegistry.register(branch);

        transaction.start(xid, 1, false, false);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch not found with xid .*")
    public void testPrepareWithUnknownXid() throws Exception {
        transaction.prepare(xid);
    }
}
