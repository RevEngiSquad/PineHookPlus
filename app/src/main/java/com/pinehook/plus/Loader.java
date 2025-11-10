package com.pinehook.plus;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class Loader extends ContentProvider {
    private static final String TAG = "Loader";

    @Override
    public boolean onCreate() {
        try {
            Context context = getContext();
            if (context != null) {
                Log.d(TAG, "Loading native library");
                NativeLibLoader.loadNativeLib(context, "libpine.so");
                Log.d(TAG, "Loading modules");
                Hook.loadModules(context);
                Log.d(TAG, "Loading config");
                Map<String, Map<String, Object>> config = NativeLibLoader.loadConfig(context);
                Log.d(TAG, "Executing hook");
                Hook.doHook(config);
                Log.d(TAG, "Hook executed successfully");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during initialization", e);
            return false;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
