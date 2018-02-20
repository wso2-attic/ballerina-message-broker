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

package io.ballerina.messaging.broker.integration.util;

import com.ibatis.common.jdbc.ScriptRunner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DbUtils {

    private static final String DATABASE_URL = "jdbc:h2:mem:mbDB;DB_CLOSE_DELAY=-1";

    private static final String path = "../launcher/src/main/resources/dbscripts/h2-mb.sql";

    public static DataSource getDataSource() {

        HikariConfig hikariDataSourceConfig = new HikariConfig();
        hikariDataSourceConfig.setJdbcUrl(DATABASE_URL);
        hikariDataSourceConfig.setAutoCommit(false);
        return new HikariDataSource(hikariDataSourceConfig);
    }

    public static void setupDB() throws SQLException, IOException {

        Connection connection = DriverManager.getConnection(DATABASE_URL + ";create=true");

        ScriptRunner scriptRunner = new ScriptRunner(connection, true, true);
        scriptRunner.runScript(new BufferedReader(new FileReader(path)));

        connection.close();
    }
}
