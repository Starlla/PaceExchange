package com.example.barterapp;

import android.content.res.Resources;
import android.util.TypedValue;

public class Util {


    public static int dpToPx(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }
}
