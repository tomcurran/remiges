package org.tomcurran.remiges.util;


import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.File;

public class UIUtils {

    public final static int DEFAULT_PARSE_TEXTVIEW_INT = 0;

    public static int parseTextViewInt(TextView view) {
        return parseTextViewInt(view, DEFAULT_PARSE_TEXTVIEW_INT);
    }

    public static int parseTextViewInt(TextView view, int alt) {
        String text = view.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return alt;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return alt;
        }
    }

    public static void setTextViewInt(TextView view, int value) {
        if (value != 0) {
            view.setText(String.valueOf(value));
        } else {
            view.setText("");
        }
    }

    private static final String FONTS = "fonts";

    public static final String FONT_ROBOTO_THIN = "Roboto-Thin.ttf";

    public static Typeface loadFont(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), FONTS + File.separator + font);
    }

}
