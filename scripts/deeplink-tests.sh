#!/bin/sh

#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/places
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/places
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/places --es place_name "Algarve"
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/places/1
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/places/1

#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumptypes
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumptypes
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumptypes --es jumptype_name "Freefly"
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumptypes/1
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/jumptypes/1

#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumps
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumps
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumps --ei jump_number 123 --el jump_date 1291525200000 --es jump_description "intent description" --ei jump_way 10 --ei jump_exit_altitude 12500 --ei jump_deployment_altitude 2500 --ei jump_delay 60 --el jumptype_id 2 --el place_id 2
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumps/1
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/jumps/1
