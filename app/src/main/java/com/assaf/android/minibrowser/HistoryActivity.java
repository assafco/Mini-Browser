package com.assaf.android.minibrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.assaf.android.minibrowser.History.DatabaseManager;
import com.assaf.android.minibrowser.History.Site;
import com.assaf.android.minibrowser.adapters.HistoryRecyclerViewAdapter;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = HistoryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        setContentView(R.layout.activity_history);

        ImageView ivCloseActivity = findViewById(R.id.closeActivity);
        ivCloseActivity.setOnClickListener(v -> finish());

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        try {
            List<Site> myDataLists = DatabaseManager.getInstance(this).getDb().siteDao().loadFirstN(Constants.NO_OF_SITES);
            if (myDataLists != null && myDataLists.size() > 0) {
                HistoryRecyclerViewAdapter adapter = new HistoryRecyclerViewAdapter(myDataLists, this);
                rvHistory.setAdapter(adapter);
            }
        }catch (Exception e){
            Log.e(TAG, "Error loading sites: " + e.getMessage());
        }
    }


}