package com.pluscubed.picasaclientsample;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.pluscubed.picasaclient.PicasaApiService;
import com.pluscubed.picasaclient.PicasaClient;
import com.pluscubed.picasaclient.PicasaClient2;
import com.pluscubed.picasaclient.model.AlbumEntry;
import com.pluscubed.picasaclient.model.AlbumFeed;
import com.pluscubed.picasaclient.model.ExifTags;
import com.pluscubed.picasaclient.model.PhotoEntry;
import com.pluscubed.picasaclient.model.UserFeed;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.CompletableSubscriber;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Sample MainActivity
 * <p/>
 * Only a demo of the library - not guaranteed to be complete, stateful, nor structured well.
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_ACCOUNT = "account";
    private static final String PREF_ACCOUNT = "pref_account";

    private List<AlbumEntry> mAlbumEntries;
    private List<PhotoEntry> mPhotoEntries;

    private boolean mAlbumMode;
    private long mAlbumId;

    private boolean mReloading;

    private PicasaAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private Account mAccount;
    private TextView mAccountText;
    private PicasaClient2 picasaClient2;
    private PicasaApiService picasaService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        picasaClient2 = new PicasaClient2(this);

        mAlbumMode = true;

        mAlbumEntries = new ArrayList<>();
        mPhotoEntries = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new PicasaAdapter();
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(() -> {
            if (PicasaClient.get().isInitialized()) {
                reload(true);
            }
        });

        Button choose = (Button) findViewById(R.id.choose_account);
        choose.setOnClickListener(v -> picasaClient2.createPicasaApiService()
                .subscribe(picasaApiService -> {
                    picasaService = picasaApiService;
                    reload(false);
                })
        );

        mAccountText = (TextView) findViewById(R.id.account);
    }


    private void reload(boolean force) {
        mReloading = true;
        mAdapter.notifyDataSetChanged();

        mRefreshLayout.setRefreshing(true);
        if (mAlbumMode) {
            if (force || mAlbumEntries.isEmpty()) {
                picasaService.getUserFeed()
                        .toObservable()
                        .retry(5)
                        .toSingle()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userFeed -> {
                                    mAlbumEntries = userFeed.getAlbumEntries();
                                    onReloadFinished();
                                },
                                MainActivity.this::onError
                        );
            } else {
                onReloadFinished();
            }
        } else {
            picasaService.getAlbumFeed(mAlbumId)
                    .toObservable()
                    .retry(5)
                    .toSingle()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(albumFeed -> {
                                mPhotoEntries = albumFeed.getPhotoEntries();
                                onReloadFinished();
                            },
                            MainActivity.this::onError
                    );
        }
    }

    private void onReloadFinished() {
        mReloading = false;
        mAdapter.notifyDataSetChanged();
        mRefreshLayout.setRefreshing(false);
    }

    private void onError(Throwable error) {
        mReloading = false;
        error.printStackTrace();
        mRefreshLayout.setRefreshing(false);
        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        picasaClient2.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        picasaClient2.detach();
    }

    @Override
    public void onBackPressed() {
        if (!mAlbumMode) {
            mAlbumMode = true;
            reload(false);
        } else {
            super.onBackPressed();
        }
    }

    private class PicasaAdapter extends RecyclerView.Adapter<PicasaAdapter.ViewHolder> {

        public PicasaAdapter() {
            super();
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.list_item_entry, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mAlbumMode) {

                AlbumEntry albumEntry = mAlbumEntries.get(position);

                Glide.with(MainActivity.this)
                        .load(albumEntry.getMediaGroup().getContents().get(0).getUrl())
                        .into(holder.image);

                holder.title.setText(albumEntry.getTitle());

                String gphotoAlbumType = albumEntry.getGphotoAlbumType();
                if (gphotoAlbumType != null) {
                    switch (gphotoAlbumType) {
                        case AlbumEntry.TYPE_GOOGLE_PHOTOS:
                            gphotoAlbumType = getString(R.string.google_photos);
                            break;
                        case AlbumEntry.TYPE_GOOGLE_PLUS:
                            gphotoAlbumType = getString(R.string.google_plus);
                            break;
                    }
                }
                holder.subtitle.setText(gphotoAlbumType);
            } else {
                PhotoEntry photoEntry = mPhotoEntries.get(position);

                Glide.with(MainActivity.this)
                        .load(photoEntry.getMediaGroup().getContents().get(0).getUrl())
                        .into(holder.image);

                holder.title.setText(photoEntry.getTitle());
                String text = photoEntry.getGphotoWidth() + "x" + photoEntry.getGphotoHeight();
                holder.subtitle.setText(text);
            }
        }

        @Override
        public int getItemCount() {
            if (mReloading) return 0;
            else return mAlbumMode ? mAlbumEntries.size() : mPhotoEntries.size();
        }

        @Override
        public long getItemId(int position) {
            return mAlbumMode ? mAlbumEntries.get(position).getGphotoId() : mPhotoEntries.get(position).getGphotoId();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title;
            TextView subtitle;

            public ViewHolder(View itemView) {
                super(itemView);

                image = (ImageView) itemView.findViewById(R.id.image);
                title = (TextView) itemView.findViewById(R.id.title);
                subtitle = (TextView) itemView.findViewById(R.id.subtitle);

                itemView.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        if (mAlbumMode) {
                            mAlbumMode = false;
                            mAlbumId = mAlbumEntries.get(getAdapterPosition()).getGphotoId();
                            reload(false);
                        } else {
                            PhotoEntry entry = mPhotoEntries.get(getAdapterPosition());

                            ExifTags exifTags = entry.getExifTags();

                            String dateTime = DateUtils.formatDateTime(MainActivity.this, entry.getGphotoTimestamp(),
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
                    }
                });
            }
        }
    }
}
