package com.pinehook.plus;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.luckypray.dexkit.DexKitBridge;

import java.util.Map;

public class Loader extends ContentProvider {
    private static final String TAG = "Loader";
    public static DexKitBridge dexKitBridge;

    @Override
    public boolean onCreate() {
        try {
            if (getContext() != null) {
                Log.d(TAG, "Loading native library");
                NativeLibLoader.load(getContext());
                dexKitBridge = DexKitBridge.create(getContext().getApplicationInfo().sourceDir);
                Log.d(TAG, "Loading config");
                Map<String, Map<String, Object>> config = NativeLibLoader.loadConfig(getContext());
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
    public Cursor query(@NotNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NotNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NotNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NotNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NotNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
