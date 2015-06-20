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

package apps.provisioning.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

/**
 * Test of ConfigData class
 */
public class ConfigDataTest {

  /**
   * Test valid file.
   *
   * @throws IOException
   * @throws FileNotFoundException
   *
   * @throws Exception
   */
  @Test
  public final void testValidFile() throws FileNotFoundException, IOException, Exception {
    new ConfigData("./test/valid.properties");
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   */
  @Test
  public final void testFileNotFound() {
    try {
      new ConfigData("./test/not-existing.properties");
    } catch (FileNotFoundException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("An unexpected exception was thrown.");
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testBadAuthUser() throws Exception {
    try {
      new ConfigData("./test/bad-auth-user.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testBadCachedUsernames() throws Exception {
    try {
      new ConfigData("./test/bad-cached-usernames.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   */
  @Test
  public final void testBadDbPath() {
    try {
      new ConfigData("./test/bad-db-path.properties");
      fail("An incorrect value was passed as valid.");
    } catch (FileNotFoundException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("An unexpected exception was thrown.");
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testBadDomain() throws Exception {
    try {
      new ConfigData("./test/bad-domain.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws IOException
   *
   * @throws Exception
   */
  @Test
  public final void testBadKeyPath() throws Exception {
    try {
      new ConfigData("./test/bad-key-path.properties");
      fail("This should fail because the file doesn't exist.");
    } catch (FileNotFoundException e) {
      assertEquals("File declared in apis.GoogleAPIs.keyPath doesn't exist.", e.getMessage());
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws IOException
   *
   * @throws Exception
   */
  @Test
  public final void testBadKeyStorePath() throws IOException, Exception {
    try {
      new ConfigData("./test/bad-key-store-path.properties");
      fail("This should fail because the file doesn't exist.");
    } catch (FileNotFoundException e) {
      assertEquals("File declared in security.ssl.keyStorePath doesn't exist.", e.getMessage());
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testBadNumberOfSuggestions() throws Exception {
    try {
      new ConfigData("./test/bad-number-of-suggestions.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testBadServiceAccountEmail() throws Exception {
    try {
      new ConfigData("./test/bad-service-account-email.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testEmptyAppName() throws Exception {
    try {
      new ConfigData("./test/empty-app-name.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testEmptyAuthUser() throws Exception {
    try {
      new ConfigData("./test/empty-auth-user.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testEmptyDomain() throws Exception {
    try {
      new ConfigData("./test/empty-domain.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testEmptyKeyPath() throws Exception {
    try {
      new ConfigData("./test/empty-key-path.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  /**
   * Test method for {@link apps.provisioning.config.ConfigData}.
   *
   * @throws Exception
   */
  @Test
  public final void testEmptyServiceAccountEmail() throws Exception {
    try {
      new ConfigData("./test/empty-service-account-email.properties");
      fail("An incorrect value was passed as valid.");
    } catch (Exception e) {
      assertTrue(true);
    }
  }

}
