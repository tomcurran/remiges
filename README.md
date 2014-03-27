# Remiges

Remiges is a skydiving logbook application that aims to make the activity of maintaining a logbook both easier and faster to complete.

## Build

Android Studio is required to build the application. Get Android Studo at http://developer.android.com/sdk/installing/studio.html. All other dependencies are included in the project or will be included in the build process by gradle. Run the `android` module from with in Android Studio to build the application.

## Test

Building and running the tests depends on Ant. Get Ant at http://ant.apache.org/

### Build Tests
```
$ cd android
$ ant build
```

### Run Tests

To intall and execute the tests on a device run the following commands. The `Dclass` parameter specifies which test class or method to execute.

#### Run Test Flow
```
$ ant testclass -Dclass=JumpTypeTestCase
```

#### Run Test Case
```
$ ant testclass -Dclass=JumpTypeTestCase#testEdit
```
