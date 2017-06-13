package com.yosriz.gphotosclient.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class MediaGroup {

    @SerializedName("media$credit")
    @Expose
    private List<SingleStringElement> media$credit = new ArrayList<>();
    @SerializedName("media$title")
    @Expose
    private MediaTitle mediaTitle;
    @SerializedName("media$thumbnail")
    @Expose
    private List<MediaThumbnail> mediaThumbnail = new ArrayList<>();
    @SerializedName("media$keywords")
    @Expose
    private Media$keywords media$keywords;
    @SerializedName("media$description")
    @Expose
    private MediaDescription mediaDescription;

    @SerializedName("media$content")
    @Expose
    private List<MediaContent> mediaContents = new ArrayList<>();

    /**
     * @return media contents
     */
    public List<MediaContent> getContents() {
        return mediaContents;
    }


    public List<SingleStringElement> getMedia$credit() {
        return media$credit;
    }


    public void setMedia$credit(List<SingleStringElement> media$credit) {
        this.media$credit = media$credit;
    }


    public MediaTitle getMediaTitle() {
        return mediaTitle;
    }


    public void setMediaTitle(MediaTitle mediaTitle) {
        this.mediaTitle = mediaTitle;
    }


    public void setMediaContents(List<MediaContent> mediaContents) {
        this.mediaContents = mediaContents;
    }


    public List<MediaThumbnail> getMediaThumbnail() {
        return mediaThumbnail;
    }


    public void setMediaThumbnail(List<MediaThumbnail> mediaThumbnail) {
        this.mediaThumbnail = mediaThumbnail;
    }


    public Media$keywords getMedia$keywords() {
        return media$keywords;
    }


    public void setMedia$keywords(Media$keywords media$keywords) {
        this.media$keywords = media$keywords;
    }


    public MediaDescription getMediaDescription() {
        return mediaDescription;
    }


    public void setMediaDescription(MediaDescription mediaDescription) {
        this.mediaDescription = mediaDescription;
    }

}
