package org.tomcurran.remiges.util;


import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * General utilities
 */
public class Utils {

    /**
     * Reads asset as string.
     *
     * @param context android {@link android.content.Context}
     * @param fileName file name of the asset
     * @return asset as string, or null if any errors
     */
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
        }
        return asset;
    }

    /**
     * Returns {@link java.io.InputStream} as string
     *
     * @param is {@link java.io.InputStream}
     * @return {@link java.io.InputStream} as string
     * @throws IOException
     */
    public static String readFromInputStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}
