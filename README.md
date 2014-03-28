# Remiges

Remiges is a skydiving logbook application that aims to make the activity of maintaining a logbook both easier and faster to complete.

## Build

### Android Studio

Click Build and select Make Project. Get Android Studo at http://developer.android.com/sdk/installing/studio.html

### Command Line

On Mac OS and Linux platforms
```
$ chmod +x gradlew
$ ./gradlew assembleDebug
```

On Windows platforms
```
> gradlew.bat assembleDebug
```

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
