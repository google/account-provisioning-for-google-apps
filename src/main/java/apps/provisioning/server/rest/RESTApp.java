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

package apps.provisioning.server.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import apps.provisioning.Application;
import apps.provisioning.Commands;
import apps.provisioning.ProvisioningApp;
import apps.provisioning.config.ConfigData;

/**
 * Class that encapsulates the logic of the username suggest RESTful API.
 */
public class RESTApp extends Application {

  private final Logger logger = Logger.getLogger(RESTApp.class.getName());

  private static final int PORT_DEFAULT_VALUE = 8080;
  private final String HTTP_VERSION = "http/1.1";

  private Server jettyServer;
  private ServletContextHandler servletContext;
  private ServletHolder jerseyServlet;
  private Integer customPort;

  public RESTApp(Commands commands) {
    this.commands = commands;
    parseCommands();
  }

  @Override
  public void initApp() throws Exception {
    initJerseyServlet();
    initJettyServer();
  }

  /**
   * Reads command line values.
   */
  private void parseCommands() {
    customPort = commands.getCommandIntValue(Commands.PORT_COMMAND_OPTION);
  }

  /**
   * Initializes the Jetty server.
   */
  private void initJettyServer() {
    logger.log(Level.INFO, "Initialzing Jetty server...");
    int port;
    if (customPort == null) {
      logger.log(Level.INFO, "Initialzing server in default port: " + PORT_DEFAULT_VALUE);
      port = PORT_DEFAULT_VALUE;
    } else {
      logger.log(Level.INFO, "Initialzing server in custom port: " + customPort.toString());
      port = customPort;
    }
    jettyServer = new Server(port);

    ConfigData config = ProvisioningApp.getInstance().getContext().getConfig();
    if (config.getUseSSL()) {
      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer(new SecureRequestCustomizer());

      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePath(config.getKeyStorePath());
      sslContextFactory.setKeyStorePassword(config.getKeyStorePassword());
      sslContextFactory.setKeyManagerPassword(config.getKeyManagerPassword());

      ServerConnector sslConnector =
          new ServerConnector(jettyServer,
              new SslConnectionFactory(sslContextFactory, HTTP_VERSION), new HttpConnectionFactory(
                  https));
      sslConnector.setPort(port);

      jettyServer.setConnectors(new Connector[] {sslConnector});
    }

    jettyServer.setHandler(servletContext);

    try {
      jettyServer.start();
      jettyServer.join();
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "Exception during server initialization", e);
      jettyServer.destroy();
    }
  }

  /**
   * Initializes the Jersey Servlet.
   */
  private void initJerseyServlet() {
    servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
    servletContext.setContextPath("/");
    // This is used for allowing access to different domains/ports.
    FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
    filterHolder.setInitParameter("allowedOrigins", "*");
    filterHolder.setInitParameter("allowedMethods", "GET, POST");
    servletContext.addFilter(filterHolder, "/*", null);

    jerseyServlet = servletContext.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
    jerseyServlet.setInitOrder(0);

    // Tell the Jersey Servlet which REST class to load.
    jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
        ProvisioningAction.class.getCanonicalName());
  }
}
