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

package apps.provisioning.server.account;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import apps.provisioning.data.UsernameCache;
import apps.provisioning.server.Context;
import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

/**
 * Test for UsernameManager class.
 */
public class UsernameManagerTest {

  private final String CACHE_ENABLED_CONFIG_FILE_PATH = "./test/cache-enabled.properties";
  private final String CACHE_DISABLED_CONFIG_FILE_PATH = "./test/cache-disabled.properties";
  private final String USERNAME_FIELD = "username";
  private final String FIRST_NAME_FIELD = "firstname";
  private final String LAST_NAME_FIELD = "lastname";
  private final String PASSWORD_FIELD = "password";
  private final String NOT_EXISTING_USERNAME_01 = "notexisting01";
  private final String NOT_EXISTING_USERNAME_02 = "notexisting02";

  private HashMap<String, String> userData;

  @Before
  public void setUp() throws Exception {
    userData = new HashMap<String, String>();
  }

  @Test
  public final void testSuggestWithNullAsInput() throws Exception {
    Context context = new Context(CACHE_ENABLED_CONFIG_FILE_PATH);
    UsernameManager usernameManager = new UsernameManager(context);
    try {
      usernameManager.suggest(null);
      fail("userData can not be null.");
    } catch (Exception e) {
      assertEquals("User data parameter can't be null.", e.getMessage());
    }

  }

  @Test
  public final void testSuggestWithCacheEnabled() throws Exception {
    Context context = new Context(CACHE_ENABLED_CONFIG_FILE_PATH);
    UsernameManager usernameManager = new UsernameManager(context);
    userData.put(FIRST_NAME_FIELD, "Carlos");
    userData.put(LAST_NAME_FIELD, "Álvarez");
    String[] expectedResult = new String[] {"carlos.alvarez", "carlosalvarez", "c.alvarez"};
    assertArrayEquals(expectedResult, usernameManager.suggest(userData).toArray());
  }

  @Test
  public final void testCreateWithCacheEnabled() throws Exception {
    Context context = new Context(CACHE_ENABLED_CONFIG_FILE_PATH);
    // Waits ten seconds more than testCreateWithCacheDisabled that delays one second in the user
    // creation.
    Thread.sleep(4000);
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof UsernameCache);
    UsernameManager usernameManager = new UsernameManager(context);
    userData.put(USERNAME_FIELD, NOT_EXISTING_USERNAME_01);
    userData.put(FIRST_NAME_FIELD, "Carlos");
    userData.put(LAST_NAME_FIELD, "Álvarez");
    userData.put(PASSWORD_FIELD, "P@$$w0rd");
    usernameManager.create(userData);
    assertTrue(context.getDirectory().exists(NOT_EXISTING_USERNAME_01));
    assertTrue(context.getUsernameCache().exists(NOT_EXISTING_USERNAME_01));
    context.getDirectory().remove(NOT_EXISTING_USERNAME_01);
    assertFalse(context.getDirectory().exists(NOT_EXISTING_USERNAME_01));
    context.getUsernameCache().disposeDataSource();
  }

  @Test
  public final void testCreateWithCacheDisabled() throws Exception {
    Context context = new Context(CACHE_DISABLED_CONFIG_FILE_PATH);
    // Waits 2 seconds in order to give time to getDatasource method to retrieve a UsernameCache
    // object in case that cacheUsername config property is turned off.
    Thread.sleep(2000);
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof GoogleDirectory);
    UsernameManager usernameManager = new UsernameManager(context);
    userData.put(USERNAME_FIELD, NOT_EXISTING_USERNAME_02);
    userData.put(FIRST_NAME_FIELD, "Carlos");
    userData.put(LAST_NAME_FIELD, "Álvarez");
    userData.put(PASSWORD_FIELD, "P@$$w0rd");
    usernameManager.create(userData);
    assertTrue(context.getDirectory().exists(NOT_EXISTING_USERNAME_02));
    context.getDirectory().remove(NOT_EXISTING_USERNAME_02);
    assertFalse(context.getDirectory().exists(NOT_EXISTING_USERNAME_02));
  }

}
