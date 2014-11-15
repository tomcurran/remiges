package org.tomcurran.remiges.ui.singlepane;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.ui.PlaceEditFragment;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;

public class PlaceEditActivity extends SimpleSinglePaneActivity {
    private static final String TAG = makeLogTag(PlaceEditActivity.class);

    @Override
    protected Fragment onCreatePane() {
        return new PlaceEditFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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

        int displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME;
        ActionBar.LayoutParams layoutParams;
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;
        } else {
            layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            displayOptions |= ActionBar.DISPLAY_SHOW_TITLE;
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, displayOptions);
        actionBar.setCustomView(customActionBarView, layoutParams);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void barDone() {
        Intent intent = getIntent();
        String placeId = ((PlaceEditFragment)getFragment()).barDone();
        intent.setData(RemigesContract.Places.buildPlaceUri(placeId));
        setResult(FragmentActivity.RESULT_OK, intent);
        finish();
    }

    private void barCancel() {
        ((PlaceEditFragment)getFragment()).barCancel();
        setResult(FragmentActivity.RESULT_CANCELED, getIntent());
        finish();
    }

}
