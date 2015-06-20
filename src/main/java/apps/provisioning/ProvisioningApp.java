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

import java.util.logging.Level;
import java.util.logging.Logger;

import apps.provisioning.server.Context;
import apps.provisioning.server.account.UsernameManager;
import apps.provisioning.server.rest.RESTApp;

/**
 * The provisioning application.
 */
public class ProvisioningApp extends Application {

  private static ProvisioningApp instance;
  private static final String CONFIG_FILE_PATH = "./config.properties";
  private final Logger logger = Logger.getLogger(ProvisioningApp.class.getName());
  private RESTApp restApp;
  private UsernameManager usernameManager;

  public static ProvisioningApp getInstance() {
    if (instance == null) {
      instance = new ProvisioningApp();
    }
    return instance;
  }

  private ProvisioningApp() {}

  /**
   * Get command object.
   *
   * @return Command object
   */
  public Commands getCommands() {
    return commands;
  }

  /**
   * Gets context object.
   *
   * @return Context object
   */
  public Context getContext() {
    return context;
  }

  /**
   * Gets UsernameManager object.
   *
   * @return UsernameManager object
   */
  public UsernameManager getUsernameManager() {
    return usernameManager;
  }

  @Override
  public void initApp() throws Exception {
    initApp(null, CONFIG_FILE_PATH);
  }

  /**
   * Initializes the application with the configuration file path.
   *
   * @param configFilePath the location where the config file is stored.
   * @throws Exception
   */
  public void initApp(String configFilePath) throws Exception {
    initApp(null, configFilePath);
  }

  /**
   * Initializes the application with the given commands and configuration file path.
   *
   * @param commands the command line parameters.
   * @param configFilePath the location where the config file is stored.
   * @throws Exception
   */
  public void initApp(Commands commands, String configFilePath) throws Exception {
    this.commands = commands;
    if (commands == null) {
      context = new Context(configFilePath);
      usernameManager = new UsernameManager(context);
    } else {
      if (commands.hasCommand(Commands.CONFIG_COMMAND_OPTION)) {
        configFilePath = commands.getCommandValue(Commands.CONFIG_COMMAND_OPTION);
      }
      context = new Context(configFilePath);
      usernameManager = new UsernameManager(context);
      if (commands.hasCommand(Commands.HELP_COMMAND_OPTION)) {
        commands.printHelp();
      } else if (commands.hasCommand(Commands.REST_API_COMMAND_OPTION)) {
        logger.log(Level.INFO, "Initializing RESTful API app");
        restApp = new RESTApp(commands);
        restApp.initApp();
      } else {
        logger.log(Level.INFO, "Unable to start app. No parameters matched a config.");
        commands.printHelp();
      }
    }
  }


  /**
   * Sets the instance to null. Used for testing only.
   */
  public static void clearInstance() {
    instance = null;
  }
}
