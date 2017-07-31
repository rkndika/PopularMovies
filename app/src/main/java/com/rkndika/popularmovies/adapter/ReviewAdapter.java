package com.rkndika.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rkndika.popularmovies.model.Review;

import java.util.ArrayList;

import com.rkndika.popularmovies.R;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private ArrayList<Review> reviews;
    private Context context;

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mReviewContent, mReviewAuthor;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mReviewContent = (TextView) view.findViewById(R.id.tv_review_content);
            mReviewAuthor = (TextView) view.findViewById(R.id.tv_review_author);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mReviewContent.getVisibility() == View.GONE){
                mReviewContent.setVisibility(View.VISIBLE);
            }
            else {
                mReviewContent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_review;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder reviewAdapterViewHolder, int position) {
        Review review = reviews.get(position);
        reviewAdapterViewHolder.mReviewContent.setText(review.getContent());
        reviewAdapterViewHolder.mReviewAuthor.setText(context.getString(R.string.reviewed_by) + " " + review.getAuthor());
    }

    @Override
    public int getItemCount() {
        if (null == reviews) return 0;
        return reviews.size();
    }

    public void setReviewsData(ArrayList<Review> reviewsData) {
        reviews = reviewsData;
        notifyDataSetChanged();
    }

    public ArrayList<Review> getReviewsData() {
        return reviews;
    }
}