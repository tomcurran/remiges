package org.tomcurran.remiges.test.ui;

import android.content.ContentValues;

import com.android.uiautomator.core.UiObjectNotFoundException;


public abstract class ItemTestCase extends RemigesUiAutomatorTestCase {

    /**
     * Changes the fields of the edit item screen to the values provided in values
     * @param values new set of values to be used of this item
     * @throws UiObjectNotFoundException
     */
    public abstract void editValues(ContentValues values) throws UiObjectNotFoundException;

    /**
     * Asserts that the items detail view matches the values
     * @param values values to be asserted in the detail view
     * @throws UiObjectNotFoundException
     */
    public abstract void assertDetail(ContentValues values) throws UiObjectNotFoundException;

    /**
     * Asserts the fields of the edit item screen have their hint values set
     * @throws UiObjectNotFoundException
     */
    public abstract void assertHint() throws UiObjectNotFoundException;

    /**
     * Navigates to the test case view
     * @throws UiObjectNotFoundException
     */
    public abstract void navigateTo() throws UiObjectNotFoundException;

    /**
     * Returns a new set of values for the data item
     * @return a new set of values for the data item
     */
    public abstract ContentValues getNewValues();

    /**
     * Returns a value to use to click a master detail list view item from the values
     * @param values set of values representing the data item
     * @return a value to use to click a master detail list view item
     */
    public abstract String getListClickTarget(ContentValues values);

    /**
     * Returns the title of the current master detail activity regardless of single or two pane mode
     * @return the title of the current master detail activity regardless of single or two pane mode
     */
    public abstract String getTitle();

    /**
     * Returns the description of the edit action button for this item
     * @return the description of the edit action button for this item
     */
    public abstract String getEditAction();

    /**
     * Returns the description of the add action button for this item
     * @return the description of the add action button for this item
     */
    public abstract String getAddAction();

    /**
     * Returns the description of the delete action button for this item
     * @return the description of the delete action button for this item
     */
    public abstract String getDeleteAction();

    /**
     * Ensures navigation between this item an others
     */
    public abstract void testNavigateAwayAndBack() throws UiObjectNotFoundException;

    /**
     * Adds an item ensuring it was added correctly
     * @param values set of values representing the data item
     * @throws UiObjectNotFoundException
     */
    private void addItem(ContentValues values) throws UiObjectNotFoundException {
        // navigate to
        navigateTo();

        // get current amount of places available
        int listItemCount = getMasterDetailListCount();

        // ensure place name not present in list
        assertNotInList(getMasterDetailList(), getListClickTarget(values));

        // click add action
        getByDescription(getAddAction()).clickAndWaitForNewWindow();

        // ensure hints set
        assertHint();

        // change values
        editValues(values);

        // action bar done
        getByResource(RESOURCE_ACTIONBAR_DONE).clickAndWaitForNewWindow();

        // ensure new item in list
        assertEquals(listItemCount + 1, getMasterDetailListCount());

        // ensure place name present in list
        assertInList(getMasterDetailList(), getListClickTarget(values));
    }

    /**
     * Deletes an item ensuring it was deleted correctly
     * @param values set of values representing the data item
     * @throws UiObjectNotFoundException
     */
    private void deleteItem(ContentValues values) throws UiObjectNotFoundException {
        // count items in list
        int listItemsBefore = getMasterDetailListCount();

        // click item
        clickListItem(getMasterDetailList(), getListClickTarget(values));

        // ensure correct page
        assertEquals(getTitle(), getActionBarTitle());

        // click delete action
        getByDescription(getDeleteAction()).clickAndWaitForNewWindow();

        // ensure item removed from list
        assertEquals(listItemsBefore - 1, getMasterDetailListCount());
        assertNotInList(getMasterDetailList(), getListClickTarget(values));
    }

    /**
     * Ensures we can navigate to the item
     * @throws UiObjectNotFoundException
     */
    public void testNavigateTo() throws UiObjectNotFoundException {
        navigateTo();
    }

    /**
     * Ensures we can attempt to add an item then abort the operation with no changes to the data
     * @throws UiObjectNotFoundException
     */
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

    /**
     * Ensures we can add an item
     * @throws UiObjectNotFoundException
     */
    public void testAdd() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // delete item
        deleteItem(values);
    }

    /**
     * Ensures we can attempt to edit an item then abort the operation with no changes to the data
     * @throws UiObjectNotFoundException
     */
    public void testEditCancel() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // click item
        clickListItem(getMasterDetailList(), getListClickTarget(values));

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

    /**
     * Ensures we can edit an item
     * @throws UiObjectNotFoundException
     */
    public void testEdit() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // click item
        clickListItem(getMasterDetailList(), getListClickTarget(values));

        // ensure correct page
        assertEquals(getTitle(), getActionBarTitle());

        // ensure detail
        assertDetail(values);

        // click edit action
        getByDescription(getEditAction()).clickAndWaitForNewWindow();

        // change values
        values = getNewValues();
        editValues(values);

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

    /**
     * Ensures we can delete an item
     * @throws UiObjectNotFoundException
     */
    public void testDelete() throws UiObjectNotFoundException {
        ContentValues values = getNewValues();

        // add item
        addItem(values);

        // delete item
        deleteItem(values);
    }

}
