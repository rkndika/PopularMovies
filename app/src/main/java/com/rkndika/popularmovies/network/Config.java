package com.rkndika.popularmovies.network;

public class Config {
    // base URL from API
    public static final String BASE_URL = "https://api.themoviedb.org";
    // The Movie DB API version
    public static final String API_VERSION = "/3";
    // base URL for get movie
    public static final String URL_MOVIE = BASE_URL + API_VERSION + "/movie/";

    // base image URL
    public static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    // image size for request API
    public static final String IMAGE_SIZE = "w185";
    // base URL for get image
    public static final String IMAGE_URL = BASE_IMAGE_URL + IMAGE_SIZE;

    // path API for get popular movie
    public static final String POPULAR = "popular";
    // path API for get top rated movie
    public static final String TOP_RATED = "top_rated";
    // path API for get reviews
    public static final String REVIEWS = "reviews";
    // path API for get trailers
    public static final String TRAILERS = "videos";
}
