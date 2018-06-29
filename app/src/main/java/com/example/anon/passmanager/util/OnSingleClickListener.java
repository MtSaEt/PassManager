package com.example.anon.passmanager.util;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

/**
 * Created by Anon on 2017-02-20.
 */

public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_CLICK_INTERVAL = 300;
    private long mLastClickTime;
    private boolean isViewClicked = false;
    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;

        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL)
            return;
        if (!isViewClicked) {
            isViewClicked = true;
            startTimer();
        } else
            return;
        onSingleClick(v);
    }
    /**
     * This method delays simultaneous touch events of multiple views.
     */
    private void startTimer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isViewClicked = false;
            }
        }, MIN_CLICK_INTERVAL);
    }
}