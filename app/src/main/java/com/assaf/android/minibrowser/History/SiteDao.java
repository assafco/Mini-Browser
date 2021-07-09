package com.assaf.android.minibrowser.History;

import java.util.Date;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

@Dao
public interface SiteDao {
    @Query("SELECT * FROM site")
    List<Site> getAll();

    @Query("SELECT * FROM site WHERE sid IN (:siteIds)")
    List<Site> loadAllByIds(int[] siteIds);

    @Query("SELECT * FROM site ORDER BY visit_date DESC LIMIT :n ")
    List<Site> loadFirstN(int n);

    @Query("SELECT * FROM site WHERE site_url LIKE :url LIMIT 1")
    Site findByUrl(String url);

    @Query("SELECT COUNT(*) FROM site")
    int countRecords();

    @Query("DELETE FROM site")
    void deleteAllRecords();

    @Insert
    void insertAll(Site... sites);

    @Delete
    void delete(Site site);

    @Query("UPDATE site SET site_icon=:icon WHERE site_url = :url")
    void updateIcon(byte[] icon, String url);

    @Query("UPDATE site SET visit_date=:date WHERE sid = :id")
    @TypeConverters({TimestampConverter.class})
    void updateVisitDate(Date date, int id);
}