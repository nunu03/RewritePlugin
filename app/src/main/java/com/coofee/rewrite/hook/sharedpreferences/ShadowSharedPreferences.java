package com.coofee.rewrite.hook.sharedpreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * copy from didi booster
 */
public class ShadowSharedPreferences {

    public static boolean useSystem = false;

    public static boolean useProxy = true;

    private static SharedPreferences proxy(String name, SharedPreferences sharedPreferences) {
        if (useProxy) {
            return new SharedPreferencesProxy(name, sharedPreferences);
        }

        return sharedPreferences;
    }

    public static SharedPreferences getSharedPreferences(final Context context, String name, final int mode) {
        if (TextUtils.isEmpty(name)) {
            name = "null";
        }

        if (useSystem) {
            return proxy(name, context.getSharedPreferences(name, mode));
        }

        Log.e("ShadowSharedPreferences", "getSharedPreferences; context=" + context + ", name=" + name + ", mode=" + mode);
        return proxy(name, BoosterSharedPreferences.getSharedPreferences(context, name));
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        String name = getDefaultSharedPreferencesName(context);
        if (useSystem) {
            return proxy(name, PreferenceManager.getDefaultSharedPreferences(context));
        }

        Log.e("ShadowSharedPreferences", "getDefaultSharedPreferences; context=" + context);
        return proxy(name, BoosterSharedPreferences.getSharedPreferences(context, name));
    }

    public static String getDefaultSharedPreferencesName(Context context) {
        Log.e("ShadowSharedPreferences", "getDefaultSharedPreferencesName; context=" + context);
        return context.getPackageName() + "_preferences";
    }

    public static SharedPreferences getPreferences(final Activity activity, final int mode) {
        if (useSystem) {
            return activity.getPreferences(mode);
        }

        Log.e("ShadowSharedPreferences", "getPreferences; activity=" + activity + ", mode=" + mode);
        return getSharedPreferences(activity.getApplicationContext(), activity.getLocalClassName(), mode);
    }

}