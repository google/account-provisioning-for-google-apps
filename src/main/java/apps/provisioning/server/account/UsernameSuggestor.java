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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import apps.provisioning.server.Context;
import apps.provisioning.server.account.data.LockedDirectory;
import apps.provisioning.server.account.data.UsernameDataSource;

/**
 * Suggests the configured number of usernames.
 */
public class UsernameSuggestor {

  private Context context;
  LockedDirectory lockedUsernames;

  public UsernameSuggestor(Context context) {
    this.context = context;
    lockedUsernames = new LockedDirectory(context.getConfig());
  }

  /**
   * Generates and locks available usernames.
   *
   * @param userData Contains the fields filled by the user, at least firstname and lastname
   *        parameters must be present.
   * @return List with the username suggestions.
   * @throws Exception
   */
  public synchronized ArrayList<String> generate(HashMap<String, String> userData) throws Exception {
    ArrayList<String> suggestions = new ArrayList<String>();
    String[] patterns = context.getConfig().getPatterns();
    Integer numberOfSuggestions = context.getConfig().getNumberOfSuggestions();
    UsernameDataSource existingUsernames = context.getDatasource();
    UsernameIterator usernameIterator = new UsernameIterator(patterns, userData);
    while (suggestions.size() < numberOfSuggestions && usernameIterator.hasNext()) {
      String suggestion = usernameIterator.next();
      if (!lockedUsernames.exists(suggestion) && !existingUsernames.exists(suggestion)) {
        suggestions.add(suggestion);
        lockedUsernames.insert(suggestion);
      }
    }
    return suggestions;
  }

  /**
   * Selects the given username from the given username suggestions. This will unlock all the
   * suggestions, except the selected one.
   *
   * @param suggestions The suggested usernames.
   * @param selectedUsername The selected username.
   * @throws SQLException
   * @throws Exception
   */
  public void select(ArrayList<String> suggestions, String selectedUsername) throws SQLException,
  Exception {
    suggestions.remove(selectedUsername);
    lockedUsernames.removeMultiple(suggestions);
  }
}
