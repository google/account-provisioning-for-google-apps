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

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.provisioning.util.Utils;

/**
 * Generates username suggestions. This is designed as an Iterator, so it can be controlled by
 * hasNext and next methods.
 */
public class UsernameIterator implements Iterator<String> {

  // Matches every pattern, where each pattern is a string like "[fieldName]" for any value of
  // "fieldName" (e.g., [firstname], [lastname], [studentId]) with optional "C#" in front
  // which indicates where to shorten the field (e.g., [C3_firstname] would be the first three
  // characters of the first name.
  // Also matches [#], which is used for an auto-incrementing number.
  private final Pattern PATTERN_REGEX = Pattern.compile("\\[(?:C(\\d+)_)?([#\\w]+)\\]");

  private final String DEFAULT_PATTERN = "[C9_firstname][C9_lastname][#]";
  private final String AUTONUMERIC_PATTERN = "[#]";
  private final Logger logger = Logger.getLogger(UsernameIterator.class.getName());

  private HashMap<String, String> userData;
  private Integer autonumeric;
  private Integer patternIndex;
  private String currentPattern;
  private String nextSuggestion;
  private String[] patterns;

  /**
   * Creates a UsernameIterator object.
   *
   * @param patterns The patterns to be evaluated as a String array.
   * @param userData The user information, the required fields are at least first name and last
   *        name.
   * @throws Exception
   */
  public UsernameIterator(String[] patterns, HashMap<String, String> userData) throws Exception {
    if (userData == null || patterns == null) {
      throw new Exception("The patterns and userData parameters can't be set as null.");
    }
    if (!userData.containsKey(UsernameManager.FIRST_NAME)
        || !userData.containsKey(UsernameManager.LAST_NAME)) {
      throw new Exception(
          "The userData parameter must cointain at least firstname and lastname fields.");
    }
    String firstname = userData.get(UsernameManager.FIRST_NAME);
    String lastname = userData.get(UsernameManager.LAST_NAME);
    if (firstname.length() > UsernameManager.MAX_NAME_LENGTH
        || lastname.length() > UsernameManager.MAX_NAME_LENGTH) {
      throw new Exception("One of the fields exceds the maximum length. 60 (firstname,lastname).");
    }
    this.patterns = patterns;
    this.userData = userData;
    autonumeric = 1;
    patternIndex = 0;
  }

  /**
   * Retrieves the following pattern to be evaluated. If the pattern contains the autonumeric symbol
   * it returns the same pattern.
   */
  private String getNextPattern() {
    if (currentPattern != null && currentPattern.contains(AUTONUMERIC_PATTERN)) {
      // Retrieves the previous generated pattern.
      return currentPattern;
    }
    if (patternIndex < patterns.length) {
      return patterns[patternIndex++];
    } else {
      // No other pattern is available, so it returns the default.
      return DEFAULT_PATTERN;
    }
  }

  /**
   * Evaluates the current pattern and replaces the tags with user fields.
   *
   * @return Evaluated suggestion.
   */
  private String processPattern() {
    currentPattern = getNextPattern();
    String suggestion = currentPattern;
    Matcher matcher = PATTERN_REGEX.matcher(suggestion);
    String fieldValue = "";
    while (matcher.find()) {
      String key = matcher.group(0);
      String fieldName = matcher.group(2);
      if (key.equals(AUTONUMERIC_PATTERN)) {
        fieldValue = "" + autonumeric++;
      } else {
        fieldValue = Utils.replaceSpecialChars(userData.get(fieldName));
        if (fieldValue == null) {
          logger.log(
              Level.WARNING,
              "Field " + fieldName + " was not provided in the " + key + " pattern for user "
                  + userData.get(UsernameManager.FIRST_NAME) + " "
                  + userData.get(UsernameManager.LAST_NAME));
          return processPattern();
        }
        // Gets the matched string and retrieves the number of characters to be extracted.
        String substring_matcher = matcher.group(1);
        if (substring_matcher != null) {
          Integer characters = Integer.parseInt(substring_matcher);
          if (fieldValue.length() > characters) {
            fieldValue = fieldValue.substring(0, characters);
          }
        }
      }
      suggestion = matcher.replaceFirst(fieldValue);
      matcher.reset(suggestion);
    }
    if (suggestion.isEmpty()) {
      // This case happens when all the characters are invalid.
      return processPattern();
    }
    return suggestion;
  }

  /**
   * Checks if the next element is not longer than 64 characters.
   */
  public boolean hasNext() {
    nextSuggestion = processPattern();
    if (currentPattern == DEFAULT_PATTERN
        && nextSuggestion.length() > UsernameManager.MAX_USERNAME_LENGTH) {
      logger.log(Level.WARNING, "The user name has exceeded the maximum length.");
      nextSuggestion = null;
      return false;
    }
    return true;
  }

  /**
   * Retrieves the next suggestion
   */
  public String next() {
    if (nextSuggestion != null) {
      String next = nextSuggestion;
      nextSuggestion = null;
      return next;
    }
    return processPattern();
  }

  /**
   * This is not necessary.
   */
  public void remove() {
    throw new RuntimeException("Remove method doesn't apply to UsernameIterator.");
  }

}
