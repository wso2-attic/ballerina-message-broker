package org.wso2.broker.client.cmd.impl.delete;

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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import com.beust.jcommander.Parameters;
import org.wso2.broker.client.cmd.AbstractCmd;

import static org.wso2.broker.client.utils.Utils.createUsageException;

/**
 * Command representing the resource deletion.
 */
@Parameters(commandDescription = "Delete MB resources")
public class DeleteCmd extends AbstractCmd {

    @Override
    public void execute() {
        if (!help) {
            throw createUsageException("a command is expected after 'delete'");
        }
        processHelpLogs();
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("delete a resource in MB\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("usage:\n");
        out.append("\tmb create exchange [exchange-name] [flag]*\n");
        out.append("\tmb create queue [queue-name] [flag]*\n");
    }
}
