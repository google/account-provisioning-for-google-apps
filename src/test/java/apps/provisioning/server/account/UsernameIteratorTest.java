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

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * UsernameIterator test class.
 */
public class UsernameIteratorTest {

  private final String[] DEFAULT_PATTERN = new String[] {"[firstname].[lastname]",
      "[firstname][lastname]", "[C1_firstname].[lastname]", "[firstname][C2_lastname]",
      "[firstname][id][firstname]", "[firstname][C2_faculty]", "[firstname][id][C1000_faculty]",
      "[C10_firstname][C10_lastname][#]"};

  private HashMap<String, String> userData;

  public UsernameIteratorTest() {}

  @Before
  public void setUp() throws Exception {
    userData = new HashMap<String, String>();
    userData.put("firstname", "Car-lo*s");
    // TODO: provide a modifier like C_ for breaking at space/word boundary?
    userData.put("lastname", "Álv arez#");
  }

  @Test
  public final void testWithoutCustomFields() throws Exception {
    Integer numberOfSuggestions = DEFAULT_PATTERN.length;
    UsernameIterator usernameIterator = new UsernameIterator(DEFAULT_PATTERN, userData);
    Integer counter = 0;
    String[] output = new String[numberOfSuggestions];
    while (usernameIterator.hasNext() && counter < numberOfSuggestions) {
      output[counter++] = usernameIterator.next();
    }
    // TODO: do we want to maintain hyphens in names like this?
    String[] expectedResult =
        new String[] {"car-los.alvarez", "car-losalvarez", "c.alvarez", "car-losal",
            "car-losalvarez1", "car-losalvarez2", "car-losalvarez3", "car-losalvarez4"};
    assertArrayEquals(expectedResult, output);
  }

  @Test
  public final void testWithOneCustomField() throws Exception {
    Integer numberOfSuggestions = DEFAULT_PATTERN.length;
    userData.put("id", "123456");
    UsernameIterator usernameIterator = new UsernameIterator(DEFAULT_PATTERN, userData);
    Integer counter = 0;
    String[] output = new String[numberOfSuggestions];
    while (usernameIterator.hasNext() && counter < numberOfSuggestions) {
      output[counter++] = usernameIterator.next();
    }
    String[] expectedResult =
        new String[] {"car-los.alvarez", "car-losalvarez", "c.alvarez", "car-losal",
            "car-los123456car-los", "car-losalvarez1", "car-losalvarez2", "car-losalvarez3"};
    assertArrayEquals(expectedResult, output);
  }

  @Test
  public final void testWithTwoCustomFields() throws Exception {
    Integer numberOfSuggestions = DEFAULT_PATTERN.length;
    userData.put("id", "123456");
    userData.put("faculty", "mmc");
    UsernameIterator usernameIterator = new UsernameIterator(DEFAULT_PATTERN, userData);
    Integer counter = 0;
    String[] output = new String[numberOfSuggestions];
    while (usernameIterator.hasNext() && counter < numberOfSuggestions) {
      output[counter++] = usernameIterator.next();
    }
    String[] expectedResult =
        new String[] {"car-los.alvarez", "car-losalvarez", "c.alvarez", "car-losal",
            "car-los123456car-los", "car-losmm", "car-los123456mmc", "car-losalvarez1"};
    assertArrayEquals(expectedResult, output);
  }

}
