package com.pinehook.plus;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        StringBuilder result = new StringBuilder();

        // Test argMethod
        boolean argMethodResult = argMethod(true);
        result.append("argMethod(true): ").append(argMethodResult).append("\n");
        Log.d(TAG, "argMethod(true) called with args: true, returned: " + argMethodResult);

        // Test anotherMethod
        int anotherMethodResult = anotherMethod(42);
        result.append("anotherMethod(42): ").append(anotherMethodResult).append("\n");
        Log.d(TAG, "anotherMethod() called with args: 42, returned: " + anotherMethodResult);

        // Test yetAnotherMethod
        boolean yetAnotherMethodResult = yetAnotherMethod();
        result.append("yetAnotherMethod(): ").append(yetAnotherMethodResult).append("\n");
        Log.d(TAG, "yetAnotherMethod() called, returned: " + yetAnotherMethodResult);

        // Test method with only before hook
        String beforeOnlyResult = beforeOnlyMethod("before");
        result.append("beforeOnlyMethod(\"before\"): ").append(beforeOnlyResult).append("\n");
        Log.d(TAG, "beforeOnlyMethod(\"before\") called with args: \"before\", returned: " + beforeOnlyResult);

        // Test method with only after hook
        String afterOnlyResult = afterOnlyMethod("after");
        result.append("afterOnlyMethod(\"after\"): ").append(afterOnlyResult).append("\n");
        Log.d(TAG, "afterOnlyMethod(\"after\") called with args: \"after\", returned: " + afterOnlyResult);

        Log.d(TAG, "ConstructorClass called with args: false, null, false, 0, 0, null, null, null");
        new ConstructorClass(false, null, false, 0L, 0L, null, null, null);
        textView.setText(result.toString());

        boolean vpnStatus = detectVpn();
        Log.d(TAG, "VPN Status: " + vpnStatus);
        textView.setText(result + "VPN Status: " + vpnStatus);
    }

    public boolean argMethod(boolean input) {
        Log.d(TAG, "argMethod called with input: " + input);
        return input;
    }

    public int anotherMethod(int number) {
        Log.d(TAG, "anotherMethod called with number: " + number);
        return number;
    }

    public boolean yetAnotherMethod() {
        Log.d(TAG, "yetAnotherMethod called");
        return false;
    }

    public String beforeOnlyMethod(String input) {
        Log.d(TAG, "beforeOnlyMethod called with input: " + input);
        return input;
    }

    public String afterOnlyMethod(String input) {
        Log.d(TAG, "afterOnlyMethod called with input: " + input);
        return input;
    }

    public boolean detectVpn() {
        Log.d(TAG, "detectVpn called");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            Log.d(TAG, "VPN is active.");
            return true;
        } else {
            Log.d(TAG, "VPN is not active.");
            return false;
        }
    }
}
