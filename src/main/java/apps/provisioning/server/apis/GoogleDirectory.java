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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import apps.provisioning.config.ConfigData;
import apps.provisioning.server.account.UsernameManager;
import apps.provisioning.server.account.data.UsernameDataSource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.UserName;
import com.google.api.services.admin.directory.model.Users;

/**
 * Manages the Admin SDK service.
 */
public class GoogleDirectory extends GoogleClient implements UsernameDataSource {

  private GoogleCredential credential;
  private Directory directory;

  /**
   * Represents the administrator customerId.
   *
   * @link https://developers.google.com/admin-sdk/directory/v1/reference/users/list
   */
  private final String CUSTOMER_ID = "my_customer";

  /**
   * Number of users that can be retrieved per each API call. Maximum is 500. Acceptable values are
   * 1 to 500.
   *
   * @link https://developers.google.com/admin-sdk/directory/v1/reference/users/list
   */
  private final int MIN_RESULTS = 1;
  private final int MAX_RESULTS = 500;

  /**
   * Constructor used for testing only.
   */
  public GoogleDirectory() {
    super();
  }

  /**
   * Initializes Admin SDK credentials.
   *
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public GoogleDirectory(ConfigData config) throws GeneralSecurityException, IOException, Exception {
    super(config);
    credential = getCredentialForServiceAccount(serviceAccountEmail, keyPath);
    directory = createAuthorizedClient(appName, credential);
    // Forces Google Apps authentication (it happens in the first API call) to prevent delay in the
    // next API call (up to 30 seconds).
    directory.users().list().setCustomer(CUSTOMER_ID).setMaxResults(MIN_RESULTS).execute();
  }

  public boolean exists(String username) {
    return getUser(username) != null;
  }

  /**
   * Retrieves the requested username from Google Apps.
   *
   * @param username Username without domain.
   * @return Whether it is not found, it returns null
   */
  private User getUser(String username) {
    try {
      return directory.users().get(getEmail(username)).execute();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Retrieves the basic information of all the users from the configured domain.
   *
   * @return List with all the users.
   * @throws Exception
   */
  public void copyToDataSource(UsernameDataSource dataSource) throws Exception {
    Directory.Users.List request = directory.users().list();
    // This constant applies to every domain or multi domain Google Apps
    // account.
    request.setCustomer(CUSTOMER_ID);
    request.setMaxResults(MAX_RESULTS);
    request.setDomain(domain);
    // Get all users
    do {
      ArrayList<String> usernames = new ArrayList<String>();
      Users currentPage = request.execute();
      List<User> users = currentPage.getUsers();
      for (int i = 0; i < users.size(); i++) {
        usernames.add(users.get(i).getPrimaryEmail().split("@")[0]);
      }
      dataSource.insertMultiple(usernames);
      request.setPageToken(currentPage.getNextPageToken());
    } while (request.getPageToken() != null && request.getPageToken().length() > 0);
  }

  /**
   * Creates a user in Google Apps Diretory.
   *
   * @param username Username without domain.
   * @param firstname First name
   * @param lastname Last name
   * @param password Password with 8 characters or longer.
   * @return The created user.
   * @throws IOException
   * @throws Exception When values are null, empty, shorter or longer than allowed.
   */
  public User createUser(String username, String firstname, String lastname, String password)
      throws IOException, Exception {
    if (username == null || firstname == null || lastname == null || password == null) {
      throw new Exception("Null values are not allowed.");
    }
    if (username.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || password.isEmpty()) {
      throw new Exception("All the parameters must be filled.");
    }
    if (username.length() > UsernameManager.MAX_USERNAME_LENGTH
        || firstname.length() > UsernameManager.MAX_NAME_LENGTH
        || lastname.length() > UsernameManager.MAX_NAME_LENGTH
        || password.length() > UsernameManager.MAX_PASSWORD_LENGTH) {
      throw new Exception(
          "One of the fields exceds the maximum length. 60 (firstname,lastname), 64 (username),"
              + " 100 (password)");
    }
    if (password.length() < UsernameManager.MIN_PASSWORD_LENGTH) {
      throw new Exception("Password must have at least 8 characters.");
    }
    User user = new User();
    UserName name = new UserName();
    name.setGivenName(firstname);
    name.setFamilyName(lastname);
    user.setName(name);
    user.setPrimaryEmail(getEmail(username));
    user.setPassword(password);
    return directory.users().insert(user).execute();
  }

  public void insert(String username) throws Exception {
    throw new Exception("insert is not implemented in GoogleDirectory. Call createUser instead.");
  }

  public void insertMultiple(ArrayList<String> usernames) throws Exception {
    throw new Exception(
        "insertMultiple is not implemented in GoogleDirectory. Call createUser instead.");
  }

  /**
   * Deletes a user.
   *
   * @param username Username without domain.
   * @throws IOException
   */
  public void remove(String username) throws IOException {
    directory.users().delete(getEmail(username)).execute();
  }

  /**
   * Appends the configured domain to username.
   *
   * @param username Username without domain.
   * @return Email
   */
  private String getEmail(String username) {
    return username + "@" + domain;
  }

}
