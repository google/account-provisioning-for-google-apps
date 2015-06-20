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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import apps.provisioning.data.UsernameCache;
import apps.provisioning.server.Context;
import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

public class UsernameSuggestorTest {

  private HashMap<String, String> userData;

  @Before
  public void setUp() throws Exception {
    userData = new HashMap<String, String>();
  }

  /**
   * Gets one suggestion.
   *
   * @throws Exception
   */
  @Test
  public final void testGenerateOneSuggestion() throws Exception {
    userData.put("firstname", "Carlos");
    userData.put("lastname", "Álvarez");
    String configFile = "./test/change-number-of-suggestions-to-one.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultFirst = new String[] {"carlos.alvarez"};
    assertArrayEquals(expectedResultFirst, suggestions.toArray());
  }

  /**
   * Testing user suggestions and the selection process.
   *
   * @throws Exception
   */
  @Test
  public final void testGenerateUsernameAndSelectOne() throws Exception {
    userData.put("firstname", "Carlos");
    userData.put("lastname", "Álvarez");
    String configFile = "./test/apps-provisioning-test.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof GoogleDirectory);
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultFirst = new String[] {"carlos.alvarez", "carlosalvarez", "c.alvarez"};
    assertArrayEquals(expectedResultFirst, suggestions.toArray());
    usernameSuggestor.select(suggestions, "carlosalvarez");
    suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultSecond = new String[] {"carlos.alvarez", "c.alvarez", "carlos_alvarez"};
    assertArrayEquals(expectedResultSecond, suggestions.toArray());
  }

  /**
   * Testing user suggestions executed two times.
   *
   * @throws Exception
   */
  @Test
  public final void testGenerateUsernameTwoTimes() throws Exception {
    userData.put("firstname", "Carlos");
    userData.put("lastname", "Álvarez");
    String configFile = "./test/apps-provisioning-test.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultFirst = new String[] {"carlos.alvarez", "carlosalvarez", "c.alvarez"};
    assertArrayEquals(expectedResultFirst, suggestions.toArray());
    suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultSecond = new String[] {"carlos_alvarez", "carlosa", "carlosalvarez1"};
    assertArrayEquals(expectedResultSecond, suggestions.toArray());
  }

  /**
   * Testing user suggestions checking availability from UsernameCache and select method.
   *
   * @throws Exception
   */
  @Test
  public final void testGenerateAndSelectFromUsernameCache() throws Exception {
    userData.put("firstname", "Carlos");
    userData.put("lastname", "Álvarez");
    String configFile = "./test/apps-provisioning-test.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    Thread.sleep(2000);
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof UsernameCache);
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultFirst = new String[] {"carlos.alvarez", "carlosalvarez", "c.alvarez"};
    assertArrayEquals(expectedResultFirst, suggestions.toArray());
    usernameSuggestor.select(suggestions, "carlosalvarez");
    suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultSecond = new String[] {"carlos.alvarez", "c.alvarez", "carlos_alvarez"};
    assertArrayEquals(expectedResultSecond, suggestions.toArray());
  }

  /**
   * Tests a firstname with 60 characters, this test checks that duplicate values are excluded.
   *
   * @throws Exception
   */
  @Test
  public final void testLongFirstname() throws Exception {
    // 60 characters long.
    userData.put("firstname", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    userData.put("lastname", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
    String configFile = "./test/apps-provisioning-test.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    String[] expectedResultSecond =
        new String[] {
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbb"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
        "a.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "aaaaaaaaaabbbbbbbbbb1"};
    assertArrayEquals(expectedResultSecond, suggestions.toArray());
  }

  /**
   * Test when names have more than 60 characters. These must throw an exception.
   *
   * @throws Exception
   */
  @Test
  public final void testExceededFirstname() throws Exception {
    // 61 characters long.
    userData.put("firstname", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    userData.put("lastname", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
    String configFile = "./test/apps-provisioning-test.properties";
    Context context = new Context(configFile);
    UsernameSuggestor usernameSuggestor = new UsernameSuggestor(context);
    try {
      usernameSuggestor.generate(userData);
      fail("Exception must be thrown.");
    } catch (Exception e) {
      assertEquals("One of the fields exceds the maximum length. 60 (firstname,lastname).",
          e.getMessage());
    }
  }

}
