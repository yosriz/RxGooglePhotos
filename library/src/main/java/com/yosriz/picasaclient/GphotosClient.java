package com.yosriz.picasaclient;

import com.yosriz.picasaclient.model.AlbumEntry;
import com.yosriz.picasaclient.model.AlbumFeed;
import com.yosriz.picasaclient.model.UserFeed;

import rx.Single;
import rx.functions.Func1;

public class GphotosClient {

    public Single<AlbumEntry> getGphotosAlbum() {
        return com.yosriz.picasaclient.PicasaClient.get().getUserFeed()
                .map(new Func1<UserFeed, AlbumEntry>() {
                    @Override
                    public AlbumEntry call(UserFeed userFeed) {
                        AlbumEntry  album = null;
                        for (AlbumEntry entry : userFeed.getAlbumEntries()) {
                            if (AlbumEntry.TYPE_GOOGLE_PHOTOS.equals(entry.getGphotoAlbumType())) {
                                album = entry;
                                break;
                            }
                        }
                        return album;
                    }
                });
    }

    public Single<AlbumFeed> getGphotosAlbumFeed() {
        return getGphotosAlbum().flatMap(new Func1<AlbumEntry, Single<AlbumFeed>>() {
            @Override
            public Single<AlbumFeed> call(AlbumEntry albumEntry) {
                return com.yosriz.picasaclient.PicasaClient.get()
                        .getAlbumFeed(albumEntry.getGphotoId());
            }
        });
    }

    public Single<AlbumFeed> getGphotosAlbumFeed(final int startIndex, final int maxResults) {
        return getGphotosAlbum().flatMap(new Func1<AlbumEntry, Single<AlbumFeed>>() {
            @Override
            public Single<AlbumFeed> call(AlbumEntry albumEntry) {
                return com.yosriz.picasaclient.PicasaClient.get()
                        .getAlbumFeed(albumEntry.getGphotoId(), startIndex, maxResults);
            }
        });
    }
}
