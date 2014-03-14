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
    private static final String NAME1 = "jump type tester 1";
    private static final String NAME2 = "jump type tester 2";

    public static void navigateToJumpType(String navigateFrom) throws UiObjectNotFoundException {
        NavigationDrawerTestCase.navigateTo(navigateFrom, TEXT_NAVIGATION, TEXT_TITLE);
    }

    private static void navigateToJumpType() throws UiObjectNotFoundException {
        navigateToJumpType(NavigationDrawerTestCase.DESCRIPTION_HOME_OPEN_DRAWER);
    }

    public void test010Navigation() throws UiObjectNotFoundException {
        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType();

        // navigate to places
        PlacesTestCase.navigateToPlaces(JumpTypeTestCase.DESCRIPTION_OPEN_DRAWER);

        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType(PlacesTestCase.DESCRIPTION_OPEN_DRAWER);
    }

    public void test011AddJumpType() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemCount = getMasterDetailListCount();

        // click add jump type action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        // ensure jump type name is set to the hint
        assertTrue(name.getText().equals(NAME_HINT));
        // change jump type name
        name.setText(NAME1);
        assertTrue(name.getText().equals(NAME1));

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure new jump type in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure jump type name present in list
        try {
            getListItem(getMasterDetailList(), NAME1);
        } catch (UiObjectNotFoundException e) {
            fail(String.format("%s not found in %s list", NAME2, "jump type"));
        }
    }

    public void test010NavigateToJumpType() throws UiObjectNotFoundException {
        navigateToJumpType();
    }

    public void test012CancelAddJumpType() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemsBefore = getMasterDetailListCount();

        // click add jump type action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        // ensure jump type name is set to the hint
        assertTrue(name.getText().equals(NAME_HINT));

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure no new jump type in list
        assertEquals(listItemsBefore, getMasterDetailListCount());
    }

    public void test013EditJumpTypeCancel() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // click jump type
        getListItem(getMasterDetailList(), NAME1).click();

        // ensure correct page
        assertTrue(getActionBarTitle().equals(TEXT_DETAIL_TITLE));

        // ensure detail name
        assertTrue(getByResource(RESOURCE_DETAIL_NAME).getText().equals(NAME1));

        // click edit jump type action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure detail name
        assertTrue(getByResource(RESOURCE_DETAIL_NAME).getText().equals(NAME1));
    }

    public void test014EditJumpType() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // click jump type
        getListItem(getMasterDetailList(), NAME1).click();

        // ensure correct page
        assertTrue(getActionBarTitle().equals(TEXT_DETAIL_TITLE));

        // ensure detail name
        assertTrue(getByResource(RESOURCE_DETAIL_NAME).getText().equals(NAME1));

        // click edit jump type action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        // change jump type name
        name.setText(NAME2);
        assertTrue(name.getText().equals(NAME2));

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure detail name
        assertTrue(getByResource(RESOURCE_DETAIL_NAME).getText().equals(NAME2));
    }

    public void test015DeleteJumpType() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemsBefore = getMasterDetailListCount();

        // click jump type
        try {
            getListItem(getMasterDetailList(), NAME2).click();
        } catch (UiObjectNotFoundException e) {
            fail(String.format("%s not found in %s list", NAME2, "jump type"));
        }

        // ensure correct page
        assertTrue(getActionBarTitle().equals(TEXT_DETAIL_TITLE));

        // click delete jump type action
        getByResource(RESOURCE_ACTION_DELETE).clickAndWaitForNewWindow();

        // ensure jump type removed from list
        assertEquals(listItemsBefore - 1, getMasterDetailListCount());
        try {
            getListItem(getMasterDetailList(), NAME2);
            fail(String.format("%s found in %s list but it should have been deleted", NAME2, "jump type"));
        } catch (UiObjectNotFoundException e) {
        }
    }

}
