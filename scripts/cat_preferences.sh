#!/bin/sh
adb shell cat /data/data/org.tomcurran.remiges/shared_prefs/org.tomcurran.remiges_preferences.xml | xmllint --format -
