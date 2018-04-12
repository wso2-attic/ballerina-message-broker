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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import javax.transaction.xa.Xid;

/**
 * Test transaction timeout functionality.
 */
public class TransactionTimeoutTest {

    private DistributedTransaction transaction;

    private Branch branch;

    private Xid xid;

    @BeforeClass
    public void setXid() {
        xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
    }

    @BeforeMethod
    public void setUp() throws ValidationException {
        BranchFactory branchFactory = new BranchFactory(null, new NullMessageStore());
        Registry transactionRegistry = new Registry(branchFactory);
        transaction = new DistributedTransaction(branchFactory, transactionRegistry);
        StoreFactory storeFactory = new MemBackedStoreFactory(new NullBrokerMetricManager(),
                                                              new BrokerCoreConfiguration());
        branch = new Branch(xid, storeFactory.getMessageStore(), null);
        transactionRegistry.register(branch);
    }

    @AfterMethod
    public void tearDown() {
        transaction = null;
    }

    @Test
    public void testTimeoutWithValueZero() throws Exception {
        transaction.setTimeout(xid, 0, TimeUnit.SECONDS);
        Assert.assertEquals(branch.isExpired(), false);
    }

    @Test
    public void testTransactionAfterTimeout() throws Exception {
        transaction.setTimeout(xid, 2, TimeUnit.MILLISECONDS);
        TimeUnit.MILLISECONDS.sleep(20);
        Assert.assertEquals(branch.isExpired(), true);
        Assert.assertEquals(branch.getState(), Branch.State.TIMED_OUT);
    }

    @Test
    public void testTransactionBeforeTimeout() throws Exception {
        transaction.setTimeout(xid, 20, TimeUnit.MILLISECONDS);
        Assert.assertEquals(branch.isExpired(), false);
        Assert.assertEquals(branch.getState(), Branch.State.ACTIVE);
    }

    @Test
    public void testTimeoutForAlreadyPreparedBranch() throws Exception {
        branch.setState(Branch.State.PREPARED);
        transaction.setTimeout(xid, 2, TimeUnit.MILLISECONDS);
        TimeUnit.MILLISECONDS.sleep(20);

        Assert.assertEquals(branch.isExpired(), false);
        Assert.assertEquals(branch.getState(), Branch.State.PREPARED);

    }
}
