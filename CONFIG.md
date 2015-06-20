## Google Apps domain configuration guide

Follow these steps to configure your Google Apps domain to work with the Account provisioning for Google Apps API.

### 1. Create a Developers Project

Login to your Google Apps domain with an admin account and go to https://console.developers.google.com/project.

Create a new project, e.g. My project.

![create project][createProject]

Copy the **Project Name** you provided somewhere as you will use it later.

The project dashboard will automatically open once the project is created.

![dashboard][dash]

### 2. Create a Service Account

On the left side panel go to `APIs & auth > Credentials` and click on the button *Create new Client ID*

![new client ID][cred]

Under *Application type* select **Service account**, then click on *Create Client ID*

![clientID window][clientID]

The *Service account* is now created

![Service account][serviceAccountData]

Copy the **Client ID** and **Email address** fields somewhere as you will use them later.

### 3. Generate a P12 key

Click on **Generate new P12 key**

![generate new P12 key][serviceAccount]

A P12 file will download. e.g. `My project-84d807544f50.p12`. You will use this file later.

### 4. Enable the Admin SDK API

On the left side panel go to `APIs & auth > APIs` and search for *admin sdk*. Click on the **Admin SDK** result

![admin sdk api][adminsdkapi]

Click on **Enable API**

![enable api][enableapi]

### 5. Enable the OAuth consumer key

Go to https://admin.google.com and navigate to `Security > Advanced settings > Manage OAuth domain key`

> If you don't see the *Advanced settings* click on *Show more*

![oauth key][oauthkey]

Check the **Enable this consumer key** box

![oauth enabled][oauthset]

### 6. Authorize the Admin SDK API for your Service Account Client

Finally, navigate to `Security > Advanced settings > Manage OAuth domain key`

![Manage OAuth domain key][manageclient]

Enter the **Client ID** that was created in [step 2][step2] in the *Client Name* input box. In the *One or More API Scopes* input box enter `https://www.googleapis.com/auth/admin.directory.user`. Click on **Authorize**

![authorize service][authorizeservice]

### 7. Fill out the API configuration fields

Awesome! You now have everything you need to configure the API.

Open the `config.properties` file and fill out the following fields:


##### `apis.GoogleAPIs.domain`
Enter your Google Apps domain. Example:
```properties
apis.GoogleAPIs.domain=yourdomain.com
```

-------------

##### `apis.GoogleAPIs.authUser`
Enter your Google Apps account. This is the account you used to sign in and create the developers project in [step 1][step1]. Example:
```properties
apis.GoogleAPIs.authUser=you@yourdomain.com
```

-------------

##### `apis.GoogleAPIs.keyPath`
Move the P12 file you created in [step 3][step3] to the location where the `appsProvisioning-0.0.1.jar` file is located. Set the `keyPath` value to your P12 file location. Example:
```properties
apis.GoogleAPIs.keyPath=./My project-84d807544f50.p12
```

-------------

##### `apis.GoogleAPIs.serviceAccountEmail`
Enter the Service account **Email address** you created in [step 2][step2]. Example: 
```properties
apis.GoogleAPIs.serviceAccountEmail=1234567890123-abcdefghijklmnopqrstuvwxz01234567@developer.gserviceaccount.com
```

-------------

##### `apis.GoogleAPIs.appName`
Provide the **Project Name** you created in [step 1][step1]. Example:

```properties
apis.GoogleAPIs.appName=My project
```

<br/>

Great! The configuration is now complete. You can now start using the Account provisioning for Google Apps API in your Google Apps domain.

[step1]: #1-create-a-developers-project
[step2]: #2-create-a-service-account
[step3]: #3-generate-a-p12-key
[authorizeservice]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/authorizeservice.png
[serviceAccountData]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/serviceAccountData.png
[manageclient]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/manageclient.png
[oauthset]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/oauthset.png
[oauthkey]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/oauthkey.png
[enableapi]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/enableapi.png
[adminsdkapi]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/adminsdkapi.png
[serviceAccount]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/serviceAccount.png
[clientID]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/clientID.png
[dash]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/dash.png
[cred]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/cred.png
[createProject]: http://googledrive.com/host/0B0hbybT0K1l7fjR6aEt3bl9XZFVFS1RGV3RMNW16LXlIWmhuRXhUWGsxWndIV2p6cEtlSkE/createProject.png
