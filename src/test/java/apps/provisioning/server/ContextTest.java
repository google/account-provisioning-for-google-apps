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

package apps.provisioning.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Test;

import apps.provisioning.data.UsernameCache;
import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

public class ContextTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void testGetsUernameCacheWhenCacheDisabled() throws SQLException, Exception {
    Context context = new Context("./test/cache-disabled.properties");
    try {
      context.getUsernameCache();
      fail("This method must throw an exception.");
    } catch (Exception e) {
      assertEquals("Caching is not enabled", e.getMessage());
    }
  }

  @Test
  public final void testGetsUernameCacheWhenCacheEnabled() throws SQLException, Exception {
    Context context = new Context("./test/cache-enabled.properties");
    assertNotNull(context.getUsernameCache());
  }

  @Test
  public final void testGetsDataSourceWhenCacheEnabled() throws SQLException, Exception {
    Context context = new Context("./test/cache-enabled.properties");
    // Wait until caching is finished.
    Thread.sleep(60000);
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof UsernameCache);
  }

  @Test
  public final void testGetsDataSourceWhenCacheDisabled() throws SQLException, Exception {
    Context context = new Context("./test/cache-disabled.properties");
    UsernameDataSource dataSource = context.getDatasource();
    assertTrue(dataSource instanceof GoogleDirectory);
  }

}
