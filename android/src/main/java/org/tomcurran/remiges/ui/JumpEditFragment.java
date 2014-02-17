package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.DbAdpater;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpEditFragment.class);

    private static final int STATE_INSERT = 0;
    private static final int STATE_EDIT = 1;

    private static final String SAVE_STATE_JUMP_URI = "jump_uri";
    private static final String SAVE_STATE_JUMP_STATE = "jump_state";
    private static final String SAVE_SATE_JUMP_TIME = "jump_time";

    private int mState;
    private Uri mJumpUri;
    private Cursor mJumpCursor;

    private EditText mJumpNumber;
    private TextView mJumpDate;
    private EditText mJumpDescription;

    private Time mTime;
    private View.OnClickListener mDateClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDatePickerDialog(view);
        }
    };

    public interface Callbacks {
        public void onJumpEdited(Uri uri);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onJumpEdited(Uri uri) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        FragmentActivity activity = (FragmentActivity) getActivity();
        mTime = new Time();

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_INSERT)) {
                mState = STATE_INSERT;
                // TODO: incorporate values passed in ?
                mTime.setToNow();
                ContentValues values = new ContentValues();
                values.put(RemigesContract.Jumps.JUMP_NUMBER, DbAdpater.getHighestJumpNumber(getActivity()) + 1);
                values.put(RemigesContract.Jumps.JUMP_DATE, mTime.toMillis(false));
                values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, "");
                mJumpUri = activity.getContentResolver().insert(intent.getData(), values);
                if (mJumpUri == null) {
                    LOGE(TAG, "Failed to insert new jump into " + intent.getData());
                    activity.setResult(FragmentActivity.RESULT_CANCELED);
                    activity.finish();
                    return;
                }
            } else if (action.equals(Intent.ACTION_EDIT)) {
                mState = STATE_EDIT;
                mJumpUri = intent.getData();
            } else {
                LOGE(TAG, "Unknown action. Exiting");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mJumpUri = savedInstanceState.getParcelable(SAVE_STATE_JUMP_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_JUMP_STATE);
            mTime.set(savedInstanceState.getLong(SAVE_SATE_JUMP_TIME));
        }
        activity.setResult(FragmentActivity.RESULT_OK, (new Intent()).setAction(mJumpUri.toString()));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jump_edit, container, false);

        mJumpNumber = (EditText) rootView.findViewById(R.id.edit_jump_number);
        mJumpDate = (TextView) rootView.findViewById(R.id.edit_jump_date);
        mJumpDescription = (EditText) rootView.findViewById(R.id.edit_jump_description);

        mJumpDate.setOnClickListener(mDateClickedListener);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateJump();
        outState.putParcelable(SAVE_STATE_JUMP_URI, mJumpUri);
        outState.putInt(SAVE_STATE_JUMP_STATE, mState);
        outState.putLong(SAVE_SATE_JUMP_TIME, mTime.toMillis(false));
    }

    @Override
    public void onPause() {
        super.onPause();
        updateJump();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    private void loadJump() {
        Cursor jumpCursor = mJumpCursor;
        if (jumpCursor.moveToFirst()) {
            mJumpNumber.setText(jumpCursor.getString(JumpQuery.NUMBER));
            mTime.set(jumpCursor.getLong(JumpQuery.DATE));
            updateDate();
            mJumpDescription.setText(jumpCursor.getString(JumpQuery.DESCRIPTION));
        }
    }

    private void updateDate() {
        mJumpDate.setText(DateFormat.format(getString(R.string.format_detail_jump_date), mTime.toMillis(false)));
    }

    private void updateJump() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Jumps.JUMP_NUMBER, UIUtils.parseTextViewInt(mJumpNumber));
        values.put(RemigesContract.Jumps.JUMP_DATE, mTime.toMillis(false));
        values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, mJumpDescription.getText().toString());
        int rowsUpdate = getActivity().getContentResolver().update(mJumpUri, values, null, null);
        if (rowsUpdate > 0) {
            mCallbacks.onJumpEdited(mJumpUri);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mJumpUri,
                JumpQuery.PROJECTION,
                null,
                null,
                RemigesContract.Jumps.DEFAULT_SORT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mJumpCursor = cursor;
        loadJump();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mJumpCursor = null;
    }

    private interface JumpQuery {

        String[] PROJECTION = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_DESCRIPTION
        };

        int NUMBER = 0;
        int DATE = 1;
        int DESCRIPTION = 2;

    }

    public void showDatePickerDialog(View view) {
        DialogFragment fragment = new DatePickerFragment();
        fragment.setTargetFragment(this, 0);
        fragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    public void setDate(int year, int month, int day) {
        mTime.set(day, month, year);
        updateDate();
    }

    public Time getDate() {
        return mTime;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            JumpEditFragment fragment = ((JumpEditFragment)getTargetFragment());
            Time time = fragment.getDate();
            return new DatePickerDialog(getActivity(), this, time.year, time.month, time.monthDay);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            JumpEditFragment fragment = ((JumpEditFragment)getTargetFragment());
            if (fragment.isAdded()) {
                fragment.setDate(year, month, day);
            }
        }

    }

}
