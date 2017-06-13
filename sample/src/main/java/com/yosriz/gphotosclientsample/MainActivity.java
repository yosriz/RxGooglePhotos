package com.yosriz.gphotosclientsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yosriz.gphotosclient.GooglePhotosClient;
import com.yosriz.gphotosclient.GooglePhotosService;
import com.yosriz.gphotosclient.model.ExifTags;
import com.yosriz.gphotosclient.model.PhotoEntry;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Sample MainActivity
 * <p/>
 * Only a demo of the library - not guaranteed to be complete, stateful, nor structured well.
 */
public class MainActivity extends AppCompatActivity {


    private boolean albumMode;
    private long mAlbumId;

    private GooglePhotosAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private TextView mAccountText;
    private GooglePhotosClient googlePhotosClient;
    private GooglePhotosService googlePhotosService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googlePhotosClient = new GooglePhotosClient(this);

        albumMode = true;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new GooglePhotosAdapter(this, true);
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(this::reload);

        Button choose = (Button) findViewById(R.id.choose_account);
        choose.setOnClickListener(v -> googlePhotosClient.createGooglePhotosService()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(googlePhotosService -> {
                    this.googlePhotosService = googlePhotosService;
                    reload();
                }, throwable ->
                        new MaterialDialog.Builder(MainActivity.this)
                                .content("Error getting access.\n" + throwable.getMessage())
                                .positiveText(android.R.string.ok)
                                .show())
        );

        mAccountText = (TextView) findViewById(R.id.account);
        mAdapter.setItemClickListener(
                new GooglePhotosAdapter.ClickListener() {
                    @Override
                    public void photoClicked(PhotoEntry photo) {
                        ExifTags exifTags = photo.getExifTags();

                        String dateTime = DateUtils.formatDateTime(MainActivity.this, photo.getGphotoTimestamp(),
                                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
                        String camera = exifTags.getExifMake() + " " + exifTags.getExifModel();

                        new MaterialDialog.Builder(MainActivity.this)
                                .content("Time: " + dateTime
                                        + "\n" + "Camera: " + camera
                                        + "\n" + "ISO: " + exifTags.getExifIso()
                                        + "\n" + "F-Stop: " + exifTags.getExifFstop()
                                        + "\n" + "Exposure: 1/" + (int) (1 / exifTags.getExifExposure() + 0.5) + "s"
                                        + "\n" + "Focal Length: " + exifTags.getExifFocalLength() + "mm"
                                        + "\n" + "Distance: " + exifTags.getExifDistance())
                                .positiveText(android.R.string.ok)
                                .show();
                    }

                    @Override
                    public void albumClicked(long albumId) {
                        albumMode = false;
                        mAlbumId = albumId;
                        reload();
                    }
                }
        );
    }

    private void reload() {
        mAdapter.clear();
        mAdapter.setAlbumMode(albumMode);

        mRefreshLayout.setRefreshing(true);
        if (albumMode) {
            googlePhotosService.getUserFeed()
                    .retry(2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userFeed -> {
                                mAdapter.setAlbumEntries(userFeed.getAlbumEntries());
                                onReloadFinished();
                            },
                            MainActivity.this::onError
                    );
        } else {
            googlePhotosService.getAlbumFeed(mAlbumId)
                    .retry(2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(albumFeed -> {
                                mAdapter.setPhotoEntries(albumFeed.getPhotoEntries());
                                onReloadFinished();
                            },
                            MainActivity.this::onError
                    );
        }
    }

    private void onReloadFinished() {
        mRefreshLayout.setRefreshing(false);
    }

    private void onError(Throwable error) {
        error.printStackTrace();
        mRefreshLayout.setRefreshing(false);
        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        googlePhotosClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!albumMode) {
            albumMode = true;
            reload();
        } else {
            super.onBackPressed();
        }
    }

}
