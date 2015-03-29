package org.tomcurran.remiges.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class GeoUtil {

    /**
     * Returns LatLngBounds with centre in the middle and diameter wide
     * @param centre centre of the LatLngBounds to be returned
     * @param diameter diameter in metres of the LatLngBounds to be returned
     * @return LatLngBounds with centre in the middle and diameter wide
     */
    public static LatLngBounds LatLngBoundary(LatLng centre, final double diameter) {
        final double HEADING_NORTH_EAST = 45;
        final double HEADING_SOUTH_WEST = 215;
        final double HEADING_DISTANCE = hypotenuse(diameter, diameter) / 2;
        return new LatLngBounds.Builder()
                .include(SphericalUtil.computeOffset(centre, HEADING_DISTANCE, HEADING_NORTH_EAST))
                .include(SphericalUtil.computeOffset(centre, HEADING_DISTANCE, HEADING_SOUTH_WEST))
                .build();
    }

    private static double hypotenuse(final double a, final double b) {
        return Math.sqrt((a * a) + (b * b));
    }
}
