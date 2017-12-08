/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.store.dao.impl.MessageDaoImpl;
import org.wso2.broker.core.store.dao.impl.QueueDaoImpl;


/**
 * Instantiates different DAO objects required to access persistent storage.
 */
public class DaoFactory {

    //private BrokerConfiguration.DatasourceConfiguration datasourceConfiguration;
    
    
    public DaoFactory(BrokerConfiguration.DatasourceConfiguration datasourceConfiguration) {
       // this.datasourceConfiguration = datasourceConfiguration;
        
    }
    
    public QueueDao createQueueDao() {
        return new QueueDaoImpl();
    }

    public MessageDao createMesageDao() {
        return new MessageDaoImpl();
    }

}
