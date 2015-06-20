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

package apps.provisioning.server.apis;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import apps.provisioning.config.ConfigData;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;

/**
 * Creates credentials for the Java Google Client.
 */
public class GoogleClient {

  public HttpTransport httpTransport = new NetHttpTransport();
  public JsonFactory jsonFactory = new JacksonFactory();

  protected String domain;
  protected String authUser;
  protected String serviceAccountEmail;
  protected String keyPath;
  protected String appName;

  /**
   * Creates a Google API Client using the credentials stored in the
   * configuration file.
   *
   * @throws IOException
   */
  public GoogleClient(ConfigData config) throws IOException, Exception {
    domain = config.getDomain();
    authUser = config.getAuthUser();
    serviceAccountEmail = config.getServiceAccountEmail();
    keyPath = config.getKeyPath();
    appName = config.getAppName();
  }

  /**
   * Constructor used for testing only.
   */
  public GoogleClient() {}

  /**
   * Retrieves the service account credentials.
   *
   * @param serviceAccountEmail Service email from the Google Developer Console
   *        project.
   * @param keyPath Where the p12 file is stored.
   * @return GoogleCredential object with the active section.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  protected GoogleCredential getCredentialForServiceAccount(String serviceAccountEmail,
      String keyPath) throws GeneralSecurityException, IOException {
    return new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(Collections.singleton(DirectoryScopes.ADMIN_DIRECTORY_USER))
        .setServiceAccountPrivateKeyFromP12File(new File(keyPath)).setServiceAccountUser(authUser)
        .build();
  }

  /**
   * Create a new authorized Google API client.
   *
   * @param projectName The project name that is displayed in the Google
   *        Developer Console.
   * @param credential The GoogleCredential object.
   * @return The Admin SDK client object
   */
  protected Directory createAuthorizedClient(String projectName, GoogleCredential credential) {
    return new Directory.Builder(httpTransport, jsonFactory, credential).setApplicationName(
        projectName).build();
  }

}
