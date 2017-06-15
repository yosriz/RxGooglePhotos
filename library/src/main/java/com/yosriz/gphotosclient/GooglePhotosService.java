package com.yosriz.gphotosclient;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.yosriz.gphotosclient.model.AlbumEntry;
import com.yosriz.gphotosclient.model.AlbumFeed;
import com.yosriz.gphotosclient.model.AlbumFeedResponse;
import com.yosriz.gphotosclient.model.UserFeed;
import com.yosriz.gphotosclient.model.UserFeedResponse;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class GooglePhotosService {

    private final PicasaApi picasaApi;
    private final GoogleSignInAccount account;

    GooglePhotosService(PicasaApi picasaApi,
                        GoogleSignInAccount account) {
        this.picasaApi = picasaApi;
        this.account = account;
    }

    public Single<UserFeed> getUserFeed() {
        return picasaApi.getUserFeedResponse()
                .map(UserFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    public Single<UserFeed> getUserFeed(int startIndex, int maxResults) {
        if (startIndex < 1) {
            throw new IllegalArgumentException("Illegal start index, must be above 0");
        }
        return picasaApi.getUserFeedResponse(startIndex, maxResults)
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

    public Single<AlbumEntry> getGooglePhotosInstantUploadAlbum() {
        return getUserFeed()
                .flattenAsObservable(UserFeed::getAlbumEntries)
                .filter(albumEntry -> AlbumEntry.TYPE_GOOGLE_PHOTOS_INSTANT_UPLOAD.equals(albumEntry.getGphotoAlbumType()))
                .firstOrError();
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }
}
