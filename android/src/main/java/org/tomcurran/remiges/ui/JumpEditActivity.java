package org.tomcurran.remiges.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.tomcurran.remiges.R;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpEditActivity extends FragmentActivity implements JumpEditFragment.Callbacks {
    private static final String TAG = makeLogTag(JumpEditActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump_edit);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            JumpEditFragment fragment = new JumpEditFragment();
            fragment.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.jump_edit_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, JumpDetailActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJumpEdited(Uri uri) {
    }

    @Override
    public void onDeleteJump(Uri uri) {
        setResult(RESULT_OK, new Intent(Intent.ACTION_DELETE, uri));
        finish();
    }

}
