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

package io.ballerina.messaging.broker.auth;

import com.ibatis.common.jdbc.ScriptRunner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DbUtil {

    private static final String DATABASE_URL = "jdbc:h2:mem:mbDB;DB_CLOSE_DELAY=-1";

    private static final String DRIVER_CLASS_NAME = "org.h2.Driver";

    private static final String path = "../launcher/src/main/resources/dbscripts/h2-mb.sql";

    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);

    private static DataSource dataSource;

    static {
        try {
            setupDB();
            dataSource = createDataSource();
        } catch (SQLException | IOException e) {
            LOGGER.error("Error occurred while setting up database for unit tests", e);
        }
    }

    private static DataSource createDataSource() {
        HikariConfig hikariDataSourceConfig = new HikariConfig();
        hikariDataSourceConfig.setJdbcUrl(DATABASE_URL);
        hikariDataSourceConfig.setDriverClassName(DRIVER_CLASS_NAME);
        hikariDataSourceConfig.setAutoCommit(false);
        return new HikariDataSource(hikariDataSourceConfig);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    private static void setupDB() throws SQLException, IOException {
        Connection connection = DriverManager.getConnection(DATABASE_URL + ";create=true");

        ScriptRunner scriptRunner = new ScriptRunner(connection, true, true);
        scriptRunner.runScript(new BufferedReader(new FileReader(path)));
        connection.close();
    }
}
