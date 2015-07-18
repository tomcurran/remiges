#!/bin/sh

RESOURCES_FOLDER=$1 # "android/src/main/res"
MATERIAL_ICONS_REPO=$2 # "../../repos/material-design-icons"

DENSITIES=(
	'mdpi'
	'hdpi'
	'xhdpi'
	'xxhdpi'
	'xxxhdpi'
)

VECTOR_DENSITY='anydpi-v21'

ICON_FOLDERS=(
	'navigation'
	'content'
	'action'
	'editor'
	'av'
	'action'
	'maps'
	'navigation'
)

ICONS=(
	'ic_close_white_24dp'
	'ic_add_white_24dp'
	'ic_delete_white_24dp'
	'ic_mode_edit_white_24dp'
	'ic_library_books_black_24dp'
	'ic_accessibility_black_24dp'
	'ic_map_black_24dp'
	'ic_menu_white_24dp'
)

VECTOR_ICONS=(
	'ic_close_black_24dp' # white
	'ic_add_black_24dp' # white
	'ic_delete_black_24dp' # white
	'ic_mode_edit_black_24dp' # white
	'ic_library_books_black_24dp'
	'ic_accessibility_black_24dp'
	'ic_map_black_24dp'
	'ic_menu_black_24dp' # white
)
VECTOR_ICONS_REPLACE_BLACK_WITH_WHITE=(
	true
	true
	true
	true
	false
	false
	false
	true
)

OUTPUT_ICONS=(
	'ic_action_close'
	'ic_menu_add'
	'ic_menu_delete'
	'ic_menu_edit'
	'ic_navigation_drawer_jumps'
	'ic_navigation_drawer_jumptypes'
	'ic_navigation_drawer_places'
	'ic_navigation_drawer'
)

for i in "${!ICONS[@]}"; do
	for DENSITY in "${DENSITIES[@]}"; do
		DRAWABLE_FOLDER="drawable-$DENSITY"
  		cp "$MATERIAL_ICONS_REPO/${ICON_FOLDERS[$i]}/$DRAWABLE_FOLDER/${ICONS[$i]}.png" "$RESOURCES_FOLDER/$DRAWABLE_FOLDER/${OUTPUT_ICONS[$i]}.png"
	done
done

VECTOR_DRAWABLE_FOLDER="drawable-$VECTOR_DENSITY"
for i in "${!VECTOR_ICONS[@]}"; do
    OUTPUT_FILE="$RESOURCES_FOLDER/$VECTOR_DRAWABLE_FOLDER/${OUTPUT_ICONS[$i]}.xml"
	cp "$MATERIAL_ICONS_REPO/${ICON_FOLDERS[$i]}/$VECTOR_DRAWABLE_FOLDER/${VECTOR_ICONS[$i]}.xml" $OUTPUT_FILE
	if [ "${VECTOR_ICONS_REPLACE_BLACK_WITH_WHITE[$i]}" = true ]; then
	    TMP_FILE="$OUTPUT_FILE.tmp"
	    cp $OUTPUT_FILE $TMP_FILE
	    sed "s/fillColor=\"#FF000000\"/fillColor=\"#FFFFFF\"/" $TMP_FILE > $OUTPUT_FILE
	    rm $TMP_FILE
	fi
done
