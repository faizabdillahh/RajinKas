package com.rajinkas.util;

import android.os.SystemClock;
import android.view.View;

public class ClickUtils {
    private static final long MIN_CLICK_INTERVAL = 1000; // 1 second
    private static long lastClickTime = 0;

    /**
     * Prevents multiple clicks in short succession.
     * Returns true if the click is valid (not a rapid repeat).
     */
    public static boolean isClickValid() {
        long currentTimestamp = SystemClock.elapsedRealtime();
        if (currentTimestamp - lastClickTime < MIN_CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTimestamp;
        return true;
    }

    /**
     * Sets a click listener that automatically handles debouncing.
     */
    public static void applySingleClick(View view, View.OnClickListener listener) {
        view.setOnClickListener(v -> {
            if (isClickValid()) {
                listener.onClick(v);
            }
        });
    }
}
