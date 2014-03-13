package org.tomcurran.remiges.test.ui;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class NavigationDrawer extends RemigesUiAutomatorTestCase {

    public static final String DESCRIPTION_HOME_OPEN_DRAWER = "Remiges, Open navigation drawer";
    public static final String DESCRIPTION_CLOSE_DRAWER = "Remiges, Close navigation drawer";

    private static final String RESOURCE_NAVIGATION_DRAWER = "org.tomcurran.remiges:id/navigation_drawer";

    public static void openDrawer(String openDescription) throws UiObjectNotFoundException {
        new UiObject(new UiSelector().description(openDescription)).click();
    }

    public static void closeDrawer() throws UiObjectNotFoundException {
        new UiObject(new UiSelector().description(DESCRIPTION_CLOSE_DRAWER)).click();
    }

    public static void selectNavigation(String navigation) throws UiObjectNotFoundException {
        new UiScrollable(getNavigationDrawerList()).getChildByText(getTextView(), navigation).click();
    }

    public static UiSelector getNavigationDrawerList() {
        return new UiSelector().resourceId(RESOURCE_NAVIGATION_DRAWER)
                .childSelector(new UiSelector().className(CLASS_LISTVIEW));
    }

    public void testOpenCloseNavigationDrawer() throws UiObjectNotFoundException {
        // open navigation drawer
        openDrawer(DESCRIPTION_HOME_OPEN_DRAWER);

        // close navigation drawer
        closeDrawer();
    }

}
