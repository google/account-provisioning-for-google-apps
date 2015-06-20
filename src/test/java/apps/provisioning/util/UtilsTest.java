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

package apps.provisioning.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test of class Utils.
 */
public class UtilsTest {

  /**
   * Test method for {@link apps.provisioning.util.Utils#replaceSpecialChars(java.lang.String)}.
   * More than 60 characters Test.
   */
  @Test
  public void testChecksMaxLength() {
    String input, output;
    input = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    output = Utils.replaceSpecialChars(input);
    assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", output);
  }

  /**
   * Test method for {@link apps.provisioning.util.Utils#replaceSpecialChars(java.lang.String)}.
   */
  @Test
  public void testSpecialCharacters() {
    String input, output;
    input = "carlos.á样本ñ*)lvarez";
    output = Utils.replaceSpecialChars(input);
    assertEquals("carlos.anlvarez", output);
    input = "carlos.á样本*)lvarez";
    output = Utils.replaceSpecialChars(input);
    assertEquals("carlos.alvarez", output);
    input = "¡¿ÄäÀàÁáÂâÃãÅåǍǎĄąĂăÆæÇçĆćĈĉČčĎđĐďðÈèÉéÊêËëĚěĘęĜĝĢģĞğĤĥ";
    output = Utils.replaceSpecialChars(input);
    // Invalid characters are: ¡¿ÆæđĐð
    assertEquals("aaaaaaaaaaaaaaaaaaccccccccddeeeeeeeeeeeegggggghh", output);
    input = "ÌìÍíÎîÏïıĴĵĶķĹĺĻļŁłĽľÑñŃńŇňÖöÒòÓóÔôÕõŐőØøŒœŔŕŘřẞßŚśŜŝŞşŠš";
    output = Utils.replaceSpecialChars(input);
    // Invalid characters are: ıŁłØøŒœẞß
    assertEquals("iiiiiiiijjkkllllllnnnnnnoooooooooooorrrrssssssss", output);
    input = "ŤťŢţÞþÜüÙùÚúÛûŰűŨũŲųŮůŴŵÝýŸÿŶŷŹźŽžŻż";
    output = Utils.replaceSpecialChars(input);
    // Invalid characters are: Þþ
    assertEquals("ttttuuuuuuuuuuuuuuuuwwyyyyyyzzzzzz", output);
    input = "¡¿ÆæđĐðıŁłØøŒœẞßÞþ";
    output = Utils.replaceSpecialChars(input);
    assertEquals("", output);
  }

  /**
   * Test method for {@link apps.provisioning.util.Utils#replaceSpecialChars(java.lang.String)}.
   * Replace special characters.
   */
  @Test
  public void testStripsWhiteSpaces() {
    String input, output;
    input = "Carlos Álvarez";
    output = Utils.replaceSpecialChars(input);
    assertEquals("carlosalvarez", output);
  }

  /**
   * Test method for {@link apps.provisioning.util.Utils#replaceSpecialChars(java.lang.String)}.
   * Maintains email valid characters.
   */
  @Test
  public void testPreserveValidCharacters() {
    String input, output;
    input = "Carlos-Álvarez";
    output = Utils.replaceSpecialChars(input);
    assertEquals("carlos-alvarez", output);
  }

}
