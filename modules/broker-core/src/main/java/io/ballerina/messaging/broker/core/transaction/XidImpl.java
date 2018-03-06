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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;
import java.util.Objects;
import javax.transaction.xa.Xid;

/**
 * Unique id for a given distributed transaction.
 */
public class XidImpl implements Xid {

    private final byte[] branchQualifier;

    private final int formatId;

    private final byte[] globalTransactionId;

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP2"},
            justification = "Data holder of global transaction identifier and branch qualifier")
    public XidImpl(int formatId, byte[] branchQualifier, byte[] globalTransactionId) {
        this.branchQualifier = branchQualifier;
        this.formatId = formatId;
        this.globalTransactionId = globalTransactionId;
    }

    @Override
    public int getFormatId() {
        return formatId;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Data holder of global transaction identifier")
    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Data holder of branch qualifier")
    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Xid) {
            return Arrays.equals(branchQualifier, ((Xid) obj).getBranchQualifier()) &&
                    Arrays.equals(globalTransactionId, ((Xid) obj).getGlobalTransactionId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(branchQualifier), Arrays.hashCode(globalTransactionId));
    }
}
