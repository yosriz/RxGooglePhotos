package com.pluscubed.picasaclient.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GeorssWhere {

    @SerializedName("gml$Point")
    @Expose
    private GmlPoint gmlPoint;


    public GmlPoint getGmlPoint() {
        return gmlPoint;
    }


    public void setGmlPoint(GmlPoint gmlPoint) {
        this.gmlPoint = gmlPoint;
    }

}
