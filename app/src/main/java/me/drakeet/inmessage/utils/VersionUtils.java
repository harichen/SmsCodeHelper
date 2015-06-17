package me.drakeet.inmessage.utils;

import android.os.Build;

/**
 * Created by drakeet on 12/11/14.
 */
public class VersionUtils {
    public static final boolean IS_JBMR2 = Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final boolean IS_ISC = Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static final boolean IS_GINGERBREAD_MR1 = Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1;
    public static final boolean IS_MORE_THAN_16 = Build.VERSION.SDK_INT >= 16;
    public static final boolean IS_MORE_THAN_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
}
