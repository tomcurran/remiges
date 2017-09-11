#!/bin/sh
adb shell am instrument -w -r org.tomcurran.remiges.test/android.test.InstrumentationTestRunner
