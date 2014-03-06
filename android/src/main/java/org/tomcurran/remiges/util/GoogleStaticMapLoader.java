package org.tomcurran.remiges.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCacheException;
import edu.mit.mobile.android.maps.GoogleStaticMapView;
import edu.mit.mobile.android.maps.OnMapUpdateListener;

import static org.tomcurran.remiges.util.LogUtils.LOGE;
import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class GoogleStaticMapLoader {
    private static final String TAG = makeLogTag(GoogleStaticMapLoader.class);

    private static final int CACHE_SIZE = 1024 * 1024;

    private ImageCache mCache;
    private GoogleStaticMapView mView;
    private OnMapUpdateListener mMapUpdateListener;

    static class StaticMap {
        Uri uri;
        int width;
        int height;
    }

    private class FetchStaticMapTask extends AsyncTask<StaticMap, Integer, Drawable> {

        protected Drawable doInBackground(StaticMap... maps) {
            Drawable drawable = null;
            try {
                drawable = mCache.getImage(maps[0].uri, maps[0].width, maps[0].height);
            } catch (IOException e) {
                LOGE(TAG, String.format("I/O error: %s", e.getMessage()));
            } catch (ImageCacheException e) {
                LOGE(TAG, String.format("Image cache error: %s", e.getMessage()));
            }
            return drawable;
        }

        protected void onPostExecute(Drawable result) {
            if (result != null && mView != null) {
                mView.setImageDrawable(result);
            }
        }

    }

    public GoogleStaticMapLoader(Context context) {
        mCache = ImageCache.getInstance(context);
        mCache.setCacheMaxSize(CACHE_SIZE);
        mMapUpdateListener = new OnMapUpdateListener() {
            @Override
            public void onMapUpdate(GoogleStaticMapView view, Uri mapUrl) {
                StaticMap staticMap = new StaticMap();
                staticMap.uri = mapUrl;
                staticMap.width = view.getWidth();
                staticMap.height = view.getHeight();
                new FetchStaticMapTask().execute(staticMap);
            }
        };
    }

    public void setView(GoogleStaticMapView view) {
        mView = view;
        mView.setOnMapUpdateListener(mMapUpdateListener);
    }

}
