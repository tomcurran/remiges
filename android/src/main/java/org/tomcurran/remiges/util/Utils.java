package org.tomcurran.remiges.util;


import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static String readAsset(Context context, String fileName) {
        String asset = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            asset = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return asset;
    }

}
