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

package apps.provisioning.server.account.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import apps.provisioning.config.ConfigData;

/**
 * Test of LockedDirectory class.
 */
public class LockedDirectoryTest {

  private final String CONFIG_FILE_PATH = "./test/apps-provisioning-test.properties";
  private LockedDirectory lockedDirectory;
  private String input;
  private ConfigData config;
  private long lockedUsernamesTimeout;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    config = new ConfigData(CONFIG_FILE_PATH);
    lockedDirectory = new LockedDirectory(config);
    input = "carlosalvarez";
    lockedUsernamesTimeout = config.getSuggestedUsernamesTimeout();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    lockedDirectory.clear();
  }

  /**
   * Checks if the username exist. Test method for exists.
   * {@link apps.provisioning.server.account.data.LockedDirectory#exists(java.lang.String)} . When
   * user name exists.
   *
   * @throws Exception
   */
  @Test
  public final void testUsernameExists() throws Exception {
    lockedDirectory.insert(input);
    assertTrue(lockedDirectory.exists(input));
  }


  /**
   * Checks if the username doesn't exist. Test method for exists
   * {@link apps.provisioning.server.account.data.LockedDirectory#exists(java.lang.String)} . When
   */
  @Test
  public final void testUsernameDoesNotExist() {
    assertFalse(lockedDirectory.exists(input));
  }

  /**
   * Checks if the method is adding an existing username. Test method for add.
   * {@link apps.provisioning.server.account.data.LockedDirectory#insert(java.lang.String)} .
   *
   * @throws Exception
   */
  @Test
  public final void testAddExisting() throws Exception {
    lockedDirectory.insert(input);
    try {
      lockedDirectory.insert(input);
      fail("The username already existed but added again.");
    } catch (Exception e) {
      assertEquals("Username alrealy exists in Locked Directory.", e.getMessage());
    }
  }


  /**
   * Checks if multiple users are added.
   * {@link apps.provisioning.server.account.data.LockedDirectory#remove(java.lang.String)} .
   *
   * @throws Exception
   */
  @Test
  public final void testAddMultiple() throws Exception {
    lockedDirectory.insert(input + "1");
    lockedDirectory.insert(input + "2");
    lockedDirectory.insert(input + "3");
    assertTrue(lockedDirectory.exists(input + "1"));
    assertTrue(lockedDirectory.exists(input + "2"));
    assertTrue(lockedDirectory.exists(input + "3"));
  }


  /**
   * Checks if method is deleting an existing username. Test method for remove.
   * {@link apps.provisioning.server.account.data.LockedDirectory#remove(java.lang.String)} .
   *
   * @throws Exception
   */
  @Test
  public final void testRemoveExisting() throws Exception {
    lockedDirectory.insert(input);
    assertTrue(lockedDirectory.exists(input));
    assertTrue(lockedDirectory.remove(input));
    assertFalse(lockedDirectory.exists(input));
  }

  @Test
  public final void testExpiredUsernames() throws Exception {
    lockedDirectory.insert(input);
    Thread.sleep((long) (lockedUsernamesTimeout * 1.5));
    assertFalse(lockedDirectory.exists(input));
  }

  @Test
  public final void testExpiredUsernamesMixed() throws Exception {
    lockedDirectory.insert(input + "1");
    Thread.sleep((long) (lockedUsernamesTimeout * 1.5));
    lockedDirectory.insert(input + "2");
    Thread.sleep(lockedUsernamesTimeout / 2);
    assertFalse(lockedDirectory.exists(input + "1"));
    assertTrue(lockedDirectory.exists(input + "2"));
  }


  @Test
  public final void testExpiredUsernamesMultiple() throws Exception {
    lockedDirectory.insert(input + "1");
    lockedDirectory.insert(input + "2");
    lockedDirectory.insert(input + "3");
    Thread.sleep((long) (lockedUsernamesTimeout * 1.5));
    assertFalse(lockedDirectory.exists(input + "1"));
    assertFalse(lockedDirectory.exists(input + "2"));
    assertFalse(lockedDirectory.exists(input + "3"));
  }


  @Test
  public final void testExpiredUsernamesInsertTwice() throws Exception {
    lockedDirectory.insert(input);
    Thread.sleep((long) (lockedUsernamesTimeout * 1.5));
    lockedDirectory.insert(input);
    assertTrue(lockedDirectory.exists(input));
  }

  /**
   * Checks if method is deleting a non existing username. Test method for remove.
   * {@link apps.provisioning.server.account.data.LockedDirectory#remove(java.lang.String)} .
   */
  @Test
  public final void testRemoveNotExisting() {
    assertFalse(lockedDirectory.remove(input));
  }
}
