package org.tomcurran.remiges.ui.singlepane;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.ui.JumpTypeEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class JumpTypeEditActivity extends SimpleSinglePaneActivity {
    private static final String TAG = makeLogTag(JumpTypeEditActivity.class);

    public interface Callbacks {
        public void barDone();
        public void barCancel();
    }

    @Override
    protected Fragment onCreatePane() {
        return new JumpTypeEditFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.donebar_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        barDone();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        barCancel();
                    }
                });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void barDone() {
        ((JumpTypeEditFragment)getFragment()).barDone();
        finish();
    }

    private void barCancel() {
        ((JumpTypeEditFragment)getFragment()).barCancel();
        finish();
    }

}
