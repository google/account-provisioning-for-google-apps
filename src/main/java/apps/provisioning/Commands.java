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

package apps.provisioning;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Class that parses and retrieves command line arguments.
 */
public final class Commands {
  // App name shown in the command line help.
  private static final String APP_NAME = "apps-provisioning";

  // Command line options.
  public static final String REST_API_COMMAND_OPTION = "rest";
  public static final String REST_COMMAND_DESCRIPTION = "runs the app in RESTful API mode";
  public static final boolean REST_HAS_ARGUMENT = false;
  public static final boolean REST_IS_REQUIRED = false;

  public static final String PORT_COMMAND_OPTION = "port";
  public static final String PORT_COMMAND_DESCRIPTION =
      "the port where the server will run. Used when running in RESTful API mode.";
  public static final boolean PORT_HAS_ARGUMENT = true;
  public static final boolean PORT_IS_REQUIRED = false;

  public static final String HELP_COMMAND_OPTION = "help";
  public static final String HELP_COMMAND_DESCRIPTION =
      "prints the description of all the commands";
  public static final boolean HELP_HAS_ARGUMENT = false;
  public static final boolean HELP_IS_REQUIRED = false;

  public static final String CONFIG_COMMAND_OPTION = "config";
  public static final String CONFIG_COMMAND_DESCRIPTION = "the configuration file path.";
  public static final boolean CONFIG_HAS_ARGUMENT = true;
  public static final boolean CONFIG_IS_REQUIRED = false;

  private Options options;
  private CommandLine parsedArguments;

  /**
   * @param args The command line arguments.
   * @throws ParseException
   */
  public Commands(String[] args) throws ParseException {
    initOptions();
    parseArgs(args);
  }

  /**
   * Initializes the options for the app.
   */
  private void initOptions() {
    options = new Options();
    // Create the options.
    Option restOption =
        new Option(REST_API_COMMAND_OPTION, REST_HAS_ARGUMENT, REST_COMMAND_DESCRIPTION);
    restOption.setRequired(REST_IS_REQUIRED);
    options.addOption(restOption);
    Option portOption =
        new Option(PORT_COMMAND_OPTION, PORT_HAS_ARGUMENT, PORT_COMMAND_DESCRIPTION);
    portOption.setRequired(PORT_IS_REQUIRED);
    options.addOption(portOption);
    Option helpOption =
        new Option(HELP_COMMAND_OPTION, HELP_HAS_ARGUMENT, HELP_COMMAND_DESCRIPTION);
    helpOption.setRequired(HELP_IS_REQUIRED);
    options.addOption(helpOption);
    Option configOption =
        new Option(CONFIG_COMMAND_OPTION, CONFIG_HAS_ARGUMENT, CONFIG_COMMAND_DESCRIPTION);
    configOption.setRequired(CONFIG_IS_REQUIRED);
    options.addOption(configOption);
  }

  /**
   * Parses the given arguments.
   *
   * @param args The command line arguments.
   * @throws ParseException
   */
  private void parseArgs(String[] args) throws ParseException {
    // Parse the arguments.
    CommandLineParser parser = new PosixParser();
    parsedArguments = parser.parse(options, args);
  }

  /**
   * @param command The command.
   * @return Whether the given command was passed in the command line.
   */
  public boolean hasCommand(String command) {
    return parsedArguments.hasOption(command);
  }

  /**
   * @param command The command.
   * @return The value for the given command.
   */
  public String getCommandValue(String command) {
    return parsedArguments.getOptionValue(command);
  }

  /**
   * @param command The command.
   * @return The integer value for the given command.
   */
  public Integer getCommandIntValue(String command) throws NumberFormatException {
    String value = getCommandValue(Commands.PORT_COMMAND_OPTION);
    if (value == null) {
      return null;
    }
    return new Integer(value);
  }

  /**
   * Prints the help for all the commands.
   */
  public void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(APP_NAME, options);
  }
}
