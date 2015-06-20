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

package apps.provisioning.server.rest;

import java.util.HashMap;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.json.JSONException;
import org.json.JSONObject;

import apps.provisioning.ProvisioningApp;
import apps.provisioning.config.ConfigData;
import apps.provisioning.server.Action;
import apps.provisioning.server.account.UsernameManager;

@Path("/rest")
public class ProvisioningAction implements Action {

  private final String[] JSON_FIELDS_CREATE = new String[] {UsernameManager.FIRST_NAME,
      UsernameManager.LAST_NAME, UsernameManager.USERNAME, UsernameManager.PASSWORD};
  private final String[] JSON_FIELDS_SELECT = new String[] {UsernameManager.USERNAME,
      UsernameManager.SUGGESTIONS};
  private final String[] JSON_FIELDS_SUGGEST = new String[] {UsernameManager.FIRST_NAME,
      UsernameManager.LAST_NAME};

  /**
   * Method exposed as a REST POST service that suggests usernames.
   *
   * @param userData Serialized map with the following fields: firstname (String) and lastname
   *        (String).
   * @return In case of success, it returns a JSON serialized array with suggestions, in case of
   *         error it returns a JSON serialized map with the "errorMessage" index explaining the
   *         error.
   */
  @POST
  @Path("suggest")
  public String suggest(String userData) {
    HashMap<String, String> userDataMap;
    try {
      userDataMap = parseAndValidateJSON(userData, JSON_FIELDS_SUGGEST);
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
    try {
      return JSONObject.valueToString(ProvisioningApp.getInstance().getUsernameManager()
          .suggest(userDataMap));
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
  }

  /**
   * Method exposed as a REST GET service that suggests usernames. This method doesn't support
   * custom fields. Use the POST service instead. Exposed for testing only.
   *
   * @param firstName The user's first name.
   * @param lastName The user's last name.
   * @return In case of success, it returns a JSON serialized array with suggestions, in case of
   *         error it returns a JSON serialized map with the "errorMessage" index explaining the
   *         error.
   */
  @GET
  @Path("suggest")
  public String suggestGet(@QueryParam("firstname") String firstName,
      @QueryParam("lastname") String lastName) {
    HashMap<String, String> userDataMap = new HashMap<String, String>();
    userDataMap.put(UsernameManager.FIRST_NAME, firstName);
    userDataMap.put(UsernameManager.LAST_NAME, lastName);
    try {
      return JSONObject.valueToString(ProvisioningApp.getInstance().getUsernameManager()
          .suggest(userDataMap));
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
  }

  /**
   * Method exposed as a REST POST service that creates users in Google Apps.
   *
   * @param userData Serialized map with the following fields: username (String), firstname
   *        (String), lastname (String) and password (String).
   * @return In case of success, it returns a JSON serialized map with the "message" index or in
   *         case of failure with the "errorMessage" index.
   */
  @POST
  @Path("create")
  public String create(String userData) {
    HashMap<String, String> userDataMap;
    try {
      userDataMap = parseAndValidateJSON(userData, JSON_FIELDS_CREATE);
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
    try {
      ProvisioningApp.getInstance().getUsernameManager().create(userDataMap);
      return createJSONSuccessResponse("User created successfully.");
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
  }

  /**
   * Method exposed as a REST POST service that unlocks the usernames suggested that were locked
   * meanwhile user chooses one.
   *
   * @param userData Serialized map with username (String) and patterns (String array) keys.
   * @return In case of success, it returns a JSON serialized map with the "message" index or in
   *         case of failure with the "errorMessage" index.
   */
  @POST
  @Path("select")
  public String select(String userData) {
    HashMap<String, String> userDataMap;
    try {
      userDataMap = parseAndValidateJSON(userData, JSON_FIELDS_SELECT);
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
    try {
      ProvisioningApp.getInstance().getUsernameManager().select(userDataMap);
      return createJSONSuccessResponse("User selected successfully.");
    } catch (Exception e) {
      return createJSONErrorResponse(e.getMessage());
    }
  }

  /**
   * Method exposed as a REST POST service to get the server's configuration parameters used by a
   * client.
   *
   * @return A JSON serialized map with the following configuration parameters:
   *         suggestedUsernamesTimeout, numberOfSuggestions and domain.
   */
  @POST
  @Path("config")
  public String getServerConfigPost() {
    return getServerConfig();
  }

  /**
   * Method exposed as a REST GET service to get the server's configuration parameters used by a
   * client.
   *
   * @return A JSON serialized map with the following configuration parameters:
   *         suggestedUsernamesTimeout, numberOfSuggestions and domain.
   */
  @GET
  @Path("config")
  public String getServerConfigGet() {
    return getServerConfig();
  }

  /**
   * @return A JSON serialized map with the following configuration parameters:
   *         suggestedUsernamesTimeout, numberOfSuggestions and domain.
   */
  public String getServerConfig() {
    ConfigData config = ProvisioningApp.getInstance().getContext().getConfig();
    long suggestedUsernamesTimeout = config.getSuggestedUsernamesTimeout();
    Integer numberOfSuggestions = config.getNumberOfSuggestions();
    String domain = config.getDomain();
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("suggestedUsernamesTimeout", String.valueOf(suggestedUsernamesTimeout));
    configMap.put("numberOfSuggestions", numberOfSuggestions.toString());
    configMap.put("domain", domain);
    return JSONObject.valueToString(configMap);
  }

  /**
   * Parses the incoming JSON text and validates that needed fields are contained.
   *
   * @param userData The raw body sent in the HTTP POST payload section.
   * @param validateFields The fields to be validated after parsing the object.
   * @return HashMap with the fields as keys containing their values.
   * @throws Exception
   */
  private HashMap<String, String> parseAndValidateJSON(String userData, String[] validateFields)
      throws Exception {
    if (userData == null || userData.isEmpty()) {
      throw new Exception("No parameters received.");
    }
    JSONObject jsonObject;
    try {
      jsonObject = new JSONObject(userData);
    } catch (JSONException e) {
      throw new Exception("Parse errors in JSON input: " + e.getMessage());
    }
    for (String field : validateFields) {
      if (!jsonObject.has(field)) {
        throw new Exception("User data must contain " + field + " field.");
      }

    }
    HashMap<String, String> userDataMap = new HashMap<String, String>();
    Set<?> keySet = jsonObject.keySet();
    for (Object field : keySet) {
      String fieldName = (String) field;
      // The value associated to a key is not always a String, (i.e. it can be a JSONArray in the
      // select method)
      userDataMap.put(fieldName, jsonObject.get(fieldName).toString());
    }
    return userDataMap;
  }

  private String createJSONSuccessResponse(String message) {
    return "{\"message\":\"" + message + "\"}";
  }

  private String createJSONErrorResponse(String message) {
    return "{\"errorMessage\":\"" + message + "\"}";
  }

}
