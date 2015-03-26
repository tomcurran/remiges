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

    public static final String APP_PACKAGE = "org.tomcurran.remiges";
    public static final String APP_ID = APP_PACKAGE + ":id/";

    public static final String RESOURCE_EDIT_SAVE = APP_ID + "menu_edit_save";
    public static final String DESCRIPTION_NAVIGATE_UP = "Navigate up";
    public static final String TEXT_OK = "OK";

    private static final String APP_TITLE = "Remiges";
    private static final String RESOURCE_TOOLBAR = APP_ID + "toolbar_actionbar";
    private static final String RESOURCE_MASTER_DETAIL_CONTAINER = APP_ID + "container";

    private static final int SMALLEST_WIDTH_TWO_PANE = 600;

    public static UiSelector getTextView() {
        return new UiSelector().className(android.widget.TextView.class.getName());
    }

    public static UiSelector getListView(UiSelector container) {
        return container.childSelector(new UiSelector().className(android.widget.ListView.class.getName()));
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
        return new UiObject(new UiSelector().resourceId(RESOURCE_TOOLBAR).childSelector(new UiSelector().index(1))).getText();
    }

    public static UiSelector getMasterDetailContainer() {
        return new UiSelector().resourceId(RESOURCE_MASTER_DETAIL_CONTAINER);
    }

    public static UiSelector getMasterDetailList() {
        return getListView(getMasterDetailContainer());
    }

    public static int getMasterDetailListCount() {
        try {
            return new UiObject(getMasterDetailList()).getChildCount();
        } catch (UiObjectNotFoundException exception) {
            return 0;
        }
    }

    /**
     * Sets the value of a text field to value. Field must have a placeholder when blank.
     * @param resource resource id
     * @param value string value to set
     * @param hint string placeholder text when field is blank
     * @throws UiObjectNotFoundException
     */
    public static void changeTextField(String resource, String value, String hint) throws  UiObjectNotFoundException {
        while (!getByResource(resource).getText().equals(hint)) {
            getByResource(resource).clearTextField();
        }
        getByResource(resource).setText(value);
        assertEquals(value, getByResource(resource).getText());
    }

    /**
     * Returns true if the application is in two pane mode based on SMALLEST_WIDTH_TWO_PANE
     * @return true if the application is in two pane mode based on SMALLEST_WIDTH_TWO_PANE
     */
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

    /**
     * Opens the application regardless of where the UI current is
     * @throws UiObjectNotFoundException
     */
    private void openApp() throws UiObjectNotFoundException {
        getUiDevice().pressHome();

        getByDescription("Apps").clickAndWaitForNewWindow();

        UiObject appsTab = getByText("Apps");
        if (appsTab.exists()) {
            appsTab.click();
        }

        UiScrollable apps = new UiScrollable(new UiSelector().scrollable(true));
        apps.setAsHorizontalList();
        apps.getChildByText(getTextView(), APP_TITLE).clickAndWaitForNewWindow();

        assertTrue("Unable to detect " + APP_TITLE, new UiObject(new UiSelector().packageName(APP_PACKAGE)).exists());
    }

    /**
     * Back out of the application
     * @throws InterruptedException
     */
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
