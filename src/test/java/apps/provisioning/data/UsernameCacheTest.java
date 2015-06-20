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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

public class UsernameCacheTest {

  private final String TESTING_DB_PATH = Thread.currentThread().getContextClassLoader()
      .getResource(".").getPath();
  private final String TESTING_DB_NAME = "testdb";
  // Simulate that copying a large number of users to the DB takes a whole
  // second.
  private final int SIMULATED_COPY_USERNAMES_DELAY = 1000;

  private final Logger logger = Logger.getLogger(UsernameCacheTest.class.getName());

  private IMocksControl control;
  private UsernameCache usernameCache;

  /**
   * Inner class used to fake a Google Directory with different user names for each call to copy to
   * a data source. This class is used instead of actually retrieving and updating usernames from a
   * real Google Directory. It also simulates a delay when populating the data source.
   *
   */
  private class FakeGoogleDirectory extends GoogleDirectory {

    private ArrayList<String> usernames;

    public FakeGoogleDirectory(ArrayList<String> usernames) {
      super();
      this.usernames = usernames;
    }

    @Override
    public void copyToDataSource(UsernameDataSource dataSource) throws Exception {
      dataSource.insertMultiple(usernames);
      Thread.sleep(SIMULATED_COPY_USERNAMES_DELAY);
    }
  }


  @Before
  public void setUp() throws Exception {
    control = EasyMock.createStrictControl();
  }

  @After
  public void tearDown() throws Exception {
    // Dispose the DB if one was created.
    if (usernameCache != null && usernameCache.isReady()) {
      usernameCache.disposeDataSource();
    }
  }

  /**
   * Tests refreshing the cache every second two times and asserts that the expected user names
   * after the second refresh are correct. Makes sure that the UsernameCache takes into account the
   * time spent when refreshing the cache.
   *
   * @throws Exception
   */
  @Test
  public void testRefreshCacheTwice() throws Exception {
    // Refresh every second starting at second 0.
    int initialUpdateDelayInSeconds = 0;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    // Set up the Google Directory mock to return a set of users in the first
    // call to populate it and a different set in the second call.
    ArrayList<String> usernames1 = new ArrayList<String>();
    usernames1.add("dummyuser1");
    usernames1.add("dummyuser2");
    FakeGoogleDirectory fakeGoogleDirectory1 = new FakeGoogleDirectory(usernames1);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory1);

    // Second set of users for the second call to refresh.
    ArrayList<String> usernames2 = new ArrayList<String>();
    usernames2.add("dummyuser3");
    usernames2.add("dummyuser4");
    usernames2.add("dummyuser5");
    FakeGoogleDirectory fakeGoogleDirectory2 = new FakeGoogleDirectory(usernames2);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory2);

    control.replay();

    logger.log(Level.INFO, "Testing with DB at path: " + TESTING_DB_PATH);
    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);

    // We set the cache to refresh every second starting at second 0. Sleep for
    // 1.5 seconds so that it refreshes twice and then verify that the state is
    // as expected. Take into account the SIMULATED_COPY_USERNAMES_DELAY.
    Thread.sleep(1500 + 2 * SIMULATED_COPY_USERNAMES_DELAY);
    control.verify();

    Assert.assertTrue(usernameCache.isReady());

    // Assert that the cache was refreshed properly.
    // The first set should not exist anymore.
    for (String username : usernames1) {
      Assert.assertFalse(usernameCache.exists(username));
    }

    // The second set should exist.
    for (String username : usernames2) {
      Assert.assertTrue(usernameCache.exists(username));
    }
  }


  /**
   * Tests calling exists while the cache is being refreshed.
   *
   * @throws Exception
   */
  @Test
  public void testExistsWhileRefreshing() throws Exception {
    // Refresh every second starting at second 0.
    int initialUpdateDelayInSeconds = 0;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    // Set up the Google Directory mock to return a set of users in the first
    // call to populate it and a different set in the second call.
    ArrayList<String> usernames1 = new ArrayList<String>();
    usernames1.add("dummyuser1");
    usernames1.add("dummyuser2");
    FakeGoogleDirectory fakeGoogleDirectory1 = new FakeGoogleDirectory(usernames1);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory1);

    // Second set of users for the second call to refresh.
    ArrayList<String> usernames2 = new ArrayList<String>();
    usernames2.add("dummyuser3");
    usernames2.add("dummyuser4");
    usernames2.add("dummyuser5");
    FakeGoogleDirectory fakeGoogleDirectory2 = new FakeGoogleDirectory(usernames2);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory2);

    control.replay();

    logger.log(Level.INFO, "Testing with DB at path: " + TESTING_DB_PATH);
    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);

    // We set the cache to refresh every second starting at second 0. Sleep for
    // 1.5 seconds so that we are in the process of refreshing the cache. Take
    // into account one SIMULATED_COPY_USERNAMES_DELAY.
    Thread.sleep(1500 + SIMULATED_COPY_USERNAMES_DELAY);
    control.verify();

    // Assert that the username cache is still refreshing.
    Assert.assertEquals(UsernameCache.STATUS_REFRESHING, usernameCache.getStatus());

    // Assert that the old cache is still read.
    for (String username : usernames1) {
      Assert.assertTrue(usernameCache.exists(username));
    }

    // Assert that the new cache is not read yet.
    for (String username : usernames2) {
      Assert.assertFalse(usernameCache.exists(username));
    }
  }

  /**
   * Tests that creating a username while caching inserts it when ready.
   *
   * @throws SQLException
   * @throws Exception
   */
  @Test
  public void testInsertUserWhileCaching() throws SQLException, Exception {
    // Refresh every second starting at second 0.
    int initialUpdateDelayInSeconds = 0;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    // Set up the Google Directory mock to return a set of users in the first
    // call to populate it and a different set in the second call.
    ArrayList<String> usernames1 = new ArrayList<String>();
    usernames1.add("dummyuser1");
    usernames1.add("dummyuser2");
    FakeGoogleDirectory fakeGoogleDirectory1 = new FakeGoogleDirectory(usernames1);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory1);

    control.replay();

    logger.log(Level.INFO, "Testing with DB at path: " + TESTING_DB_PATH);
    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);

    // Sleep for half the time it takes to refresh the cache so that we test
    // inserting a username while caching.
    Thread.sleep(SIMULATED_COPY_USERNAMES_DELAY / 2);
    control.verify();

    // Assert that the username cache is still caching.
    Assert.assertEquals(UsernameCache.STATUS_CACHING, usernameCache.getStatus());
    // Now insert a new username to the cache.
    String newUsername = "dummyuser3";
    usernameCache.insert(newUsername);

    // Sleep again to make sure the cache is ready.
    Thread.sleep(500 + SIMULATED_COPY_USERNAMES_DELAY / 2);

    Assert.assertEquals(UsernameCache.STATUS_READY, usernameCache.getStatus());

    // Assert that all the users are present, including the one added at the
    // middle of the caching process.
    for (String username : usernames1) {
      Assert.assertTrue(usernameCache.exists(username));
    }
    Assert.assertTrue(usernameCache.exists(newUsername));
  }


  /**
   * Tests inserting a username when the cache is ready.
   *
   * @throws SQLException
   * @throws Exception
   */
  @Test
  public void testInsertUserWhenReady() throws SQLException, Exception {
    // Refresh every second starting at second 0.
    int initialUpdateDelayInSeconds = 0;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    // Set up the Google Directory mock to return a set of users in the first
    // call to populate it and a different set in the second call.
    ArrayList<String> usernames1 = new ArrayList<String>();
    usernames1.add("dummyuser1");
    usernames1.add("dummyuser2");
    FakeGoogleDirectory fakeGoogleDirectory1 = new FakeGoogleDirectory(usernames1);
    googleDirectoryMock.copyToDataSource(EasyMock.anyObject(H2DataSource.class));
    EasyMock.expectLastCall().andDelegateTo(fakeGoogleDirectory1);

    control.replay();

    logger.log(Level.INFO, "Testing with DB at path: " + TESTING_DB_PATH);
    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);

    // Sleep so that the cache is ready.
    Thread.sleep(500 + SIMULATED_COPY_USERNAMES_DELAY);
    control.verify();

    // Assert that the username cache is ready.
    Assert.assertEquals(UsernameCache.STATUS_READY, usernameCache.getStatus());
    // Now insert a new username to the cache.
    String newUsername = "dummyuser3";
    usernameCache.insert(newUsername);

    // Assert that all the users are present, including the one added at the
    // end of the caching process.
    for (String username : usernames1) {
      Assert.assertTrue(usernameCache.exists(username));
    }
    Assert.assertTrue(usernameCache.exists(newUsername));
  }

  /**
   * Tests that trying to read an unpopulated cache throws an exception.
   *
   * @throws SQLException
   * @throws Exception
   */
  @Test
  public void testReadWhenEmptyThrowsError() throws SQLException, Exception {
    // Refresh every second starting at second 1.
    int initialUpdateDelayInSeconds = 1;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    control.replay();

    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);
    control.verify();

    try {
      usernameCache.exists("dummyuser");
      Assert.fail("Should have thrown an exception when reading an unpopulated cache.");
    } catch (Exception e) {
      Assert.assertEquals("Should not call exists if the data source hasn't been created",
          e.getMessage());
    }
  }

  /**
   * Tests that the status of an unpopulated cache is not ready.
   *
   * @throws Exception
   */
  @Test
  public void testNotReady() throws Exception {
    // Refresh every second starting at second 1.
    int initialUpdateDelayInSeconds = 1;
    int updateRateInSeconds = 1;

    GoogleDirectory googleDirectoryMock = control.createMock(GoogleDirectory.class);
    control.replay();

    usernameCache =
        new UsernameCache(initialUpdateDelayInSeconds, updateRateInSeconds, TESTING_DB_PATH,
            TESTING_DB_NAME, googleDirectoryMock);
    control.verify();

    Assert.assertFalse(usernameCache.isReady());
  }
}
