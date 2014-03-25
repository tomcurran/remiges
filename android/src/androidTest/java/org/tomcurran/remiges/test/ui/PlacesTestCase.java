package org.tomcurran.remiges.test.ui;


import android.content.ContentValues;

import com.android.uiautomator.core.UiObjectNotFoundException;

public class PlacesTestCase extends ItemTestCase {

    public static final String PLACE_NAME = "place_name";
    public static final String PLACE_LATITUDE = "place_latitude";
    public static final String PLACE_LONGITUDE = "place_longitude";

    public static final String DESCRIPTION_OPEN_DRAWER = "Places, Open navigation drawer";
    public static final String TEXT_NAVIGATION = "Places";
    public static final String TEXT_TITLE = "Places";

    private static final String DESCRIPTION_ADD = "Add Place";
    private static final String DESCRIPTION_EDIT = "Edit";
    private static final String DESCRIPTION_DELETE = "Delete";

    private static final String TEXT_DETAIL_TITLE = "Place Detail";

    private static final String RESOURCE_EDIT_NAME = APP_PACKAGE + ":id/edit_place_name";
    private static final String RESOURCE_EDIT_LATITUDE = APP_PACKAGE + ":id/edit_place_latitude";
    private static final String RESOURCE_EDIT_LONGITUDE = APP_PACKAGE + ":id/edit_place_longitude";
    private static final String RESOURCE_DETAIL_NAME = APP_PACKAGE + ":id/detail_place_name";
    private static final String RESOURCE_DETAIL_LATITUDE = APP_PACKAGE + ":id/detail_place_latitude";
    private static final String RESOURCE_DETAIL_LONGITUDE = APP_PACKAGE + ":id/detail_place_longitude";

    private static final String NAME_HINT = "Place Name";
    private static final String LATITUDE_HINT = "Latitude";
    private static final String LONGITUDE_HINT = "Longitude";

    private static int nameCount = 0;

    private static String getNextPlaceName() {
        return String.format("place %d", ++nameCount);
    }

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
    public void changeValues(ContentValues values) throws UiObjectNotFoundException {
        String placeName = values.getAsString(PLACE_NAME);
        String placeLatitude = values.getAsString(PLACE_LATITUDE);
        String placeLongitude = values.getAsString(PLACE_LONGITUDE);

        getByResource(RESOURCE_EDIT_NAME).clearTextField();
        getByResource(RESOURCE_EDIT_NAME).setText(placeName);
        assertEquals(placeName, getByResource(RESOURCE_EDIT_NAME).getText());

        getByResource(RESOURCE_EDIT_LATITUDE).clearTextField();
        getByResource(RESOURCE_EDIT_LATITUDE).setText(String.valueOf(placeLatitude));
        assertEquals(placeLatitude, getByResource(RESOURCE_EDIT_LATITUDE).getText());

        getByResource(RESOURCE_EDIT_LONGITUDE).clearTextField();
        getByResource(RESOURCE_EDIT_LONGITUDE).setText(String.valueOf(placeLongitude));
        assertEquals(placeLongitude, getByResource(RESOURCE_EDIT_LONGITUDE).getText());
    }

    @Override
    public void assertDetail(ContentValues values) throws UiObjectNotFoundException {
        assertEquals(values.getAsString(PLACE_NAME), getByResource(RESOURCE_DETAIL_NAME).getText());
        assertEquals(values.getAsString(PLACE_LATITUDE), getByResource(RESOURCE_DETAIL_LATITUDE).getText());
        assertEquals(values.getAsString(PLACE_LONGITUDE), getByResource(RESOURCE_DETAIL_LONGITUDE).getText());
    }

    @Override
    public void assertHint() throws UiObjectNotFoundException {
        assertEquals(NAME_HINT, getByResource(RESOURCE_EDIT_NAME).getText());
        assertEquals(LATITUDE_HINT, getByResource(RESOURCE_EDIT_LATITUDE).getText());
        assertEquals(LONGITUDE_HINT, getByResource(RESOURCE_EDIT_LONGITUDE).getText());
    }

    @Override
    public String getClick(ContentValues values) {
        return values.getAsString(PLACE_NAME);
    }

    @Override
    public String getTitle() {
        return isTwoPane() ? TEXT_TITLE : TEXT_DETAIL_TITLE;
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
        navigateToPlaces();
    }

    public static void navigateToPlaces(String navigateFrom) throws UiObjectNotFoundException {
        NavigationDrawerTestCase.navigateTo(navigateFrom, TEXT_NAVIGATION, TEXT_TITLE);
    }

    private static void navigateToPlaces() throws UiObjectNotFoundException {
        navigateToPlaces(NavigationDrawerTestCase.DESCRIPTION_HOME_OPEN_DRAWER);
    }

    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to places
        PlacesTestCase.navigateToPlaces();

        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType(PlacesTestCase.DESCRIPTION_OPEN_DRAWER);

        // navigate to places
        PlacesTestCase.navigateToPlaces(JumpTypeTestCase.DESCRIPTION_OPEN_DRAWER);
    }

}
