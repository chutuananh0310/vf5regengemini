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

public class Module5Manager {
    private static Module5Manager instance;
    private IRemoteToolkit remoteToolkit;
    private IRemoteModule module5;
    private Context context;
    private boolean isConnecting = false;

    private static String tag = "VF5Regen_Module5";

    public interface OnDataListener {
        void onDataUpdate(int code, int value);
        void onConnectionStatus(boolean connected);
        void onModuleFound(int moduleId);
    }

    private OnDataListener listener;

    public static synchronized Module5Manager getInstance(Context ctx) {
        if (instance == null) instance = new Module5Manager(ctx);
        return instance;
    }

    private Module5Manager(Context ctx) {
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
        if (!bound) {
            isConnecting = false;
            retryConnection();
        }
    }

    private void retryConnection() {
        if (listener != null) listener.onConnectionStatus(false);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
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
                // Probe for available modules (0-25)
                for (int m = 0; m <= 25; m++) {
                    try {
                        IRemoteModule mod = remoteToolkit.getRemoteModule(m);
                        if (mod != null) {
                            if (listener != null) listener.onModuleFound(m);
                            Log.d("VF5Regen_Probe", ">>> Found Module: " + m);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }

                module5 = remoteToolkit.getRemoteModule(5);
                if (module5 != null) {
                    // Register a very wide range of IDs to be sure
                    for (int i = 0; i <= 1000; i++) {
                        module5.register(mCallback, i, 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(tag, "Service Disconnected");
            module5 = null;
            isConnecting = false;
            retryConnection();
        }
    };

    private IModuleCallback mCallback = new IModuleCallback.Stub() {
        @Override
        public void update(int code, int[] ints, float[] flts, String[] strs) {
            if (ints != null && ints.length > 0) {
                if (listener != null) {
                    listener.onDataUpdate(code, ints[0]);
                }
            }
        }
    };
}
