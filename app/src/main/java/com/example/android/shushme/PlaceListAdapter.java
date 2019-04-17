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
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private List<Place> mPlaces;
    private int mPosition;
    // private onLongItemClickListener mOnLongItemClickListener;
    private onItemClickListener mOnItemClickListener;
    private static final String TAG = PlaceListAdapter.class.getSimpleName();

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    PlaceListAdapter(Context context, List<Place> places) {
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
        Log.i(TAG, "onBindViewHolder Name: " + placeName + " address: " + placeAddress);
        holder.nameTextView.setText(placeName);
        holder.addressTextView.setText(placeAddress);
        holder.radiusTextView.setText("100");
/*
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.ItemLongClicked(v, position);
                }
                return true;
            }
        });
*/
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.ItemClicked(v, position);
                }
                return;
            }
        });
    }

    @UiThread
    void swapPlaces(List<Place> newPlaces){
        boolean needToRefresh = (newPlaces == null && mPlaces.size() > 0);
        mPlaces = newPlaces;
        if (mPlaces != null || needToRefresh) {
            Log.i(TAG, "in swapPlaces, mPlaces not null or needToRefresh" );
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

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }



    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder
            /* implements View.OnCreateContextMenuListener */ {

        TextView nameTextView;
        TextView addressTextView;
        TextView radiusTextView;

        PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);
            radiusTextView = itemView.findViewById(R.id.radius_text_view);
            // itemView.setOnCreateContextMenuListener(this);
        }

/*
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select The Action");
            menu.add(Menu.NONE, R.id.new_radius, Menu.NONE, "fence radius");
            menu.add(Menu.NONE, R.id.update_rate, Menu.NONE, "update rate in seconds");
            menu.add(Menu.NONE, R.id.delete, Menu.NONE, "Delete this location");
        }
*/

    }

    public interface onItemClickListener {
        void ItemClicked(View v, int position);
    }
}
