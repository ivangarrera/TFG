All commands must be executed from this directory.

1. Setup [Firebase](https://console.firebase.google.com/) for the project

   - Create an account (this project uses Firestore to store the data)
   - Create a new project, named GVIDI
   - Create a new android app within the project (the name of the Android package must be `com.example.ivangarrera.example`)
   - Download the `google-services.json` file and place inside the _GVIDI/app_ directory

2. Replace the Google Maps API key with your own key

3. Build a docker image that will be used for building Android projects

`docker build -t android-build:android-gradle .`

4. Clean the project

`docker run --rm -v "$PWD":/home/gradle/ -w /home/gradle/GVIDI android-build:android-gradle gradle -PdisablePreDex clean`

5. Build the APK for the project

`docker run --rm -v "$PWD":/home/gradle/ -w /home/gradle/GVIDI android-build:android-gradle gradle -PdisablePreDex assembleRelease`

6. The APK file is placed in the _GVIDI/app/build/outputs/apk/release_ directory. This file can be installed on an Android device.
