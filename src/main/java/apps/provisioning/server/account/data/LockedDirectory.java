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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import apps.provisioning.config.ConfigData;

/**
 * In-memory data source that locks usernames to prevent to be taken meanwhile user is choosing one
 * option.
 */
public class LockedDirectory implements UsernameDataSource {

  /**
   * Class to compare entries by their timestamp.
   *
   */
  private class LockedDirectoryComparator implements Comparator<LockedDirectoryEntry> {

    /*
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(LockedDirectoryEntry entry1, LockedDirectoryEntry entry2) {
      if (entry1.equals(entry2)) {
        return 0;
      }
      if (entry1.getTimestamp().after(entry2.getTimestamp())) {
        return 1;
      } else {
        return -1;
      }
    }
  }

  /**
   * Contains a locked username and the time when it was locked. Used to keep track of expired
   * usernames.
   */
  private class LockedDirectoryEntry {
    String username;
    Timestamp timestamp;

    public LockedDirectoryEntry(String username) {
      this.username = username;
      timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getTimestamp() {
      return timestamp;
    }

    public String getUsername() {
      return username;
    }

    @Override
    public boolean equals(Object entry) {
      return ((LockedDirectoryEntry) entry).getUsername().equals(username);
    }
  }

  // In-memory data source.
  private PriorityQueue<LockedDirectoryEntry> lockedEntries;
  private long suggestedUsernamesTimeout;

  public LockedDirectory(ConfigData config) {
    lockedEntries = new PriorityQueue<LockedDirectory.LockedDirectoryEntry>(
        10, new LockedDirectoryComparator());
    // Get the suggested usernames timeout and convert it to milliseconds.
    suggestedUsernamesTimeout = config.getSuggestedUsernamesTimeout() * 1000;
  }

  public synchronized boolean exists(String username) {
    removeExpiredUsernames();
    return lockedEntries.contains(new LockedDirectoryEntry(username));
  }

  public synchronized void insert(String username) throws Exception {
    if (exists(username)) {
      throw new Exception("Username alrealy exists in Locked Directory.");
    }
    lockedEntries.add(new LockedDirectoryEntry(username));
  }


  /**
   * Removes expired usernames.
   */
  private void removeExpiredUsernames() {
    if (lockedEntries.size() == 0) {
      return;
    }
    Timestamp now = new Timestamp(System.currentTimeMillis());
    LockedDirectoryEntry entry = lockedEntries.peek();
    while (entry != null) {
      // The peek of the queue is always the oldest entry. Check if it has expired and if so, remove
      // it.
      Timestamp oldestTimestamp = entry.getTimestamp();
      long diff = now.getTime() - oldestTimestamp.getTime();
      if (diff > suggestedUsernamesTimeout) {
        // Remove the expired entry.
        lockedEntries.poll();
        // Get the new oldest entry so it is checked it in the next loop.
        entry = lockedEntries.peek();
      } else {
        // The oldest entry is not expired yet. It is safe to assume the rest of the entries are
        // still valid.
        entry = null;
      }
    }
  }

  /**
   * Removes a username from the locked data source.
   *
   * @param username The user name.
   * @return Whether the user was deleted.
   */
  public boolean remove(String username) {
    return lockedEntries.remove(new LockedDirectoryEntry(username));
  }

  /**
   * Removes usernames from the locked data source.
   *
   * @param usernames Usernames collection.
   */
  public void removeMultiple(Collection<String> usernames) {
    for (String username : usernames) {
      lockedEntries.remove(new LockedDirectoryEntry(username));
    }
  }

  /**
   * Inserts multiple usernames to the locked data source.
   *
   * @param usernames ArrayList with usernames to be added.
   */
  public void insertMultiple(ArrayList<String> usernames) {
    for (String username : usernames) {
      lockedEntries.add(new LockedDirectoryEntry(username));
    }
  }

  /**
   * Clears the HashMap.
   */
  public void clear() {
    lockedEntries.clear();
  }
}
