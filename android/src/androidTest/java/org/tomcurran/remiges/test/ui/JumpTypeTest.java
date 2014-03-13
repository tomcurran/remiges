package org.tomcurran.remiges.test.ui;


import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

public class JumpTypeTest extends RemigesUiAutomatorTestCase {

    public static final String DESCRIPTION_OPEN_DRAWER = "Jump Types, Open navigation drawer";
    public static final String TEXT_NAVIGATION = "Jump Types";
    public static final String TEXT_TITLE = "Jump Types";

    private static final String RESOURCE_JUMPTYPE_NAME = "org.tomcurran.remiges:id/edit_jumptype_name";
    private static final String DESCRIPTION_ADD_JUMPTYPE = "Add Jump Type";
    private static final String JUMPTYPE_NAME_HINT = "Jump Type";
    private static final String JUMPTYPE_NAME_TEST1 = "jump type tester 1";
    private static final String JUMPTYPE_NAME_TEST2 = "jump type tester 2";

    private void clickActionAddJumpType() throws UiObjectNotFoundException {
        new UiObject(new UiSelector().description(DESCRIPTION_ADD_JUMPTYPE)).clickAndWaitForNewWindow();
    }

    private UiObject getJumpTypeName() throws UiObjectNotFoundException {
        return new UiObject(new UiSelector().resourceId(RESOURCE_JUMPTYPE_NAME));
    }

    private void navigateToJumpType() throws UiObjectNotFoundException {
        // open navigation drawer
        NavigationDrawer.openDrawer(NavigationDrawer.DESCRIPTION_HOME_OPEN_DRAWER);

        // select navigation to jump types
        NavigationDrawer.selectNavigation(TEXT_NAVIGATION);

        // ensure title correct
        assertTrue(getActionBarTitle().getText().equals(TEXT_TITLE));
    }

    public void test011AddJumpType() throws UiObjectNotFoundException {
        // navigate to jump type
        navigateToJumpType();

        // get current amount of jump types available
        int listItemCount = getMasterDetailListCount();

        // click add jump type action
        clickActionAddJumpType();

        UiObject name = getJumpTypeName();
        // ensure jump type name is set to the hint
        assertTrue(name.getText().equals(JUMPTYPE_NAME_HINT));
        // change jump type name
        name.setText(JUMPTYPE_NAME_TEST1);
        assertTrue(name.getText().equals(JUMPTYPE_NAME_TEST1));

        // action bar done
        actionBarDone();

        // ensure new jump type in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure jump type name present in list
        UiScrollable list = new UiScrollable(getMasterDetailContainer());
        list.getChildByText(getTextView(), JUMPTYPE_NAME_TEST1);
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
        clickActionAddJumpType();

        UiObject name = getJumpTypeName();
        // ensure jump type name is set to the hint
        assertTrue(name.getText().equals(JUMPTYPE_NAME_HINT));

        // action bar cancel
        actionBarCancel();

        // ensure no new jump type in list
        assertEquals(listItemsBefore, getMasterDetailListCount());
    }
}
