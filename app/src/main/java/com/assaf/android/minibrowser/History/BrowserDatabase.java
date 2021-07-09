package com.assaf.android.minibrowser.History;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Site.class}, version = 1)
public abstract class BrowserDatabase extends RoomDatabase {
    public abstract SiteDao siteDao();
}