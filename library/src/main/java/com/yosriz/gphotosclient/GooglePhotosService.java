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

    /**
     * Get user feed, feed contains all user various albums and photos collection.
     * <p>there's a limit of 1,000 albums per response, for getting paged result use {@link #getUserFeed(int, int)}.</p>
     *
     * @return {@link UserFeed} object.
     */
    public Single<UserFeed> getUserFeed() {
        return picasaApi.getUserFeedResponse()
                .map(UserFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get user feed, feed contains all user various albums and photos collection.
     *
     * @param startIndex start album index (first index is 1)
     * @param maxResults max results
     * @return {@link UserFeed} object.
     */
    public Single<UserFeed> getUserFeed(int startIndex, int maxResults) {
        if (startIndex < 1) {
            throw new IllegalArgumentException("Illegal start index, must be above 0");
        }
        return picasaApi.getUserFeedResponse(startIndex, maxResults)
                .map(UserFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get album feed by id. {@link AlbumFeed} photos can be retrieved using {@link AlbumFeed#getPhotoEntries()}.
     * <p>there's a limit of 1,000 photos per response, for getting paged result use {@link #getAlbumFeed(long, int, int)}.</p>
     *
     * @param albumId album id
     * @return {@link AlbumFeed}
     */
    public Single<AlbumFeed> getAlbumFeed(long albumId) {
        return picasaApi.getAlbumFeedResponse(albumId)
                .map(AlbumFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());

    }

    /**
     * Get album feed by id. {@link AlbumFeed} photos can be retrieved using {@link AlbumFeed#getPhotoEntries()}.
     *
     * @param albumId    album id
     * @param startIndex start photo index (first index is 1)
     * @param maxResults max results
     * @return {@link AlbumFeed}
     */
    public Single<AlbumFeed> getAlbumFeed(long albumId, int startIndex, int maxResults) {
        if (startIndex < 1) {
            throw new IllegalArgumentException("Illegal start index, must be above 0");
        }
        return picasaApi.getAlbumFeedResponse(albumId, startIndex, maxResults)
                .map(AlbumFeedResponse::getFeed)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Get Instant upload album, this album, contains all photos that are backed up automatically by Google Photos.
     *
     * @return {@link AlbumEntry} represents instant upload album
     */
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
