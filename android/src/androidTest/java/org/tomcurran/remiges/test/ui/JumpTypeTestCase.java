package org.tomcurran.remiges.test.ui;


import android.content.ContentValues;

import com.android.uiautomator.core.UiObjectNotFoundException;

public class JumpTypeTestCase extends ItemTestCase {

    public static final String JUMPTYPE_NAME = "jumptype_name";

    public static final String DESCRIPTION_OPEN_DRAWER = "Jump Types, Open navigation drawer";
    public static final String TEXT_NAVIGATION = "Jump Types";
    public static final String TEXT_TITLE = "Jump Types";

    private static final String DESCRIPTION_ADD = "Add Jump Type";
    private static final String DESCRIPTION_EDIT = "Edit";
    private static final String DESCRIPTION_DELETE = "Delete";

    private static final String TEXT_DETAIL_TITLE = "Jump Type Detail";

    private static final String RESOURCE_EDIT_NAME = "org.tomcurran.remiges:id/edit_jumptype_name";
    private static final String RESOURCE_DETAIL_NAME = "org.tomcurran.remiges:id/detail_jumptype_name";

    private static final String NAME_HINT = "Jump Type";

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
    public void changeValues(ContentValues values) throws UiObjectNotFoundException {
        changeTextField(RESOURCE_EDIT_NAME, values.getAsString(JUMPTYPE_NAME));
    }

    @Override
    public void assertDetail(ContentValues values) throws UiObjectNotFoundException {
        assertEquals(values.getAsString(JUMPTYPE_NAME), getByResource(RESOURCE_DETAIL_NAME).getText());
    }

    @Override
    public void assertHint() throws UiObjectNotFoundException {
        assertEquals(NAME_HINT, getByResource(RESOURCE_EDIT_NAME).getText());
    }

    @Override
    public String getClick(ContentValues values) {
        return values.getAsString(JUMPTYPE_NAME);
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
        navigateToJumpType();
    }

    public static void navigateToJumpType(String navigateFrom) throws UiObjectNotFoundException {
        NavigationDrawerTestCase.navigateTo(navigateFrom, TEXT_NAVIGATION, TEXT_TITLE);
    }

    private static void navigateToJumpType() throws UiObjectNotFoundException {
        navigateToJumpType(NavigationDrawerTestCase.DESCRIPTION_HOME_OPEN_DRAWER);
    }

    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType();

        // navigate to places
        PlacesTestCase.navigateToPlaces(JumpTypeTestCase.DESCRIPTION_OPEN_DRAWER);

        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType(PlacesTestCase.DESCRIPTION_OPEN_DRAWER);
    }

}
