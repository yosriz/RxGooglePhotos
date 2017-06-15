package com.yosriz.gphotosclientsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yosriz.gphotosclient.GooglePhotosClient;
import com.yosriz.gphotosclient.GooglePhotosService;
import com.yosriz.gphotosclient.model.ExifTags;
import com.yosriz.gphotosclient.model.PhotoEntry;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Sample MainActivity
 * <p/>
 * Only a demo of the library - not guaranteed to be complete, stateful, nor structured well.
 */
public class MainActivity extends AppCompatActivity {


    private boolean albumMode;
    private long mAlbumId;

    private SwipeRefreshLayout refreshLayout;
    private TextView textViewAccount;
    private Button btnChoose;
    private GooglePhotosAdapter adapter;

    private GooglePhotosClient googlePhotosClient;
    private GooglePhotosService photosService;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();

        googlePhotosClient = new GooglePhotosClient();
        albumMode = true;

        btnChoose.setOnClickListener(this::btnChooseClick);
        adapter.setItemClickListener(adapterClickListener);
    }

    private void btnChooseClick(View v) {
        Disposable subscribe = googlePhotosClient.createGooglePhotosService(MainActivity.this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(photosService -> {
                    this.photosService = photosService;
                    String accountInfo = String.format("%s\n%s", photosService.getAccount().getDisplayName()
                            , photosService.getAccount().getEmail());
                    textViewAccount.setText(accountInfo);
                    reload();
                }, throwable ->
                        new MaterialDialog.Builder(MainActivity.this)
                                .content("Error getting access.\n" + throwable.getMessage())
                                .positiveText(android.R.string.ok)
                                .show());
        disposables.add(subscribe);
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        adapter = new GooglePhotosAdapter(this, true);
        recyclerView.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        refreshLayout.setOnRefreshListener(this::reload);

        btnChoose = (Button) findViewById(R.id.choose_account);
        textViewAccount = (TextView) findViewById(R.id.account);
    }

    private GooglePhotosAdapter.ClickListener adapterClickListener = new GooglePhotosAdapter.ClickListener() {
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
    };

    private void reload() {
        adapter.clear();
        adapter.setAlbumMode(albumMode);

        refreshLayout.setRefreshing(true);
        if (albumMode) {
            Disposable subscribe = photosService.getUserFeed()
                    .retry(2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userFeed -> {
                                adapter.setAlbumEntries(userFeed.getAlbumEntries());
                                onReloadFinished();
                            },
                            MainActivity.this::onError
                    );
            disposables.add(subscribe);
        } else {
            Disposable subscribe = photosService.getAlbumFeed(mAlbumId)
                    .retry(2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(albumFeed -> {
                                adapter.setPhotoEntries(albumFeed.getPhotoEntries());
                                onReloadFinished();
                            },
                            MainActivity.this::onError
                    );
            disposables.add(subscribe);
        }
    }

    private void onReloadFinished() {
        refreshLayout.setRefreshing(false);
    }

    private void onError(Throwable error) {
        error.printStackTrace();
        refreshLayout.setRefreshing(false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposables.clear();
    }
}
