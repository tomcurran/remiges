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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.tomcurran.remiges.ui.singlepane.EditItemActivity;
import org.tomcurran.remiges.util.DbAdapter;
import org.tomcurran.remiges.util.FragmentUtils;
import org.tomcurran.remiges.util.UIUtils;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class JumpEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EditItemActivity.Callbacks {
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
                LOGE(TAG, "No intent action provided");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            } else if (action.equals(Intent.ACTION_INSERT)) {
                mState = STATE_INSERT;
                mJumpUri = null;
            } else if (action.equals(Intent.ACTION_EDIT)) {
                mState = STATE_EDIT;
                mJumpUri = intent.getData();
                getLoaderManager().initLoader(LOADER_JUMP, null, this);
            } else {
                LOGE(TAG, "Unknown intent action provided");
                activity.setResult(FragmentActivity.RESULT_CANCELED);
                activity.finish();
                return;
            }
        } else {
            mJumpUri = savedInstanceState.getParcelable(SAVE_STATE_JUMP_URI);
            mState = savedInstanceState.getInt(SAVE_STATE_JUMP_STATE);
            mTime.set(savedInstanceState.getLong(SAVE_SATE_JUMP_TIME));
        }

        if (mState == STATE_INSERT) {
            activity.setTitle(R.string.title_jump_insert);
        }
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            ContentValues values = getDefaultValues();
            if (mState == STATE_INSERT) {
                Bundle extras = BaseActivity.fragmentArgumentsToIntent(getArguments()).getExtras();
                if (extras != null) {
                    values = passIntentValues(extras, values);
                }
            }
            setViewValues(values);
        }
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

    @Override
    public void onSaveItem() {
        switch (mState) {
            case STATE_INSERT:
                insertJump();
                break;
            case STATE_EDIT:
                updateJump();
                break;
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
        values.put(RemigesContract.Jumps.JUMP_WAY, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_WAY, getString(R.string.preference_default_defaults_way)));
        values.put(RemigesContract.Jumps.PLACE_ID, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_PLACE));
        values.put(RemigesContract.Jumps.JUMPTYPE_ID, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_JUMPTYPE));
        values.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_EXIT_ALTITUDE));
        values.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_DEPLOYMENT_ALTITUDE));
        values.put(RemigesContract.Jumps.JUMP_DELAY, UIUtils.parseIntPreference(preferences, SettingsFragment.PREFERENCE_DELAY));
        return values;
    }

    private ContentValues getViewValues() {
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
        return values;
    }

    private void setViewValues(ContentValues values) {
        mJumpNumber.setText(values.getAsString(RemigesContract.Jumps.JUMP_NUMBER));
        setDate(values.getAsLong(RemigesContract.Jumps.JUMP_DATE));
        setPlace(values.getAsLong(RemigesContract.Jumps.PLACE_ID));
        mJumpWay.setText(values.getAsString(RemigesContract.Jumps.JUMP_WAY));
        setJumpType(values.getAsLong(RemigesContract.Jumps.JUMPTYPE_ID));
        UIUtils.setTextViewInt(mJumpExitAltitude, values.getAsInteger(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE));
        UIUtils.setTextViewInt(mJumpDeploymentAltitude, values.getAsInteger(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE));
        UIUtils.setTextViewInt(mJumpDelay, values.getAsInteger(RemigesContract.Jumps.JUMP_DELAY));
        mJumpDescription.setText(values.getAsString(RemigesContract.Jumps.JUMP_DESCRIPTION));
    }

    private ContentValues passIntentValues(Bundle extras, ContentValues values) {
        ContentValues newValues = new ContentValues(values);
        if (extras.containsKey(RemigesContract.Jumps.JUMP_NUMBER))
            newValues.put(RemigesContract.Jumps.JUMP_NUMBER, extras.getInt(RemigesContract.Jumps.JUMP_NUMBER));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DATE))
            newValues.put(RemigesContract.Jumps.JUMP_DATE, extras.getLong(RemigesContract.Jumps.JUMP_DATE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DESCRIPTION))
            newValues.put(RemigesContract.Jumps.JUMP_DESCRIPTION, extras.getString(RemigesContract.Jumps.JUMP_DESCRIPTION));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_WAY))
            newValues.put(RemigesContract.Jumps.JUMP_WAY, extras.getInt(RemigesContract.Jumps.JUMP_WAY));
        if (extras.containsKey(RemigesContract.Jumps.PLACE_ID))
            newValues.put(RemigesContract.Jumps.PLACE_ID, extras.getLong(RemigesContract.Jumps.PLACE_ID));
        if (extras.containsKey(RemigesContract.Jumps.JUMPTYPE_ID))
            newValues.put(RemigesContract.Jumps.JUMPTYPE_ID, extras.getLong(RemigesContract.Jumps.JUMPTYPE_ID));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE))
            newValues.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, extras.getInt(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE))
            newValues.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, extras.getInt(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE));
        if (extras.containsKey(RemigesContract.Jumps.JUMP_DELAY))
            newValues.put(RemigesContract.Jumps.JUMP_DELAY, extras.getInt(RemigesContract.Jumps.JUMP_DELAY));
        return newValues;
    }

    private void loadJump() {
        Cursor cursor = mJumpCursor;
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(RemigesContract.Jumps.JUMP_NUMBER, cursor.getInt(JumpQuery.NUMBER));
            values.put(RemigesContract.Jumps.JUMP_DATE, cursor.getLong(JumpQuery.DATE));
            values.put(RemigesContract.Jumps.PLACE_ID, cursor.getLong(JumpQuery.PLACE));
            values.put(RemigesContract.Jumps.JUMP_WAY, cursor.getInt(JumpQuery.WAY));
            values.put(RemigesContract.Jumps.JUMPTYPE_ID, cursor.getLong(JumpQuery.TYPE));
            values.put(RemigesContract.Jumps.JUMP_EXIT_ALTITUDE, cursor.getInt(JumpQuery.EXIT_ALTITUDE));
            values.put(RemigesContract.Jumps.JUMP_DEPLOYMENT_ALTITUDE, cursor.getInt(JumpQuery.DEPLOYMENT_ALTITUDE));
            values.put(RemigesContract.Jumps.JUMP_DELAY, cursor.getInt(JumpQuery.DELAY));
            values.put(RemigesContract.Jumps.JUMP_DESCRIPTION, cursor.getString(JumpQuery.DESCRIPTION));
            setViewValues(values);
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

    private void insertJump() {
        FragmentActivity activity = getActivity();
        Uri jumpUri = activity.getContentResolver().insert(RemigesContract.Jumps.CONTENT_URI, getViewValues());
        if (jumpUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(jumpUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
    }

    private void updateJump() {
        FragmentActivity activity = getActivity();
        if (activity.getContentResolver().update(mJumpUri, getViewValues(), null, null) > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mJumpUri);
            activity.setResult(FragmentActivity.RESULT_OK, intent);
        } else {
            activity.setResult(FragmentActivity.RESULT_CANCELED);
        }
        activity.finish();
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
        if (id != spinner.getSelectedItemId()) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (id == ((Cursor) spinner.getItemAtPosition(i)).getLong(column)) {
                    spinner.setSelection(i);
                    break;
                }
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

        @NonNull
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
