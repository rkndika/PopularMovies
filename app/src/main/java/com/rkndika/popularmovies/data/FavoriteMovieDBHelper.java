package com.rkndika.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rkndika.popularmovies.data.FavoriteMovieContract.*;

public class FavoriteMovieDBHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "favoritemovie.db";

    // The database version
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public FavoriteMovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold favorite movie data
        final String SQL_CREATE_FAVORITE_MOVIE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteMovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
                FavoriteMovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                FavoriteMovieEntry.COLUMN_VIDEO + " BOOLEAN NOT NULL, " +
                FavoriteMovieEntry.COLUMN_VOTE_AVERAGE + " DOUBLE NOT NULL, " +
                FavoriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POPULARITY + " DOUBLE NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_ADULT + " BOOLEAN NOT NULL, " +
                FavoriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
