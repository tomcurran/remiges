package org.tomcurran.remiges.test.ui;

import android.content.ContentValues;

import com.android.uiautomator.core.UiObjectNotFoundException;


public abstract class ItemTestCase extends RemigesUiAutomatorTestCase {

    public abstract void changeValues(ContentValues values) throws UiObjectNotFoundException;
    public abstract void assertDetail(ContentValues values) throws UiObjectNotFoundException;
    public abstract void assertHint() throws UiObjectNotFoundException;
    public abstract void navigateTo() throws UiObjectNotFoundException;
    public abstract ContentValues getNewValues();
    public abstract String getClick(ContentValues values);
    public abstract String getTitle();
    public abstract String getEditAction();
    public abstract String getAddAction();
    public abstract String getDeleteAction();

    private void addItem(ContentValues values) throws UiObjectNotFoundException {
        // navigate to
        navigateTo();

        // get current amount of places available
        int listItemCount = getMasterDetailListCount();

        // ensure place name not present in list
        assertNotInList(getMasterDetailList(), getClick(values));

        // click add action
        getByDescription(getAddAction()).clickAndWaitForNewWindow();

        // ensure hints set
        assertHint();

        // change values
        changeValues(values);

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure new item in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure place name present in list
        assertInList(getMasterDetailList(), getClick(values));
    }

    private void deleteItem(ContentValues values) throws UiObjectNotFoundException {
        // count items in list
        int listItemsBefore = getMasterDetailListCount();

        // click item
        clickListItem(getMasterDetailList(), getClick(values));

        // ensure correct page
        assertEquals(getTitle(), getActionBarTitle());

        // click delete action
        getByDescription(getDeleteAction()).clickAndWaitForNewWindow();

        // ensure item removed from list
        assertEquals(listItemsBefore - 1, getMasterDetailListCount());
        assertNotInList(getMasterDetailList(), getClick(values));
    }

    public void testNavigateTo() throws UiObjectNotFoundException {
        navigateTo();
    }

    public void testAddCancel() throws UiObjectNotFoundException {
        // navigate
        navigateTo();

        // count items in list
        int listItemsBefore = getMasterDetailListCount();

        // click add action
        getByDescription(getAddAction()).clickAndWaitForNewWindow();

        // ensure hints set
        assertHint();

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure no new item in list
        assertEquals(listItemsBefore, getMasterDetailListCount());
    }

    public void testAdd() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // delete item
        deleteItem(values);
    }

    public void testEditCancel() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // click item
        clickListItem(getMasterDetailList(), getClick(values));

        // ensure correct page
        assertEquals(getTitle(), getActionBarTitle());

        // ensure detail
        assertDetail(values);

        // click edit action
        getByDescription(getEditAction()).clickAndWaitForNewWindow();

        // action bar cancel
        getByResource(RESOURCE_ACTIONBAR_CANCEL).clickAndWaitForNewWindow();

        // ensure detail
        assertDetail(values);

        // ensure correct view
        if (!isTwoPane()) {
            getUiDevice().pressBack();
        }

        // delete item
        deleteItem(values);
    }

    public void testEdit() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // click item
        clickListItem(getMasterDetailList(), getClick(values));

        // ensure correct page
        assertEquals(getTitle(), getActionBarTitle());

        // ensure detail
        assertDetail(values);

        // click edit action
        getByDescription(getEditAction()).clickAndWaitForNewWindow();

        // change values
        values = getNewValues();
        changeValues(values);

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure detail
        assertDetail(values);

        // ensure correct view
        if (!isTwoPane()) {
            getUiDevice().pressBack();
        }

        // delete item
        deleteItem(values);
    }

    public void testDelete() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // delete item
        deleteItem(values);
    }

}
