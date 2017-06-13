package com.yosriz.picasaclient;


import com.yosriz.picasaclient.model.AlbumFeedResponse;
import com.yosriz.picasaclient.model.UserFeedResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PicasaApi {

    @GET("default")
    Single<UserFeedResponse> getUserFeedResponse();

    @GET("default")
    Single<UserFeedResponse> getUserFeedResponse(@Query("start-index") int startIndex,
                                                 @Query("max-results") int maxResults);

    @GET("default/albumid/{albumId}")
    Single<AlbumFeedResponse> getAlbumFeedResponse(@Path("albumId") long albumId);

    @GET("default/albumid/{albumId}")
    Single<AlbumFeedResponse> getAlbumFeedResponse(
            @Path("albumId") long albumId,
            @Query("start-index") int startIndex,
            @Query("max-results") int maxResults);
}
