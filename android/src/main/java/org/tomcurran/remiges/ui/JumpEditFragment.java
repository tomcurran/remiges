package org.tomcurran.remiges.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.provider.RemigesContract;
import org.tomcurran.remiges.util.DbAdapter;
import org.tomcurran.remiges.util.FragmentUtils;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(JumpEditFragment.class);

    private static final int STATE_INSERT = 0;
    private static final int STATE_EDIT = 1;

    private static final int LOADER_JUMP = 0;
    private static final int LOADER_PLACES = 1;
    private static final int LOADER_JUMPTYPES = 2;

    private static final String SAVE_STATE_JUMP_URI = "jump_uri";
    private static final String SAVE_STATE_JUMP_STATE = "jump_state";
    private static final String SAVE_SATE_JUMP_TIME = "jump_time";

    private int mState;
    private Uri mJumpUri;
    private Cursor mJumpCursor;
    private Long mPlaceId;
    private Long mJumpTypeId;
    private Time mTime;

    private EditText mJumpNumber;
    private TextView mJumpDate;
    private EditText mJumpDescription;
    private Spinner mJumpPlace;
    private EditText mJumpWay;
    private Spinner mJumpType;
    private EditText mJumpExitAltitude;
    private EditText mJumpDeploymentAltitude;
    private EditText mJumpDelay;

    private View.OnClickListener mDateClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDatePickerDialog(view);
        }
    };
    private View.OnClickListener mAddPlaceClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallbacks.onAddPlace();
        }
    };
    private View.OnClickListener mAddTypeClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallbacks.onAddJumpType();
        }
    };

    private AdapterView.OnItemSelectedListener mPlaceOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setPlace(id);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    private AdapterView.OnItemSelectedListener mJumpTypeOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setJumpType(id);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    public interface Callbacks {
        public void onAddPlace();
        public void onAddJumpType();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onAddPlace() {
        }
        @Override
        public void onAddJumpType() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    public JumpEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        FragmentActivity activity = getActivity();
        mTime = new Time();

        if (savedInstanceState == null) {
            final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
            final String action = intent.getAction();
            if (action == null) {
                LOGE(TAG, "No action provided for jump");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            } else if (action.equals(Intent.ACTION_INSERT)) {
                mState = STATE_INSERT;
                ContentValues values = getDefaultValues();
                if (intent.getExtras() != null) {
                    passInExtras(intent.getExtras(), values);
                }
                mJumpUri = activity.getContentResolver().insert(RemigesContract.Jumps.CONTENT_URI, values);
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

        Intent intent = new Intent();
        switch (mState) {
            case STATE_INSERT: intent.setAction(Intent.ACTION_INSERT); break;
            case STATE_EDIT:   intent.setAction(Intent.ACTION_EDIT);   break;
        }
        intent.setData(mJumpUri);
        activity.setResult(FragmentActivity.RESULT_OK, intent);

        getLoaderManager().initLoader(LOADER_JUMP, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jump_edit, container, false);

        mJumpNumber = (EditText) rootView.findViewById(R.id.edit_jump_number);
        mJumpDate = (TextView) rootView.findViewById(R.id.edit_jump_date);
        mJumpDescription = (EditText) rootView.findViewById(R.id.edit_jump_description);
        mJumpPlace = (Spinner) rootView.findViewById(R.id.edit_jump_place);
        mJumpWay = (EditText) rootView.findViewById(R.id.edit_jump_way);
        mJumpType = (Spinner) rootView.findViewById(R.id.edit_jump_type);
        mJumpExitAltitude = (EditText) rootView.findViewById(R.id.edit_jump_exit_altitude);
        mJumpDeploymentAltitude = (EditText) rootView.findViewById(R.id.edit_jump_deployment_altitude);
        mJumpDelay = (EditText) rootView.findViewById(R.id.edit_jump_delay);

        mJumpDate.setOnClickListener(mDateClickedListener);
        rootView.findViewById(R.id.edit_jump_add_place).setOnClickListener(mAddPlaceClickedListener);
        rootView.findViewById(R.id.edit_jump_add_type).setOnClickListener(mAddTypeClickedListener);

        mJumpPlace.setAdapter(new SpinnerAdapter(getActivity(), PlaceQuery.PROJECTION));
        mJumpPlace.setOnItemSelectedListener(mPlaceOnItemSelectedListener);
        getLoaderManager().initLoader(LOADER_PLACES, null, this);

        mJumpType.setAdapter(new SpinnerAdapter(getActivity(), JumpTypeQuery.PROJECTION));
        mJumpType.setOnItemSelectedListener(mJumpTypeOnItemSelectedListener);
        getLoaderManager().initLoader(LOADER_JUMPTYPES, null, this);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_JUMP_URI, mJumpUri);
        outState.putInt(SAVE_STATE_JUMP_STATE, mState);
        outState.putLong(SAVE_SATE_JUMP_TIME, mTime.toMillis(false));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
        if (mCallbacks == null) {
            throw new IllegalStateException("Parent must implement fragment's callbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    public String barDone() {
        updateJump();
        return RemigesContract.Jumps.getJumpId(mJumpUri);
    }

    public void barCancel() {
        if (mState == STATE_INSERT) {
            deleteJump();
        }
    }

    private ContentValues getDefaultValues() {
        FragmentActivity activity = getActivity();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        mTime.setToNow();
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Jumps.JUMP_NUMBER, DbAdapter.getHighestJumpNumber(activity) + 1);
        values.put(RemigesContract.Jumps.JUMP_DATE, mTime.toMillis(false));
        values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, "");
        values.put(RemigesContract.Jumps.JUMP_WAY, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_WAY, SettingsFragment.PREFERENCE_DEFAULT_WAY));
        values.put(RemigesContract.Jumps.PLACE_ID, Integer.parseInt(preferences.getString(SettingsFragment.PREFERENCE_PLACE, SettingsFragment.PREFERENCE_DEFAULT_PLACE)));
        values.put(RemigesContract.Jumps.JUMPTYPE_ID, Integer.parseInt(preferences.getString(SettingsFragment.PREFERENCE_JUMPTYPE, SettingsFragment.PREFERENCE_DEFAULT_JUMPTYPE)));
        values.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_EXIT_ALTITUDE, SettingsFragment.PREFERENCE_DEFAULT_EXIT_ALTITUDE));
        values.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_DEPLOYMENT_ALTITUDE, SettingsFragment.PREFERENCE_DEFAULT_DEPLOYMENT_ALTITUDE));
        values.put(RemigesContract.Jumps.JUMP_DELAY, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_DELAY, SettingsFragment.PREFERENCE_DEFAULT_DELAY));
        return values;
    }

    private void passInExtras(Bundle extras, ContentValues values) {
        if (extras.containsKey(RemigesContract.Jumps.JUMP_NUMBER))
            values.put(RemigesContract.Jumps.JUMP_NUMBER, extras.getInt(RemigesContract.Jumps.JUMP_NUMBER));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DATE))
            values.put(RemigesContract.Jumps.JUMP_DATE, extras.getLong(RemigesContract.Jumps.JUMP_DATE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DESCRIPTION))
            values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, extras.getString(RemigesContract.Jumps.JUMP_DESCRIPTION));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_WAY))
            values.put(RemigesContract.Jumps.JUMP_WAY, extras.getInt(RemigesContract.Jumps.JUMP_WAY));
        if (extras.containsKey(RemigesContract.Jumps.PLACE_ID))
            values.put(RemigesContract.Jumps.PLACE_ID, extras.getLong(RemigesContract.Jumps.PLACE_ID));
        if (extras.containsKey(RemigesContract.Jumps.JUMPTYPE_ID))
            values.put(RemigesContract.Jumps.JUMPTYPE_ID, extras.getLong(RemigesContract.Jumps.JUMPTYPE_ID));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE))
            values.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, extras.getInt(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE))
            values.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, extras.getInt(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DELAY))
            values.put(RemigesContract.Jumps.JUMP_DELAY, extras.getInt(RemigesContract.Jumps.JUMP_DELAY));
    }

    private void loadJump() {
        Cursor jumpCursor = mJumpCursor;
        if (jumpCursor.moveToFirst()) {
            mJumpNumber.setText(jumpCursor.getString(JumpQuery.NUMBER));
            setDate(jumpCursor.getLong(JumpQuery.DATE));
            setPlace(jumpCursor.getLong(JumpQuery.PLACE));
            mJumpWay.setText(jumpCursor.getString(JumpQuery.WAY));
            setJumpType(jumpCursor.getLong(JumpQuery.TYPE));
            UIUtils.setTextViewInt(mJumpExitAltitude, jumpCursor.getInt(JumpQuery.EXIT_ALTITUDE));
            UIUtils.setTextViewInt(mJumpDeploymentAltitude, jumpCursor.getInt(JumpQuery.DEPLOYMENT_ALTITUDE));
            UIUtils.setTextViewInt(mJumpDelay, jumpCursor.getInt(JumpQuery.DELAY));
            mJumpDescription.setText(jumpCursor.getString(JumpQuery.DESCRIPTION));
        }
    }

    private void setDate(long millis) {
        mTime.set(millis);
        mJumpDate.setText(DateFormat.format(getString(R.string.format_edit_jump_date), mTime.toMillis(false)));
    }

    public void setPlace(String placeId) {
        try {
            setPlace(Long.parseLong(placeId));
        } catch (NumberFormatException e) {
            LOGE(TAG, String.format("Invalid place id: %s", e.getMessage()));
        }
    }

    private void setPlace(long placeId) {
        mPlaceId = placeId;
        updatePlaceSpinner();
    }

    public void setJumpType(String jumpTypeId) {
        try {
            setJumpType(Long.parseLong(jumpTypeId));
        } catch (NumberFormatException e) {
            LOGE(TAG, String.format("Invalid jump type id: %s", e.getMessage()));
        }
    }

    private void setJumpType(long jumpTypeId) {
        mJumpTypeId = jumpTypeId;
        updateJumpTypeSpinner();
    }

    private boolean updateJump() {
        ContentValues values = new ContentValues();
        values.put(RemigesContract.Jumps.JUMP_NUMBER, UIUtils.parseTextViewInt(mJumpNumber));
        values.put(RemigesContract.Jumps.JUMP_DATE, mTime.toMillis(false));
        values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, mJumpDescription.getText().toString());
        values.put(RemigesContract.Jumps.PLACE_ID, mPlaceId);
        int way = UIUtils.parseTextViewInt(mJumpWay);
        values.put(RemigesContract.Jumps.JUMP_WAY, way > 1 ? way : 1);
        values.put(RemigesContract.Jumps.JUMPTYPE_ID, mJumpTypeId);
        values.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, UIUtils.parseTextViewInt(mJumpExitAltitude));
        values.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, UIUtils.parseTextViewInt(mJumpDeploymentAltitude));
        values.put(RemigesContract.Jumps.JUMP_DELAY, UIUtils.parseTextViewInt(mJumpDelay));
        return getActivity().getContentResolver().update(mJumpUri, values, null, null) > 0;
    }

    private boolean deleteJump() {
        return getActivity().getContentResolver().delete(mJumpUri, null, null) > 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_JUMP:
                return new CursorLoader(
                        getActivity(),
                        mJumpUri,
                        JumpQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.Jumps.DEFAULT_SORT
                );
            case LOADER_PLACES:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.Places.CONTENT_URI,
                        PlaceQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.Places.DEFAULT_SORT
                );
            case LOADER_JUMPTYPES:
                return new CursorLoader(
                        getActivity(),
                        RemigesContract.JumpTypes.CONTENT_URI,
                        JumpTypeQuery.PROJECTION,
                        null,
                        null,
                        RemigesContract.JumpTypes.DEFAULT_SORT
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_JUMP:
                mJumpCursor = cursor;
                loadJump();
                break;
            case LOADER_PLACES:
                ((SimpleCursorAdapter) mJumpPlace.getAdapter()).swapCursor(cursor);
                updatePlaceSpinner();
                break;
            case LOADER_JUMPTYPES:
                ((SimpleCursorAdapter) mJumpType.getAdapter()).swapCursor(cursor);
                updateJumpTypeSpinner();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_JUMP:
                mJumpCursor = null;
                break;
            case LOADER_PLACES:
                ((SimpleCursorAdapter) mJumpPlace.getAdapter()).swapCursor(null);
                break;
            case LOADER_JUMPTYPES:
                ((SimpleCursorAdapter) mJumpType.getAdapter()).swapCursor(null);
                break;
        }
    }

    private interface JumpQuery {

        String[] PROJECTION = {
                RemigesContract.Jumps.JUMP_NUMBER,
                RemigesContract.Jumps.JUMP_DATE,
                RemigesContract.Jumps.JUMP_DESCRIPTION,
                RemigesContract.Jumps.PLACE_ID,
                RemigesContract.Jumps.JUMP_WAY,
                RemigesContract.Jumps.JUMPTYPE_ID,
                RemigesContract.Jumps.JUMP_EXIT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE,
                RemigesContract.Jumps.JUMP_DELAY
        };

        int NUMBER = 0;
        int DATE = 1;
        int DESCRIPTION = 2;
        int PLACE = 3;
        int WAY = 4;
        int TYPE = 5;
        int EXIT_ALTITUDE = 6;
        int DEPLOYMENT_ALTITUDE = 7;
        int DELAY = 8;

    }

    private interface PlaceQuery {

        String[] PROJECTION = {
                RemigesContract.Places.PLACE_NAME,
                RemigesContract.Places._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

    private interface JumpTypeQuery {

        String[] PROJECTION = {
                RemigesContract.JumpTypes.JUMPTPYE_NAME,
                RemigesContract.JumpTypes._ID
        };

        int NAME = 0;
        int _ID = 1;

    }

    public static class SpinnerAdapter extends SimpleCursorAdapter {

        private final static int[] TO = {
                android.R.id.text1
        };

        public SpinnerAdapter(Context context, final String[] projection) {
            super(context, android.R.layout.simple_spinner_item, null, projection, TO, 0);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

    }

    private void updatePlaceSpinner() {
        updateSpinner(mJumpPlace, mPlaceId, PlaceQuery._ID);
    }

    private void updateJumpTypeSpinner() {
        updateSpinner(mJumpType, mJumpTypeId, JumpTypeQuery._ID);
    }

    private void updateSpinner(Spinner spinner, Long id, int column) {
        spinner.setVisibility(spinner.getCount() <= 0 ? View.GONE : View.VISIBLE);
        if (id == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (id == ((Cursor) spinner.getItemAtPosition(i)).getLong(column)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment fragment = new DatePickerFragment();
        fragment.setTargetFragment(this, 0);
        fragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    public void setDate(int year, int month, int day) {
        Time time = new Time();
        time.set(day, month, year);
        setDate(time.toMillis(false));
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
