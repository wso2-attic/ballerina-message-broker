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
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.store.MemBackedStoreFactory;
import io.ballerina.messaging.broker.core.store.NullMessageStore;
import io.ballerina.messaging.broker.core.store.StoreFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import javax.transaction.xa.Xid;

/**
 * Test class for {@link DistributedTransaction} object
 */
public class DistributedTransactionValidationTest {

    private DistributedTransaction transaction;

    private Registry transactionRegistry;

    private Xid xid;

    @BeforeClass
    public void setXid() {
        xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
    }

    @BeforeMethod
    public void setUp() throws BrokerException {
        transactionRegistry = new Registry(new BranchFactory(null, new NullMessageStore()));
        transaction = new DistributedTransaction(new BranchFactory(null, new NullMessageStore()),
                                                 transactionRegistry);
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

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch still has associated active sessions for xid .*")
    public void testPrepareWithAssociatedActiveSession() throws Exception {
        transaction.start(xid, 1, false, false);
        transaction.prepare(xid);
    }

    @Test (expectedExceptions = UnknownDtxBranchException.class)
    public void testCommitWithUnknownXid() throws Exception {
        transaction.commit(xid, true);
    }

    @Test (expectedExceptions = ValidationException.class,
            expectedExceptionsMessageRegExp = "Branch still has associated active sessions for xid .*")
    public void testCommitWithAssociatedActiveSession() throws Exception {
        transaction.start(xid, 1, false, false);
        transaction.commit(xid, true);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch is set to rollback only. Can't commit with xid .*")
    public void testCommitWithRollbackOnlyBranch() throws Exception {
        StoreFactory storeFactory = new MemBackedStoreFactory(new NullBrokerMetricManager(),
                                                              new BrokerCoreConfiguration());
        Branch branch = new Branch(xid, storeFactory.getMessageStore(), null);
        transactionRegistry.register(branch);
        branch.setState(Branch.State.ROLLBACK_ONLY);
        transaction.commit(xid, true);
    }

    @Test (expectedExceptions = UnknownDtxBranchException.class)
    public void testRollbackWithUnknownXid() throws Exception {
        transaction.rollback(xid);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch still has associated active sessions for xid .*")
    public void testRollbackWithAssociatedActiveSession() throws Exception {
        transaction.start(xid, 1, false, false);
        transaction.rollback(xid);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch not found with xid .*")
    public void testForgetWithUnknownXid() throws Exception {
        transaction.forget(xid);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch still has associated active sessions for xid .*")
    public void testForgetWithAssociatedActiveSession() throws Exception {
        transaction.start(xid, 1, false, false);
        transaction.forget(xid);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch is not heuristically complete, hence unable to forget. Xid .*")
    public void testForgetWithoutHeuristicCommitOrRollback() throws Exception {
        transaction.start(xid, 1, false, false);
        transaction.end(xid, 1, false, false);
        transaction.forget(xid);
    }

    @Test (expectedExceptions = ValidationException.class,
           expectedExceptionsMessageRegExp = "Branch not found with xid .*")
    public void testSetTimeoutWithUnkownXid() throws Exception {
        transaction.setTimeout(xid, 5, TimeUnit.SECONDS);
    }
}
