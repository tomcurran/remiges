package org.tomcurran.remiges.test.ui;


import com.android.uiautomator.core.UiObjectNotFoundException;

public class PlacesTestCase extends RemigesUiAutomatorTestCase {

    public static final String DESCRIPTION_OPEN_DRAWER = "Places, Open navigation drawer";
    public static final String TEXT_NAVIGATION = "Places";
    public static final String TEXT_TITLE = "Places";

    public static void navigateToPlaces(String navigateFrom) throws UiObjectNotFoundException {
        NavigationDrawerTestCase.navigateTo(navigateFrom, TEXT_NAVIGATION, TEXT_TITLE);
    }

}
