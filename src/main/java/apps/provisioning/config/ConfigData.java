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

package apps.provisioning.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Maps the properties file to methods.
 */
public class ConfigData {

  private final String CACHED_USERNAMES = "accounts.UsernameGeneration.cachedUsernames";
  private final Boolean CACHED_USERNAMES_DEFAULT = false;
  private final String CACHE_EXPIRATION_HOURS = "accounts.UsernameGeneration.cacheExpirationHours";
  private final Integer CACHE_EXPIRATION_HOURS_DEFAULT = 24;
  private final Integer CACHE_EXPIRATION_HOURS_MIN = 1;
  private final String NUMBER_OF_SUGGESTIONS = "accounts.UsernameGeneration.numberOfSuggestions";
  private final Integer NUMBER_OF_SUGGESTIONS_DEFAULT = 3;
  private final Integer NUMBER_OF_SUGGESTIONS_MIN = 1;
  private final Integer NUMBER_OF_SUGGESTIONS_MAX = 10;
  private final String PATTERNS = "accounts.UsernameGeneration.patterns";
  private final String[] PATTERNS_DEFAULT = {"[firstname][lastname]", "[firstname].[lastname]",
      "[firstname]_[lastname]", "[C1_firstname][lastname]", "[firstname][C1_lastname]",
  "[C9_firstname][C9_lastname][#]"};
  private final String SUGGESTED_USERNAMES_TIMEOUT =
      "accounts.UsernameGeneration.suggestedUsernamesTimeout";
  private final int SUGGESTED_USERNAMES_TIMEOUT_DEFAULT = 120; // 2 minutes
  private final String AUTH_USER = "apis.GoogleAPIs.authUser";
  private final String KEY_PATH = "apis.GoogleAPIs.keyPath";
  private final String APP_NAME = "apis.GoogleAPIs.appName";
  private final String DOMAIN = "apis.GoogleAPIs.domain";
  private final String DOMAIN_PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
  private final String SERVICE_ACCOUNT_EMAIL = "apis.GoogleAPIs.serviceAccountEmail";
  private final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private final String DB_PATH = "db.h2.path";
  private final String DB_PATH_DEFAULT = "./";
  private final String DB_NAME = "db.h2.name";
  private final String DB_NAME_DEFAULT = "usernames";
  private final String USE_SSL = "security.ssl.useSSL";
  private final Boolean USE_SSL_DEFAULT = false;
  private final String KEY_STORE_PATH = "security.ssl.keyStorePath";
  private final String KEY_STORE_PASSWORD = "security.ssl.keyStorePassword";
  private final String KEY_MANAGER_PASSWORD = "security.ssl.keyManagerPassword";
  private final String[] ILLEGAL_CHARACTERS = {"/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*",
      "\\", "<", ">", "|", "\"", ":"};
  private final String ARRAY_SEPARATOR = ",";
  private static final Logger log = Logger.getLogger(ConfigData.class.getName());

  private Properties properties;
  private Integer numberOfSuggestions;
  private String[] patterns;
  private String authUser;
  private String keyPath;
  private String serviceAccountEmail;
  private String appName;
  private String domain;
  private Boolean cacheUsernames;
  private Integer cacheExpirationHours;
  private String dbName;
  private String dbPath;
  private Boolean useSSL;
  private String keyStorePath;
  private String keyStorePassword;
  private String keyManagerPassword;
  private long suggestedUsernamesTimeout;

  public ConfigData(String configFilePath) throws FileNotFoundException, IOException, Exception {
    properties = new Properties();
    properties.load(new FileInputStream(configFilePath));
    parsePropertiesFile();
  }

  /**
   * Checks that every property is set correctly.
   *
   * @throws Exception
   */
  private void parsePropertiesFile() throws Exception {
    numberOfSuggestions = parseNumberOfSuggestions();
    log.log(Level.INFO, "Number of suggestions: " + numberOfSuggestions);
    patterns = parsePatterns();
    log.log(Level.INFO, "Patterns: " + patterns);
    authUser = parseAuthUser();
    log.log(Level.INFO, "Auth user: " + authUser);
    keyPath = parseKeyPath();
    log.log(Level.INFO, "Key path: " + keyPath);
    serviceAccountEmail = parseServiceAccountEmail();
    log.log(Level.INFO, "Service account email: " + serviceAccountEmail);
    appName = parseAppName();
    log.log(Level.INFO, "App name: " + appName);
    domain = parseDomain();
    log.log(Level.INFO, "Domain: " + domain);
    suggestedUsernamesTimeout = parseSuggestedUsernamesTimeout();
    log.log(Level.INFO, "Suggested usernames timeout: " + suggestedUsernamesTimeout);
    cacheUsernames = parseCacheUsernames();
    log.log(Level.INFO, "Cached usernames: " + cacheUsernames);
    if (cacheUsernames) {
      cacheExpirationHours = parseCacheExpirationHours();
      log.log(Level.INFO, "Cache expiration in hours: " + cacheExpirationHours);
      dbPath = parseDbPath();
      log.log(Level.INFO, "Database path: " + dbPath);
      dbName = parseDbName();
      log.log(Level.INFO, "Database name: " + dbName);
    }
    useSSL = parseUseSSL();
    log.log(Level.INFO, "Uses SSL: " + useSSL);
    if (useSSL) {
      keyStorePath = parseKeyStorePath();
      log.log(Level.INFO, "Key store path: " + keyStorePath);
      keyStorePassword = parseKeyStorePassword();
      log.log(Level.INFO, "Key store password: " + keyStorePassword);
      keyManagerPassword = parseKeyManagerPassword();
      log.log(Level.INFO, "Key manager password: " + keyManagerPassword);
    }
  }

  /**
   * Gets a property value from the properties object as String.
   *
   * @param key Property name to be retrieved.
   * @return The property requested as String.
   */
  private String getString(String key) {
    String value = properties.getProperty(key);
    return value;
  }

  /**
   * Gets a property value from the properties object as String Array
   *
   * @param key Property name to be retrieved
   * @return The property requested as String array.
   */
  private String[] getStringArray(String key) {
    String string = getString(key);
    if (string == null || string.isEmpty()) {
      return null;
    }
    return string.split(ARRAY_SEPARATOR);
  }

  /**
   * Gets a property value from the properties object as Boolean
   *
   * @param key Property name to be retrieved
   * @return The property requested as boolean.
   * @throws Exception
   */
  private Boolean getBoolean(String key) throws Exception {
    String value = getString(key);
    if (value == null || value.isEmpty()) {
      return null;
    }
    value = value.toLowerCase();
    if (value.equals("yes")) {
      return true;
    } else if (value.equals("no")) {
      return false;
    } else {
      throw new Exception("Only YES and NO options are allowed");
    }
  }

  /**
   * Gets a property value from the properties object as Integer
   *
   * @param key Property name to be retrieved
   * @return The property requested as integer.
   * @throws NumberFormatException
   */
  private Integer getInteger(String key) throws NumberFormatException {
    String value = getString(key);
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Integer.parseInt(value);
  }

  /**
   * Parses the cachedUsernames property value.
   *
   * @return The cachedUsernames value from UsernameGeneration module.
   * @throws Exception
   */
  private Boolean parseCacheUsernames() throws Exception {
    try {
      Boolean cachedUsernames = getBoolean(CACHED_USERNAMES);
      if (cachedUsernames == null) {
        log.log(Level.WARNING, "cachedUsernames set with the default value: "
            + CACHED_USERNAMES_DEFAULT);
        return CACHED_USERNAMES_DEFAULT;
      }
      return cachedUsernames;
    } catch (Exception e) {
      throw new Exception("Invalid value in " + CACHED_USERNAMES + "property.");
    }
  }

  /**
   * Gets the cachedUsernames property value.
   *
   * @return The cachedUsernames value from UsernameGeneration module.
   */
  public Boolean getCacheUsernames() {
    return cacheUsernames;
  }

  /**
   * Parses the cacheExpirationHours property value.
   *
   * @return How long the cached information will be valid.
   * @throws Exception
   */
  private Integer parseCacheExpirationHours() throws Exception {
    try {
      Integer cacheExpirationHours = getInteger(CACHE_EXPIRATION_HOURS);
      if (cacheExpirationHours == null) {
        return CACHE_EXPIRATION_HOURS_DEFAULT;
      }
      if (cacheExpirationHours < CACHE_EXPIRATION_HOURS_MIN) {
        throw new Exception("Expiration time is lower than minimum:" + NUMBER_OF_SUGGESTIONS_MIN);
      }
      return cacheExpirationHours;
    } catch (Exception e) {
      throw new Exception("Invalid value in " + CACHE_EXPIRATION_HOURS + " property.");
    }
  }

  /**
   * Parses the suggestedUsernamesTimeout property value.
   *
   * @return How long the suggested usernames will be suggested.
   * @throws Exception
   */
  private Integer parseSuggestedUsernamesTimeout() throws Exception {
    try {
      Integer suggestedUsernamesTimeout = getInteger(SUGGESTED_USERNAMES_TIMEOUT);
      if (suggestedUsernamesTimeout == null) {
        return SUGGESTED_USERNAMES_TIMEOUT_DEFAULT;
      }
      return suggestedUsernamesTimeout;
    } catch (Exception e) {
      throw new Exception("Invalid value in " + SUGGESTED_USERNAMES_TIMEOUT + " property.");
    }
  }

  /**
   * Gets the cacheExpirationHours property value.
   *
   * @return How long the cached information will be valid.
   */
  public Integer getCacheExpirationHours() {
    return cacheExpirationHours;
  }

  /**
   * Parses the numberOfSuggestions property value.
   *
   * @return Number of configured username suggestions.
   * @throws Exception
   */
  private Integer parseNumberOfSuggestions() throws Exception {
    try {
      Integer numberOfSuggestions = getInteger(NUMBER_OF_SUGGESTIONS);
      if (numberOfSuggestions == null) {
        return NUMBER_OF_SUGGESTIONS_DEFAULT;
      }
      if (numberOfSuggestions < NUMBER_OF_SUGGESTIONS_MIN
          || numberOfSuggestions > NUMBER_OF_SUGGESTIONS_MAX) {
        throw new Exception("The number of suggestions value must be between "
            + NUMBER_OF_SUGGESTIONS_MIN + " and " + NUMBER_OF_SUGGESTIONS_MAX);
      }
      return numberOfSuggestions;
    } catch (Exception e) {
      throw new Exception("Invalid value in " + NUMBER_OF_SUGGESTIONS + " property.");
    }
  }

  /**
   * Gets the numberOfSuggestions property value.
   *
   * @return Number of configured username suggestions.
   */
  public Integer getNumberOfSuggestions() {
    return numberOfSuggestions;
  }

  /**
   * Parses the patterns property value.
   *
   * @return The supported patterns as an array
   */
  private String[] parsePatterns() {
    String[] patterns = getStringArray(PATTERNS);
    if (patterns == null) {
      log.log(Level.WARNING, "Pattern value set with the default value: " + PATTERNS_DEFAULT);
      return PATTERNS_DEFAULT;
    }
    return patterns;
  }

  /**
   * Gets the patterns property value.
   *
   * @return The supported patterns as an array
   */
  public String[] getPatterns() {
    return patterns;
  }

  /**
   * Parses the authUser property value.
   *
   * @return The owner email of the project
   * @throws Exception
   */
  private String parseAuthUser() throws Exception {
    String authUser = getString(AUTH_USER);
    if (authUser == null || authUser.isEmpty()) {
      throw new Exception("You must set " + AUTH_USER + " property.");
    }
    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    if (pattern.matcher(authUser).find()) {
      return authUser;
    } else {
      throw new Exception("Invalid value in " + AUTH_USER + " property");
    }
  }

  /**
   * Gets the authUser property value.
   *
   * @return The owner email of the project
   */
  public String getAuthUser() {
    return authUser;
  }

  /**
   * Parses the keyPath property value.
   *
   * @return The location of the P12 key file.
   * @throws FileNotFoundException
   * @throws Exception
   */
  private String parseKeyPath() throws FileNotFoundException, Exception {
    String keyPath = getString(KEY_PATH);
    if (keyPath == null) {
      throw new Exception("You must set " + KEY_PATH + " property.");
    }
    File file = new File(keyPath);
    if (!file.exists()) {
      throw new FileNotFoundException("File declared in " + KEY_PATH + " doesn't exist.");
    }
    return keyPath;
  }

  /**
   * Gets the keyPath property value.
   *
   * @return The location of the P12 key file.
   */
  public String getKeyPath() {
    return keyPath;
  }

  /**
   * Parses the serviceAccountEmail property value.
   *
   * @return The email generated for the service account credentials
   * @throws Exception
   */
  private String parseServiceAccountEmail() throws Exception {
    String serviceAccountEmail = getString(SERVICE_ACCOUNT_EMAIL);
    if (serviceAccountEmail == null) {
      throw new Exception("You must set " + SERVICE_ACCOUNT_EMAIL + " property.");
    }
    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    if (pattern.matcher(serviceAccountEmail).find()) {
      return serviceAccountEmail;
    } else {
      throw new Exception("Invalid value in " + SERVICE_ACCOUNT_EMAIL + " property");
    }
  }

  /**
   * Gets the serviceAccountEmail property value.
   *
   * @return The email generated for the service account credentials
   */
  public String getServiceAccountEmail() {
    return serviceAccountEmail;
  }

  /**
   * Parses the appName property value.
   *
   * @return Application name
   * @throws Exception
   */
  private String parseAppName() throws Exception {
    String appName = getString(APP_NAME);
    if (appName == null || appName.isEmpty()) {
      throw new Exception("You must set " + APP_NAME + " property.");
    }
    return appName;
  }

  /**
   * Gets the appName property value.
   *
   * @return Application name
   */
  public String getAppName() {
    return appName;
  }


  /**
   * Parses the domain property value.
   *
   * @return Domain
   * @throws Exception
   */
  private String parseDomain() throws Exception {
    String domain = getString(DOMAIN);
    if (domain == null || appName.isEmpty()) {
      throw new Exception("You must set " + DOMAIN + " property.");
    }
    Pattern pattern = Pattern.compile(DOMAIN_PATTERN);
    if (pattern.matcher(domain).find()) {
      return domain;
    } else {
      throw new Exception("Invalid value in " + DOMAIN + " property");
    }
  }

  /**
   * Gets the domain property value.
   *
   * @return Domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Parses the database path value.
   *
   * @return Database path
   * @throws IOException
   */
  private String parseDbPath() throws Exception {
    String dbPath = getString(DB_PATH);
    if (dbPath == null || dbPath.isEmpty()) {
      return DB_PATH_DEFAULT;
    }
    File path = new File(dbPath);
    if (!path.exists()) {
      throw new FileNotFoundException("The database path configured in " + DB_PATH + " must exist.");
    }
    if (!path.isDirectory()) {
      throw new Exception("The database path configured in " + DB_PATH + " must be a directory");
    }
    return dbPath;
  }

  /**
   * Gets the database path value.
   *
   * @return Database path
   */
  public String getDbPath() {
    return dbPath;
  }

  /**
   * Parses the database name value.
   *
   * @return Database name
   * @throws IOException
   */
  private String parseDbName() throws Exception {
    String dbName = getString(DB_NAME);
    if (dbName == null || dbName.isEmpty()) {
      return DB_NAME_DEFAULT;
    }
    for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++) {
      if (dbName.contains(ILLEGAL_CHARACTERS[i])) {
        throw new Exception("The database name has the following invalid character: "
            + ILLEGAL_CHARACTERS[i]);
      }
    }
    return dbName;
  }

  /**
   * Gets the database name value.
   *
   * @return Database name
   */
  public String getDbName() {
    return dbName;
  }

  /**
   * Parses the useSSL property value.
   *
   * @return The useSSL value.
   * @throws Exception
   */
  private Boolean parseUseSSL() throws Exception {
    try {
      Boolean useSSL = getBoolean(USE_SSL);
      if (useSSL == null) {
        log.log(Level.WARNING, "useSSL set with the default value: " + USE_SSL_DEFAULT);
        return USE_SSL_DEFAULT;
      }
      return useSSL;
    } catch (Exception e) {
      throw new Exception("Invalid value in " + USE_SSL + " property.");
    }
  }

  /**
   * Gets the useSSL property value.
   *
   * @return The useSSL value.
   */
  public Boolean getUseSSL() {
    return useSSL;
  }

  /**
   * Parses the keyStorePath property value.
   *
   * @return The location of the key store file.
   * @throws FileNotFoundException
   * @throws Exception
   */
  private String parseKeyStorePath() throws FileNotFoundException, Exception {
    String keyStorePath = getString(KEY_STORE_PATH);
    if (keyStorePath == null) {
      throw new Exception("You must set " + KEY_STORE_PATH + " property.");
    }
    File file = new File(keyStorePath);
    if (!file.exists()) {
      throw new FileNotFoundException("File declared in " + KEY_STORE_PATH + " doesn't exist.");
    }
    return keyStorePath;
  }


  /**
   * Gets the keyStorePath property value.
   *
   * @return The location of the key store file.
   */
  public String getKeyStorePath() {
    return keyStorePath;
  }

  /**
   * Parses the keyStorePassword property value.
   *
   * @return Key store password
   * @throws Exception
   */
  private String parseKeyStorePassword() throws Exception {
    String keyStorePassword = getString(KEY_STORE_PASSWORD);
    if (keyStorePassword == null) {
      throw new Exception("You must set " + KEY_STORE_PASSWORD + " property.");
    }
    return keyStorePassword;
  }

  /**
   * Gets the keyStorePassword property value.
   *
   * @return Key store password
   */
  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  /**
   * Parses the keyManagerPassword property value.
   *
   * @return Key manager password
   * @throws Exception
   */
  private String parseKeyManagerPassword() throws Exception {
    String keyManagerPassword = getString(KEY_MANAGER_PASSWORD);
    if (keyManagerPassword == null) {
      throw new Exception("You must set " + KEY_MANAGER_PASSWORD + " property.");
    }
    return keyManagerPassword;
  }

  /**
   * Gets the keyManagerPassword property value.
   *
   * @return Key manager password
   */
  public String getKeyManagerPassword() {
    return keyManagerPassword;
  }


  /**
   * @return The suggested usernames expiration timeout in seconds. A suggested username will remain
   *         suggested for this amount of time (in seconds).
   */
  public long getSuggestedUsernamesTimeout() {
    return suggestedUsernamesTimeout;
  }
}
