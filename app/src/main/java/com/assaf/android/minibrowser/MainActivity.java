package com.assaf.android.minibrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.assaf.android.minibrowser.History.DatabaseManager;
import com.assaf.android.minibrowser.History.Site;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener{

    private WebView webView;
    private ProgressBar progressBar;
    private EditText textUrl;
    private ImageView webIcon;
    private ImageView goBack;
    private ImageView goForward;
    private ImageView btnStart;
    private InputMethodManager manager;
    private long exitTime = 0;
    private PopupMenu settingsMenu;
    private SharedPreferences prefs;

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String SHOW_IMAGES = "SHOW_IMAGES";
    private static final int PRESS_BACK_EXIT_GAP = 2000;
    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_main);

        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initView();
        initWeb();
    }

    private void initView() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        textUrl = findViewById(R.id.textUrl);
        webIcon = findViewById(R.id.webIcon);
        btnStart = findViewById(R.id.btnStart);
        goBack = findViewById(R.id.backButton);
        goForward = findViewById(R.id.forwardButton);
        TextView menuButton = findViewById(R.id.menuButton);

        btnStart.setOnClickListener(this);
        goBack.setOnClickListener(this);
        goForward.setOnClickListener(this);
        menuButton.setOnClickListener(this);

        settingsMenu = new PopupMenu(this, menuButton);
        settingsMenu.setOnMenuItemClickListener(this);
        MenuInflater inflater = settingsMenu.getMenuInflater();
        inflater.inflate(R.menu.settings_menu, settingsMenu.getMenu());
        settingsMenu.getMenu().findItem(R.id.showImages).setChecked(prefs.getBoolean(SHOW_IMAGES, true));

        textUrl.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                textUrl.setText(webView.getUrl());
                textUrl.setSelection(textUrl.getText().length());
                webIcon.setImageResource(R.drawable.internet);
                btnStart.setImageResource(R.drawable.go);
            } else {
                textUrl.setText(webView.getTitle());
                webIcon.setImageBitmap(webView.getFavicon());
                btnStart.setImageResource(R.drawable.refresh);
            }
        });

        textUrl.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                btnStart.callOnClick();
                textUrl.clearFocus();
            }
            return false;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWeb() {

        webView.setWebViewClient(new BrowserWebViewClient());
        webView.setWebChromeClient(new BrowserWebChromeClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkLoads (true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.loadUrl(getResources().getString(R.string.home_url));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                if (textUrl.hasFocus()) {
                    if (manager.isActive()) {
                        manager.hideSoftInputFromWindow(textUrl.getApplicationWindowToken(), 0);
                    }
                    String input = textUrl.getText().toString();

                    boolean isValidUrl = Patterns.WEB_URL.matcher(input).matches();
                    if (!isValidUrl) {
                        try {
                            input = URLEncoder.encode(input, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        input = "https://www.google.com/search?q=" + input;
                    }
                    if(!input.startsWith(HTTPS) && !input.startsWith(HTTP)){
                        input = HTTPS + input;
                    }
                    webView.loadUrl(input);

                    textUrl.clearFocus();
                } else {
                    webView.clearCache(true);
                    webView.reload();
                }
                break;
            case R.id.backButton:
                webView.goBack();
                break;
            case R.id.forwardButton:
                webView.goForward();
                break;
            case R.id.menuButton:
                settingsMenu.show();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showImages:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(SHOW_IMAGES, item.isChecked()).apply();
                return true;
            case R.id.history:
                Intent myIntent = new Intent(this, HistoryActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > PRESS_BACK_EXIT_GAP) {
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }

    private void setImageTint(ImageView view, int color){
        Drawable mWrappedDrawable = view.getDrawable().mutate();
        mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
        DrawableCompat.setTint(mWrappedDrawable, color);
        DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);
    }


    private class BrowserWebViewClient extends WebViewClient {
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            if (webView.canGoBack()) {
                setImageTint(goBack, Color.parseColor("#000000"));
            } else {
                setImageTint(goBack, Color.parseColor("#cccccc"));
            }

            if (webView.canGoForward()) {
                setImageTint(goForward, Color.parseColor("#000000"));
            } else {
                setImageTint(goForward, Color.parseColor("#cccccc"));
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url == null) {
                return true;
            }

            if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
                view.loadUrl(url);
                return true;
            }

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (settingsMenu != null && settingsMenu.getMenu() != null && settingsMenu.getMenu().findItem(R.id.showImages) != null && !settingsMenu.getMenu().findItem(R.id.showImages).isChecked()) {
                if (url.endsWith(".gif") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".webp")) {
                    view.stopLoading();
                    return;
                }
            }
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            webIcon.setImageResource(R.drawable.internet);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
            setTitle(webView.getTitle());
            textUrl.setText(webView.getTitle());

            try {
                Site site = DatabaseManager.getInstance(MainActivity.this).getDb().siteDao().findByUrl(url);
                Date visitDate = Calendar.getInstance().getTime();
                if (site == null) {
                    site = new Site();
                    site.siteUrl = url;
                    site.siteName = webView.getTitle();
                    site.visitDate = visitDate;
                    DatabaseManager.getInstance(MainActivity.this).getDb().siteDao().insertAll(site);
                } else {
                    site.visitDate = Calendar.getInstance().getTime();
                    DatabaseManager.getInstance(getApplicationContext()).getDb().siteDao().updateVisitDate(visitDate, site.sid);
                }
            } catch (Exception e) {
                Log.e(TAG, "Saving to room didn't work: " + e.getMessage());
            }
        }
    }

    private class BrowserWebChromeClient extends WebChromeClient {
        private final static int WEB_PROGRESS_MAX = 100;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
            if (newProgress > 0) {
                if (newProgress == WEB_PROGRESS_MAX) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if (icon != null) {
                webIcon.setImageBitmap(icon);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
                try {
                    DatabaseManager.getInstance(MainActivity.this).getDb().siteDao().updateIcon(stream.toByteArray(), webView.getUrl());
                }catch (Exception e){
                    Log.e(TAG, "Failed to update site icon: " + e.getMessage());
                }
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setTitle(title);
            textUrl.setText(title);
        }
    }
}