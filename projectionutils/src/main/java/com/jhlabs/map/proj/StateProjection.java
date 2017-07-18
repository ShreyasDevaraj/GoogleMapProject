package com.jhlabs.map.proj;

/**
 * Created by sandeeppenchala on 4/4/17.
 */

public enum StateProjection {
    ILLINOIS_STATE_PLANE("nad83:1201");
    private final String stateProjection;
    private StateProjection(String projection) {
        stateProjection = projection;
    }
    public String getStateProjection() {
        return stateProjection;
    }
}
