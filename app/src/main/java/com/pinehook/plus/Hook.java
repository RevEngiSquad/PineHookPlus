package com.pinehook.plus;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodReplacement;
import top.canyie.pine.callback.MethodHook;
import top.canyie.pine.xposed.PineXposed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class Hook {
    private static final String TAG = "Hook";

    public static void loadModules(Context context) {
        String[] apkFiles = getApkFiles(context);
        if (apkFiles.length == 0) {
            Log.d(TAG, "No APK files found in resources, skipping module loading.");
        } else {
            for (String apkFile : apkFiles) {
                String modulePath = extractApkToCache(context, "hook/" + apkFile, apkFile);
                if (modulePath != null) {
                    Log.d(TAG, "Loading module: " + modulePath);
                    PineXposed.loadModule(new File(modulePath));

                    try {
                        String packageName = context.getPackageName();
                        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
                        ClassLoader classLoader = context.getClassLoader();
                        String processName = getProcessName(context);

                        if (processName == null) {
                            Log.e(TAG, "Could not determine process name, aborting module load.");
                            return;
                        }

                        Log.d(TAG, "Activating module for package: " + packageName + " in process: " + processName);

                        PineXposed.onPackageLoad(packageName, processName, appInfo, false, classLoader);

                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "Failed to get ApplicationInfo, cannot activate module.", e);
                    }
                }
            }
        }
    }

    public static void doHook(Map<String, Map<String, Object>> config) {
        try {
            for (Map.Entry<String, Map<String, Object>> classEntry : config.entrySet()) {
                Class<?> clazz = Class.forName(classEntry.getKey());
                for (Map.Entry<String, Object> methodEntry : classEntry.getValue().entrySet()) {
                    String methodName = methodEntry.getKey();
                    Map<String, Object> methodDetails = (Map<String, Object>) methodEntry.getValue();

                    if (Objects.equals(methodName, "constructor")) {
                        Constructor<?> constructor = getConstructor(clazz, methodName, methodDetails);

                        Log.d(TAG, "Hooking constructor in class: " + clazz.getName());

                        Pine.hook(constructor, new MethodHook() {
                            @Override
                            public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
                                Log.d(TAG, "Before constructor call: " + clazz.getName());
                                handleBeforeCall(callFrame, methodDetails);
                            }

                            @Override
                            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                                Log.d(TAG, "After constructor call: " + clazz.getName());
                                handleAfterCall(callFrame, methodDetails);
                            }
                        });

                    } else if (!Objects.equals(methodName, "constructor")) {
                        Method method = getMethod(clazz, methodName, methodDetails);

                        Log.d(TAG, "Hooking method: " + methodName + " in class: " + clazz.getName());

                        if (methodDetails.containsKey("before") || methodDetails.containsKey("after")) {
                            Pine.hook(method, new MethodHook() {
                                @Override
                                public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
                                    Log.d(TAG, "Before call: " + methodName);
                                    handleBeforeCall(callFrame, methodDetails);
                                }

                                @Override
                                public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                                    Log.d(TAG, "After call: " + methodName);
                                    handleAfterCall(callFrame, methodDetails);
                                }
                            });
                        } else {
                            Pine.hook(method, MethodReplacement.returnConstant(methodDetails.get("result")));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Log.e(TAG, "Error during hooking", e);
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(Class<?> clazz, String methodName, Map<String, Object> methodDetails) throws NoSuchMethodException {
        if (methodDetails.containsKey("paramTypes")) {
            List<String> paramTypeNames = (List<String>) methodDetails.get("paramTypes");
            Class<?>[] paramTypes = paramTypeNames.stream()
                    .map(Hook::getClassForName)
                    .toArray(Class<?>[]::new);
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } else {
            return clazz.getDeclaredMethod(methodName);
        }
    }

    private static Constructor<?> getConstructor(Class<?> clazz, String methodName, Map<String, Object> methodDetails) throws NoSuchMethodException {
        if (methodDetails.containsKey("paramTypes")) {
            List<String> paramTypeNames = (List<String>) methodDetails.get("paramTypes");
            Class<?>[] paramTypes = paramTypeNames.stream()
                    .map(Hook::getClassForName)
                    .toArray(Class<?>[]::new);
            return clazz.getDeclaredConstructor(paramTypes);
        } else {
            return clazz.getDeclaredConstructor();
        }
    }

    private static Class<?> getClassForName(String className) {
        try {
            switch (className) {
                case "int":
                    return int.class;
                case "boolean":
                    return boolean.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "long":
                    return long.class;
                case "short":
                    return short.class;
                case "byte":
                    return byte.class;
                case "char":
                    return char.class;
                default:
                    return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProcessName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }

        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return null;

        for (ActivityManager.RunningAppProcessInfo processInfo : am.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    private static String[] getApkFiles(Context context) {
        String resourcePath = "hook/modules.txt";
        List<String> apkFiles = new ArrayList<>();

        try {
            InputStream inputStream = context.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                Log.w(TAG, "Resource file not found: " + resourcePath);
                return apkFiles.toArray(new String[0]);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.endsWith(".apk")) {
                        apkFiles.add(line);
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not read resource file: " + resourcePath, e);
        }
        return apkFiles.toArray(new String[0]);
    }

    private static void handleBeforeCall(Pine.CallFrame callFrame, Map<String, Object> methodDetails) {
        if (methodDetails.containsKey("before")) {
            Map<String, Object> beforeConfig = (Map<String, Object>) methodDetails.get("before");
            if (beforeConfig.containsKey("args")) {
                List<Object> args = (List<Object>) beforeConfig.get("args");
                for (int i = 0; i < Objects.requireNonNull(args).size(); i++) {
                    callFrame.args[i] = args.get(i);
                }
            }
            if (beforeConfig.containsKey("result")) {
                callFrame.setResultIfNoException(beforeConfig.get("result"));
            }
        }
    }

    private static void handleAfterCall(Pine.CallFrame callFrame, Map<String, Object> methodDetails) {
        if (methodDetails.containsKey("after")) {
            Map<String, Object> afterConfig = (Map<String, Object>) methodDetails.get("after");
            if (afterConfig.containsKey("result")) {
                callFrame.setResultIfNoException(afterConfig.get("result"));
            }
        }
    }

    private static String extractApkToCache(Context context, String resourcePath, String fileName) {
        File cacheDir = context.getCodeCacheDir();
        File outputFile = new File(cacheDir, fileName);

        if (!outputFile.exists()) {
            Log.d(TAG, "File not found, extracting: " + fileName);
            try (InputStream inputStream = context.getClassLoader().getResourceAsStream(resourcePath);
                 OutputStream outputStream = new FileOutputStream(outputFile)) {

                if (inputStream == null) {
                    Log.e(TAG, "Cannot get resource as stream for: " + resourcePath);
                    return null;
                }

                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                Log.i(TAG, "Extracted " + fileName + " to " + outputFile.getAbsolutePath());

            } catch (IOException e) {
                Log.e(TAG, "Failed to extract APK from resources", e);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                return null;
            }
        } else {
            Log.d(TAG, "File already exists: " + fileName);
        }

        try {
            outputFile.setReadable(true, false);
            outputFile.setWritable(false, false);
            Log.i(TAG, "Set file read-only: " + outputFile.getName());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to set file read-only", e);
            return null;
        }

        return outputFile.getAbsolutePath();
    }
}
