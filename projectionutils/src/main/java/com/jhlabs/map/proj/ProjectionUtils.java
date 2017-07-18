package com.jhlabs.map.proj;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;


public class ProjectionUtils {

    private static final double FEET_TO_METER = .3048;
    private static Projection getProjection(Context context,String projection) {
        return ProjectionFactory.getNamedPROJ4CoordinateSystem(context,projection);
    }

    /**
     * Get LAT LNG based on state plane Coordinate
     *
     * @param xCoordinate
     * @param yCoordinate
     * @return
     */
    public static LatLng getLatLng(Context context, StateProjection projection, double xCoordinate, double yCoordinate) {
        xCoordinate *= FEET_TO_METER;
        yCoordinate *= FEET_TO_METER;
        Point2D statPlanePoint = new Point2D(xCoordinate, yCoordinate);
        Point2D origin = new Point2D(0, 0);
        getProjection(context,projection.getStateProjection()).inverseTransform(statPlanePoint, origin);
        return new LatLng(origin.getY(), origin.getX());
    }
}
