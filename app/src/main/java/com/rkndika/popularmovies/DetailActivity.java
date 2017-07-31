package com.rkndika.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rkndika.popularmovies.adapter.ReviewAdapter;
import com.rkndika.popularmovies.adapter.TrailerAdapter;
import com.rkndika.popularmovies.model.Reviews;
import com.rkndika.popularmovies.model.Trailer;
import com.rkndika.popularmovies.model.Trailers;
import com.rkndika.popularmovies.network.Client;
import com.rkndika.popularmovies.network.Service;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rkndika.popularmovies.model.Movie;
import com.rkndika.popularmovies.network.Config;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerAdapterOnClickHandler {
    private final static String YOUTUBE_APP = "vnd.youtube:";
    private final static String YOUTUBE_WEB = "http://www.youtube.com/watch?v=";

    private Movie movie;

    private RecyclerView mRecyclerViewReview, mRecyclerViewTrailer;
    private TextView mErrorMessageReview, mErrorMessageTrailer;
    private TextView mNoDataMessageReview, mNoDataMessageTrailer;
    private ProgressBar mLoadingIndicatorReview, mLoadingIndicatorTrailer;
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // get movie detail from clicked list
        movie = getIntent().getExtras().getParcelable(MainActivity.PUT_EXTRA_MOVIES);

        // initialize view
        initViews();

        // load review data
        loadReviewsData();

        // load trailer data
        loadTrailerData();
    }

    private void initViews(){
        mRecyclerViewReview = (RecyclerView) findViewById(R.id.rv_reviews);
        mErrorMessageReview = (TextView) findViewById(R.id.tv_error_message_display);
        mNoDataMessageReview = (TextView) findViewById(R.id.tv_no_data_message_display);

        mRecyclerViewTrailer = (RecyclerView) findViewById(R.id.rv_trailers);
        mErrorMessageTrailer = (TextView) findViewById(R.id.tv_error_message_trailer);
        mNoDataMessageTrailer = (TextView) findViewById(R.id.tv_no_data_message_trailer);

        LinearLayoutManager layoutManagerReview = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        LinearLayoutManager layoutManagerTrailer = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);

        mReviewAdapter = new ReviewAdapter();
        mTrailerAdapter = new TrailerAdapter(this);

        mRecyclerViewReview.setLayoutManager(layoutManagerReview);
        mRecyclerViewReview.setNestedScrollingEnabled(false);
        mRecyclerViewReview.setHasFixedSize(true);
        mRecyclerViewReview.setAdapter(mReviewAdapter);

        mRecyclerViewTrailer.setLayoutManager(layoutManagerTrailer);
        mRecyclerViewTrailer.setNestedScrollingEnabled(false);
        mRecyclerViewTrailer.setHasFixedSize(true);
        mRecyclerViewTrailer.setAdapter(mTrailerAdapter);

        mLoadingIndicatorReview = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mLoadingIndicatorTrailer = (ProgressBar) findViewById(R.id.pb_loading_indicator_trailer);

        TextView tvTitle, tvReleaseDate, tvUserRating, tvSynopsis;
        ImageView ivPoster;

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        tvUserRating = (TextView) findViewById(R.id.tv_user_rating);
        tvSynopsis = (TextView) findViewById(R.id.tv_synopsis);
        ivPoster = (ImageView) findViewById(R.id.iv_poster);

        // set title from movie original title
        tvTitle.setText(movie.getOriginalTitle());
        // set rating from movie vote average
        tvUserRating.setText(String.valueOf(movie.getVoteAverage()));
        // set release date from movie release date
        tvReleaseDate.setText(parseDate(movie.getReleaseDate()));
        // set synopsis from movie overview
        tvSynopsis.setText(movie.getOverview());

        // set poster image from movie poster path
        Picasso.with(this)
                .load(Config.IMAGE_URL + movie.getPosterPath())
                .placeholder(R.drawable.rec_grey)
                .error(R.drawable.rec_grey)
                .into(ivPoster);
    }

    // method for parse date from "yyyy-MM-dd" format to "MMM d, yyyy" format
    private String parseDate(String date){
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat to = new SimpleDateFormat("MMM d, yyyy");

        // date not initialize
        if(date == null || date.isEmpty()){
            return getString(R.string.unknown_date);
        }

        try {
            // return new format
            return to.format(from.parse(movie.getReleaseDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            // unknown date format
            return getString(R.string.unknown_date);
        }
    }

    private void loadReviewsData() {
        showReviewsDataView();

        // show loading
        mLoadingIndicatorReview.setVisibility(View.VISIBLE);

        // check network status
        if(!isNetworkAvailable()){
            // disable loading
            mLoadingIndicatorReview.setVisibility(View.INVISIBLE);
            // show no connection message
            showErrorNoIntenetMessageReview();
            return;
        }

        try {

            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);

            // get The Movie DB API Key from api_keys.xml
            String apiKey = getString(R.string.THE_MOVIE_DB_API_KEY);

            Call<Reviews> call = apiService.getReviews(movie.getId(), apiKey);

            // get data asynchronously with Retrofit
            call.enqueue(new Callback<Reviews>() {
                @Override
                public void onResponse(Call<Reviews> call, Response<Reviews> response) {
                    // disable loading
                    mLoadingIndicatorReview.setVisibility(View.INVISIBLE);

                    // if no error when fetching data
                    if(response.isSuccessful()){
                        if(response.body().getTotalResults()>0){
                            // show recyclerview
                            showReviewsDataView();
                            // set data to adapter recyclerview
                            mReviewAdapter.setReviewsData(response.body().getReviews());
                        }
                        else {
                            // show no data message
                            showNoDataMessageReview();
                        }
                    }
                    // something error when fetching data
                    else {
                        Log.d("Error", "Response code : " + String.valueOf(response.code()));
                        // show error message
                        showErrorMessageReview();
                    }
                }

                // Retrofit faulire access API
                @Override
                public void onFailure(Call<Reviews> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    // disable loading
                    mLoadingIndicatorReview.setVisibility(View.INVISIBLE);
                    // show error message
                    showErrorMessageReview();
                }
            });
        }catch (Exception e){
            Log.d("Error", e.getMessage());
            // disable loading
            mLoadingIndicatorReview.setVisibility(View.INVISIBLE);
            // show error message
            showErrorMessageReview();
        }
    }

    private void loadTrailerData() {
        showTrailersDataView();

        // show loading
        mLoadingIndicatorTrailer.setVisibility(View.VISIBLE);

        // check network status
        if(!isNetworkAvailable()){
            // disable loading
            mLoadingIndicatorTrailer.setVisibility(View.INVISIBLE);
            // show no connection message
            showErrorNoIntenetMessageTrailer();
            return;
        }

        try {

            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);

            // get The Movie DB API Key from api_keys.xml
            String apiKey = getString(R.string.THE_MOVIE_DB_API_KEY);

            Call<Trailers> call = apiService.getTrailers(movie.getId(), apiKey);

            // get data asynchronously with Retrofit
            call.enqueue(new Callback<Trailers>() {
                @Override
                public void onResponse(Call<Trailers> call, Response<Trailers> response) {
                    // disable loading
                    mLoadingIndicatorTrailer.setVisibility(View.INVISIBLE);

                    // if no error when fetching data
                    if(response.isSuccessful()){
                        if(response.body().getTrailers().size()>0){
                            // show recyclerview
                            showTrailersDataView();
                            // set data to adapter recyclerview
                            mTrailerAdapter.setTrailersData(response.body().getTrailers());
                        }
                    }
                    // something error when fetching data
                    else {
                        Log.d("Error", "Response code : " + String.valueOf(response.code()));
                        // show error message
                        showErrorMessageTrailer();
                    }
                }

                // Retrofit faulire access API
                @Override
                public void onFailure(Call<Trailers> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    // disable loading
                    mLoadingIndicatorTrailer.setVisibility(View.INVISIBLE);
                    // show error message
                    showErrorMessageTrailer();
                }
            });
        }catch (Exception e){
            Log.d("Error", e.getMessage());
            // disable loading
            mLoadingIndicatorTrailer.setVisibility(View.INVISIBLE);
            // show error message
            showErrorMessageTrailer();
        }
    }

    private void showReviewsDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageReview.setVisibility(View.INVISIBLE);
        mNoDataMessageReview.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerViewReview.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageReview() {
        /* First, hide the currently visible data */
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        mNoDataMessageReview.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageReview.setVisibility(View.VISIBLE);
        mErrorMessageReview.setText(getString(R.string.error_message));
    }

    private void showErrorNoIntenetMessageReview() {
        /* First, hide the currently visible data */
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        mNoDataMessageReview.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageReview.setVisibility(View.VISIBLE);
        mErrorMessageReview.setText(getString(R.string.error_no_internet_message));
    }

    private void showTrailersDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageTrailer.setVisibility(View.INVISIBLE);
        mNoDataMessageTrailer.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerViewTrailer.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageTrailer() {
        /* First, hide the currently visible data */
        mRecyclerViewTrailer.setVisibility(View.INVISIBLE);
        mNoDataMessageTrailer.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageTrailer.setVisibility(View.VISIBLE);
        mErrorMessageTrailer.setText(getString(R.string.error_message));
    }

    private void showErrorNoIntenetMessageTrailer() {
        /* First, hide the currently visible data */
        mRecyclerViewTrailer.setVisibility(View.INVISIBLE);
        mNoDataMessageTrailer.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageTrailer.setVisibility(View.VISIBLE);
        mErrorMessageTrailer.setText(getString(R.string.error_no_internet_message));
    }

    private void showNoDataMessageReview() {
        /* First, hide the currently visible data */
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        mErrorMessageReview.setVisibility(View.INVISIBLE);
        /* Then, show no data message */
        mNoDataMessageReview.setVisibility(View.VISIBLE);
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onItemClick(Trailer trailerClicked) {
        watchTrailer(trailerClicked.getKey());
    }

    public void watchTrailer(String key){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_APP + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_WEB + key));

        try {
            startActivity(appIntent);
        }
        catch (ActivityNotFoundException ex){
            startActivity(webIntent);
        }
    }

    public void shareTrailer(){
        if(mTrailerAdapter.getTrailersData().isEmpty()){
            Toast.makeText(DetailActivity.this, getString(R.string.no_trailer_message), Toast.LENGTH_SHORT).show();
            return;
        }
        Trailer trailer = mTrailerAdapter.getTrailersData().get(0);
        String trailerUrl = YOUTUBE_WEB + trailer.getKey();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(getString(R.string.share_url_type));
        i.putExtra(Intent.EXTRA_SUBJECT, trailer.getName());
        i.putExtra(Intent.EXTRA_TEXT, trailerUrl);
        startActivity(Intent.createChooser(i, getString(R.string.share_trailer_title)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // finish activity when clicked back button on actionbar
            case android.R.id.home :
                finish();
                return true;
            case R.id.action_share :
                shareTrailer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
