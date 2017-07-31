package com.rkndika.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.rkndika.popularmovies.R;
import com.rkndika.popularmovies.model.Trailer;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private ArrayList<Trailer> trailers;
    private Context context;

    private final TrailerAdapterOnClickHandler mClickHandler;

    public interface TrailerAdapterOnClickHandler {
        void onItemClick(Trailer trailerClicked);
    }

    public TrailerAdapter(TrailerAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTrailerTitle;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            mTrailerTitle = (TextView) view.findViewById(R.id.tv_trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Trailer trailerClicked = trailers.get(adapterPosition);
            mClickHandler.onItemClick(trailerClicked);
        }
    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder trailerAdapterViewHolder, int position) {
        Trailer trailer = trailers.get(position);
        trailerAdapterViewHolder.mTrailerTitle.setText(trailer.getName());
    }

    @Override
    public int getItemCount() {
        if (null == trailers) return 0;
        return trailers.size();
    }

    public void setTrailersData(ArrayList<Trailer> trailersData) {
        trailers = trailersData;
        notifyDataSetChanged();
    }

    public ArrayList<Trailer> getTrailersData() {
        return trailers;
    }
}