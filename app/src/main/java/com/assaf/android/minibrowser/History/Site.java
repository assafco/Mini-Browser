package com.assaf.android.minibrowser.History;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Site {
    @PrimaryKey(autoGenerate = true)
    public int sid;

    @ColumnInfo(name = "site_name")
    public String siteName;

    @ColumnInfo(name = "site_url")
    public String siteUrl;

    @ColumnInfo(name = "visit_date")
    @TypeConverters({TimestampConverter.class})
    public Date visitDate;

    @ColumnInfo(name = "site_icon", typeAffinity = ColumnInfo.BLOB)
    public byte[] siteIcon;

}