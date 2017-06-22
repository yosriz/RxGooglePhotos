# Google Photos Reactive API Client

[![](https://jitpack.io/v/yosriz/RxGooglePhotos.svg)](https://jitpack.io/#yosriz/RxGooglePhotos)

Reactive (RxJava2) API client for Google Photos (formerly known as Picasa)

based on work done at [android-picasa-client](https://github.com/plusCubed/android-picasa-client) by Daniel Ciao.

### Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```

Add this to your module's `build.gradle` file:

```Gradle
dependencies {
    compile 'com.github.yosriz:RxGooglePhotos:{latest-version}'	
}
```


### Usage

- construct `GooglePhotosClient` object, and delegate `onActivityResult` event  :
    ```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        googlePhotosClient.onActivityResult(requestCode, resultCode, data);
    }
    ```
    
- `GooglePhotosClient` relies on Google Sign-In mechanism to get access to user photos.
  use `GooglePhotosClient` object to create a service, either sliently (if already signed in), or non-sliently (to display account selection and permission dialogs) :
   ```java
   googlePhotosClient.createServiceSilently(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(googlePhotosService -> {
                    //use googlePhotosService to perform queries
                }
   ```
    
- `createServiceSilently`/`createServiceWithSignIn` methods will return `GooglePhotosService` with proper authorization on success,
   `GooglePhotosService` is the API gate to perform Google Photos queries.
   
- for switching user/sign out, use `GooglePhotosClient.signout()`.  

  
    

### Sample

see Sample project for full demo.

### Create Google API OAuth credential

In order to be able to sign in using Google credentials, an app must be registered through Google Dev Console, using key store that app was signed with. 
see this SO [answer](https://stackoverflow.com/a/41012703/3903847) for required steps.

### Note 
This library using [Picasa Web Albums Data API](https://developers.google.com/picasa-web/), as Google stated that [Picasa is retiring](https://developers.google.com/picasa-web/docs/3.0/releasenotes#picasa-is-retiring-february-12-2016), any breaking changes can be happen in the future.

### License

```
Copyright 2017 Yossi Rizgan
Copyright 2016 Daniel Ciao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
