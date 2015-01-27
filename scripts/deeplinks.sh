#!/bin/sh

# Jumps list
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumps

# Add Jump
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumps
# --ei jump_number 123 \
# --el jump_date 1291525200000 \
# --es jump_description "intent description" \
# --ei jump_way 10 \
# --ei jump_exit_altitude 12500 \
# --ei jump_deployment_altitude 2500 \
# --ei jump_delay 60 \
# --el jumptype_id 2 \
# --el place_id 2

# Edit Jump
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/jumps/#

# Places list
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/places

# Add Place
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/places
# --es place_name "Algarve"
# place_latitude & place_longitude cannot pass double to shell

# Edit Place
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/places/#

# Jump Types list
#adb shell am start -a android.intent.action.VIEW -d content://org.tomcurran.remiges/jumptypes

# Add Jump Type
#adb shell am start -a android.intent.action.INSERT -d content://org.tomcurran.remiges/jumptypes
# --es jumptype_name "Freefly"

# Edit Jump Types
#adb shell am start -a android.intent.action.EDIT -d content://org.tomcurran.remiges/jumptypes/#
