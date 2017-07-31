package com.rkndika.popularmovies.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    public static Retrofit retrofit = null;

    // method for get retrofit object
    public Retrofit getClient(){
        // create retrofit object if no retrofit object created
        if(retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.URL_MOVIE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
