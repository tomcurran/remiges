package org.tomcurran.remiges.test.ui;


import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

public class NavigationDrawerTestCase extends RemigesTestCase {

    // content description and text values used for navigation
    public static final String DESCRIPTION_OPEN_DRAWER = "Open navigation drawer";
    public static final String DESCRIPTION_CLOSE_DRAWER = "Close navigation drawer";
    public static final String TEXT_HOME_TITLE = "Remiges";
    public static final String TEXT_NAVIGATION_TITLE = "Remiges";

    private static final String RESOURCE_DRAWER = APP_ID + "drawer_layout";

    /**
     * Opens the navigation drawer
     * @throws UiObjectNotFoundException
     */
    protected void openDrawer() throws UiObjectNotFoundException {
        getByDescription(DESCRIPTION_OPEN_DRAWER).click();
    }

    /**
     * Closes the navigation drawer
     * @throws UiObjectNotFoundException
     */
    protected void closeDrawer() throws UiObjectNotFoundException {
        getByDescription(DESCRIPTION_CLOSE_DRAWER).click();
    }

    /**
     * Selects a item in the navigate drawer to navigate into
     * @param navigation
     * @throws UiObjectNotFoundException
     */
    protected void selectNavigation(String navigation) throws UiObjectNotFoundException {
        getListItem(new UiSelector().resourceId(RESOURCE_DRAWER), navigation).click();
    }

    /**
     * Navigates from navigateFrom to navigateTo ensuring we landed at the right view
     * @param navigateTo description of the view we are navigating to
     * @param navigateToTitle title of the page we are navigating to
     * @throws UiObjectNotFoundException
     */
    protected void navigateTo(String navigateTo, String navigateToTitle) throws UiObjectNotFoundException {
        // open navigation drawer
        openDrawer();

        // ensure correct navigation
        assertEquals(TEXT_NAVIGATION_TITLE, getActionBarTitle());

        // select navigation
        selectNavigation(navigateTo);

        // ensure title correct
        assertEquals(navigateToTitle, getActionBarTitle());
    }

    /**
     * Ensures we can open and close the navigation drawer
     * @throws UiObjectNotFoundException
     */
    public void testOpenCloseNavigationDrawer() throws UiObjectNotFoundException {
        // ensure correct navigation
        assertEquals(TEXT_HOME_TITLE, getActionBarTitle());

        // open navigation drawer
        openDrawer();

        // ensure correct navigation
        assertEquals(TEXT_NAVIGATION_TITLE, getActionBarTitle());

        // close navigation drawer
        closeDrawer();

        // ensure correct navigation
        assertEquals(TEXT_HOME_TITLE, getActionBarTitle());
    }

}
