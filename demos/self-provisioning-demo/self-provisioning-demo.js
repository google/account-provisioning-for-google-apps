/**
 * @fileoverview A demo to self-provision Google Apps accounts.
 */

// Demo configurable variables
// ==========================

/**
 * The address of the server hosting the REST API.
 * {string}
 */
var API_HOST = 'http://localhost:8080';

// ==========================


/**
 * Contains all the account provisioning logic to get available usernames,
 * create a Google Apps account and present results in the HTML view.
 */
AccountProvisioningApp = function() {
  /**
   * The firstname HTML input element.
   * {Element}
   */
  this.firstnameElement = document.getElementById('firstname');

  /**
   * The lastname HTML input element.
   * {Element}
   */
  this.lastnameElement = document.getElementById('lastname');

  /**
   * The custom1 HTML input element.
   * {Element}
   */
  this.custom1Element = document.getElementById('custom1');

  /**
   * The password HTML input element.
   * {Element}
   */
  this.passwordElement = document.getElementById('password');

  /**
   * The message that indicates a name should be provided to get suggestions.
   * {Element}
   */
  this.usernameMessage = document.getElementById('usernameMessage');

  /**
   * A DIV that contains the suggested usernames.
   * {Element}
   */
  this.suggestionsContainer = document.getElementById('suggestionsContainer');

  /**
   * A DIV that contains a button to get more suggestions.
   * {Element}
   */
  this.moreSuggestions = document.getElementById('moreSuggestions');

  /**
   * A DIV that contains the form.
   * {Element}
   */
  this.formElement = document.getElementById('mainForm');

  /**
   * A DIV that loading spinner.
   * {Element}
   */
  this.spinnerElement = document.getElementById('loadingAccount');

  /**
   * The DIV that is displayed while the configuration is retrieved.
   * {Element}
   */
  this.loadingDiv = document.getElementById('loadingDiv');

  /**
   * A DIV that contains all the elements to create an account.
   * {Element}
   */
  this.accountCreationElement = document.getElementById('accountCreationPanel');

  /**
   * The container of the message shown to the user after creating an account.
   * {Element}
   */
  this.accountCreatedElement = document.getElementById('accountCreatedPanel');

  /**
   * The span where the created account is displayed.
   * {Element}
   */
  this.createdAccountElement = document.getElementById('accountPlaceholder');

  this.setupConfig();
  this.addListeners();
  setTimeout(this.updateTimeout, 1000);
}

/**
 * A counter used for the countdown element.
 * {number}
 */
AccountProvisioningApp.prototype.secondsLeft = 0;

/**
 * The selected username.
 * {?string}
 */
AccountProvisioningApp.prototype.selectedUsername;

/**
 * The response text from invoking the suggest REST method.
 * {?string}
 */
AccountProvisioningApp.prototype.suggestResponseText;

/**
 * The number of username suggestions returned by the server.
 * {number}
 */
AccountProvisioningApp.prototype.numberOfSuggestions;

/**
 * Stores the API calls queue. This is used to synchronize API calls.
 * {array}
 */
AccountProvisioningApp.prototype.postQueue = [];

/**
 * Detects if some API calls are still being executed.
 * {boolean}
 */
AccountProvisioningApp.prototype.waitingForRespone = false;

/**
 * The domain name.
 * {string}
 */
AccountProvisioningApp.prototype.domain;

/**
 * The time in which usernames expire.
 * {number}
 */
AccountProvisioningApp.prototype.suggestedUsernamesTimeout;


/**
 * Adds radio buttons for username suggestions.
 */
AccountProvisioningApp.prototype.maybeDrawOptions = function() {
  if (document.getElementById('r1') == null) {
    var optionsHtml = "";
    for (var i = 1; i <= this.numberOfSuggestions; i++) {
      optionsHtml += '<input id="r' + i + '" type="radio" name="usernames">' +
          '<span id="r' + i + '-label"></span><br/>';
    }
    this.suggestionsContainer.innerHTML = optionsHtml + this.suggestionsContainer.innerHTML;
  }
}


/**
 * Adds event listeners to the HTML elements.
 */
AccountProvisioningApp.prototype.addListeners = function() {
  // Refresh username suggestions as the user types.
  this.firstnameElement.addEventListener('input', this.refreshUsernames);
  this.lastnameElement.addEventListener('input', this.refreshUsernames);
  this.custom1Element.addEventListener('input', this.refreshUsernames);
}


/**
 * Updates the countdown used to indicate when suggestions will expire.
 */
AccountProvisioningApp.prototype.updateTimeout = function() {
  var app = window['app'];
  var countdownElement = document.getElementById('countdown');
  countdownElement.innerHTML = ' ' + (app.secondsLeft--) + ' seconds.';
  var countdownContainer = document.getElementById('countdownContainer');
  // Only show the countdown element the last 30 seconds.
  if (app.secondsLeft > 0 && app.secondsLeft < 30) {
    HtmlUtils.showElement(countdownContainer);
  } else {
    HtmlUtils.hideElement(countdownContainer);
  }
  if (app.secondsLeft == 0) {
    // By now suggestions are not guaranteed to still be available. Therefore,
    // invalidate the suggest response and show a button to get new
    // (valid) suggestions.
    app.suggestResponseText = undefined;
    app.showGetSuggestionsButton();
  }
  setTimeout(app.updateTimeout, 1000);
}


/**
 * @return {?string} The selected username, if any.
 */
AccountProvisioningApp.prototype.getSelectedUsername = function() {
  var i = 1;
  while (i <= this.numberOfSuggestions) {
    var radioButtonId = 'r' + i;
    var radioButtonElement = document.getElementById(radioButtonId);
    if (radioButtonElement.checked) {
      return radioButtonElement.value;
    }
    i++;
  }
  return undefined;
}


/**
 * Creates a Google Apps account.
 */
AccountProvisioningApp.prototype.createAccount = function() {
  if (!this.areSuggestionsValid()) {
    alert('Get and select an available account first.');
    return;
  }
  this.selectedUsername = this.getSelectedUsername();
  if (!this.selectedUsername) {
    alert('Select a username');
    return;
  }
  var password = this.passwordElement.value;
  if (!password || password.length < 8) {
    alert('Password should have 8 or more characters');
    return;
  }
  console.log('Creating an account for username: ' + this.selectedUsername);
  HtmlUtils.hideElement(this.formElement);
  HtmlUtils.showElement(this.spinnerElement);
  var firstname = this.firstnameElement.value;
  var lastname = this.lastnameElement.value;
  this.create(this.selectedUsername, firstname, lastname, password,
      this.createCallback);
}


/**
 * The function that will be called once the create REST method returns a value.
 * @param {string} The response text.
 */
AccountProvisioningApp.prototype.createCallback = function(responseText) {
  var app = window['app'];
  HtmlUtils.hideElement(app.spinnerElement);
  if (responseText.search('errorMessage:') > 0) {
    HtmlUtils.showElement(app.formElement);
    alert('Error: ' + responseText);
    return;
  }
  HtmlUtils.showElement(app.accountCreatedElement);
  HtmlUtils.hideElement(app.accountCreationElement);
  var account = app.selectedUsername + '@' + app.domain;
  app.createdAccountElement.innerHTML = account;
}


/**
 * @return {boolean} Whether suggestions are still valid.
 */
AccountProvisioningApp.prototype.areSuggestionsValid = function() {
  return !!this.suggestResponseText;
}


/**
 * Shows the "Get more suggestions" button.
 */
AccountProvisioningApp.prototype.showGetSuggestionsButton = function() {
  HtmlUtils.hideElement(this.usernameMessage);
  HtmlUtils.hideElement(this.suggestionsContainer);
  HtmlUtils.showElement(this.moreSuggestions);
}


/**
 * Hides the suggestions radio buttons and shows a message that indicates that
 * first and last names needs to be provided.
 */
AccountProvisioningApp.prototype.hideSuggestions = function() {
  HtmlUtils.hideElement(this.suggestionsContainer);
  HtmlUtils.hideElement(this.moreSuggestions);
  HtmlUtils.showElement(this.usernameMessage);
}


/**
 * Shows the suggestions radio buttons.
 */
AccountProvisioningApp.prototype.showSuggestions = function() {
  HtmlUtils.showElement(this.suggestionsContainer);
  HtmlUtils.hideElement(this.moreSuggestions);
  HtmlUtils.hideElement(this.usernameMessage);
}


/**
 * Sends a POST request to the Account provisioning for Google Apps REST API.
 * @param {string} action The API method name: suggest, select or create.
 * @param {string} parameters The serialized parameters for the API method.
 * @param {Function} callback The function to call when the API method returns a
 * value.
 */
AccountProvisioningApp.prototype.sendPostRequest = function(
    action, parameters, callback) {
  this.prepareSend({
      action: action,
      parameters: parameters,
      callback: callback
  });
}

/**
 * Calls the suggest REST API method. Calls the given callback when the method
 * returns a value.
 * @param {string} firstname The user's first name.
 * @param {string} lastname The user's last name.
 * @param {?string} custom1 The custom field. Optional.
 * @param {Function} suggestCallback The function to call when the suggest API
 * method returns a value.
 */
AccountProvisioningApp.prototype.suggest = function(
    firstname, lastname, custom1, suggestCallback) {
  var parameters;
  if (custom1 && custom1.length > 0) {
    parameters = '{' +
        '"firstname":"' + firstname + '",' +
        '"lastname":"' + lastname + '",' +
        '"customfield":"' + custom1 + '"' +
    '}';
  } else {
    parameters = '{' +
        '"firstname":"' + firstname + '",' +
        '"lastname":"' + lastname +
    '"}';
  }
  this.sendPostRequest('suggest', parameters, suggestCallback);
}


/**
 * Calls the select REST API method. Calls the given callback when the method
 * returns a value.
 * @param {string} username The selected username.
 * @param {string} suggestions A serialized array of suggestions to unlock.
 * @param {Function} selectCallback The function to call when the select API
 * method returns a value.
 */
AccountProvisioningApp.prototype.select = function(
    username, suggestions, selectCallback) {
  var parameters = '{' +
    '"username":"' + username + '",' +
    '"suggestions":' + suggestions +
  '}';
  this.sendPostRequest('select', parameters, selectCallback);
}

/**
 * Calls the create REST API method. Calls the given callback when the method
 * returns a value.
 * @param {string} username The account's username.
 * @param {string} firstname The account's first name.
 * @param {string} lastname The account's last name.
 * @param {string} password The account's password.
 * @param {Function} createCallback The function to call when the create API
 * method is done.
 */
AccountProvisioningApp.prototype.create = function(
    username, firstname, lastname, password, createCallback) {
  var parameters = '{' +
    '"username":"' + username + '",' +
    '"firstname":"' + firstname + '",' +
    '"lastname":"' + lastname + '",'+
    '"password":"' + password  + '"' +
  '}';
  this.sendPostRequest('create', parameters, createCallback);
}


/**
 * Unlocks current suggested usernames.
 */
AccountProvisioningApp.prototype.unlockUsernames = function() {
  if (this.suggestResponseText) {
    this.select('', this.suggestResponseText, function(r) {console.log(r)});
  }
}


/**
 * Refreshes the suggested usernames.
 */
AccountProvisioningApp.prototype.refreshUsernames = function() {
  var app = window['app'];
  var firstname = app.firstnameElement.value;
  var lastname = app.lastnameElement.value;
  var custom1 = app.custom1Element.value;
  if (firstname.length == 0 || lastname.length == 0) {
    app.hideSuggestions();
    return;
  }
  app.secondsLeft = app.suggestedUsernamesTimeout;
  app.unlockUsernames();
  app.suggest(firstname, lastname, custom1, app.suggestCallback);
}


/**
 * The function that will be called once the suggest REST method returns a
 * value.
 * @param {string} The response text.
 */
AccountProvisioningApp.prototype.suggestCallback = function(responseText) {
  var app = window['app'];
  app.suggestResponseText = responseText;
  var suggestionsArray = JSON.parse(responseText);
  app.populateSuggesitons(suggestionsArray, 1);
  app.showSuggestions();
}

/**
 * Populates the HTML radio buttons with the returned suggestions.
 * @param {Array} suggestions The suggested usernames.
 */
AccountProvisioningApp.prototype.populateSuggesitons = function(suggestions) {
  this.maybeDrawOptions(suggestions);
  for (var i = 1; i <= this.numberOfSuggestions; i++) {
    this.setSuggesitonRadioButton(i, suggestions[i - 1]);
  }
}


/**
 * Populates an HTML radio button with a suggested username.
 * @param {number} id The ID of the radio button to set.
 * @param {string} username The suggested username.
 */
AccountProvisioningApp.prototype.setSuggesitonRadioButton = function(
    id, username) {
  var account = username + '@' + this.domain;
  document.getElementById('r' + id + '-label').innerHTML = account;
  document.getElementById('r' + id).value = username;
}


/**
 * Gets new suggestions.
 */
AccountProvisioningApp.prototype.getNewSuggestions = function() {
  this.suggestResponseText = undefined;
  this.refreshUsernames();
}


/**
 * Appends API calls and executes if this is not running yet.
 * @param {map} postData Stores parameters used in the sendPostRequest method.
 */
AccountProvisioningApp.prototype.prepareSend = function(postData) {
  this.postQueue.push(postData);
  if (!this.waitingForRespone) {
    this.sendRequest();
  }
}


/**
 * Executes the API calls synchronously.
 */
AccountProvisioningApp.prototype.sendRequest = function() {
  this.waitingForRespone = true;
  var postData = this.postQueue.shift();
  var url = API_HOST + '/rest/' + postData.action;
  var xhr = new XMLHttpRequest();
  xhr.onload = function() {
  var app = window['app'];
  postData.callback(this.responseText);
    if (app.postQueue.length > 0) {
      app.sendRequest();
    } else {
      app.waitingForRespone = false;
    }
  };
  xhr.open('POST', url, true);
  xhr.send(postData.parameters)
}


/**
 * Retrieves and sets config parameters from the REST API server.
 */
AccountProvisioningApp.prototype.setupConfig = function() {
  var hostUrl = document.getElementById('hostUrl');
  hostUrl.href = API_HOST + '/rest/config';
  hostUrl.innerHTML = API_HOST;
  var loadingMessage = document.getElementById('loadingMessage');
  HtmlUtils.showElement(loadingMessage);
  var configCallback = function(responseText) {
    var configMap = JSON.parse(responseText);
    console.log(configMap);
    var app = window['app'];
    app.domain = configMap['domain'];
    app.numberOfSuggestions = configMap['numberOfSuggestions'];
    app.suggestedUsernamesTimeout = configMap['suggestedUsernamesTimeout'];

    // Show the configuration properties relevant to the client.
    loadingMessage.innerHTML =
        '<strong>Connection successful!</strong><br/><br/>' +
        'Google Apps Domain: <strong>' + app.domain + '</strong><br/>' +
        'Number of suggestions: <strong>' + app.numberOfSuggestions +
        '</strong><br/>' +
        'Suggested usernames timeout: <strong>' +
        app.suggestedUsernamesTimeout + ' seconds</strong><br/><br/>' +
        'RESTful API Host: <strong>' + API_HOST + '</strong>';
    var hideConfigSuccessfulDiv = function() {
      var app = window['app'];
      HtmlUtils.hideElement(app.loadingDiv);
    };
    setTimeout(hideConfigSuccessfulDiv, 2500);
  };
  this.sendPostRequest('config', '', configCallback);
}



/**
 * Contains static methods to manipulate HTML elements.
 */
HtmlUtils = function() {
}


/**
 * Hides the given HTML element.
 * @param {Element} the HTML element.
 */
HtmlUtils.hideElement = function(element) {
  element.style.display = 'none';
}


/**
 * Shows the given HTML element.
 * @param {Element} the HTML element.
 */
HtmlUtils.showElement = function(element) {
  element.style.display = 'inherit';
}


/**
 * Initializes the account provisioning app.
 */
var initApp = function() {
  window['app'] = new AccountProvisioningApp();
}


/**
 * Wrapper to call the createAccount method.
 */
var createAccount = function() {
  window['app'].createAccount();
}


/**
 * Wrapper to call the getNewSuggestions method.
 */
var getSuggestions = function() {
  window['app'].getNewSuggestions();
}
