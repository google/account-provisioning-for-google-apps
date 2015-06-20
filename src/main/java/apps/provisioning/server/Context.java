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

import java.sql.SQLException;

import apps.provisioning.config.ConfigData;
import apps.provisioning.data.UsernameCache;
import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

/**
 * Encapsulates the common objects for different modules.
 */
public class Context {

  private ConfigData config;
  private GoogleDirectory directory;
  private UsernameCache usernameCache;
  private boolean useCache = false;
  private final Integer INITIAL_UPDATE_RATE_IN_SECONDS = 0;

  /**
   * The common objects are created here.
   *
   * @throws Exception
   * @throws SQLException
   */
  public Context(String configFilePath) throws SQLException, Exception {
    config = new ConfigData(configFilePath);
    directory = new GoogleDirectory(config);
    if (config.getCacheUsernames()) {
      String dbPath = config.getDbPath();
      String dbName = config.getDbName();
      Integer updateRateInSeconds = config.getCacheExpirationHours() * 3600;
      usernameCache =
          new UsernameCache(INITIAL_UPDATE_RATE_IN_SECONDS, updateRateInSeconds, dbPath, dbName,
              directory);
      useCache = true;
    }
  }

  /**
   * Gets the configuration object.
   *
   * @return Configuration object.
   */
  public ConfigData getConfig() {
    return config;
  }

  /**
   * Gets the Google Directory object from the Admin SDK library.
   *
   * @return Google Directory Object
   */
  public GoogleDirectory getDirectory() {
    return directory;
  }

  /**
   * Gets the UsernameCache object. This is exposed just for testing, use getDatasource instead.
   *
   * @return UsernameCache Object
   * @throws Exception
   */
  public UsernameCache getUsernameCache() throws Exception {
    if (!useCache) {
      throw new Exception("Caching is not enabled");
    }
    return usernameCache;
  }

  /**
   * Gets the DataSource object.
   *
   * @return UsernameCache if cache is enabled or Google Directory if not.
   */
  public UsernameDataSource getDatasource() {
    if (useCache && usernameCache.isReady()) {
      return usernameCache;
    }
    return directory;
  }
}
