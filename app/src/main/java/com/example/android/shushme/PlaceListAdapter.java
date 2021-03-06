package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private List<LocationObj> mPlaces;
    private int mPosition;
    // private onLongItemClickListener mOnLongItemClickListener;
    private onItemClickListener mOnItemClickListener;
    private static final String TAG = PlaceListAdapter.class.getSimpleName();

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    PlaceListAdapter(Context context, List<LocationObj> places) {
        this.mContext = context;
        this.mPlaces = places;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new PlaceViewHolder that holds a View with the item_place_card layout
     */
    @Override
    public @NonNull PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    /**
     * Binds the data from a particular position in the cursor to the corresponding view holder
     *
     * @param holder   The PlaceViewHolder instance corresponding to the required position
     * @param position The current position that needs to be loaded with data
     */
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        String placeName = mPlaces.get(position).getName();
        String placeAddress = mPlaces.get(position).getAddress();
        if (!mPlaces.get(position).getEnabled()) {
            holder.placeAccent.setImageResource(R.drawable.ic_place_disabled_24dp);
        }
        holder.nameTextView.setText(placeName);
        holder.addressTextView.setText(placeAddress);
        holder.radiusTextView.setText(String.format("%.1f", mPlaces.get(position).getRadius()));
        holder.itemView.setOnClickListener((View v) -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.ItemClicked(v, position);
                }
                return;
        });
    }

    @UiThread
    void swapPlaces(List<LocationObj> newPlaces){
        boolean needToRefresh = (newPlaces == null && mPlaces.size() > 0);
        mPlaces = newPlaces;
        if (mPlaces != null || needToRefresh) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
            ///this.notifyItemRangeChanged(0, mPlaces.size());
        }
    }

    /**
     * Returns the number of items in the cursor
     *
     * @return Number of items in the cursor, or 0 if null
     */
    @Override
    public int getItemCount() {
        if(mPlaces==null) {
            return 0;
        }
        return mPlaces.size();
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public LocationObj getItem(int position) {
        return mPlaces.get(position);
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public List<LocationObj> getPlaces() {
        return mPlaces;
    }



    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder
            /* implements View.OnCreateContextMenuListener */ {

        TextView nameTextView;
        TextView addressTextView;
        TextView radiusTextView;
        ImageView placeAccent;

        PlaceViewHolder(View itemView) {
            super(itemView);
            placeAccent = itemView.findViewById(R.id.place_accent);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);
            radiusTextView = itemView.findViewById(R.id.radius_text_view);
        }
    }

    public interface onItemClickListener {
        void ItemClicked(View v, int position);
    }
}
