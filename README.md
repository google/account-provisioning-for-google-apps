# Account provisioning for Google Apps

Account provisioning for Google Apps is an open source API to:
* **Generate available usernames**  in your Google Apps domain
* **Create Google Apps accounts** in your domain

It can be used in a website where users create their own accounts...

![self account provisioning demo][selfGif]  | 
-------------

a script that creates accounts in bulk...

![bulk account provisioning demo][bulkGif]  | 
-------------

 or via a CSV input...

![csv input][csv]  | 
-------------

Usernames are generated automatically via configurable patterns. Sample images are taken from the included demos. Give them a try [here](#quick-start)!

This API can be installed as a RESTful service (to be invoked from almost any programming language and platform) or as a Java library.

### Who should use it
Google Apps deployments where new usernames need to be created.
Deployments that need to sync existing usernames can use [Google Apps Directory Sync] (https://support.google.com/a/answer/106368?hl=en) or [Google Apps School Directory Sync] (https://support.google.com/a/answer/6027781?hl=en).


### Why you should use it
* **Username cache:** It caches all your Google Apps accounts, which makes it fast (minimum calls to Google servers) and less likely to hit [Directory API calls/day limits] (https://developers.google.com/admin-sdk/directory/v1/limits)
* **Custom user fields:** Usernames are generated from the user's first name, last name and a set of (optional) custom fields, i.e. second last name, student ID, department, nickname, etc.
* **Locked suggestions:** Suggested usernames will remain locked (unavailable to other users) until they expire or are explicitly unlocked.
* **Backed by Google Apps Admin SDK:** Uses the Google Apps [AdminSDK Directory API] (https://developers.google.com/admin-sdk/directory/)
* **SSL support:** All REST calls can be encrypted using SSL
* **Any programming language:** You can choose any programming language and platform that supports REST calls. Almost any language and platform do.


### What methods does this API offer

* **`suggest`**: returns a list of username suggestions based on first name, last name and a set of custom fields.
* **`create`**: creates a Google Apps account.
* **`select`**: unlocks username suggestions that will no longer be used.

> See the [API Overview](#api-overview-and-sample-code) section to learn more about how to invoke these methods.

# Quick start

*Ready? Try the demos!*

This API needs Java, but don't worry you don't need to develop your client in Java. You can use any language and platform you like (Python, JavaScript, PHP, C#, ObjectiveC, Go, etc.) as long as it can do [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) calls.

> Included demos are built in JavaScript.

### 1. Check your Java version

In the terminal run:
```bash
java -version
```
If you see: `java version "1.7.X"` or a newer version you are ready to go. If not, [install Java 7 or a newer verion](https://java.com/en/download/help/download_options.xml).

### 2. Set your Google Apps domain configuration

Follow the steps in the [Google Apps domain configuration guide][configGuide] to configure your domain to work with this API. 

### 3. Start the RESTful API service

 Move the `config.properties` and the `p12` file (created in the previoius step) to the `bin/` folder.
 
 In the terminal, run:

```bash
java -jar appsProvisioning-0.0.1.jar -rest -port 8080
```

> This will start the RESTful API service on port 8080. If you already have a server running on port 8080, change it to another port, e.g. 8888.


### 4. Open a demo

Awesome! You can now start getting usernames suggestions and creating Google Apps accounts in your domain.

> See the Note below if you used a different port from 8080

Open any of the `index.html` demos under the `demos/` folder:

Demo | Sample
------------- | -------------
<strong>self-provisioning-demo</strong><br/>Each user selects and creates their own account | ![self account provisioning demo][selfGif]
<strong>bulk-provisioning-demo</strong><br/>All accounts are created in bulk | ![bulk account provisioning demo][bulkGif]
<strong>csv-provisioning-demo</strong><br/>All accounts are created in bulk from a CSV input | ![csv demo][csv]

**Note:** If you used a different port from 8080, open first the JavaScript `.js` file inside the demo folder and update

```javascript
var API_HOST = 'http://localhost:8080';
```
to point to the right port. For example:

```javascript
var API_HOST = 'http://localhost:8888';
```

Both demos will initially show the configuration parameters that are relevant to the client. For example:

![demo info][demoInfo]

> This screen will disappear after a couple of seconds.

You can change these parameters in the `config.properties` file and use this screen to verify the current configuration. Follow the next step to see how.

> A quick way of testing your server is by invoking the `suggest` method via the GET service <a href="http://localhost:8080/rest/suggest?firstname=john&lastname=smith"target="_blank">http://localhost:8080/rest/suggest?firstname=john&lastname=smith</a>


### 5. Play with the configuration

Now that you have the demos up and running is a great time to learn how usernames are generated.

1. Open the `config.properties` file and look for the `accounts.UsernameGeneration.patterns` property.
2. Replace its value with the following one:
```properties
accounts.UsernameGeneration.patterns=[C1_firstname].[lastname][custom1],[C3_lastname]_[custom1],[C1_custom1][firstname],[firstname][C1_lastname]_[custom1],[lastname][custom1]
```

Now kill the Java process (`Ctrl+C` or `Cmd+C`) in the terminal and start the RESTful API service again ([step 3](#3-start-the-restful-api-service)). This will load the new `patterns` configuration, which will result in different usernames being generated.

Open the `self-provisioning-demo\index.html` demo and notice how the generated usernames are now different. They now follow the new patterns set in the config file. The [configuration section](#accountsusernamegenerationpatterns) explains how to patterns work.

Next, you can try changing other `accounts.UsernameGeneration` properties. For example, updating the following properties:

```properties
accounts.UsernameGeneration.numberOfSuggestions=5

accounts.UsernameGeneration.suggestedUsernamesTimeout=10
```

This will result in:

* 5 usernames being suggested (instead of 3)
* usernames will expire after 10 seconds (instead of in 2 minutes)

### Next steps

* To learn more about other configuration properties (e.g. how to enable SSL in your server), take a look at the [Configuration properties][config] section.
* To start changing the client side code, take a look at the [API overview](#api-overview-and-sample-code) section.
* To modify the server code side, take a look at the [Contributing](#contributing) section.
* If you have any questions or feedback, please let us know in the [forum][forum]!

## Index
* [System overview](#system-overview)
* [API overview and sample code](#api-overview-and-sample-code)
* [Configuration properties][config]
* [Building][building]
* [Feedback](#feedback)
* [License](#license)
* [Contributing](#contributing)

# System overview

This API is developed in Java and can be invoked from any language and platform that can do REST API calls. The username cache is an [H2](http://www.h2database.com/) database, so you will see a `usernames(*).mv.db` file when running the API.

![use cases diagram][useCasesDiagram]

>  Only usernames are cached, no names or other user's data is ever stored. The [H2 Console Application](http://www.h2database.com/html/quickstart.html#h2_console) can be used to inspect the cache.

The cache can be disabled with the [`cachedUsernames`](#accountsusernamegenerationcachedusernames) property. When disabled it will do Admin SDK API calls. When enabled, the cache will refresh periodically (see the [`cacheExpirationHours`](#accountsusernamegenerationcacheexpirationhours) property).

# API overview and sample code

### `suggest` method
**Description:** Method that suggests available usernames in a domain. Uses the configuration file (see [configuration][config]) to determine:

* Number of suggested usernames: [`numberofsuggestions`](#accountsusernamegenerationnumberofsuggestions)
* Patterns used to generates usernames: [`patterns`](#accountsusernamegenerationpatterns)
* Google Apps Domain: [`domain`](#apisgoogleapisdomain)

**Note:** All suggested usernames will remain locked until they expire (see [`suggestedUsernamesTimeout`](#accountsusernamegenerationsuggestedusernamestimeout)) or the [`select`](#select-method) method is called.

 | REST API | Java API |
------------ | ------------- | ----------------
**Method** | `rest/suggest` | `apps.provisioning.server.account.UsernameManager.suggest`
**Parameters** | JSON map with the following fields: <ul><li>`firstname` the user's first name</li><li>`lastname` the user's last name</li></ul>The JSON map might include customizable fields. For example:<ul><li>`secondLastname`</li><li>`nickname`</li></ul> | `userData`: A `java.util.HashMap<String, String>`with the following fields: <ul><li>`firstname` the user's first name</li><li>`lastname` the user's last name</li></ul>The HashMap might include customizable fields. For example:<ul><li>`secondLastname`</li><li>`nickname`</li></ul>
**Returns** | In case of success, it returns a JSON serialized array with username suggestions, in case of error it returns a JSON serialized map with the `"errorMessage"` index explaining the error. | A `java.util.ArrayList<String>` of suggestions. Throws an `Exception` in case of an error.

<br/>

#### Sample code for `suggest` 

##### REST API
```javascript
var url = 'http://localhost:8080/rest/suggest';
var parameters = '{' +
    '"firstname": "Carlos",' +
    '"lastname": "Alvarez",' +
    '"secondLastname": "Martinez"' +
'}';
var xhr = new XMLHttpRequest();
xhr.onload = function() {
  alert(this.responseText);
};
xhr.open('POST', url, true);
xhr.send(parameters);
```

##### Java API
```java
HashMap<String, String> userData = new HashMap<String, String>();
userData.put("firstname", "Carlos");
userData.put("lastname", "Álvarez");
userData.put("secondLastname", "Martinez");

ProvisioningApp provisioningApp = ProvisioningApp.getInstance();
provisioningApp.initApp();
UsernameManager usernameManager = provisioningApp.getUsernameManager();
ArrayList<String> suggestions = usernameManager.suggest(userData);
```

**Result**
```javascript
["carlos.alvarez","carlosalvarez","c.alvarez_martinez"]
```

**Note:**

The following `config.properties` (see [configuration][config]) was used for this example:
```properties
...
patterns = [firstname].[lastname], [firstname][lastname], [C1_firstname].[lastname]_[secondLastname]
numberOfSuggestions = 3
...
```

<br/>

## `select` method
**Description:** Selects the given username from the given suggestions. This will unlock all the suggestions, except the selected one (if any).

 | REST API | Java API |
------------ | ------------- | ----------------
**Method** | `rest/select` | `apps.provisioning.server.account.UsernameManager.select`
**Parameters** | JSON map with the following fields: <ul><li>`username` the selected username</li><li>`suggestions` a list of suggestions</li></ul> | <ul><li>`suggestions` an `ArrayList<String>` of suggested usernames</li><li>`selectedUsername` the selected username</li></ul>
**Returns** | In case of success, it returns a JSON serialized array with username suggestions, in case of error it returns a JSON serialized map with the `"errorMessage"` index explaining the error. | `void` <br/> Throws an `Exception` if an error occurs.

#### Sample code for `select`

##### REST API
```javascript
var url = 'http://localhost:8080/rest/select';
var parameters = '{"username":"carlos.alvarez", "suggestions":["carlos.alvarez","carlosalvarez","c.alvarez"]}';
var xhr = new XMLHttpRequest();
xhr.onload = function() {
  alert(this.responseText);
};
xhr.open('POST', url, true);
xhr.send(parameters);
```

**Result**
```javascript
{"message":"User selected successfully."}
```

##### Java API
```java
String selectedUsername = "carlos.alvarez";
ArrayList<String> suggestions = new ArrayList<String>();
suggestions.add("carlos.alvarez");
suggestions.add("carlosalvarez");
suggestions.add("c.alvarez");

ProvisioningApp provisioningApp = ProvisioningApp.getInstance();
provisioningApp.initApp();
UsernameManager usernameManager = provisioningApp.getUsernameManager();
usernameManager.select(suggestions, selectedUsername);
```

<br/>

## `create` method
**Description:** Creates a Google Apps account in the provided Google Apps Domain (see [`domain`](#apisgoogleapisdomain)).

 | REST API | Java API |
------------ | ------------- | ----------------
**Method** | `rest/create` | `apps.provisioning.server.account.UsernameManager.create`
**Parameters** | JSON map with the following fields: <ul><li>`username` account's username</li><li>`firstname` user's first name</li><li>`lastname` user's last name</li><li>`password` account's password</li></ul> | <ul><li>`username` account's username</li><li>`firstname` user's first name</li><li>`lastname` user's last name</li><li>`password` account's password</li></ul>
**Returns** | In case of success, it returns a JSON serialized array with username suggestions, in case of error it returns a JSON serialized map with the `"errorMessage"` index explaining the error. | `void` <br/> Throws an `Exception` if an error occurs.

> Username and password fields must comply with the [Google Apps Name and password guidelines] (https://support.google.com/a/answer/33386?hl=en)

#### Sample code for `create`

##### REST API
```javascript
var url = 'http://localhost:8080/rest/create';
var parameters = '{"username":"carlos.alvarez", "firstname":"Carlos", "lastname":"Alvarez", "password":"12345678"}';
var xhr = new XMLHttpRequest();
xhr.onload = function() {
  alert(this.responseText);
};
xhr.open('POST', url, true);
xhr.send(parameters);
```

**Result**
```javascript
{"message":"User created successfully."}
```

##### Java API
```java
String username = "carlos.alvarez";
String firstname = "Carlos";
String lastname = "Alvarez";
String password = "12345678";

ProvisioningApp provisioningApp = ProvisioningApp.getInstance();
provisioningApp.initApp();
UsernameManager usernameManager = provisioningApp.getUsernameManager();
usernameManager.create(username, firstname, lastname, password);
```

<br/>

## API limits
Account provisioning for Google Apps follows the same [AdminSDK Directory API limits] (https://developers.google.com/admin-sdk/directory/v1/limits). Each call to `create`, `select` and `suggest` consumes a different number of Directory API calls:
- `create`: 1 API call
- `select`: 0 API calls
- `suggest`:
 - cache enabled ([`cachedUsernames=YES`](#accountsusernamegenerationcachedusernames)): 0 API calls
 - cache disabled ([`cachedUsernames=NO`](#accountsusernamegenerationcachedusernames)): number of API calls is equal or larger than the [`numberOfSuggestions`](#accountsusernamegenerationnumberofsuggestions) property

# Configuration properties
The configuration is set in the `config.properties` file. Configuration properties are divided in four categories:

1. [Username generation properties](#username-generation-properties): use the property prefix `accounts.UsernameGeneration.`
2. [Google API properties](#google-api-properties): use the property prefix `apis.GoogleAPIs.`
3. [Cache location properties](#cache-location-properties): use the property prefix `db.h2.`
4. [SSL properties](#ssl-properties): use the property prefix `security.ssl.`

<br/>

## Username generation properties

##### `accounts.UsernameGeneration.cachedUsernames`
**Description**: A username cache can be used to check if a username already exists. This prevents reaching AdminSDK API calls/day limit. If `cachedUsernames` is set to `YES` username availability will be checked against the cache. If set to `NO` it will be checked against the Google Directory (using a Directory API call).

**Possible values**: `YES` and `NO`

-------------

##### `accounts.UsernameGeneration.cacheExpirationHours`
**Description**: Defines the expiration time in hours of the usernames cache. After expiration, the application refreshes the username cache. For reference, refreshing an account with 1 million users takes approximately 35 minutes.

**Possible values**: Integers larger or equal to 1

**Default**: `24`

-------------

##### `accounts.UsernameGeneration.numberOfSuggestions`
**Description**: The number of username suggestions to be returned for each call to `suggest`.

**Possible values**: Integer between 1 and 10 (inclusive)

**Default**: `3`

-------------

##### `accounts.UsernameGeneration.suggestedUsernamesTimeout`
**Description**: The amount of time (in seconds) that suggested usernames will remain locked (unavailable to another client).

**Possible values**: Integer greater than 0.

**Default**: `120` (2 minutes)

-------------

##### `accounts.UsernameGeneration.patterns`
**Description**: A pattern is something that looks like `[firstname][lastname]`. This pattern indicates the API that we want to generate a username with *"the firstname followed by the lastname"*. Now, if that username happens to be taken the API will need another pattern. Therefore, a list of multiple patterns is recommended. For example:

```properties
accounts.UsernameGeneration.patterns=[firstname][lastname],[lastname][firstname]
```

This will first try to generate a username with *"the firstname followed by the lastname"* if that is taken then it will try to generate a username with *"the lastname followed by the firstname"*.

*So far so good?*

Great. Now, say we want to generate a username with *"the nickname followed by the lastname"* of a person. We'd simply do:

```properties
accounts.UsernameGeneration.patterns=[nickname][lastname]
```

*What's `[nickname]` you might ask?*

Well, `nickname` is a field that should be passed to the [`suggest`](#suggest-method) method. For example:

```javascript
var parameters = '{' +
    '"firstname": "Jonathan",' +
    '"lastname": "Bravo",' +
    '"nickname": "Jonny"' +
'}';
```

This will generate the username `jonnybravo`.

**NOTE:**
* It is not mandatory to pass `nickname` (or any custom field) for all users. If `nickname` is not provided for a user the API will skip that pattern and move on to the next one.
* `firstname` and `lastname` are mandatory. Even if the pattern doesn't make use of them.


#### Taking the first char(acters) of a field

Now say that for someone named *John Smith* we'd like to generate a username `jsmith`. We'd do it with the following pattern:

```properties
[C1_firstname][lastname]
```

**C1** indicates that the API should take **the first character** of **firstname**. If we'd like to take the first **two** characters then we'd do:

```properties
[C2_firstname][lastname]
```

This same method applies for custom fields. For example, if we wanted to split the first character of the `nickname` we'd do:

```properties
[C1_nickname]
```

#### Adding a counter to a pattern

Now, say we have so many people named "John Smith" in a school district that we ran out of patterns. We could then define a pattern that adds a number at the end of the username:

```properties
[firstname][lastname][#]
```

**[#]** indicates that a number will be added to the username. For example: `johnsmith3`. This numeric value will start at 1 and continue adding until a username is available.

**Note:** The use of **[#]** should be the last resort as this counter can become hard to remember, e.g. `johnsmith3816`. A list of multiple patterns without **[#]** is encouraged before using a pattern with **[#]**.


#### Adding separators to usernames

A common practice is to separate usernames with a period (.), an underscore (_) or a dash (-). These separators can be added to patterns. Example:

```properties
[firstname].[lastname]
```

would generate the username `john.smith`

#### Adding a string to a pattern

Same as the separators, it is possible to add a static string to username suggestions. For example, the pattern:

```properties
[lastname]_nyc
```

would generate the username `smith_nyc`


#### What if the API runs out of patterns?

The following pattern is used as the last resort:

`[C9_firstname][C9_lastname][#]`


#### `patterns` example
<br/>
Given the following user data map, customFields and patterns:
```json
{
  "firstname": "Carlos",
  "lastname": "Álvarez",
  "region": "CA",
  "group": "5A"
}
```

```properties
patterns = [firstname].[lastname], [C1_firstname].[lastname], [firstname][lastname]_[region], [firstname][lastname]_[group], [lastname]_nyc, [firstname][lastname][#]
```

Then consecutive calls to the suggest method will return (in that order):

 Generated username | Used pattern
------------ | -------------
carlos.alvarez | `[firstname].[lastname]`
c.alvarez | `[C1_firstname].[lastname]`
carlosalvarez_mx | `[firstname][lastname]_[region]`
carlosalvarez_5A | `[firstname][lastname]_[group]`
alvarez_nyc | `[lastname]_nyc`
carlosalvarez1 | `[firstname][lastname][#]`
carlosalvarez2 | `[firstname][lastname][#]`
carlosalvarez3 | `[firstname][lastname][#]`
... | `[firstname][lastname][#]`

> **Notes:**
- Special characters (e.g accents) are removed
- All text gets converted to lowercase
- Using a [#] will ignore the following patterns (as it will continue increasing the counter)

<br/>
<br/>

## Google API properties

Follow the steps in the [Google Apps domain configuration guide][configGuide] to configure these properties.

##### `apis.GoogleAPIs.domain`

**Description**: The Google Apps domain

**Example:** `apis.GoogleAPIs.domain=yourdomain.com`

-------------

##### `apis.GoogleAPIs.authUser`

**Description**: Admin user who created the project in the Google Developer Console.

**Example:** `apis.GoogleAPIs.authUser=admin@yourdomain.com`

-------------

##### `apis.GoogleAPIs.keyPath`

**Description**: Path to the file that stores the Google private key. Can be generated following the steps in: https://cloud.google.com/storage/docs/authentication#service_accounts

**Example:** `apis.GoogleAPIs.keyPath=./service_account_key.p12`

-------------


##### `apis.GoogleAPIs.serviceAccountEmail`

**Description**: Internal user for server side applications. Can be generated following the steps in:
https://cloud.google.com/storage/docs/authentication#service_accounts

**Note:** You should enable API scopes in the Google Admin Console. This scopes can be registered following the steps in: https://support.google.com/a/answer/162106?hl=en

The following scope to the service account should be added: https://www.googleapis.com/auth/admin.directory.user

**Example:** `apis.GoogleAPIs.serviceAccountEmail=1234567890123-abcdefghijklmnopqrstuvwxz01234567@developer.gserviceaccount.com`

-------------

##### `apis.GoogleAPIs.appName`

**Description**: This value is the project name in the Google Developer Console. Can be obtained following the steps in: https://developers.google.com/console/help/new/#creatingdeletingprojects

**Example:** `apis.GoogleAPIs.appName=My project`

<br/>
<br/>

## Cache location properties

##### `db.h2.name`

**Description**: The name of the H2 database `.mv.db` file.

**Default**: `usernames`

-------------

##### `db.h2.path`

**Description**: The path where the H2 database (`.mv.db` file) will be created.

**Default**: `./`

<br/>
<br/>

## SSL properties

##### `security.ssl.useSSL`

**Description**: Enables HTTPS support over SSL.

**Possible values**: `YES` and `NO`

**Default**: `NO`

-------------

##### `security.ssl.keyStorePath`

**Description**: The path where the KeyStore (`jks` file) is located. This can be generated executing:
```bash
keytool -genkey -alias sitename -keyalg RSA -keystore keystore.jks -keysize 2048
```

-------------

##### `security.ssl.keyStorePassword`

**Description**: The KeyStore password. Password provided when the `jks` file was generated.

-------------

##### `security.ssl.keyManagerPassword`

**Description**: Commonly the same as `keyStorePassword`. Can be different if it is not a self-generated certificate.


# Building

### Requirements
- [Java 7+](https://www.java.com/en/download/help/download_options.xml)
- [Maven](https://maven.apache.org/download.cgi)

## Build steps

To generate a `appsProvisioning-0.0.1.jar` file under *./target*:

**From bash**

From the project's root folder, run:
```bash
mvn clean install -Dmaven.test.skip=true
```

**From Eclipse**
 1. Right click on the project
 2. Run As > Maven build...
  - Under **Goals:** use `clean install`
  - Check the **Skip test** checkbox

> **Note:** Tests create and remove Google Apps accounts in a test domain. Therefore, they are discouraged unless you plan to submit a change that affects those tests.

# Feedback

If you have any questions or feedback, please let us know in the [forum][forum]!

## Impressions

This API logs the number of calls to `suggest`, `create` and `select` per Google Apps domain. No other information is ever collected. This helps us justify adding more resources and support to this API.

# License

Account provisioning for Google Apps is licensed under Apache 2.0. Full license text is available in the [LICENSE][license] file.

This is not an official Google product (experimental or otherwise), it is just code that happens to be owned by Google.


# Contributing

See [CONTRIBUTING][contributing].

A good starting point to look at the Java code is 

```java
apps.provisioning.server.rest.ProvisioningAction
```

you can navigate through each of the methods: `suggest`, `select` and `create`.

[contributing]: https://github.com/google/...


[forum]: https://groups.google.com/forum/?hl=en#!forum/account-provisioning-for-google-apps
[api]: #api--examples
[building]: #building
[installing]: #installing
[config]: #configuration-properties
[customfields]: #customfields
[csv]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/csv.png
[useCasesDiagram]: http://googledrive.com/host/0B0hbybT0K1l7fjVIYnZDdG5jMjg3QUwyOFN3UVFTSFg4cnZHQ3VIU3JWc25hU0RWVlo2TXM/diagram3.png
[tokens]: http://googledrive.com/host/0B0hbybT0K1l7fjVIYnZDdG5jMjg3QUwyOFN3UVFTSFg4cnZHQ3VIU3JWc25hU0RWVlo2TXM/tokens.png
[bulkGif]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/bulk-demo.gif
[selfGif]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/self-demo.gif
[demoInfo]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/demoInfo.png
[configGuide]: https://github.com/google/account-provisioning-for-google-apps/blob/master/CONFIG.md
[contributing]: https://github.com/google/account-provisioning-for-google-apps/blob/master/CONTRIBUTING.txt
[license]: https://github.com/google/account-provisioning-for-google-apps/blob/master/LICENSE.txt
