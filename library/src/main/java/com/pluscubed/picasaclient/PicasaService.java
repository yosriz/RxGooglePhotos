package com.pluscubed.picasaclient;


import com.pluscubed.picasaclient.model.AlbumFeedResponse;
import com.pluscubed.picasaclient.model.UserFeedResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface PicasaService {

    @GET("default")
    Observable<UserFeedResponse> getUserFeedResponse();

    @GET("default/albumid/{albumId}")
    Observable<AlbumFeedResponse> getAlbumFeedResponse(@Path("albumId") long albumId);

    @GET("default/albumid/{albumId}")
    Observable<AlbumFeedResponse> getAlbumFeedResponse(
            @Path("albumId") long albumId,
            @Query("start-index") int startIndex,
            @Query("max-results") int maxResults);
}
