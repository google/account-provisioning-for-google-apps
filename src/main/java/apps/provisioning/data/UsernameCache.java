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
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import apps.provisioning.server.account.data.UsernameDataSource;
import apps.provisioning.server.apis.GoogleDirectory;

/**
 * Contains an H2 DB and refreshes it periodically with all the user names from the Google
 * Directory.
 */
public class UsernameCache implements UsernameDataSource {

  private final Logger logger = Logger.getLogger(UsernameCache.class.getName());

  public static String STATUS_READY = "ready";
  public static String STATUS_REFRESHING = "refreshing";
  public static String STATUS_CACHING = "caching";
  public static String STATUS_EMPTY = "empty";
  public static String STATUS_DISPOSED = "disposed";

  private final ScheduledExecutorService updateScheduler = Executors
      .newSingleThreadScheduledExecutor();
  private int initialUpdateDelayInSeconds;
  private int updateRateInSeconds;
  private H2DataSource dataSource;
  private GoogleDirectory googleDirectory;
  private String databasePath;
  private String databaseName;
  private String status;
  // List that is used to store usernames that were created before the cache is
  // ready.
  private ArrayList<String> tempUsernames;

  /**
   * Initializes the cache. Populates it after initialUpdateDelayInSeconds and refreshes it every
   * updateRateInSeconds after that.
   *
   * @param initialUpdateDelayInSeconds The seconds to wait for the first time the cache will be
   *        populated.
   * @param updateRateInSeconds The second rate to refresh the cache.
   * @param databasePath The path where the H2 DB will be created.
   * @param databaseName The name of the H2 DB that will be created.
   * @param googleDirectory The Google Directory. Used to get the user names from Google.
   * @throws SQLException
   * @throws Exception
   */
  public UsernameCache(int initialUpdateDelayInSeconds, int updateRateInSeconds,
      String databasePath, String databaseName, GoogleDirectory googleDirectory)
      throws SQLException, Exception {
    this.initialUpdateDelayInSeconds = initialUpdateDelayInSeconds;
    this.updateRateInSeconds = updateRateInSeconds;
    this.googleDirectory = googleDirectory;
    this.databasePath = databasePath;
    this.databaseName = databaseName;
    this.tempUsernames = new ArrayList<String>();
    this.status = STATUS_EMPTY;
    initDataSource();
  }

  /**
   * Initializes the cache. Populates it after initialUpdateDelayInSeconds and refreshes it every
   * updateRateInSeconds after that.
   *
   * @throws Exception
   */
  private void initDataSource() throws Exception {
    updateScheduler.scheduleWithFixedDelay(new Runnable() {
      public void run() {
        try {
          refreshCache();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }, initialUpdateDelayInSeconds, updateRateInSeconds, TimeUnit.SECONDS);
  }

  /**
   * Refreshes the cache.
   *
   * @throws Exception
   */
  private synchronized void refreshCache() throws Exception {
    if (dataSource == null) {
      this.status = STATUS_CACHING;
    } else {
      this.status = STATUS_REFRESHING;
    }
    Date date = new Date();
    H2DataSource newDataSource =
        new H2DataSource(databasePath, databaseName + "(" + date.toString() + ")");
    // Fill out the temporary data source while the old data source serves
    // calls.
    googleDirectory.copyToDataSource(newDataSource);
    // Point the old data source to the new one and dispose the old one.
    H2DataSource oldDataSource = dataSource;
    dataSource = newDataSource;
    if (oldDataSource != null) {
      oldDataSource.dispose();
    }
    // Insert any username that was created while the cache was being updated
    // and is not in the cache yet.
    for (String username : tempUsernames) {
      if (!dataSource.exists(username)) {
        dataSource.insert(username);
      }
    }
    tempUsernames.clear();
    this.status = STATUS_READY;
  }

  /**
   * @return Whether the cache is ready to be read.
   */
  public boolean isReady() {
    return status == STATUS_READY || status == STATUS_REFRESHING;
  }


  /**
   * @return The current cache status.
   */
  public String getStatus() {
    return status;
  }

  public boolean exists(String username) throws SQLException, Exception {
    if (dataSource == null) {
      throw new Exception("Should not call exists if the data source hasn't been created");
    }
    if (this.status != STATUS_READY) {
      if (this.status == STATUS_REFRESHING) {
        logger.log(Level.WARNING, "Checking an out-of-date cache.");
      } else {
        throw new Exception("Trying to read the cache when it's not ready. Current status: "
            + status);
      }
    }
    return dataSource.exists(username);
  }

  public void insert(String username) throws SQLException, Exception {
    if (status != STATUS_READY) {
      // Cache isn't ready. Insert the username in a temporary list, which will
      // be inserted in the cache when ready.
      tempUsernames.add(username);
    } else {
      dataSource.insert(username);
    }
  }

  public void insertMultiple(ArrayList<String> usernames) throws SQLException, Exception {
    throw new Exception("Should not call to insertMultiple outside of the cache.");
  }

  /**
   * Disposes the H2 data source.
   *
   * @throws Exception
   */
  public synchronized void disposeDataSource() throws Exception {
    if (dataSource == null) {
      return;
    }
    dataSource.dispose();
    this.dataSource = null;
    this.status = STATUS_DISPOSED;
  }

  /**
   * Used only in testing.
   *
   * @throws Exception
   */
  public synchronized void reset() throws Exception {
    dataSource.reset();
  }
}
