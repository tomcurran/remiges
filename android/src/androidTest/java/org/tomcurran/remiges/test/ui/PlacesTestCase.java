package org.tomcurran.remiges.test.ui;


import android.content.ContentValues;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.text.TextUtils;

public class PlacesTestCase extends ItemTestCase {

    // content value keys
    public static final String PLACE_NAME = "place_name";
    public static final String PLACE_LATITUDE = "place_latitude";
    public static final String PLACE_LONGITUDE = "place_longitude";

    // content description and text values used for navigation
    public static final String TEXT_NAVIGATION = "Places";
    public static final String TEXT_TITLE = "Places";

    // content descriptions, text values and resource IDs
    private static final String DESCRIPTION_ADD = "Add Place";
    private static final String DESCRIPTION_EDIT = "Edit";
    private static final String DESCRIPTION_DELETE = "Delete";
    private static final String TEXT_DETAIL_TITLE = "Place Detail";
    private static final String RESOURCE_EDIT_NAME = APP_PACKAGE + ":id/edit_place_name";
    private static final String RESOURCE_EDIT_LATITUDE = APP_PACKAGE + ":id/edit_place_latitude";
    private static final String RESOURCE_EDIT_LONGITUDE = APP_PACKAGE + ":id/edit_place_longitude";

    // edit field hint text values
    private static final String HINT_NAME = "Place Name";
    private static final String HINT_LATITUDE = "Latitude";
    private static final String HINT_LONGITUDE = "Longitude";

    // generates unique names
    private static int nameCount = 0;
    private static String getNextPlaceName() {
        return String.format("place %d", ++nameCount);
    }

    // generates random coordinates
    private static double getRandomCoordinate() {
        return Math.round((Math.random() * 360 - 180) * 1000) / 1000;
    }

    @Override
    public ContentValues getNewValues() {
        ContentValues values = new ContentValues();
        values.put(PLACE_NAME, getNextPlaceName());
        values.put(PLACE_LATITUDE, getRandomCoordinate());
        values.put(PLACE_LONGITUDE, getRandomCoordinate());
        return values;
    }

    @Override
    public void editValues(ContentValues values) throws UiObjectNotFoundException {
        changeTextField(RESOURCE_EDIT_NAME, values.getAsString(PLACE_NAME), HINT_NAME);
        changeTextField(RESOURCE_EDIT_LATITUDE, values.getAsString(PLACE_LATITUDE), HINT_LATITUDE);
        changeTextField(RESOURCE_EDIT_LONGITUDE, values.getAsString(PLACE_LONGITUDE), HINT_LONGITUDE);
    }

    @Override
    public void assertDetail(ContentValues values) throws UiObjectNotFoundException {
        if (!isTwoPane()) {
            assertEquals(values.getAsString(PLACE_NAME), getActionBarTitle());
            // TODO assert PLACE_LATITUDE && PLACE_LONGITUDE
        }
    }

    @Override
    public void assertHint() throws UiObjectNotFoundException {
        assertEquals(HINT_NAME, getByResource(RESOURCE_EDIT_NAME).getText());
        assertEquals(HINT_LATITUDE, getByResource(RESOURCE_EDIT_LATITUDE).getText());
        assertEquals(HINT_LONGITUDE, getByResource(RESOURCE_EDIT_LONGITUDE).getText());
    }

    @Override
    public String getListClickTarget(ContentValues values) {
        return values.getAsString(PLACE_NAME);
    }

    @Override
    public String getTitle(ContentValues values) {
        if (isTwoPane()) {
            return TEXT_TITLE;
        } else {
            String name = values.getAsString(PLACE_NAME);
            return TextUtils.isEmpty(name) ? TEXT_DETAIL_TITLE : name ;
        }
    }

    @Override
    public String getEditAction() {
        return DESCRIPTION_EDIT;
    }

    @Override
    public String getDeleteAction() {
        return DESCRIPTION_DELETE;
    }

    @Override
    public String getAddAction() {
        return DESCRIPTION_ADD;
    }

    @Override
    public void navigateTo() throws UiObjectNotFoundException {
        getNavigationDrawerTestCase().navigateTo(PlacesTestCase.TEXT_NAVIGATION, PlacesTestCase.TEXT_TITLE);
    }

    @Override
    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to places
        navigateTo();

        // navigate to jump type
        getNavigationDrawerTestCase().navigateTo(JumpTypeTestCase.TEXT_NAVIGATION, JumpTypeTestCase.TEXT_TITLE);

        // navigate to places
        navigateTo();
    }

}
