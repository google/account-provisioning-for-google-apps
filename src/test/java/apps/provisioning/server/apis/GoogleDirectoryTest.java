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

package apps.provisioning.server.apis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import apps.provisioning.config.ConfigData;
import apps.provisioning.server.account.data.LockedDirectory;

import com.google.api.services.admin.directory.model.User;

/**
 * Test for GoogleDirectory class.
 */
public class GoogleDirectoryTest {

  private final String CONFIG_FILE_PATH = "./test/apps-provisioning-test.properties";
  private final String EXISTING_USERNAME = "existinguser";
  private final String NOT_EXISTING_USERNAME = "carlosalvares";

  private ConfigData config;
  private GoogleDirectory googleDirectory;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    config = new ConfigData(CONFIG_FILE_PATH);
    googleDirectory = new GoogleDirectory(config);
  }

  /**
   * Test method for
   * {@link apps.provisioning.server.apis.GoogleDirectory#copyToDataSource(apps.provisioning.server.account.data.UsernameDataSource)}
   * .
   *
   * @throws Exception
   */
  @Test
  public final void testCopyToDataSource() throws Exception {
    LockedDirectory lockedDirectory = new LockedDirectory(config);
    googleDirectory.copyToDataSource(lockedDirectory);
    assertTrue(lockedDirectory.exists(EXISTING_USERNAME));
    lockedDirectory.remove(EXISTING_USERNAME);
  }

  /**
   * Test method for
   * {@link apps.provisioning.server.apis.GoogleDirectory#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
   * .
   *
   * @throws Exception
   * @throws IOException
   */
  @Test
  public final void testCreateUser() throws IOException, Exception {
    String firstname = "Carlos";
    String lastname = "Alvares";
    String password = "12345678";
    User user = googleDirectory.createUser(NOT_EXISTING_USERNAME, firstname, lastname, password);
    if (user == null) {
      fail("User hasn't been created.");
    } else {
      googleDirectory.remove(NOT_EXISTING_USERNAME);
    }
  }

  /**
   * {@link apps.provisioning.server.apis.GoogleDirectory#remove(java.lang.String)}.
   *
   * @throws Exception
   */
  @Test
  public final void testRemove() throws Exception {
    googleDirectory.remove(EXISTING_USERNAME);
    boolean wasUserFound = googleDirectory.exists(EXISTING_USERNAME);
    if (wasUserFound) {
      fail("User was not deleted.");
    } else {
      String firstname = "Existing";
      String lastname = "User";
      String password = "12345678";
      assertNotNull(googleDirectory.createUser(EXISTING_USERNAME, firstname, lastname, password));
    }
  }

  /**
   * Test method for {@link apps.provisioning.server.apis.GoogleDirectory#exists(java.lang.String)}.
   */
  @Test
  public final void testUserThatExists() {
    assertTrue(googleDirectory.exists(EXISTING_USERNAME));
  }

  /**
   * Test method for {@link apps.provisioning.server.apis.GoogleDirectory#exists(java.lang.String)}.
   */
  @Test
  public final void testUserThatDoesNotExist() {
    assertFalse(googleDirectory.exists(NOT_EXISTING_USERNAME));
  }

}
