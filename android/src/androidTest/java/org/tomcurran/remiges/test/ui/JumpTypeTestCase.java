package org.tomcurran.remiges.test.ui;


import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;

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

    public void test010NavigateTo() throws UiObjectNotFoundException {
        navigateToJumpType();
    }

    public void test011NavigateAwayAndBack() throws UiObjectNotFoundException {
        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType();

        // navigate to places
        PlacesTestCase.navigateToPlaces(JumpTypeTestCase.DESCRIPTION_OPEN_DRAWER);

        // navigate to jump type
        JumpTypeTestCase.navigateToJumpType(PlacesTestCase.DESCRIPTION_OPEN_DRAWER);
    }

    public void test012AddCancel() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemsBefore = getMasterDetailListCount();

        // click add jump type action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        // ensure jump type name is set to the hint
        assertEquals(NAME_HINT, name.getText());

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure no new jump type in list
        assertEquals(listItemsBefore, getMasterDetailListCount());
    }

    public void test013Add() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemCount = getMasterDetailListCount();

        // click add jump type action
        getByDescription(DESCRIPTION_ADD).clickAndWaitForNewWindow();

        // ensure jump type name is set to the hint then change it
        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        assertEquals(NAME_HINT, name.getText());
        name.setText(NAME1);
        assertEquals(NAME1, name.getText());

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure new jump type in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure jump type name present in list
        assertInList(getMasterDetailList(), NAME1);
    }

    public void test014EditCancel() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // click jump type
        clickListItem(getMasterDetailList(), NAME1);

        // ensure correct page
        assertEquals(TEXT_DETAIL_TITLE, getActionBarTitle());

        // ensure detail name
        assertEquals(NAME1, getByResource(RESOURCE_DETAIL_NAME).getText());

        // click edit jump type action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure detail name
        assertEquals(NAME1, getByResource(RESOURCE_DETAIL_NAME).getText());
    }

    public void test015Edit() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // click jump type
        clickListItem(getMasterDetailList(), NAME1);

        // ensure correct page
        assertEquals(TEXT_DETAIL_TITLE, getActionBarTitle());

        // ensure detail name
        assertEquals(NAME1, getByResource(RESOURCE_DETAIL_NAME).getText());

        // click edit jump type action
        getByResource(RESOURCE_ACTION_EDIT).clickAndWaitForNewWindow();

        // change jump type name
        UiObject name = getByResource(RESOURCE_EDIT_NAME);
        name.setText(NAME2);
        assertEquals(NAME2, name.getText());

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure detail name
        assertEquals(NAME2, getByResource(RESOURCE_DETAIL_NAME).getText());
    }

    public void test016Delete() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemsBefore = getMasterDetailListCount();

        // click jump type
        clickListItem(getMasterDetailList(), NAME2);

        // ensure correct page
        assertEquals(TEXT_DETAIL_TITLE, getActionBarTitle());

        // click delete jump type action
        getByResource(RESOURCE_ACTION_DELETE).clickAndWaitForNewWindow();

        // ensure jump type removed from list
        assertEquals(listItemsBefore - 1, getMasterDetailListCount());
        assertNotInList(getMasterDetailList(), NAME2);
    }

}
