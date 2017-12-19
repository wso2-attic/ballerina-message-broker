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

package org.wso2.broker.coordination.rdbms;

/**
 * Prepared statements, table names, column names and tasks for RDBMS coordination.
 */
public class RdbmsCoordinationConstants {

    /*
    * ===================================== RDBMS related Tasks =========================================
    */

    protected static final String TASK_ADD_COORDINATOR_ROW = "adding coordinator row";
    protected static final String TASK_ADD_NODE_ROW = "adding node row";
    protected static final String TASK_CHECK_IF_COORDINATOR = "checking if coordinator";
    protected static final String TASK_GET_ALL_HEARTBEAT = "getting heartbeat for all nodes";
    protected static final String TASK_GET_COORDINATOR_INFORMATION = "reading coordinator information";
    protected static final String TASK_MARK_NODE_NOT_NEW = "marking node as not new";
    protected static final String TASK_REMOVE_COORDINATOR = "removing coordinator heartbeat";
    protected static final String TASK_REMOVE_NODE_HEARTBEAT = "removing node heartbeat entry";
    protected static final String TASK_RETRIEVE_MEMBERSHIP_EVENTS = "retrieving membership events";
    protected static final String TASK_STORE_MEMBERSHIP_EVENT = "storing membership event";
    protected static final String TASK_UPDATE_COORDINATOR_HEARTBEAT = "updating coordinator heartbeat";
    protected static final String TASK_UPDATE_NODE_HEARTBEAT = "updating node heartbeat";

    /*
    * ===================================== Database Tables =========================================
    */

    //Membership table for nodes
    protected static final String MEMBERSHIP_TABLE = "MB_MEMBERSHIP";

    //Coordination heartbeat tables
    protected static final String CLUSTER_COORDINATOR_HEARTBEAT_TABLE = "MB_COORDINATOR_HEARTBEAT";
    protected static final String CLUSTER_NODE_HEARTBEAT_TABLE = "MB_NODE_HEARTBEAT";

    /*
    * ===================================== Columns of the Database Tables =========================================
    */

    //Coordination related columns
    protected static final String ANCHOR = "ANCHOR";
    protected static final String IS_NEW_NODE = "IS_NEW_NODE";
    protected static final String LAST_HEARTBEAT = "LAST_HEARTBEAT";
    protected static final String NODE_ID = "NODE_ID";

    //Columns for cluster membership changes
    protected static final String EVENT_ID = "EVENT_ID";
    protected static final String MEMBERSHIP_CHANGE_TYPE = "CHANGE_TYPE";
    protected static final String MEMBERSHIP_CHANGED_MEMBER_ID = "CHANGED_MEMBER_ID";

    // Constants
    protected static final int COORDINATOR_ANCHOR = 1;

    /*
    * ===================================== Prepared Statements =========================================
    */

    /**
     * Prepared statement to clear membership change events destined to a particular member.
     */
    protected static final String PS_CLEAN_MEMBERSHIP_EVENTS_FOR_NODE =
            "DELETE FROM " + MEMBERSHIP_TABLE
                    + " WHERE " + NODE_ID + "=?";

    /**
     * Prepared statement to clear the all events of the specified type of membership event.
     */
    protected static final String PS_CLEAN_MEMBERSHIP_EVENTS_FOR_EVENT_ID =
            "DELETE FROM " + MEMBERSHIP_TABLE
                    + " WHERE " + EVENT_ID + "=?";

    /**
     * Prepared statement to retrieve the last updated node heartbeats of all the nodes.
     */
    protected static final String PS_GET_ALL_NODE_HEARTBEAT =
            "SELECT " + NODE_ID + "," + LAST_HEARTBEAT + "," + IS_NEW_NODE
                    + " FROM " + CLUSTER_NODE_HEARTBEAT_TABLE;

    /**
     * Prepared statement to delete the coordinator entry.
     */
    protected static final String PS_DELETE_COORDINATOR =
            "DELETE FROM " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + " WHERE " + ANCHOR + "=" + COORDINATOR_ANCHOR;

    /**
     * Prepared statement to delete the node heart beat entry for the specified node.
     */
    protected static final String PS_DELETE_NODE_HEARTBEAT =
            "DELETE FROM " + CLUSTER_NODE_HEARTBEAT_TABLE
                    + " WHERE " + NODE_ID + "=?";

    /**
     * Prepared statement to get the node ID of the coordinator node.
     */
    protected static final String PS_GET_COORDINATOR_NODE_ID =
            "SELECT " + NODE_ID
                    + " FROM " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + " WHERE " + ANCHOR + "=" + COORDINATOR_ANCHOR;

    /**
     * Prepared statement to get the heartbeat of the coordinator.
     */
    protected static final String PS_GET_COORDINATOR_HEARTBEAT =
            "SELECT " + LAST_HEARTBEAT
                    + " FROM " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + " WHERE " + ANCHOR + "=" + COORDINATOR_ANCHOR;

    /**
     * Prepared statement to update the node heartbeat of the specified node.
     */
    protected static final String PS_UPDATE_NODE_HEARTBEAT =
            "UPDATE " + CLUSTER_NODE_HEARTBEAT_TABLE
                    + " SET " + LAST_HEARTBEAT + " =? "
                    + " WHERE " + NODE_ID + "=?";

    /**
     * Prepared statement to insert the node heartbeat entry of a new node.
     */
    protected static final String PS_INSERT_NODE_HEARTBEAT_ROW =
            "INSERT INTO " + CLUSTER_NODE_HEARTBEAT_TABLE
                    + "(" + NODE_ID + ","
                    + LAST_HEARTBEAT + ","
                    + IS_NEW_NODE + ")"
                    + " VALUES (?,?,1)";

    /**
     * Prepared statement to update the coordinator heartbeat entry.
     */
    protected static final String PS_UPDATE_COORDINATOR_HEARTBEAT =
            "UPDATE " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + " SET " + LAST_HEARTBEAT + " =? "
                    + " WHERE " + NODE_ID + "=?"
                    + " AND " + ANCHOR + "=" + COORDINATOR_ANCHOR;

    /**
     * Prepared statement to mark the specified node as not new.
     */
    protected static final String PS_MARK_NODE_NOT_NEW =
            "UPDATE " + CLUSTER_NODE_HEARTBEAT_TABLE
                    + " SET " + IS_NEW_NODE + " =0 "
                    + " WHERE " + NODE_ID + "=?";

    /**
     * Prepared statement to insert the coordinator row.
     */
    protected static final String PS_INSERT_COORDINATOR_ROW =
            "INSERT INTO " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + "(" + ANCHOR + ","
                    + NODE_ID + ","
                    + LAST_HEARTBEAT + ")"
                    + " VALUES (?,?,?)";

    /**
     * Prepared statement to check if the specified node is the coordinator.
     */
    protected static final String PS_GET_COORDINATOR_ROW_FOR_NODE_ID =
            "SELECT " + LAST_HEARTBEAT
                    + " FROM " + CLUSTER_COORDINATOR_HEARTBEAT_TABLE
                    + " WHERE " + NODE_ID + "=?"
                    + " AND " + ANCHOR + "=" + COORDINATOR_ANCHOR;

    /**
     * Prepared statement to insert a membership change event entry.
     */
    protected static final String PS_INSERT_MEMBERSHIP_EVENT =
            "INSERT INTO " + MEMBERSHIP_TABLE + " ("
                    + NODE_ID + ","
                    + MEMBERSHIP_CHANGE_TYPE + ","
                    + MEMBERSHIP_CHANGED_MEMBER_ID + ")"
                    + " VALUES ( ?,?,?)";

    /**
     * Prepared statement to retrieve membership change events destined to a particular member.
     */
    protected static final String PS_SELECT_MEMBERSHIP_EVENT =
            "SELECT " + EVENT_ID + "," + MEMBERSHIP_CHANGE_TYPE + "," + MEMBERSHIP_CHANGED_MEMBER_ID
                    + " FROM " + MEMBERSHIP_TABLE
                    + " WHERE " + NODE_ID + "=?"
                    + " ORDER BY "  + EVENT_ID;

}
