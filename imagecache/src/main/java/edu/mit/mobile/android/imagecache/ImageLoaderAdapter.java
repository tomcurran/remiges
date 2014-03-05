package edu.mit.mobile.android.imagecache;

/*
 * Copyright (C) 2011-2013 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.IOException;
import java.lang.ref.SoftReference;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.commonsware.cwac.adapter.AdapterWrapper;

/**
 * <p>
 * An adapter that wraps another adapter, loading images into ImageViews asynchronously.
 * </p>
 *
 * <p>
 * To use, pass in a {@link android.widget.ListAdapter} that generates {@link android.widget.ImageView}s in the layout hierarchy
 * of getView(). ImageViews are searched for using the IDs specified in {@code imageViewIDs}. When
 * found, {@link android.widget.ImageView#getTag(R.id.ic__uri)} is called and should return a {@link android.net.Uri}
 * referencing a local or remote image. See {@link ImageCache#loadImage(int, android.net.Uri, int, int)} for
 * details on the types of URIs and images supported.
 * </p>
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class ImageLoaderAdapter extends AdapterWrapper implements ImageCache.OnImageLoadListener {
    private static final String TAG = ImageLoaderAdapter.class.getSimpleName();

    /**
     * The unit specified is in pixels
     */
    public static final int UNIT_PX = 0;

    /**
     * The unit specified is in density-independent pixels (DIP)
     */
    public static final int UNIT_DIP = 1;

    // //////////////////////////////////////////////
    // / private
    // //////////////////////////////////////////////

    private final SparseArray<SoftReference<ImageView>> mImageViewsToLoad = new SparseArray<SoftReference<ImageView>>();

    private final int[] mImageViewIDs;
    private final ImageCache mCache;

    private final int mDefaultWidth, mDefaultHeight;

    private final boolean mAutosize;

    private final SparseArray<ViewDimensionCache> mViewDimensionCache;

    // ///////////////////////////////////////////////

    /**
     * Like the
     * {@link #ImageLoaderAdapter(android.content.Context, android.widget.ListAdapter, ImageCache, int[], int, int, int, boolean)}
     * constructor with a default of {@code true} for autosize.
     *
     * @param context
     *            a context for getting the display density. You don't need to worry about this
     *            class holding on to a reference to this: it's only used in the constructor.
     * @param wrapped
     *            the adapter that's wrapped. See {@link edu.mit.mobile.android.imagecache.ImageLoaderAdapter} for the requirements of
     *            using this adapter wrapper.
     * @param cache
     *            an instance of your image cache. This can be shared with the process.
     * @param imageViewIDs
     *            a list of resource IDs matching the ImageViews that should be scanned and loaded.
     * @param defaultWidth
     *            the default maximum width, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param defaultHeight
     *            the default maximum height, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param unit
     *            one of {@link #UNIT_PX} or {@link #UNIT_DIP}
     */
    public ImageLoaderAdapter(Context context, ListAdapter wrapped, ImageCache cache,
            int[] imageViewIDs, int defaultWidth, int defaultHeight, int unit) {
        this(context, wrapped, cache, imageViewIDs, defaultWidth, defaultHeight, unit, true);
    }

    /**
     * @param context
     *            a context for getting the display density. You don't need to worry about this
     *            class holding on to a reference to this: it's only used in the constructor.
     * @param wrapped
     *            the adapter that's wrapped. See {@link edu.mit.mobile.android.imagecache.ImageLoaderAdapter} for the requirements of
     *            using this adapter wrapper.
     * @param cache
     *            an instance of your image cache. This can be shared with the process.
     * @param imageViewIDs
     *            a list of resource IDs matching the ImageViews that should be scanned and loaded.
     * @param defaultWidth
     *            the default maximum width, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param defaultHeight
     *            the default maximum height, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param unit
     *            one of {@link #UNIT_PX} or {@link #UNIT_DIP}
     * @param autosize
     *            if true, the view's dimensions will be cached the first time it's loaded and an
     *            image of the appropriate size will be requested the next time an image is loaded.
     *            False uses defaultWidth and defaultHeight only.
     */
    public ImageLoaderAdapter(Context context, ListAdapter wrapped, ImageCache cache,
            int[] imageViewIDs, int defaultWidth, int defaultHeight, int unit, boolean autosize) {
        super(wrapped);

        mImageViewIDs = imageViewIDs;
        mCache = cache;
        mCache.registerOnImageLoadListener(this);

        mAutosize = autosize;

        if (autosize) {
            mViewDimensionCache = new SparseArray<ViewDimensionCache>();
        } else {
            mViewDimensionCache = null;
        }

        switch (unit) {
            case UNIT_PX:
                mDefaultHeight = defaultHeight;
                mDefaultWidth = defaultWidth;
                break;

            case UNIT_DIP: {
                final float scale = context.getResources().getDisplayMetrics().density;
                mDefaultHeight = (int) (defaultHeight * scale);
                mDefaultWidth = (int) (defaultWidth * scale);
            }
                break;

            default:
                throw new IllegalArgumentException("invalid unit type");

        }
    }

    /**
     * Constructs a new adapter with a default unit of pixels.
     *
     * @param wrapped
     *            the adapter that's wrapped. See {@link edu.mit.mobile.android.imagecache.ImageLoaderAdapter} for the requirements of
     *            using this adapter wrapper.
     * @param cache
     *            an instance of your image cache. This can be shared with the process.
     * @param imageViewIDs
     *            a list of resource IDs matching the ImageViews that should be scan
     * @param width
     *            the maximum width, in pixels
     * @param height
     *            the maximum height, in pixels
     */
    public ImageLoaderAdapter(ListAdapter wrapped, ImageCache cache, int[] imageViewIDs, int width,
            int height) {
        this(null, wrapped, cache, imageViewIDs, width, height, UNIT_PX);
    }

    @Override
    protected void finalize() throws Throwable {
        unregisterOnImageLoadListener();
        super.finalize();
    }

    /**
     * This can be called from your {@link android.app.Activity#onResume()} method.
     */
    public void registerOnImageLoadListener() {
        mCache.registerOnImageLoadListener(this);
    }

    /**
     * This can be called from your {@link android.app.Activity#onPause()} method.
     */
    public void unregisterOnImageLoadListener() {
        mCache.unregisterOnImageLoadListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View v = super.getView(position, convertView, parent);

        for (final int id : mImageViewIDs) {
            if (convertView != null) {
                final ImageView iv = (ImageView) convertView.findViewById(id);
                if (iv != null) {
                    final Integer tagId = (Integer) iv.getTag(R.id.ic__load_id);
                    if (tagId != null) {
                        mCache.cancel(tagId);
                    }
                }
            }

            final ImageView iv = (ImageView) v.findViewById(id);
            if (iv == null) {
                continue;
            }

            final Uri tag = (Uri) iv.getTag(R.id.ic__uri);
            // short circuit if there's no tag
            if (tag == null) {
                continue;
            }

            ViewDimensionCache viewDimension = null;

            if (mAutosize) {
                viewDimension = mViewDimensionCache.get(id);
                if (viewDimension == null) {
                    final int w = iv.getMeasuredWidth();
                    final int h = iv.getMeasuredHeight();
                    if (w > 0 && h > 0) {
                        viewDimension = new ViewDimensionCache();
                        viewDimension.width = w;
                        viewDimension.height = h;
                        mViewDimensionCache.put(id, viewDimension);
                    }
                }
            }

            final int imageID = mCache.getNewID();

            // ic__load_id is used to keep track of what load ID is associated with what
            // particular ImageView

            iv.setTag(R.id.ic__load_id, imageID);
            // attempt to bypass all the loading machinery to get the image loaded as quickly
            // as possible
            Drawable d = null;
            try {
                if (viewDimension != null && viewDimension.width > 0 && viewDimension.height > 0) {
                    d = mCache.loadImage(imageID, tag, viewDimension.width, viewDimension.height);
                } else {
                    d = mCache.loadImage(imageID, tag, mDefaultWidth, mDefaultHeight);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            if (d != null) {
                iv.setImageDrawable(d);
            } else {
                if (ImageCache.DEBUG) {
                    Log.d(TAG, "scheduling load with ID: " + imageID + "; URI;" + tag);
                }
                mImageViewsToLoad.put(imageID, new SoftReference<ImageView>(iv));
            }

        }
        return v;
    }

    @Override
    public void onImageLoaded(int id, Uri imageUri, Drawable image) {
        final SoftReference<ImageView> ivRef = mImageViewsToLoad.get(id);
        if (ivRef == null) {
            return;
        }
        final ImageView iv = ivRef.get();
        if (iv == null) {
            mImageViewsToLoad.remove(id);
            return;
        }
        if (ImageCache.DEBUG) {
            Log.d(TAG, "loading ID " + id + " with an image");
        }
        if (imageUri.equals(iv.getTag(R.id.ic__uri))) {
            iv.setImageDrawable(image);
        }
        mImageViewsToLoad.remove(id);
    }

    private static class ViewDimensionCache {
        int width;
        int height;
    }
}
