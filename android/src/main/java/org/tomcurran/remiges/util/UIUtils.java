package org.tomcurran.remiges.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.tomcurran.remiges.R;
import org.tomcurran.remiges.ui.widget.MaterialProgressDrawable;

import java.io.File;

/**
 * User interface utilities
 */
public class UIUtils {

    public final static int DEFAULT_PARSE_TEXTVIEW_INT = 0;

    /**
     * Parses and returns the integer value in view or returns DEFAULT_PARSE_TEXTVIEW_INT if view
     * is empty or an error occurs parsing
     *
     * @param view {@link android.widget.TextView} to parse
     * @return the integer value in view or DEFAULT_PARSE_TEXTVIEW_INT if view is empty or an
     * error occurs parsing
     */
    public static int parseTextViewInt(TextView view) {
        return parseTextViewInt(view, DEFAULT_PARSE_TEXTVIEW_INT);
    }

    /**
     * Parses and returns the integer value in view or returns alt if view is empty or an error
     * occurs parsing
     *
     * @param view {@link android.widget.TextView} to parse
     * @param alt  default value that will be returned if view is empty or an error occurs parsing
     * @return the integer value in view or alt if view is empty or an error occurs parsing
     */
    public static int parseTextViewInt(TextView view, int alt) {
        CharSequence text = view.getText();
        if (TextUtils.isEmpty(text)) {
            return alt;
        }
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {
            return alt;
        }
    }

    /**
     * Sets the text of view to value
     * @param view {@link android.widget.TextView}
     * @param value integer to set as view text
     */
    public static void setTextViewInt(TextView view, int value) {
        if (value != DEFAULT_PARSE_TEXTVIEW_INT) {
            view.setText(String.valueOf(value));
        } else {
            view.setText("");
        }
    }

    public final static double DEFAULT_PARSE_TEXTVIEW_DOUBLE = 0.0;

    /**
     * Parses and returns the double value in view or returns DEFAULT_PARSE_TEXTVIEW_INT if view
     * is empty or an error occurs parsing
     *
     * @param view {@link android.widget.TextView} to parse
     * @return the double value in view or DEFAULT_PARSE_TEXTVIEW_INT if view is empty or an
     * error occurs parsing
     */
    public static double parseTextViewDouble(TextView view) {
        return parseTextViewDouble(view, DEFAULT_PARSE_TEXTVIEW_DOUBLE);
    }

    /**
     * Parses and returns the double value in view or returns alt if view is empty or an error
     * occurs parsing
     *
     * @param view {@link android.widget.TextView} to parse
     * @param alt  default value that will be returned if view is empty or an error occurs parsing
     * @return the double value in view or alt if view is empty or an error occurs parsing
     */
    public static double parseTextViewDouble(TextView view, double alt) {
        CharSequence text = view.getText();
        if (TextUtils.isEmpty(text)) {
            return alt;
        }
        try {
            return Double.parseDouble(text.toString());
        } catch (NumberFormatException e) {
            return alt;
        }
    }

    /**
     * Sets the text of view to value
     * @param view {@link android.widget.TextView}
     * @param value double to set as view text
     */
    public static void setTextViewDouble(TextView view, double value) {
        if (value != DEFAULT_PARSE_TEXTVIEW_DOUBLE) {
            view.setText(String.valueOf(value));
        } else {
            view.setText("");
        }
    }

    // location of fonts
    private static final String FONTS = "fonts";

    /**
     * Roboto thin font
     */
    public static final String FONT_ROBOTO_THIN = "Roboto-Thin.ttf";

    /**
     * Loads a font asset
     *
     * @param context android {@link android.content.Context}
     * @param font asset file name
     * @return font {@link android.graphics.Typeface}
     */
    public static Typeface loadFont(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), FONTS + File.separator + font);
    }

    public final static String DEFAULT_PARSE_INT_PREFERENCE = "0";

    public static int parseIntPreference(SharedPreferences preferences, String key) {
        return parseIntPreference(preferences, key, DEFAULT_PARSE_INT_PREFERENCE);
    }

    public static int parseIntPreference(SharedPreferences preferences, String key, String defValue) {
        String pref = preferences.getString(key, defValue);
        return Integer.parseInt(TextUtils.isEmpty(pref) ? defValue : pref);
    }

    public static MaterialProgressDrawable getProgressDrawable(Context context, View parent) {
        MaterialProgressDrawable progress = new MaterialProgressDrawable(context, parent);
        progress.setAlpha(255);
        progress.setColorSchemeColors(context.getResources().getColor(R.color.primary));
        progress.updateSizes(MaterialProgressDrawable.LARGE);
        progress.start();
        return progress;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
