package org.tomcurran.remiges.util;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

// https://gist.github.com/keyboardr/5455206
public class FragmentUtils {
    /**
     * @param frag
     *            The Fragment whose parent is to be found
     * @param callbackInterface
     *            The interface class that the parent should implement
     * @return The parent of frag that implements the callbackInterface or null
     *         if no such parent can be found
     */
    @SuppressWarnings("unchecked") // Casts are checked using runtime methods
    public static <T> T getParent(Fragment frag, Class<T> callbackInterface) {
        Fragment parentFragment = frag.getParentFragment();
        if (parentFragment != null
                && callbackInterface.isInstance(parentFragment)) {
            return (T) parentFragment;
        } else {
            FragmentActivity activity = frag.getActivity();
            if (activity != null && callbackInterface.isInstance(activity)) {
                return (T) activity;
            }
        }
        return null;
    }
}
