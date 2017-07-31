package com.rkndika.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rkndika.popularmovies.R;
import com.rkndika.popularmovies.data.FavoriteMovieContract;
import com.rkndika.popularmovies.model.Movie;
import com.rkndika.popularmovies.network.Config;
import com.squareup.picasso.Picasso;


public class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.MovieViewHolder> {

    // Class variables for the Cursor that holds movie data and the Context
    private Cursor mCursor;
    private Context mContext;

    private final CustomCursorAdapterOnClickHandler mClickHandler;

    public interface CustomCursorAdapterOnClickHandler {
        void onItemClick(Movie movieClicked);
    }

    public CustomCursorAdapter(CustomCursorAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }


    /**
     * Constructor for the CustomCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    /*public CustomCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }*/


    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new MovieViewHolder that holds the view for each movie
     */
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        // Inflate the item_movie to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_movie, parent, false);

        return new MovieViewHolder(view);
    }


    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry._ID);
        int posterPathIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH);

        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        String posterPath = mCursor.getString(posterPathIndex);

        //Set values
        holder.itemView.setTag(id);

        Picasso.with(mContext)
                .load(Config.IMAGE_URL + posterPath)
                .placeholder(R.drawable.rec_grey)
                .error(R.drawable.rec_grey)
                .into(holder.poster);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    // Inner class for creating ViewHolders
    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the movie description and priority TextViews
        ImageView poster;

        /**
         * Constructor for the MovieViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public MovieViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.iv_poster_item);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            int voteCountIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_COUNT);
            int idMovieIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ID);
            int videoIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VIDEO);
            int voteAverageIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE);
            int titleIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE);
            int popularityIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POPULARITY);
            int posterPathIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH);
            int originalLanguageIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ORIGINAL_LANGUAGE);
            int originalTitleIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ORIGINAL_TITLE);
            int backdropPathIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_BACKDROP_PATH);
            int adultIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ADULT);
            int overviewIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW);
            int releaseDateIndex = mCursor.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE);

            mCursor.moveToPosition(adapterPosition); // get to the right location in the cursor

            Movie movie = new Movie(mCursor.getInt(voteCountIndex),
                    mCursor.getInt(idMovieIndex),
                    mCursor.getInt(videoIndex)>0,
                    mCursor.getDouble(voteAverageIndex),
                    mCursor.getString(titleIndex),
                    mCursor.getDouble(popularityIndex),
                    mCursor.getString(posterPathIndex),
                    mCursor.getString(originalLanguageIndex),
                    mCursor.getString(originalTitleIndex),
                    mCursor.getString(backdropPathIndex),
                    mCursor.getInt(adultIndex)>0,
                    mCursor.getString(overviewIndex),
                    mCursor.getString(releaseDateIndex)
            );


            mClickHandler.onItemClick(movie);
        }
    }
}