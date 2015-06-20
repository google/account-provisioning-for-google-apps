/**
 * @fileoverview A demo to create Google Apps accounts in bulk via a CSV input.
 */


// Demo configurable variables
// ==========================

/**
 * The address of the server hosting the REST API.
 * {string}
 */
var API_HOST = 'http://localhost:8080';

/**
 * The password of all the created accounts.
 * {string}
 */
var DEFAULT_PASSWORD = '12345678';

// ==========================



/**
 * The Google Apps domain.
 * {string}
 */
window.domain;


/**
 * The results after parsing a CSV file.
 * {Array}
 */
window.fileResults;


/**
 * Sends a POST request to the Account provisioning for Google Apps REST API.
 * @param {string} action The API method name: suggest, select or create.
 * @param {string} parameters The serialized parameters for the API method.
 * @param {Function} callback The function to call when the API method returns a
 * value.
 */
var sendPostRequest = function(action, parameters, callback) {
  var url = API_HOST + /rest/ + action;
  var xhr = new XMLHttpRequest();
  xhr.onload = function() {
    callback(this.responseText);
  };
  xhr.open('POST', url, true);
  xhr.send(parameters)
}


/**
 * Calls the suggest REST API method. Calls the given callback when the method
 * returns a value.
 * @param {Object} userData an object containing user's data.
 * @param {Function} suggestCallback The function to call when the suggest API
 * method returns a value.
 */
var suggest = function(userData, suggestCallback) {
  var parameters = JSON.stringify(userData);
  sendPostRequest('suggest', parameters, suggestCallback);
}


/**
 * Calls the select REST API method. Calls the given callback when the method
 * returns a value.
 * @param {string} username The selected username.
 * @param {string} suggestions A serialized array of suggestions to unlock.
 * @param {Function} selectCallback The function to call when the select API
 * method returns a value.
 */
var select = function(username, suggestions, selectCallback) {
  var parameters = '{' +
    '"username":"' + username + '",' +
    '"suggestions":' + suggestions +
  '}';
  sendPostRequest('select', parameters, selectCallback);
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
var create = function(username, firstname, lastname, password, createCallback) {
  var parameters = '{' +
    '"username":"' + username + '",' +
    '"firstname":"' + firstname + '",' +
    '"lastname":"' + lastname + '",'+
    '"password":"' + password  + '"' +
  '}';
  sendPostRequest('create', parameters, createCallback);
}


/**
 * Auto-generates a Google Apps account using the Account provisioning for
 * Google Apps REST API.
 * @param {Object} userData an object containing user's data.
 */
var generateGoogleAppsAccount = function(userData) {
  var firstname = userData.firstname;
  var lastname = userData.lastname;

  if (firstname == '' || lastname == '') {
    // Skipping.
    return;
  }

  console.log('Generating account for: ' + firstname + ' ' +lastname);
  var selectedUsername;

  // Displays the account that was created.
  var createCallback = function(responseText) {
    var response = JSON.parse(responseText);
    if (response['errorMessage']) {
      throw Error(response['errorMessage']);
    }
    console.log(responseText);
    var account = selectedUsername + '@' + window.domain;
    var log = 'Account ' + account + ' created for: \n' +
        JSON.stringify(userData);
    document.getElementById('csvOutput').innerHTML += log + '\n\n';
    console.log(log + '\n');
  }

  // Create a Google Apps account when selection is done.
  var selectCallback = function(responseText) {
    var response = JSON.parse(responseText);
    if (response['errorMessage']) {
      throw Error(response['errorMessage']);
    }
    console.log(responseText);
    // Call create to create the Google Apps account.
    create(selectedUsername, firstname, lastname, DEFAULT_PASSWORD,
        createCallback);
  }

  // Select the first username suggestion when suggestions are generated.
  var suggestCallback = function(responseText) {
    var suggestions = JSON.parse(responseText);
    // For self account creation, suggestions can be presented to the user
    // through a website.
    // For bulk account creation the first suggestion can be selected, as shown
    // below.
    selectedUsername = suggestions[0];
    // Call select to unlock the non-selected suggesitons.
    select(selectedUsername, responseText /* suggestions in JSON format */,
        selectCallback);
  }

  // Call suggest to generate a username.
  // Through the callbacks defined above, this will also:
  // 1. select the first username suggestion and
  // 2. create a Google Apps account
  suggest(userData, suggestCallback);
}


/**
 * Creates all the accounts in the Google Directory.
 */
var createAccounts = function() {
  if (window.fileResults) {
    parseResults(window.fileResults);
  } else {
    var csvString = document.getElementById('csvInput').value;
    var results = Papa.parse(csvString);
    console.log(results.data);
    parseResults(results);
  }
}


/**
 * Generates accounts for each of the CSV parsing results.
 * @param {Object} Results after parsing the CSV input.
 */
var parseResults = function(results) {
  if (results.data.length <= 1) {
    alert('Please provide an input. First row should contain headers.');
    return;
  }
  var headers = results.data[0];
  if (headers.length < 2) {
    alert('At least firsname and lastname need to be provided in the header.');
    return;
  }
  if (headers[0] != 'firstname' || headers[1] != 'lastname') {
    alert('First two columns should be firstname and lastname.');
    return;
  }
  // Generate an account per result.
  for (var i = 1; i < results.data.length; i++) {
    var userData = results.data[i];
    var userDataObj = {};
    for (var j = 0; j < headers.length; j++) {
      userDataObj[headers[j]] = userData[j];
    }
    console.log(JSON.stringify(userDataObj));
    generateGoogleAppsAccount(userDataObj);
  }
}


/**
 * Retrieves and sets config parameters from the REST API server.
 */
setupConfig = function() {
  var hostUrl = document.getElementById('hostUrl');
  hostUrl.href = API_HOST + '/rest/config';
  hostUrl.innerHTML = API_HOST;
  var loadingMessage = document.getElementById('loadingMessage');
  loadingMessage.style.display = 'inherit';
  var configCallback = function(responseText) {
    var configMap = JSON.parse(responseText);
    console.log(configMap);
    domain = configMap['domain'];

    // Show the configuration properties relevant to the client.
    loadingMessage.innerHTML =
        '<strong>Connection successful!</strong><br/><br/>' +
        'Google Apps Domain: <strong>' + domain + '</strong><br/><br/>' +
        'RESTful API Host: <strong>' + API_HOST + '</strong>';
    var hideConfigSuccessfulDiv = function() {
      document.getElementById('loadingDiv').style.display = 'none';
    };
    setTimeout(hideConfigSuccessfulDiv, 2500);
  };
  this.sendPostRequest('config', '', configCallback);
}


/**
 * Parses a CSV file.
 * @param {event} The event.
 */
function handleFileSelect(event) {
  var files = event.target.files;
  if (files.length == 0) {
    return;
  }
  Papa.parse(files[0], {
    complete: function(results) {
      document.getElementById('copyPasteDiv').style.display = 'none';
      document.getElementById('orP').style.display = 'none';
      window.fileResults = results;
    }
  });
}


/**
 * Initializes the bulk CSV provisioning demo.
 */
var initApp = function() {
  document.getElementById('passwordSpan').innerHTML = DEFAULT_PASSWORD;
  // Check for the various File API support.
  if (window.File && window.FileReader && window.FileList && window.Blob) {
    document.getElementById('fileDiv').style.display = 'inherit';
    document.getElementById('fileLoader').addEventListener(
        'change', handleFileSelect, false);
  } else {
    alert('The File APIs are not fully supported in this browser.\n' +
        'You can still copy/paste a CSV input.');
  }
  setupConfig();
}
