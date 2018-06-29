package com.example.anon.passmanager.util;

/**
 * Created by Anon on 2017-02-06.
 */

import android.text.format.DateFormat;

import java.util.Calendar;

import static android.media.CamcorderProfile.get;

public class Formatter {
    private static final String FULL_FORMAT = "h:mm a - d LLL, yyyy";
    private static final String SHORT_DATE_FORMAT = "LLL d, yyyy";
    private static final String HOURS_FORMAT =  "h:mm a";
    private static final String DAY_FORMAT = "MMM d";

    public static CharSequence formatAdaptableDate(long pwdMillis) {
        String formatToUse = SHORT_DATE_FORMAT;
        Calendar cMillis = Calendar.getInstance(),
                pMillis = Calendar.getInstance();
        cMillis.setTimeInMillis(System.currentTimeMillis());
        pMillis.setTimeInMillis(pwdMillis);
        if (cMillis.get(Calendar.YEAR) == pMillis.get(Calendar.YEAR)) {
            if (cMillis.get(Calendar.DAY_OF_MONTH) == pMillis.get(Calendar.DAY_OF_MONTH)) {
                formatToUse = HOURS_FORMAT;
            } else {
                formatToUse = DAY_FORMAT;
            }
        }
        return DateFormat.format(formatToUse, pwdMillis);
    }

    public static CharSequence formatFullDate(long millis) {
        return DateFormat.format(FULL_FORMAT, millis);
    }
}
