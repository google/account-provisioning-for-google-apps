/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package apps.provisioning.server.account.data;

import java.util.ArrayList;

/**
 * Interface that is used to retrieve a username from a data source.
 */
public interface UsernameDataSource {

  /**
   * Looks for existing user name in implemented data source.
   *
   * @return true whether the username was found.
   */
  public boolean exists(String username) throws Exception;

  /**
   * Inserts an username to the data source.
   *
   * @param username
   * @throws Exception
   */
  public void insert(String username) throws Exception;

  /**
   * Inserts multiple usernames in the data source.
   *
   * @param usernames
   */
  public void insertMultiple(ArrayList<String> usernames) throws Exception;

}
