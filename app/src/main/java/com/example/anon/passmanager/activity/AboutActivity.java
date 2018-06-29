package com.example.anon.passmanager.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.example.anon.passmanager.R;

/**
 * Created by Skool on 2016-10-25.
 */

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getActionBar().hide();
        getWindow().setLayout((int) (width * .85), (int) (height * .65));
    }
}
