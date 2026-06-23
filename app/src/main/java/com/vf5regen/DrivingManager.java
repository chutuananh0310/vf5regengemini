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

public class DrivingManager {
    private static DrivingManager instance;
    private IRemoteToolkit remoteToolkit;
    private IRemoteModule mainModule;
    private Context context;
    private boolean isConnecting = false;

    private static String tag = "VF5Regen_Driving";

    // VinFast 5 Data Indexes (Referenced from com.syu.module.canbus.Callback_0453)
    public static final int D_SPEED = 101;
    public static final int D_BRAKE = 139;        // U_CARSET_D26_D2_B40 (Potential Brake/Gear)

    public interface OnDataListener {
        void onDataUpdate(int code, int value);
        void onConnectionStatus(boolean connected);
    }

    private OnDataListener listener;

    public static synchronized DrivingManager getInstance(Context ctx) {
        if (instance == null) instance = new DrivingManager(ctx);
        return instance;
    }

    private DrivingManager(Context ctx) {
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
                // Module 0: Real-time Driving Data (Speed, Brake, Gear, Camping Mode)
                mainModule = remoteToolkit.getRemoteModule(0);
                if (mainModule != null) {
                    for (int i = 0; i <= 1000; i++) {
                        mainModule.register(mCallback, i, 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(tag, "Service Disconnected");
            mainModule = null;
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
