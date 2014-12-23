package org.tomcurran.remiges.ui.singlepane;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.tomcurran.remiges.R;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public abstract class EditItemActivity extends SimpleSinglePaneActivity {
    private static final String TAG = makeLogTag(EditItemActivity.class);

    public interface Callbacks {
        public void onSaveItem();
    }

    @Override
    protected Fragment onCreatePane() {
        Fragment fragment = onCreateEditPane();
        if (!Callbacks.class.isInstance(fragment)) {
            throw new IllegalStateException("Fragment must implement activity's callbacks.");
        }
        return fragment;
    }

    protected abstract Fragment onCreateEditPane();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.RemigesTheme_EditItem);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_close);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_save:
                ((Callbacks)getFragment()).onSaveItem();
                return true;
            case android.R.id.home:
                setResult(FragmentActivity.RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
