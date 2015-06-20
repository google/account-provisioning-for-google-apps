/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package apps.provisioning.data;

import java.sql.SQLException;
import java.util.ArrayList;

import org.h2.jdbc.JdbcSQLException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class H2DataSourceTest {

  private final String TESTING_DB_PATH = Thread.currentThread().getContextClassLoader()
      .getResource(".").getPath();
  private final String TESTING_DB_NAME = "testdb";

  H2DataSource dataSource;

  @Before
  public void setUp() throws Exception {
    createDataSource();
  }

  @After
  public void tearDown() throws Exception {
    dataSource.dispose();
  }

  private void createDataSource() throws SQLException, Exception {
    dataSource = new H2DataSource(TESTING_DB_PATH, TESTING_DB_NAME);
  }

  @Test
  public void testInsert() throws SQLException, Exception {
    dataSource.insert("user");
  }

  @Test
  public void testInsertMoreThanOne() throws SQLException, Exception {
    dataSource.insert("user1");
    dataSource.insert("user2");
    dataSource.insert("user3");
  }

  @Test
  public void testInsertSameTwice() throws SQLException, Exception {
    String email = "user";
    dataSource.insert(email);
    try {
      dataSource.insert(email);
      Assert.fail("Inserting the same email should not be allowed.");
    } catch (JdbcSQLException e) {
      // This exception is expected. Test should pass.
      return;
    } catch (Exception e) {
      Assert.fail("Unexpected exception was thrown: " + e);
    }
  }

  @Test
  public void testExists() throws SQLException, Exception {
    String dummyUser = "user";
    dataSource.insert(dummyUser);
    Assert.assertTrue(dataSource.exists(dummyUser));
  }

  @Test
  public void testDoesNotExist() throws SQLException, Exception {
    dataSource.insert("user");
    Assert.assertFalse(dataSource.exists("user2"));
  }

  @Test
  public void testInsertMultiple() throws SQLException, Exception {
    ArrayList<String> emails = new ArrayList<String>();
    emails.add("user1");
    emails.add("user2");
    emails.add("user3");
    dataSource.insertMultiple(emails);
    for (String email : emails) {
      Assert.assertTrue(dataSource.exists(email));
    }
  }

  @Test
  public void testInsertMultipleWithRepetition() throws SQLException, Exception {
    ArrayList<String> emails = new ArrayList<String>();
    emails.add("user1");
    emails.add("user2");
    // Repeat an email.
    emails.add("user2");
    try {
      dataSource.insertMultiple(emails);
    } catch (JdbcSQLException e) {
      // This exception is expected. Now check that no emails were inserted.
      for (String email : emails) {
        Assert.assertFalse(dataSource.exists(email));
      }
      return;
    } catch (Exception e) {
      Assert.fail("Unexpected exception was thrown: " + e);
    }
  }

  @Test
  public void testReuseH2DataSource() throws SQLException, Exception {
    String dummyUser1 = "user1";
    dataSource.insert(dummyUser1);
    Assert.assertTrue(dataSource.exists(dummyUser1));
    // Now create a second connection pointing to the same database. The emails
    // table should not have been re-created.
    H2DataSource dataSource2 = new H2DataSource(TESTING_DB_PATH, TESTING_DB_NAME);
    Assert.assertTrue(dataSource2.exists(dummyUser1));
    String dummyUser2 = "user2";
    dataSource2.insert(dummyUser2);
    Assert.assertTrue(dataSource.exists(dummyUser2));
    dataSource2.dispose();
  }

  @Test
  public void testReset() throws SQLException, Exception {
    String dummyUser = "user";
    dataSource.insert(dummyUser);
    Assert.assertTrue(dataSource.exists(dummyUser));
    // Now reset the databse and make sure the user no longer exists and we can
    // still insert new ones.
    dataSource.reset();
    Assert.assertFalse(dataSource.exists(dummyUser));
    dataSource.insert(dummyUser);
    Assert.assertTrue(dataSource.exists(dummyUser));
  }
}
