package com.rkndika.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.rkndika.popularmovies.adapter.CustomCursorAdapter;
import com.rkndika.popularmovies.adapter.MovieAdapter;
import com.rkndika.popularmovies.model.Movie;
import com.rkndika.popularmovies.model.Movies;
import com.rkndika.popularmovies.network.Client;
import com.rkndika.popularmovies.network.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        CustomCursorAdapter.CustomCursorAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>{

    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    // Member variables for the adapter and RecyclerView
    private CustomCursorAdapter mAdapter;
    private RecyclerView mRecyclerViewFavorite;

    // final string for intent
    public static final String PUT_EXTRA_MOVIES = "movies";

    // final string for state key
    private static final String STATE_SORT_TYPE = "sortTypeState";
    private static final String STATE_RECYCLERVIEW_LIST = "recyclerViewListState";
    private static final String STATE_RECYCLERVIEW_DATA = "recyclerViewDataState";

    // final value of sort type
    private static final int POPULAR_SORT_TYPE = 1;
    private static final int TOP_RATED_SORT_TYPE = 2;
    private static final int FAVORITE_SORT_TYPE = 3;

    /**
     * sorting type
     * 1 for sorting by popular
     * 2 for sorting by top rated
     */
    private int sortType;

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private SwipeRefreshLayout swipeContainer;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        initViews();

        // if activity have savedInstanceState then showPreviousView
        if(savedInstanceState != null){
            showPreviousView(savedInstanceState);
        }
        // if activity haven't saveInstanceState
        else {
            // initialize default sortType is sort by popular
            sortType = POPULAR_SORT_TYPE;
            // load data from The Movie DB API
            loadMoviesData();
        }
    }

    private void initViews(){
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(), GridLayoutManager.VERTICAL, false);

        mMovieAdapter = new MovieAdapter(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMovieAdapter);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark);

        // load new data from API
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMoviesData();
                swipeContainer.setRefreshing(false);
            }
        });

        // Set the RecyclerView to its corresponding view
        mRecyclerViewFavorite = (RecyclerView) findViewById(R.id.recyclerview_movies_favorite);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerViewFavorite.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new CustomCursorAdapter(this);
        mRecyclerViewFavorite.setAdapter(mAdapter);
    }

    // get number of grid's coloum dynamically
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    private void showPreviousView(Bundle savedInstanceState){
        // get sortType from previous state
        sortType = savedInstanceState.getInt(STATE_SORT_TYPE);

        // set title by sortType
        if(sortType == POPULAR_SORT_TYPE){
            setTitle(getString(R.string.popular_title));
        }
        else if(sortType == TOP_RATED_SORT_TYPE){
            setTitle(getString(R.string.top_rated_title));
        }

        // get data from previous data
        ArrayList<Movie> movieList = savedInstanceState.getParcelableArrayList(STATE_RECYCLERVIEW_DATA);

        // if no data get from state
        if(movieList == null){
            showErrorMessage();
            return;
        }
        showMoviesDataView();

        // set data from previous data
        mMovieAdapter.setMoviesData(movieList);

        //set recyclerview position from previous position
        Parcelable listState = savedInstanceState.getParcelable(STATE_RECYCLERVIEW_LIST);
        mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);

    }

    private void loadMoviesData() {
        swipeContainer.setVisibility(View.VISIBLE);
        mRecyclerViewFavorite.setVisibility(View.GONE);
        showMoviesDataView();

        // show loading
        mLoadingIndicator.setVisibility(View.VISIBLE);

        // check network status
        if(!isNetworkAvailable()){
            // disable loading
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            // show no connection message
            showErrorNoIntenetMessage();
            return;
        }

        try {

            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);

            // get The Movie DB API Key from api_keys.xml
            String apiKey = getString(R.string.THE_MOVIE_DB_API_KEY);

            Call<Movies> call;

            // get data from popular link
            if(sortType == POPULAR_SORT_TYPE){
                call = apiService.getPopularMovie(apiKey);
            }
            // get data from top rated link
            else if(sortType == TOP_RATED_SORT_TYPE){
                call = apiService.getTopRatedMovie(apiKey);
            }
            // if sortType unknown
            else {
                showErrorMessage();
                return;
            }

            // get data asynchronously with Retrofit
            call.enqueue(new Callback<Movies>() {
                @Override
                public void onResponse(Call<Movies> call, Response<Movies> response) {
                    // disable loading
                    mLoadingIndicator.setVisibility(View.INVISIBLE);

                    // if no error when fetching data
                    if(response.isSuccessful()){
                        // show recyclerview
                        showMoviesDataView();
                        // set data to adapter recyclerview
                        mMovieAdapter.setMoviesData(response.body().getResults());
                    }
                    // something error when fetching data
                    else {
                        Log.d("Error", "Response code : " + String.valueOf(response.code()));
                        // show error message
                        showErrorMessage();
                    }
                }

                // Retrofit faulire access API
                @Override
                public void onFailure(Call<Movies> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    // disable loading
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    // show error message
                    showErrorMessage();
                }
            });
        }catch (Exception e){
            Log.d("Error", e.getMessage());
            // disable loading
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            // show error message
            showErrorMessage();
        }
    }

    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setText(getString(R.string.error_message));
    }

    private void showErrorNoIntenetMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setText(getString(R.string.error_no_internet_message));
    }

    private void refreshMoviesData(){
        // delete all old data
        mMovieAdapter.setMoviesData(null);
        // load new data from API
        loadMoviesData();
    }

    @Override
    public void onItemClick(Movie movieClicked) {
        Intent i = new Intent(MainActivity.this, DetailActivity.class);
        // put data as Parcelable
        i.putExtra(PUT_EXTRA_MOVIES, movieClicked);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // if sort by popular clicked, set sortType 1 and reload data
        if (id == R.id.action_sort_by_popular) {
            sortType = POPULAR_SORT_TYPE;
            setTitle(getString(R.string.popular_title));
            refreshMoviesData();
            return true;
        }
        // if sort by top rated clicked, set sortType 2 and reload data
        else if(id == R.id.action_sort_by_top_rated){
            sortType = TOP_RATED_SORT_TYPE;
            setTitle(getString(R.string.top_rated_title));
            refreshMoviesData();
            return true;
        }
        else if(id == R.id.action_sort_by_favorite){
            sortType = FAVORITE_SORT_TYPE;
            setTitle(getString(R.string.favorite_title));
            showFavorites();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFavorites(){
        swipeContainer.setVisibility(View.GONE);
        mRecyclerViewFavorite.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // save state sortType
        savedInstanceState.putInt(STATE_SORT_TYPE, sortType);

        // save state list recyclerview
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        savedInstanceState.putParcelable(STATE_RECYCLERVIEW_LIST, listState);

        // save state adapter data
        savedInstanceState.putParcelableArrayList(STATE_RECYCLERVIEW_DATA, mMovieAdapter.getMoviesData());

        super.onSaveInstanceState(savedInstanceState);
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data
                return null;
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sortType == FAVORITE_SORT_TYPE){
            // re-queries for all tasks
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
        }
    }
}
