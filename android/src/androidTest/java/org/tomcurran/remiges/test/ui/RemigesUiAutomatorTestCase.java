package org.tomcurran.remiges.test.ui;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RemigesUiAutomatorTestCase extends UiAutomatorTestCase {

    private static final String LAUNCHER_ANDROID = "com.android.launcher";
    private static final String LAUNCHER_GOOGLE_NOW = "com.google.android.googlequicksearchbox";

    public static final String CLASS_LISTVIEW = "android.widget.ListView";
    public static final String CLASS_TEXTVIEW = "android.widget.TextView";
    public static final String RESOURCE_ACTIONBAR_DONE = "org.tomcurran.remiges:id/actionbar_done";
    public static final String RESOURCE_ACTIONBAR_CANCEL = "org.tomcurran.remiges:id/actionbar_cancel";

    private static final String APP_TITLE = "Remiges";
    private static final String APP_PACKAGE = "org.tomcurran.remiges";
    private static final String RESOURCE_ACTIONBAR_TITLE = "android:id/action_bar_title";
    private static final String RESOURCE_MASTER_DETAIL_CONTAINER = "org.tomcurran.remiges:id/container";

    private static final int SMALLEST_WIDTH_TWO_PANE = 600;

    public static UiSelector getTextView() {
        return new UiSelector().className(CLASS_TEXTVIEW);
    }

    public static UiSelector getListView(UiSelector container) {
        return container.childSelector(new UiSelector().className(CLASS_LISTVIEW));
    }

    public static UiObject getByResource(String resourceId) {
        return new UiObject(new UiSelector().resourceId(resourceId));
    }

    public static UiObject getByDescription(String description) throws UiObjectNotFoundException {
        return new UiObject(new UiSelector().description(description));
    }

    public static UiObject getByText(String text) throws UiObjectNotFoundException {
        return new UiObject(new UiSelector().text(text));
    }

    public static UiObject getListItem(UiSelector list, String text) throws UiObjectNotFoundException {
        return new UiScrollable(list).getChildByText(getTextView(), text);
    }

    public static void clickListItem(UiSelector list, String item) throws UiObjectNotFoundException {
        assertInList(list, item);
        getListItem(list, item).click();
    }

    public static void assertInList(UiSelector list, String item) {
        assertTrue(String.format("%s not found in list", item), hasListItem(list, item));
    }

    public static void assertNotInList(UiSelector list, String item) {
        assertFalse(String.format("%s found in list", item), hasListItem(list, item));
    }

    private static boolean hasListItem(UiSelector list, String item) {
        try {
            getListItem(list, item);
            return true;
        } catch (UiObjectNotFoundException e) {
            return false;
        }
    }

    public static String getActionBarTitle() throws UiObjectNotFoundException {
        return new UiObject(new UiSelector().resourceId(RESOURCE_ACTIONBAR_TITLE)).getText();
    }

    public static UiSelector getMasterDetailContainer() {
        return new UiSelector().resourceId(RESOURCE_MASTER_DETAIL_CONTAINER);
    }

    public static UiSelector getMasterDetailList() {
        return getListView(getMasterDetailContainer());
    }

    public static int getMasterDetailListCount() throws UiObjectNotFoundException {
        return new UiObject(getMasterDetailList()).getChildCount();
    }

    public boolean isTwoPane() {
        try {
            double dpi = Double.parseDouble(new BufferedReader(new InputStreamReader(
                    Runtime.getRuntime().exec("getprop ro.sf.lcd_density").getInputStream()
            )).readLine());
            double width = getUiDevice().getDisplayWidth() / (dpi / 160);
            double height = getUiDevice().getDisplayHeight() / (dpi / 160);
            return Math.min(width, height) >= SMALLEST_WIDTH_TWO_PANE;
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            return false;
        }
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
        // Simulate a click to bring up the All Apps screen.
        getByDescription("Apps").clickAndWaitForNewWindow();

        // In the All Apps screen, the Settings app is located in
        // the Apps tab. To simulate the user bringing up the Apps tab,
        // we create a UiSelector to find a tab with the text
        // label “Apps”.
        // Simulate a click to enter the Apps tab on the default android launcher
        if (getUiDevice().getCurrentPackageName().equals(LAUNCHER_ANDROID)) {
            getByText("Apps").click();
        }

        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon.  Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable apps = new UiScrollable(new UiSelector().scrollable(true));

        // Set the swiping mode to horizontal (the default is vertical)
        apps.setAsHorizontalList();

        // Create a UiSelector to find the Settings app and simulate
        // a user click to launch the app.
        UiObject app = apps.getChildByText(new UiSelector()
                .className(android.widget.TextView.class.getName()), APP_TITLE);
        app.clickAndWaitForNewWindow();

        // Validate that the package name is the expected one
        UiObject appPackage = new UiObject(new UiSelector().packageName(APP_PACKAGE));
        assertTrue("Unable to detect " + APP_TITLE, appPackage.exists());
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
