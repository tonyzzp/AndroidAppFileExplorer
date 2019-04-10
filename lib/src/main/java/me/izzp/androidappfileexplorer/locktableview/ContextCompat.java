package me.izzp.androidappfileexplorer.locktableview;

import android.content.Context;
import android.os.Build;

class ContextCompat {
    static int getColor(Context context, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(color);
        }
        return context.getResources().getColor(color);
    }
}
