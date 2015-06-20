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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import apps.provisioning.ProvisioningApp;
import apps.provisioning.data.UsernameCache;

public class ProvisioningActionTest {

  private static final String CONFIG_FILE_PATH = "./test/apps-provisioning-test.properties";
  private ProvisioningAction provisioningAction;

  @Before
  public void setUp() throws Exception {
    ProvisioningApp app = ProvisioningApp.getInstance();
    app.initApp(null, CONFIG_FILE_PATH);
    provisioningAction = new ProvisioningAction();
  }

  @After
  public void tearDown() throws Exception {
    UsernameCache cache = ProvisioningApp.getInstance().getContext().getUsernameCache();
    if (cache != null) {
      cache.disposeDataSource();
    }
    ProvisioningApp.clearInstance();
  }

  @Test
  public final void testSuggestParseNullInput() {
    assertEquals("{\"errorMessage\":\"No parameters received.\"}", provisioningAction.suggest(null));
  }

  @Test
  public final void testSuggestParseEmptyInput() {
    assertEquals("{\"errorMessage\":\"No parameters received.\"}", provisioningAction.suggest(""));
  }

  @Test
  public final void tesSuggestParseWrongJSON() {
    String output = provisioningAction.suggest("{\"firstname\",\"Carlos\"}");
    assertTrue(output.startsWith("{\"errorMessage\":\"Parse errors in JSON input:"));
  }

  @Test
  public final void testSuggestMissingFields() {
    String output = provisioningAction.suggest("{\"firstname\":\"Carlos\"}");
    assertEquals("{\"errorMessage\":\"User data must contain lastname field.\"}", output);
  }

  @Test
  public final void testSuggestCorrectInput() {
    String suggestions =
        provisioningAction.suggest("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("[\"carlos.alvarez\",\"carlosalvarez\",\"c.alvarez\"]", suggestions);
  }

  @Test
  public final void testCreateParseNullInput() {
    assertEquals("{\"errorMessage\":\"No parameters received.\"}", provisioningAction.create(null));
  }

  @Test
  public final void testCreateMissingFields() {
    String output =
        provisioningAction.create("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("{\"errorMessage\":\"User data must contain username field.\"}", output);
  }

  @Test
  public final void testCreate() throws Exception {
    String username = "carlosalvarez10";
    String output =
        provisioningAction
            .create("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\",\"username\":\"" + username
            + "\",\"password\":\"12345678\"}");
    assertEquals("{\"message\":\"User created successfully.\"}", output);
    assertTrue(ProvisioningApp.getInstance().getContext().getDirectory().exists(username));
    ProvisioningApp.getInstance().getContext().getDirectory().remove(username);
    assertFalse(ProvisioningApp.getInstance().getContext().getDirectory().exists(username));
  }

  @Test
  public final void testSuggestCreateSelect() throws Exception {
    String suggestions =
        provisioningAction.suggest("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("[\"carlos.alvarez\",\"carlosalvarez\",\"c.alvarez\"]", suggestions);
    JSONArray suggestionsJson = new JSONArray(suggestions);
    String firstUser = suggestionsJson.getString(0);
    String jsonString =
        "{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\",\"username\":\"" + firstUser
        + "\",\"password\":\"12345678\"}";
    String output = provisioningAction.create(jsonString);
    assertEquals("{\"message\":\"User created successfully.\"}", output);
    assertTrue(ProvisioningApp.getInstance().getContext().getDirectory().exists(firstUser));
    jsonString = "{\"username\":\"" + firstUser + "\",\"suggestions\":" + suggestions + "}";
    output = provisioningAction.select(jsonString);
    assertEquals("{\"message\":\"User selected successfully.\"}", output);
    suggestions = provisioningAction.suggest("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("[\"carlosalvarez\",\"c.alvarez\",\"carlos_alvarez\"]", suggestions);
    assertTrue(ProvisioningApp.getInstance().getContext().getDirectory().exists(firstUser));
    ProvisioningApp.getInstance().getContext().getDirectory().remove(firstUser);
  }

  @Test
  public final void testCreateWrongPassword() throws Exception {
    String username = "carlosalvarez10";
    String output =
        provisioningAction
            .create("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\",\"username\":\"" + username
                + "\",\"password\":\"1234\"}");
    assertEquals("{\"errorMessage\":\"Password must have at least 8 characters.\"}", output);
  }

  @Test
  public final void testSuggestAndCreate() throws Exception {
    String suggestions =
        provisioningAction.suggest("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("[\"carlos.alvarez\",\"carlosalvarez\",\"c.alvarez\"]", suggestions);
    JSONArray suggestionsJson = new JSONArray(suggestions);
    String firstUser = suggestionsJson.getString(0);
    String jsonString =
        "{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\",\"username\":\"" + firstUser
        + "\",\"password\":\"12345678\"}";
    String output = provisioningAction.create(jsonString);
    assertEquals("{\"message\":\"User created successfully.\"}", output);
    assertTrue(ProvisioningApp.getInstance().getContext().getDirectory().exists(firstUser));
    suggestions = provisioningAction.suggest("{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\"}");
    assertEquals("[\"carlos_alvarez\",\"carlosa\",\"carlosalvarez1\"]", suggestions);
    suggestionsJson = new JSONArray(suggestions);
    String secondUser = suggestionsJson.getString(0);
    jsonString =
        "{\"firstname\":\"Carlos\",\"lastname\":\"Álvarez\",\"username\":\"" + secondUser
        + "\",\"password\":\"12345678\"}";
    output = provisioningAction.create(jsonString);
    assertEquals("{\"message\":\"User created successfully.\"}", output);
    assertTrue(ProvisioningApp.getInstance().getContext().getDirectory().exists(firstUser));
    ProvisioningApp.getInstance().getContext().getDirectory().remove(firstUser);
    assertFalse(ProvisioningApp.getInstance().getContext().getDirectory().exists(firstUser));
    ProvisioningApp.getInstance().getContext().getDirectory().remove(secondUser);
    assertFalse(ProvisioningApp.getInstance().getContext().getDirectory().exists(secondUser));
  }

}
