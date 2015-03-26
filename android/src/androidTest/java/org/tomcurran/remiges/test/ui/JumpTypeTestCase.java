package org.tomcurran.remiges.test.ui;


import android.content.ContentValues;
import android.support.test.uiautomator.UiObjectNotFoundException;

public class JumpTypeTestCase extends ItemTestCase {

    // content value keys
    public static final String JUMPTYPE_NAME = "jumptype_name";

    // content description and text values used for navigation
    public static final String TEXT_NAVIGATION = "Jump Types";
    public static final String TEXT_TITLE = "Jump Types";

    // content descriptions, text values and resource IDs
    private static final String DESCRIPTION_ADD = "Add Jump Type";
    private static final String DESCRIPTION_EDIT = "Edit";
    private static final String DESCRIPTION_DELETE = "Delete";
    private static final String TEXT_DETAIL_TITLE = "Jump Type Detail";
    private static final String RESOURCE_EDIT_NAME = APP_ID + "edit_jumptype_name";
    private static final String RESOURCE_DETAIL_NAME = APP_ID + "detail_jumptype_name";

    // edit field hint text values
    private static final String HINT_NAME = "Jump Type";

    // generates unique names
    private static int nameCount = 0;
    private String getNextJumpTypeName() {
        return String.format("jump type tester %d", ++nameCount);
    }

    @Override
    public ContentValues getNewValues() {
        ContentValues values = new ContentValues();
        values.put(JUMPTYPE_NAME, getNextJumpTypeName());
        return values;
    }

    @Override
    public void editValues(ContentValues values) throws UiObjectNotFoundException {
        changeTextField(RESOURCE_EDIT_NAME, values.getAsString(JUMPTYPE_NAME), HINT_NAME);
    }

    @Override
    public void assertDetail(ContentValues values) throws UiObjectNotFoundException {
        assertEquals(values.getAsString(JUMPTYPE_NAME), getByResource(RESOURCE_DETAIL_NAME)
                .getText());
    }

    @Override
    public void assertHint() throws UiObjectNotFoundException {
        assertEquals(HINT_NAME, getByResource(RESOURCE_EDIT_NAME).getText());
    }

    @Override
    public String getListClickTarget(ContentValues values) {
        return values.getAsString(JUMPTYPE_NAME);
    }

    @Override
    public String getTitle(ContentValues values) {
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
        getNavigationDrawerTestCase().navigateTo(JumpTypeTestCase.TEXT_NAVIGATION, JumpTypeTestCase.TEXT_TITLE);
    }

    @Override
    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateTo();

        // navigate to places
        getNavigationDrawerTestCase().navigateTo(PlacesTestCase.TEXT_NAVIGATION, PlacesTestCase.TEXT_TITLE);

        // navigate to jump type
        navigateTo();
    }

}
