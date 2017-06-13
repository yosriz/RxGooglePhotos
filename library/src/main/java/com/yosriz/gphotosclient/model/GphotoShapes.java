package com.yosriz.gphotosclient.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GphotoShapes {

    @SerializedName("faces")
    @Expose
    private String faces;


    public String getFaces() {
        return faces;
    }


    public void setFaces(String faces) {
        this.faces = faces;
    }

}
