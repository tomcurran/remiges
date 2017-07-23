package org.tomcurran.remiges.test.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class RemigesTestCase extends InstrumentationTestCase {

    public static final String APP_PACKAGE = "org.tomcurran.remiges";
    public static final String APP_ID = APP_PACKAGE + ":id/";

    public static final String RESOURCE_EDIT_SAVE = APP_ID + "menu_edit_save";
    public static final String DESCRIPTION_NAVIGATE_UP = "Navigate up";
    public static final String TEXT_OK = "OK";

    private static final String RESOURCE_TOOLBAR = APP_ID + "toolbar_actionbar";
    private static final String RESOURCE_MASTER_DETAIL_CONTAINER = APP_ID + "container";

    private static final int LAUNCH_TIMEOUT = 5000;
    private static final int SMALLEST_WIDTH_TWO_PANE = 600;

    protected UiDevice getUiDevice() {
        return UiDevice.getInstance(getInstrumentation());
    }

    public static UiSelector getTextView() {
        return new UiSelector().className(android.widget.TextView.class.getName());
    }

    public static UiSelector getCheckedTextView() {
        return new UiSelector().className(android.widget.CheckedTextView.class.getName());
    }

    public static UiSelector getListView(UiSelector container) {
        return container.childSelector(new UiSelector().className(android.widget.ListView.class.getName()));
    }

    protected UiObject getByResource(String resourceId) {
        return getUiDevice().findObject(new UiSelector().resourceId(resourceId));
    }

    protected UiObject getByDescription(String description) throws UiObjectNotFoundException {
        return getUiDevice().findObject(new UiSelector().description(description));
    }

    protected UiObject getByText(String text) throws UiObjectNotFoundException {
        return getUiDevice().findObject(new UiSelector().text(text));
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

    protected String getActionBarTitle() throws UiObjectNotFoundException {
        return getUiDevice().findObject(new UiSelector().resourceId(RESOURCE_TOOLBAR).childSelector(new UiSelector().index(1))).getText();
    }

    public static UiSelector getMasterDetailContainer() {
        return new UiSelector().resourceId(RESOURCE_MASTER_DETAIL_CONTAINER);
    }

    public static UiSelector getMasterDetailList() {
        return getListView(getMasterDetailContainer());
    }

    protected int getMasterDetailListCount() {
        try {
            return getUiDevice().findObject(getMasterDetailList()).getChildCount();
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
    protected void changeTextField(String resource, String value, String hint) throws  UiObjectNotFoundException {
        UiObject textField = getByResource(resource);
        while (!textField.getText().equals(isTwoPane() ? hint : "")) {
            textField.clearTextField();
        }
        textField.setText(value);
        assertEquals(value, textField.getText());
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
     * Opens the application with an intent
     * @throws UiObjectNotFoundException
     */
    private void openApp() throws UiObjectNotFoundException {
        UiDevice device = getUiDevice();
        device.pressHome();

        final String launcherPackage = getInstrumentation().getContext().getPackageManager().resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY)
                .activityInfo.packageName;
        assertNotNull(launcherPackage);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        Context context = getInstrumentation().getContext();
        context.startActivity(context.getPackageManager()
                .getLaunchIntentForPackage(APP_PACKAGE).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    /**
     * Back out of the application
     * @throws InterruptedException
     */
    private void closeApp() throws InterruptedException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        while (device.getCurrentPackageName().equals(APP_PACKAGE)) {
            device.pressBack();
            device.waitForIdle();
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
