package com.example.anon.passmanager.util;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Anon on 2017-03-04.
 */

public abstract class CustomRecyclerScrollViewListener extends RecyclerView.OnScrollListener {
    int scrollDist = 0;
    boolean isVisible = true;
    static final float sensitivity = 200;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (isVisible && scrollDist > sensitivity) {
            hide();
            scrollDist = 0;
            isVisible = false;
        } else if (!isVisible && scrollDist < - (sensitivity / 2)) {
            show();
            scrollDist = 0;
            isVisible = true;
        }
        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }
    }
    public abstract void show();
    public abstract void hide();
}
