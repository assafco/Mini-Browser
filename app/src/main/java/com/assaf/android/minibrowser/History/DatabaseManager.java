package com.assaf.android.minibrowser.History;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Room;

public class DatabaseManager {
    @SuppressLint("StaticFieldLeak")
    private static DatabaseManager INSTANCE;
    private final BrowserDatabase db;

    public DatabaseManager(Context context){
        db = Room.databaseBuilder(context,
                BrowserDatabase.class, "browser.db")
                .createFromAsset("database/browser.db")
                .allowMainThreadQueries().build();
    }

    public static DatabaseManager getInstance(Context context){
        if(INSTANCE==null){
            INSTANCE = new DatabaseManager(context);
        }
        return INSTANCE;
    }

    public BrowserDatabase getDb() {
        return db;
    }


}