package org.tomcurran.remiges.test.ui;


import android.content.ContentValues;
import android.support.test.uiautomator.UiObjectNotFoundException;

public class JumpTestCase extends ItemTestCase {

    // content value keys
    private static final String JUMP_NUMBER = "jump_number";
    private static final String JUMP_WAY = "jump_way";
    private static final String JUMP_EXIT_ALTITUDE = "jump_exit_altitude";
    private static final String JUMP_DEPLOYMENT_ALTITUDE = "jump_deployment_altitude";
    private static final String JUMP_DELAY = "jump_delay";
    private static final String JUMP_DESCRIPTION = "jump_description";

    // content description and text values used for navigation
    public static final String TEXT_NAVIGATION = "Jumps";
    public static final String TEXT_TITLE = "Jumps";

    // content descriptions, text values and resource IDs
    private static final String DESCRIPTION_ADD = "Log Jump";
    private static final String DESCRIPTION_EDIT = "Edit";
    private static final String DESCRIPTION_DELETE = "Delete";

    private static final String TEXT_DETAIL_TITLE = "Jump Detail";
    private static final String TEXT_DETAIL_TITLE_NUMBER = "Jump %d";

    private static final String RESOURCE_EDIT_NUMBER = APP_ID + "edit_jump_number";
    private static final String RESOURCE_EDIT_WAY = APP_ID + "edit_jump_way";
    private static final String RESOURCE_EDIT_EXIT_ALTITUDE = APP_ID + "edit_jump_exit_altitude";
    private static final String RESOURCE_EDIT_DEPLOYMENT_ALTITUDE = APP_ID + "edit_jump_deployment_altitude";
    private static final String RESOURCE_EDIT_DELAY = APP_ID + "edit_jump_delay";
    private static final String RESOURCE_EDIT_DESCRIPTION = APP_ID + "edit_jump_description";

    private static final String RESOURCE_DETAIL_NUMBER = APP_ID + "detail_jump_number";
    private static final String RESOURCE_DETAIL_WAY = APP_ID + "detail_jump_way";
    private static final String RESOURCE_DETAIL_EXIT_ALTITUDE = APP_ID + "detail_jump_exit_altitude";
    private static final String RESOURCE_DETAIL_DEPLOYMENT_ALTITUDE = APP_ID + "detail_jump_deployment_altitude";
    private static final String RESOURCE_DETAIL_DELAY = APP_ID + "detail_jump_delay";
    private static final String RESOURCE_DETAIL_DESCRIPTION = APP_ID + "detail_jump_description";

    // edit field hint text values
    private static final String HINT_NUMBER = "Jump Number";
    private static final String HINT_WAY = "Way";
    private static final String HINT_EXIT_ALTITUDE = "Exit Altitude";
    private static final String HINT_DEPLOYMENT_ALTITUDE = "Deployment Altitude";
    private static final String HINT_DELAY = "Delay (seconds)";
    private static final String HINT_DESCRIPTION = "Description";

    // generates unique descriptions
    private static int descriptionCount = 0;

    private static String getNextDescription() {
        return String.format("description %d", ++descriptionCount);
    }

    private int getRandomInt(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    @Override
    public void editValues(ContentValues values) throws UiObjectNotFoundException {
        changeTextField(RESOURCE_EDIT_NUMBER, values.getAsString(JUMP_NUMBER), HINT_NUMBER);
        changeTextField(RESOURCE_EDIT_WAY, values.getAsString(JUMP_WAY), HINT_WAY);
        changeTextField(RESOURCE_EDIT_EXIT_ALTITUDE, values.getAsString(JUMP_EXIT_ALTITUDE), HINT_EXIT_ALTITUDE);
        changeTextField(RESOURCE_EDIT_DEPLOYMENT_ALTITUDE, values.getAsString(JUMP_DEPLOYMENT_ALTITUDE), HINT_DEPLOYMENT_ALTITUDE);
        changeTextField(RESOURCE_EDIT_DELAY, values.getAsString(JUMP_DELAY), HINT_DELAY);
        // TODO edit jump description
//        changeTextField(RESOURCE_EDIT_DESCRIPTION, values.getAsString(JUMP_DESCRIPTION), HINT_DESCRIPTION);
    }

    @Override
    public void assertDetail(ContentValues values) throws UiObjectNotFoundException {
        assertEquals(values.getAsString(JUMP_EXIT_ALTITUDE), getByResource(RESOURCE_DETAIL_EXIT_ALTITUDE).getText());
        assertEquals(values.getAsString(JUMP_DEPLOYMENT_ALTITUDE), getByResource(RESOURCE_DETAIL_DEPLOYMENT_ALTITUDE).getText());
        assertEquals(values.getAsString(JUMP_DELAY), getByResource(RESOURCE_DETAIL_DELAY).getText());
    }

    @Override
    public void assertHint() throws UiObjectNotFoundException {
        assertEquals(HINT_EXIT_ALTITUDE, getByResource(RESOURCE_EDIT_EXIT_ALTITUDE).getText());
        assertEquals(HINT_DEPLOYMENT_ALTITUDE, getByResource(RESOURCE_EDIT_DEPLOYMENT_ALTITUDE).getText());
        assertEquals(HINT_DELAY, getByResource(RESOURCE_EDIT_DELAY).getText());
        // TODO assert jump description
//        assertEquals(HINT_DESCRIPTION, getByResource(RESOURCE_EDIT_DESCRIPTION).getText());
    }

    @Override
    public void navigateTo() throws UiObjectNotFoundException {
        getNavigationDrawerTestCase().navigateTo(JumpTestCase.TEXT_NAVIGATION, JumpTestCase.TEXT_TITLE);
    }

    @Override
    public ContentValues getNewValues() {
        ContentValues values = new ContentValues();
        values.put(JUMP_NUMBER, getRandomInt(1, 100000));
        values.put(JUMP_WAY, getRandomInt(1, 1000));
        values.put(JUMP_EXIT_ALTITUDE, getRandomInt(0, 100000));
        values.put(JUMP_DEPLOYMENT_ALTITUDE, getRandomInt(0, 100000));
        values.put(JUMP_DELAY, getRandomInt(0, 1000));
        values.put(JUMP_DESCRIPTION, getNextDescription());
        return values;
    }

    @Override
    public String getListClickTarget(ContentValues values) {
        return values.getAsString(JUMP_NUMBER);
    }

    @Override
    public String getTitle(ContentValues values) {
        if (isTwoPane()) {
            return TEXT_TITLE;
        } else {
            return String.format(TEXT_DETAIL_TITLE_NUMBER, values.getAsInteger(JUMP_NUMBER));
        }
    }

    @Override
    public String getEditAction() {
        return DESCRIPTION_EDIT;
    }

    @Override
    public String getAddAction() {
        return DESCRIPTION_ADD;
    }

    @Override
    public String getDeleteAction() {
        return DESCRIPTION_DELETE;
    }

    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to jumps
        navigateTo();

        // navigate to places
        getNavigationDrawerTestCase().navigateTo(PlacesTestCase.TEXT_NAVIGATION, PlacesTestCase.TEXT_TITLE);

        // navigate to jumps
        navigateTo();
    }

}
