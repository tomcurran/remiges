package org.tomcurran.remiges.test.ui;


import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;

public class JumpTypeTestCase extends RemigesUiAutomatorTestCase {

    public static final String DESCRIPTION_OPEN_DRAWER = "Jump Types, Open navigation drawer";
    public static final String TEXT_NAVIGATION = "Jump Types";
    public static final String TEXT_TITLE = "Jump Types";

    private static final String DESCRIPTION_ADD = "Add Jump Type";
    private static final String TEXT_DETAIL_TITLE = "Jump Type Detail";
    private static final String RESOURCE_EDIT_NAME = "org.tomcurran.remiges:id/edit_jumptype_name";
    private static final String RESOURCE_DETAIL_NAME = "org.tomcurran.remiges:id/detail_jumptype_name";
    private static final String RESOURCE_ACTION_EDIT = "org.tomcurran.remiges:id/menu_jumptype_detail_edit";
    private static final String RESOURCE_ACTION_DELETE = "org.tomcurran.remiges:id/menu_jumptype_detail_delete";

    private static final String NAME_HINT = "Jump Type";
    private static final String NAME = "jump type tester %d";

    private int nameCount = 0;

    private String getNextJumpTypeName() {
        return String.format(NAME, ++nameCount);
    }

    public static void navigateToJumpType(String navigateFrom) throws UiObjectNotFoundException {
        NavigationDrawerTestCase.navigateTo(navigateFrom, TEXT_NAVIGATION, TEXT_TITLE);
    }

    private static void navigateToJumpType() throws UiObjectNotFoundException {
        navigateToJumpType(NavigationDrawerTestCase.DESCRIPTION_HOME_OPEN_DRAWER);
    }

    private static void addJumpType(String jumpTypeName) throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemCount = getMasterDetailListCount();

        // ensure jump type name not present in list
        assertNotInList(getMasterDetailList(), jumpTypeName);

        // click add jump type action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        // ensure jump type name is set to the hint then change it
        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        assertEquals(NAME_HINT, name.getText());
        name.setText(jumpTypeName);
        assertEquals(jumpTypeName, name.getText());

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure new jump type in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure jump type name present in list
        assertInList(getMasterDetailList(), jumpTypeName);
    }

    private void deleteJumpType(String jumpTypeName) throws UiObjectNotFoundException {
        // count items in list
        int listItemsBefore = getMasterDetailListCount();

        // click item
        clickListItem(getMasterDetailList(), jumpTypeName);

        // ensure correct page
        assertEquals(isTwoPane() ? TEXT_TITLE : TEXT_DETAIL_TITLE, getActionBarTitle());

        // click delete action
        getByResource(RESOURCE_ACTION_DELETE).clickAndWaitForNewWindow();

        // ensure item removed from list
        assertEquals(listItemsBefore - 1, getMasterDetailListCount());
        assertNotInList(getMasterDetailList(), jumpTypeName);
    }

    public void testNavigateTo() throws UiObjectNotFoundException {
        navigateToJumpType();
    }

    public void testNavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType();

        // navigate to places
        PlacesTestCase.navigateToPlaces(JumpTypeTestCase.DESCRIPTION_OPEN_DRAWER);

        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType(PlacesTestCase.DESCRIPTION_OPEN_DRAWER);
    }

    public void testAddCancel() throws UiObjectNotFoundException {
        // navigate
        navigateToJumpType();

        // count items in list
        int listItemsBefore = getMasterDetailListCount();

        // click add action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        // ensure name is set to the hint
        assertEquals(NAME_HINT, name.getText());

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure no new item in list
        assertEquals(listItemsBefore, getMasterDetailListCount());
    }

    public void testAdd() throws UiObjectNotFoundException {
        String jumpTypeName = getNextJumpTypeName();

        // add item
        addJumpType(jumpTypeName);

        // delete item
        deleteJumpType(jumpTypeName);
    }

    public void testEditCancel() throws UiObjectNotFoundException {
        String jumpTypeName = getNextJumpTypeName();

        // add item
        addJumpType(jumpTypeName);

        // click item
        clickListItem(getMasterDetailList(), jumpTypeName);

        // ensure correct page
        assertEquals(isTwoPane() ? TEXT_TITLE : TEXT_DETAIL_TITLE, getActionBarTitle());

        // ensure detail
        assertEquals(jumpTypeName, getByResource(RESOURCE_DETAIL_NAME).getText());

        // click edit action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure detail
        assertEquals(jumpTypeName, getByResource(RESOURCE_DETAIL_NAME).getText());

        // ensure correct view
        if (!isTwoPane()) {
            getUiDevice().pressBack();
        }

        // delete item
        deleteJumpType(jumpTypeName);
    }

    public void testEdit() throws UiObjectNotFoundException {
        String jumpTypeName = getNextJumpTypeName();

        // add item
        addJumpType(jumpTypeName);

        // click item
        clickListItem(getMasterDetailList(), jumpTypeName);

        // ensure correct page
        assertEquals(isTwoPane() ? TEXT_TITLE : TEXT_DETAIL_TITLE, getActionBarTitle());

        // ensure detail
        assertEquals(jumpTypeName, getByResource(RESOURCE_DETAIL_NAME).getText());

        // click edit action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        // change name
        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        name.clearTextField();
        jumpTypeName = getNextJumpTypeName();
        name.setText(jumpTypeName);
        assertEquals(jumpTypeName, name.getText());

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure detail
        assertEquals(jumpTypeName, getByResource(RESOURCE_DETAIL_NAME).getText());

        // ensure correct view
        if (!isTwoPane()) {
            getUiDevice().pressBack();
        }

        // delete item
        deleteJumpType(jumpTypeName);
    }

    public void testDelete() throws UiObjectNotFoundException {
        String jumpTypeName = getNextJumpTypeName();

        // add item
        addJumpType(jumpTypeName);

        // delete item
        deleteJumpType(jumpTypeName);
    }

}
