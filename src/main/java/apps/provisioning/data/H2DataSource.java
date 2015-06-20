/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package apps.provisioning.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.DeleteDbFiles;

import apps.provisioning.server.account.data.UsernameDataSource;

/**
 * Adds support for caching usernames from the Google Directory using a H2 database.
 *
 */
public class H2DataSource implements UsernameDataSource {

  private final String USERNAME_TABLE = "usernames";

  private final String USERNAME_COLUMN_NAME = "user";

  private final String USER_EXISTS_QUERY = "select 1 from " + USERNAME_TABLE + " where "
      + USERNAME_COLUMN_NAME + " = '%s'";

  private final String INSERT_SINGLE_USER_QUERY = "insert into " + USERNAME_TABLE + "("
      + USERNAME_COLUMN_NAME + ") VALUES ('%s')";

  private final String INSERT_MULTIPLE_USERS_QUERY = "insert into " + USERNAME_TABLE + " ("
      + USERNAME_COLUMN_NAME + ") VALUES ";

  private final String INSERT_MULTIPLE_USERS_QUERY_PARAM = "('%s'), ";

  private final String MAYBE_CREATE_TABLE_QUERY = "create table if not exists " + USERNAME_TABLE
      + "(" + USERNAME_COLUMN_NAME + " varchar(64) primary key)";

  private final Logger logger = Logger.getLogger(JdbcConnectionPool.class.getName());

  // private JdbcConnectionPool connectionPool;
  private String databasePath;
  private String databaseName;
  private String databaseUrl;
  private JdbcConnectionPool connectionPool;

  public H2DataSource(String databasePath, String databaseName) throws SQLException, Exception {
    this.databasePath = databasePath;
    this.databaseName = databaseName;
    this.databaseUrl = "jdbc:h2:" + databasePath + databaseName;
    logger.log(Level.INFO, "H2 database URL: " + this.databaseUrl);
    this.setup();
  }


  /**
   * Sets up the databse to be used to cache usersnames.
   *
   * @throws SQLException
   */
  private void setup() throws SQLException {
    connectionPool = JdbcConnectionPool.create(databaseUrl, "", "");
    maybeCreateUsernamesTable();
  }

  /**
   * Creates the usernames table if none exists.
   *
   * @throws SQLException
   */
  private void maybeCreateUsernamesTable() throws SQLException {
    executeQuery(MAYBE_CREATE_TABLE_QUERY);
  }

  public boolean exists(String username) throws SQLException, Exception {
    int resultCount = executeQuery(String.format(USER_EXISTS_QUERY, username));
    switch (resultCount) {
      case 0:
        return false;
      case 1:
        return true;
      default:
        throw new Exception(
            "Inconsistent state. Only one username should exist in the database, but "
                + resultCount + " were found for: " + username);
    }
  }

  public void insert(String username) throws SQLException, Exception {
    int updateCount = executeQuery(String.format(INSERT_SINGLE_USER_QUERY, username));
    if (updateCount != 1) {
      throw new Exception("User " + username + " could not be inserted.");
    }
  }

  /**
   * Inserts all the given usernames.
   *
   * @param usernames The usernames to be inserted.
   * @throws SQLException
   * @throws Exception when the number of inserted usernames does not match the given usernames.
   */
  public void insertMultiple(ArrayList<String> usernames) throws SQLException, Exception {
    if (usernames.size() == 0) {
      return;
    }
    String query = INSERT_MULTIPLE_USERS_QUERY;
    for (String username : usernames) {
      query += String.format(INSERT_MULTIPLE_USERS_QUERY_PARAM, username);
    }
    int updateCount = executeQuery(query);
    if (updateCount != usernames.size()) {
      throw new Exception("Issue when inserting " + usernames.size() + " users. Only "
          + updateCount + " were inserted.");
    }
  }

  /**
   * Executes the given query.
   *
   * @param query The query to execute.
   * @return Returns the number of rows returned or the number of rows affected by the query.
   * @throws SQLException
   */
  private int executeQuery(String query) throws SQLException {
    Connection connection = connectionPool.getConnection();
    Statement statement;
    int resultCount = 0;
    try {
      statement = connection.createStatement();
      logger.log(Level.INFO, "Running query: " + query);
      boolean hasResultSet = statement.execute(query);
      if (hasResultSet) {
        ResultSet resultSet = statement.getResultSet();
        if (resultSet != null && resultSet.last()) {
          resultCount = resultSet.getRow();
        }
      } else {
        resultCount = statement.getUpdateCount();
      }
    } catch (SQLException e) {
      // Something went wrong. Close the connection.
      connection.close();
      throw e;
    }
    if (statement != null) {
      statement.close();
    }
    connection.close();
    return resultCount;
  }

  /**
   * Deletes the current databse and creates a new one from scratch. Should be called with no active
   * connections.
   *
   * @throws SQLException
   * @throws Exception if active connections exist.
   */
  public void reset() throws SQLException, Exception {
    dispose();
    this.setup();
    maybeCreateUsernamesTable();
  }

  /**
   * Deletes the database files. Should be called with no active connections.
   *
   * @throws Exception if there are active connections.
   */
  private void deleteDatabseFiles() throws Exception {
    if (connectionPool.getActiveConnections() != 0) {
      throw new Exception(
          "Should not try to delete database files while there are active connections.");
    }
    logger.log(Level.INFO, "Deleting database files at: " + databasePath + databaseName);
    DeleteDbFiles.execute(databasePath, databaseName, true);
  }

  /**
   * Disposes the connection pool and deletes all databse files. Should be called with no active
   * connections.
   *
   * @throws Exception if there are active connections.
   */
  public void dispose() throws Exception {
    connectionPool.dispose();
    deleteDatabseFiles();
  }
}
