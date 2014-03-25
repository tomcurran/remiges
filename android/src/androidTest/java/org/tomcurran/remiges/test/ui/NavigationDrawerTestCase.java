package org.tomcurran.remiges.test.ui;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;

public class NavigationDrawerTestCase extends RemigesUiAutomatorTestCase {

    public static final String DESCRIPTION_HOME_OPEN_DRAWER = "Remiges, Open navigation drawer";
    public static final String DESCRIPTION_HOME_OPEN_DRAWER_UP = "Remiges, Navigate up";
    public static final String DESCRIPTION_HOME_DRAWER_UP = "Navigate up";
    public static final String DESCRIPTION_CLOSE_DRAWER = "Remiges, Close navigation drawer";
    public static final String TEXT_HOME_TITLE = "Remiges";
    public static final String TEXT_NAVIGATION_TITLE = "Remiges";

    private static final String RESOURCE_NAVIGATION_DRAWER = "org.tomcurran.remiges:id/navigation_drawer";

    public static void openDrawer(String openDescription) throws UiObjectNotFoundException {
        if (openDescription.equals(DESCRIPTION_HOME_OPEN_DRAWER)) {
            if (getByDescription(DESCRIPTION_HOME_OPEN_DRAWER).exists()) {
                getByDescription(DESCRIPTION_HOME_OPEN_DRAWER).click();
            } else if (getByDescription(DESCRIPTION_HOME_OPEN_DRAWER_UP).exists()) {
                getByDescription(DESCRIPTION_HOME_OPEN_DRAWER_UP).click();
            } else if (getByDescription(DESCRIPTION_HOME_DRAWER_UP).exists()) {
                getByDescription(DESCRIPTION_HOME_DRAWER_UP).click();
            }
        } else {
            getByDescription(openDescription).click();
        }
    }

    public static void closeDrawer() throws UiObjectNotFoundException {
        getByDescription(DESCRIPTION_CLOSE_DRAWER).click();
    }

    public static void selectNavigation(String navigation) throws UiObjectNotFoundException {
        getListItem(getListView(new UiSelector().resourceId(RESOURCE_NAVIGATION_DRAWER)), navigation).click();
    }

    protected static void navigateTo(String navigateFrom, String navigateTo, String navigateToTitle) throws UiObjectNotFoundException {
        // open navigation drawer
        openDrawer(navigateFrom);

        // ensure correct navigation
        assertEquals(TEXT_NAVIGATION_TITLE, getActionBarTitle());

        // select navigation
        selectNavigation(navigateTo);

        // ensure title correct
        assertEquals(navigateToTitle, getActionBarTitle());
    }

    public void testOpenCloseNavigationDrawer() throws UiObjectNotFoundException {
        // ensure correct navigation
        assertEquals(TEXT_HOME_TITLE, getActionBarTitle());

        // open navigation drawer
        openDrawer(DESCRIPTION_HOME_OPEN_DRAWER);

        // ensure correct navigation
        assertEquals(TEXT_NAVIGATION_TITLE, getActionBarTitle());

        // close navigation drawer
        closeDrawer();

        // ensure correct navigation
        assertEquals(TEXT_HOME_TITLE, getActionBarTitle());
    }

}
