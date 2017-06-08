package com.yosriz.picasaclientsample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yosriz.picasaclient.model.AlbumEntry;
import com.yosriz.picasaclient.model.PhotoEntry;

import java.util.ArrayList;
import java.util.List;

class PicasaAdapter extends RecyclerView.Adapter<PicasaAdapter.ViewHolder> {


    private final Context context;
    private boolean albumMode;
    private List<AlbumEntry> albumEntries = new ArrayList<>();
    private List<PhotoEntry> photoEntries = new ArrayList<>();
    private PicasaAdapaterClickListener clickListener;

    public interface PicasaAdapaterClickListener {

        void photoClicked(PhotoEntry photo);

        void albumClicked(long albumId);
    }

    public PicasaAdapter(Context context, boolean albumMode) {
        super();
        this.context = context;
        this.albumMode = albumMode;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_entry, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (albumMode) {

            AlbumEntry albumEntry = albumEntries.get(position);

            Glide.with(context)
                    .load(albumEntry.getMediaGroup().getContents().get(0).getUrl())
                    .into(holder.image);

            holder.title.setText(albumEntry.getTitle());

            String gphotoAlbumType = albumEntry.getGphotoAlbumType();
            if (gphotoAlbumType != null) {
                switch (gphotoAlbumType) {
                    case AlbumEntry.TYPE_GOOGLE_PHOTOS:
                        gphotoAlbumType = context.getString(R.string.google_photos);
                        break;
                    case AlbumEntry.TYPE_GOOGLE_PLUS:
                        gphotoAlbumType = context.getString(R.string.google_plus);
                        break;
                }
            }
            holder.subtitle.setText(gphotoAlbumType);
        } else {
            PhotoEntry photoEntry = photoEntries.get(position);

            Glide.with(context)
                    .load(photoEntry.getMediaGroup().getContents().get(0).getUrl())
                    .into(holder.image);

            holder.title.setText(photoEntry.getTitle());
            String text = photoEntry.getGphotoWidth() + "x" + photoEntry.getGphotoHeight();
            holder.subtitle.setText(text);
        }
    }

    @Override
    public int getItemCount() {
        return albumMode ? albumEntries.size() : photoEntries.size();
    }

    @Override
    public long getItemId(int position) {
        return albumMode ? albumEntries.get(position).getGphotoId() : photoEntries.get(position).getGphotoId();
    }

    public void setItemClickListener(PicasaAdapaterClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAlbumEntries(List<AlbumEntry> albumEntries) {
        this.albumEntries = albumEntries;
    }

    public void setAlbumMode(boolean albumMode) {
        this.albumMode = albumMode;
    }

    public void setPhotoEntries(List<PhotoEntry> photoEntries) {
        this.photoEntries = photoEntries;
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
                    if (albumMode) {
                        clickListener.albumClicked(albumEntries.get(getAdapterPosition()).getGphotoId());
                    } else {
                        PhotoEntry entry = photoEntries.get(getAdapterPosition());
                        clickListener.photoClicked(entry);
                    }
                }
            });
        }
    }
}
