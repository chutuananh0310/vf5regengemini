package com.vf5regen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.syu.ipc.IModuleCallback;
import com.syu.ipc.IRemoteModule;
import com.syu.ipc.IRemoteToolkit;

public class CanbusManager {
    private static CanbusManager instance;
    private IRemoteToolkit remoteToolkit;
    private IRemoteModule canbusModule;
    private IRemoteModule mainModule;
    private Context context;
    private boolean isConnecting = false;

    private static String tag = "VF5Regen_Canbus";

    // VinFast 5 Data Indexes for Module 7 (Canbus)
    public static final int U_REGEN_LEVEL = 110;  // Regen Level: 0:Off, 1:Low, 2:High
    public static final int U_RANGE = 113;        // Range (Km)
    public static final int U_BATTERY_SOC = 114;  // Battery SOC (%)
    public static final int U_CHARGING_STATUS = 115; // Charging Gun: 1:Connected, 0:Unconnected
    public static final int U_TIME_TO_FULL = 116;    // Time to full (Minutes)
    public static final int U_CHARGING_LIMIT = 118;  // Charging Limit (%) 0:50, 1:60... 5:100

    public interface OnDataListener {
        void onDataUpdate(int code, int value);
        void onConnectionStatus(boolean connected);
    }

    private OnDataListener listener;

    public static synchronized CanbusManager getInstance(Context ctx) {
        if (instance == null) instance = new CanbusManager(ctx);
        return instance;
    }

    private CanbusManager(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    public void connect(OnDataListener listener) {
        this.listener = listener;
        bindToService();
    }

    private void bindToService() {
        if (isConnecting) return;
        isConnecting = true;
        Log.d(tag, "Attempting to bind to service...");
        Intent intent = new Intent("com.syu.ms.toolkit");
        intent.setPackage("com.syu.ms");
        boolean bound = context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        Log.d(tag, "Binding result: " + bound);
        if (!bound) {
            isConnecting = false;
            retryConnection();
        }
    }

    private void retryConnection() {
        if (listener != null) listener.onConnectionStatus(false);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d(tag, "Retrying connection...");
            bindToService();
        }, 3000);
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(tag, "Service Connected");
            isConnecting = false;
            if (listener != null) listener.onConnectionStatus(true);
            remoteToolkit = IRemoteToolkit.Stub.asInterface(service);
            try {
                // Module 7: Canbus Info (SOC, Range, Regen)
                canbusModule = remoteToolkit.getRemoteModule(7); 
                if (canbusModule != null) {
                    Log.d(tag, "Registering Module 7 (Canbus) IDs 0-1000");
                    for (int i = 0; i <= 1000; i++) {
                        canbusModule.register(mCallback, i, 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(tag, "Service Disconnected");
            canbusModule = null;
            isConnecting = false;
            retryConnection();
        }
    };

    private IModuleCallback mCallback = new IModuleCallback.Stub() {
        @Override
        public void update(int code, int[] ints, float[] flts, String[] strs) {
            if (ints != null && ints.length > 0) {
                Log.d(tag, "Code: " + code + " | Value: " + ints[0]);
                if (listener != null) {
                    listener.onDataUpdate(code, ints[0]);
                }
            }
        }
    };
}
