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

package apps.provisioning.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Static helper methods
 */
public class Utils {

  static final String REGEXP_SPECIAL_CHARS = "[^a-z0-9_\\.\\-]";
  static final int USERNAME_MAX_LENGTH = 60;

  /**
   * Replaces special characters to e-mail valid characters.
   *
   * @param username User name to be formatted.
   * @return Formatted text
   */
  public static String replaceSpecialChars(String username) {
    if (username == null) {
      return null;
    }
    username = username.toLowerCase();
    // Replaces characters with accents for the same characters without accents.
    username = StringUtils.stripAccents(username);
    // Filters e-mail valid characters
    username = username.replaceAll(REGEXP_SPECIAL_CHARS, "");
    // The maximum Google Apps username length is 60 characters
    if (username.length() > USERNAME_MAX_LENGTH) {
      username = username.substring(0, USERNAME_MAX_LENGTH);
    }
    return username;
  }
}
