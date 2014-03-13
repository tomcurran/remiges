package org.tomcurran.remiges.test.ui;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;


public class RemigesUiAutomatorTestCase extends UiAutomatorTestCase {

    public static final String CLASS_LISTVIEW = "android.widget.ListView";
    public static final String CLASS_TEXTVIEW = "android.widget.TextView";

    private static final String APP_TITLE = "Remiges";
    private static final String APP_PACKAGE = "org.tomcurran.remiges";

    private static final String RESOURCE_ACTIONBAR_TITLE = "android:id/action_bar_title";
    private static final String RESOURCE_ACTIONBAR_DONE = "org.tomcurran.remiges:id/actionbar_done";
    private static final String RESOURCE_ACTIONBAR_CANCEL = "org.tomcurran.remiges:id/actionbar_cancel";

    private static final String RESOURCE_MASTERDETAIL_CONTAINER = "org.tomcurran.remiges:id/container";

    public static UiSelector getTextView() {
        return new UiSelector().className(CLASS_TEXTVIEW);
    }

    public UiObject getActionBarTitle() {
        return new UiObject(new UiSelector().resourceId(RESOURCE_ACTIONBAR_TITLE));
    }

    public void actionBarDone() throws UiObjectNotFoundException {
        new UiObject(new UiSelector().resourceId(RESOURCE_ACTIONBAR_DONE)).clickAndWaitForNewWindow();
    }

    public void actionBarCancel() throws UiObjectNotFoundException {
        new UiObject(new UiSelector().resourceId(RESOURCE_ACTIONBAR_CANCEL)).clickAndWaitForNewWindow();
    }

    public UiSelector getMasterDetailContainer() {
        return new UiSelector().resourceId(RESOURCE_MASTERDETAIL_CONTAINER);
    }

    public UiSelector getMasterDetailList() {
        return getMasterDetailContainer().childSelector(new UiSelector().className(CLASS_LISTVIEW));
    }

    public int getMasterDetailListCount() throws UiObjectNotFoundException {
        return new UiObject(getMasterDetailList()).getChildCount();
    }

    private void openApp() throws UiObjectNotFoundException {

        // Simulate a short press on the HOME button.
        getUiDevice().pressHome();

        // We’re now in the home screen. Next, we want to simulate
        // a user bringing up the All Apps screen.
        // If you use the uiautomatorviewer tool to capture a snapshot
        // of the Home screen, notice that the All Apps button’s
        // content-description property has the value “Apps”.  We can
        // use this property to create a UiSelector to find the button.
        UiObject allAppsButton = new UiObject(new UiSelector().description("Apps"));

        // Simulate a click to bring up the All Apps screen.
        allAppsButton.clickAndWaitForNewWindow();

        // In the All Apps screen, the Settings app is located in
        // the Apps tab. To simulate the user bringing up the Apps tab,
        // we create a UiSelector to find a tab with the text
        // label “Apps”.
        UiObject appsTab = new UiObject(new UiSelector().text("Apps"));

        // Simulate a click to enter the Apps tab.
        appsTab.click();

        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon.  Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));

        // Set the swiping mode to horizontal (the default is vertical)
        appViews.setAsHorizontalList();

        // Create a UiSelector to find the Settings app and simulate
        // a user click to launch the app.
        UiObject settingsApp = appViews.getChildByText(new UiSelector()
                .className(android.widget.TextView.class.getName()), APP_TITLE);
        settingsApp.clickAndWaitForNewWindow();

        // Validate that the package name is the expected one
        UiObject remigesValidation = new UiObject(new UiSelector().packageName(APP_PACKAGE));
        assertTrue("Unable to detect " + APP_TITLE, remigesValidation.exists());
    }

    private void closeApp() throws InterruptedException {
        while (getUiDevice().getCurrentPackageName().equals(APP_PACKAGE)) {
            getUiDevice().pressBack();
            getUiDevice().waitForIdle();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openApp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        closeApp();
    }
}
