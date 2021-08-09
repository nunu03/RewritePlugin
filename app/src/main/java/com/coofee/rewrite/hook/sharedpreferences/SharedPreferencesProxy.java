package com.coofee.rewrite.hook.sharedpreferences;

import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPreferencesProxy implements SharedPreferences {

    public static final Object EMPTY = new Object();

    public static Object emptyIfNull(Object value) {
        if (value == null) {
            return EMPTY;
        }

        return value;
    }

    public static Object nullIfEmpty(Object value) {
        return (value == EMPTY ? null : value);
    }

    private static final Map<String, Map<String, Object>> sStatsMap = new ConcurrentHashMap<>();

    public static Map<String, Map<String, Object>> snapshot() {
        return new HashMap<>(sStatsMap);
    }

    public static void print() {
        Map<String, Map<String, Object>> snapshot = snapshot();
        Log.d("SharedPreferencesProxy", "start print");
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : snapshot.entrySet()) {
            Map<String, Object> sharedPreferencesValues = entry.getValue();
            keys.addAll(sharedPreferencesValues.keySet());
            Log.d("SharedPreferencesProxy", "share preferences name=" + entry.getKey() + ", count=" + sharedPreferencesValues.size());

            for (Map.Entry<String, Object> keyAndValue : sharedPreferencesValues.entrySet()) {
                Log.d("SharedPreferencesProxy", "\taccess key=" + keyAndValue.getKey() + ", value=" + nullIfEmpty(keyAndValue.getValue()));
            }
        }

        Log.d("SharedPreferencesProxy", "name.count=" + snapshot.size() + ", names=" + snapshot.keySet());
        Log.d("SharedPreferencesProxy", "key.Count=" + keys.size() + ", keys=" + keys);
        Log.d("SharedPreferencesProxy", "end print");
    }

    private final String name;

    private final Map<String, Object> keyAndValueMap = new ConcurrentHashMap<>();

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesProxy(String name, SharedPreferences sharedPreferences) {
        this.name = name;
        this.sharedPreferences = sharedPreferences;
        sStatsMap.put(name, keyAndValueMap);
    }

    @Override
    public Map<String, ?> getAll() {
        Map<String, ?> all = sharedPreferences.getAll();
        keyAndValueMap.putAll(all);
        return all;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        String value = sharedPreferences.getString(key, defValue);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Set<String> value = sharedPreferences.getStringSet(key, defValues);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Override
    public int getInt(String key, int defValue) {
        int value = sharedPreferences.getInt(key, defValue);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Override
    public long getLong(String key, long defValue) {
        long value = sharedPreferences.getLong(key, defValue);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Override
    public float getFloat(String key, float defValue) {
        float value = sharedPreferences.getFloat(key, defValue);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        boolean value = sharedPreferences.getBoolean(key, defValue);
        keyAndValueMap.put(key, emptyIfNull(value));
        return value;
    }

    @Override
    public boolean contains(String key) {
        keyAndValueMap.put(key, new Object());
        return sharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return new EditorProxy(sharedPreferences.edit(), keyAndValueMap);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static class EditorProxy implements SharedPreferences.Editor {

        private final Editor editor;
        private final Map<String, Object> keyAndValueMap;

        public EditorProxy(Editor editor, Map<String, Object> keyAndValueMap) {
            this.editor = editor;
            this.keyAndValueMap = keyAndValueMap;
        }

        @Override
        public Editor putString(String key, @Nullable String value) {
            keyAndValueMap.put(key, emptyIfNull(value));
            return editor.putString(key, value);
        }

        @Override
        public Editor putStringSet(String key, @Nullable Set<String> values) {
            keyAndValueMap.put(key, emptyIfNull(values));
            return editor.putStringSet(key, values);
        }

        @Override
        public Editor putInt(String key, int value) {
            keyAndValueMap.put(key, emptyIfNull(value));
            return editor.putInt(key, value);
        }

        @Override
        public Editor putLong(String key, long value) {
            keyAndValueMap.put(key, emptyIfNull(value));
            return editor.putLong(key, value);
        }

        @Override
        public Editor putFloat(String key, float value) {
            keyAndValueMap.put(key, emptyIfNull(value));
            return editor.putFloat(key, value);
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            keyAndValueMap.put(key, emptyIfNull(value));
            return editor.putBoolean(key, value);
        }

        @Override
        public Editor remove(String key) {
            keyAndValueMap.put(key, emptyIfNull(null));
            return editor.remove(key);
        }

        @Override
        public Editor clear() {
            return editor.clear();
        }

        @Override
        public boolean commit() {
            return editor.commit();
        }

        @Override
        public void apply() {
            editor.apply();
        }
    }

}
