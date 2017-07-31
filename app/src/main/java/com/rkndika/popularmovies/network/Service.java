package com.rkndika.popularmovies.network;

import com.rkndika.popularmovies.model.Movies;
import com.rkndika.popularmovies.model.Reviews;
import com.rkndika.popularmovies.model.Trailers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    // get popular movie from API with api_key query
    @GET(Config.POPULAR)
    Call<Movies> getPopularMovie(@Query("api_key") String apiKey);

    // get top rated movie from API with api_key query
    @GET(Config.TOP_RATED)
    Call<Movies> getTopRatedMovie(@Query("api_key") String apiKey);

    // get reviews from API with api_key query
    @GET("{movie_id}/" + Config.REVIEWS)
    Call<Reviews> getReviews(@Path("movie_id") int movieId, @Query("api_key") String apiKey);

    // get trailers from API with api_key query
    @GET("{movie_id}/" + Config.TRAILERS)
    Call<Trailers> getTrailers(@Path("movie_id") int movieId, @Query("api_key") String apiKey);
}
