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

package apps.provisioning;


import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class CommandsTest {

  Commands commands;

  public void createCommands(String[] args) {
    createCommands(args, false);
  }

  public void createCommands(String[] args, boolean shouldFail) {
    try {
      commands = new Commands(args);
    } catch (ParseException e) {
      if (!shouldFail) {
        Assert.fail("Error when processing command line arguments:" + e.getMessage());
      }
    }
  }

  @Test
  public void testNoArgs() {
    String[] args = {};
    createCommands(args);
    Assert.assertNotNull(commands);
  }

  @Test
  public void testNoUnrecognizedArg() {
    String[] args = {"-unrecognizedarg"};
    createCommands(args, true);
    Assert.assertNull(commands);
  }

  @Test
  public void testRestArg() {
    String[] args = {"-rest"};
    createCommands(args);

    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.hasCommand(Commands.REST_API_COMMAND_OPTION));
  }

  @Test
  public void testRestArgWithValueIsIgnored() {
    String[] args = {"-rest", "value"};
    createCommands(args);

    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.hasCommand(Commands.REST_API_COMMAND_OPTION));
    Assert.assertNull(commands.getCommandValue(Commands.REST_API_COMMAND_OPTION));
  }

  @Test
  public void testPortArg() {
    Integer expectedPort = new Integer(8081);
    String[] args = {"-port", expectedPort.toString()};
    createCommands(args);

    Assert.assertNotNull(commands);
    Assert.assertTrue(commands.hasCommand(Commands.PORT_COMMAND_OPTION));
    Assert.assertEquals(expectedPort, commands.getCommandIntValue(Commands.PORT_COMMAND_OPTION));
  }

  @Test
  public void testPortArgInvalidValue() {
    String expectedPort = "string";
    String[] args = {"-port", expectedPort};
    createCommands(args);

    try {
      commands.getCommandIntValue(Commands.PORT_COMMAND_OPTION);
      Assert.fail("Should have failed to parse the invalid port value.");
    } catch (NumberFormatException e) {
      // This is expected. Test should pass.
    }
  }
}
