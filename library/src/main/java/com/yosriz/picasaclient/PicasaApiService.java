package com.yosriz.picasaclient;

import com.yosriz.picasaclient.model.AlbumEntry;
import com.yosriz.picasaclient.model.AlbumFeed;
import com.yosriz.picasaclient.model.AlbumFeedResponse;
import com.yosriz.picasaclient.model.UserFeed;
import com.yosriz.picasaclient.model.UserFeedResponse;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class PicasaApiService {

    private final PicasaApi picasaApi;

    PicasaApiService(PicasaApi picasaApi) {
        this.picasaApi = picasaApi;
    }

    public Single<UserFeed> getUserFeed() {
        return picasaApi.getUserFeedResponse()
                .map(UserFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    public Single<AlbumFeed> getAlbumFeed(long albumId) {
        return picasaApi.getAlbumFeedResponse(albumId)
                .map(AlbumFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());

    }

    public Single<AlbumFeed> getAlbumFeed(long albumId, int startIndex, int maxResults) {
        if (startIndex < 1) {
            throw new IllegalArgumentException("Illegal start index, must be above 0");
        }
        return picasaApi.getAlbumFeedResponse(albumId, startIndex, maxResults)
                .map(AlbumFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    public Single<AlbumEntry> getGooglePhotosAlbum() {
        return getUserFeed()
                .map(userFeed -> {
                    AlbumEntry album = null;
                    for (AlbumEntry entry : userFeed.getAlbumEntries()) {
                        if (AlbumEntry.TYPE_GOOGLE_PHOTOS.equals(entry.getGphotoAlbumType())) {
                            album = entry;
                            break;
                        }
                    }
                    return album;
                });
    }

    public Single<AlbumFeed> getGooglePhotosAlbumFeed() {
        return getGooglePhotosAlbum()
                .flatMap(albumEntry -> getAlbumFeed(albumEntry.getGphotoId()));
    }

    public Single<AlbumFeed> getGooglePhotosAlbumFeed(final int startIndex, final int maxResults) {
        return getGooglePhotosAlbum()
                .flatMap(albumEntry -> getAlbumFeed(albumEntry.getGphotoId(), startIndex, maxResults));
    }
}
