package com.coofee.rewrite.hook.provider;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

public class ShadowSystem {

    public static String getString(ContentResolver resolver, String name) {
        String value = Settings.System.getString(resolver, name);
        Log.e("ShadowSystem", "getString; name=" + name + ", value=" + value);
        return value;
    }
}
