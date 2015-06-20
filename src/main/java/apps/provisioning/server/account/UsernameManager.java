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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import apps.provisioning.server.Context;

/**
 * Generates username suggestions and generates Google Apps accounts.
 */
public class UsernameManager {

  public static final String USERNAME = "username";
  public static final String FIRST_NAME = "firstname";
  public static final String LAST_NAME = "lastname";
  public static final String PASSWORD = "password";
  public static final String SUGGESTIONS = "suggestions";
  public static final Integer MAX_USERNAME_LENGTH = 64;
  public static final Integer MAX_NAME_LENGTH = 60;
  public static final Integer MIN_PASSWORD_LENGTH = 8;
  public static final Integer MAX_PASSWORD_LENGTH = 100;
  private final String IMPRESIONS_URL =
      "https://script.google.com/macros/s/AKfycbxPFUNaquIfejljS1F3xXnSEqi2zdlwlYu-0luqw-hS0_NQ6s5n/exec";

  private Context context;
  UsernameSuggestor usernameSuggestor;

  public UsernameManager(Context context) {
    this.context = context;
    this.usernameSuggestor = new UsernameSuggestor(context);
  }

  /**
   * Suggests usernames that are available, this checks LockedDirectory, Cache data source and
   * Google Apps Directory.
   *
   * @param userData This most contain at least firstname and lastname keys, custom fields are
   *        optional.
   * @return A list with the number of suggestions configured that are available.
   * @throws Exception
   */
  public ArrayList<String> suggest(HashMap<String, String> userData) throws Exception {
    if (userData == null) {
      throw new NullPointerException("User data parameter can't be null.");
    }
    ArrayList<String> suggestions = usernameSuggestor.generate(userData);
    setImpression("suggest");
    return suggestions;
  }

  /**
   * This creates users into Google Apps.
   *
   * @param userData
   * @throws Exception
   */
  public void create(HashMap<String, String> userData) throws Exception {
    if (userData == null) {
      throw new NullPointerException("User data parameter can't be null.");
    }
    String username = userData.get(USERNAME);
    String firstname = userData.get(FIRST_NAME);
    String lastname = userData.get(LAST_NAME);
    String password = userData.get(PASSWORD);
    create(username, firstname, lastname, password);
  }

  /**
   * Creates a Google Apps account.
   *
   * @param username Username without domain.
   * @param firstname First name.
   * @param lastname Last name.
   * @param password Password with 8 characters or longer.
   * @throws Exception
   */
  public void create(String username, String firstname, String lastname, String password)
      throws Exception {
    context.getDirectory().createUser(username, firstname, lastname, password);
    if (context.getConfig().getCacheUsernames()) {
      // This line updates the existing cache until it is refreshed.
      context.getUsernameCache().insert(username);
    }
    setImpression("create");
  }

  /**
   * Selects the given username from the given username suggestions. This will unlock all the
   * suggestions, except the selected one.
   *
   * @param suggestions A list of username suggestions.
   * @param selectedUsername The selected username.
   * @throws Exception
   */
  public void select(ArrayList<String> suggestions, String selectedUsername) throws Exception {
    usernameSuggestor.select(suggestions, selectedUsername);
  }

  /**
   * Selects the given username from the given username suggestions. This will unlock all the
   * suggestions, except the selected one.
   *
   * @param selectedData map with the following fields: "username" "suggestions".
   *
   * @throws Exception
   */
  public void select(HashMap<String, String> selectedData) throws Exception {
    if (selectedData == null) {
      throw new NullPointerException("User data parameter can't be null.");
    }
    String username = selectedData.get(USERNAME);
    String suggestionsString = selectedData.get(SUGGESTIONS);
    JSONArray suggestionsJson = new JSONArray(suggestionsString);
    ArrayList<String> suggestionsList = new ArrayList<String>();
    for (int i = 0; i < suggestionsJson.length(); i++) {
      suggestionsList.add(suggestionsJson.getString(i));
    }
    usernameSuggestor.select(suggestionsList, username);
    setImpression("select");
  }

  /**
   * Records an impression. Impressions are used to measure usage and justify dedicated resources to
   * support this API.
   *
   * @param action The name of the REST method.
   */
  private void setImpression(final String action) {
    // Prevents delay in the impression request.
    (new Thread() {
      @Override
      public void run() {
        String parameters = "?domain=" + context.getConfig().getDomain() + "&action=" + action;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(IMPRESIONS_URL + parameters);
        try {
          httpClient.execute(getRequest);
        } catch (Exception e) {
        }
      }
    }).start();
  }
}
