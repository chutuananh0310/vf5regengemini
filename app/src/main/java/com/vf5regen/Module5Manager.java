package com.vf5regen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import com.syu.ipc.IModuleCallback;
import com.syu.ipc.IRemoteModule;
import com.syu.ipc.IRemoteToolkit;

public class Module5Manager {
    private static Module5Manager instance;
    private IRemoteToolkit remoteToolkit;
    private Context context;
    private boolean isServiceConnected = false;
    private final SparseArray<IRemoteModule> activeModules = new SparseArray<>();

    private static final String tag = "VF5Regen_Scanner";

    public interface OnExplorerListener {
        void onDataUpdate(int moduleId, int code, int value);
        void onServiceStatus(boolean connected);
        void onNewModuleDiscovered(int moduleId);
    }

    private OnExplorerListener listener;

    public static synchronized Module5Manager getInstance(Context ctx) {
        if (instance == null) instance = new Module5Manager(ctx);
        return instance;
    }

    private Module5Manager(Context ctx) {
        this.context = ctx.getApplicationContext();
        bindToService();
    }

    private void bindToService() {
        Intent intent = new Intent("com.syu.ms.toolkit");
        intent.setPackage("com.syu.ms");
        context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isServiceConnected = true;
            remoteToolkit = IRemoteToolkit.Stub.asInterface(service);
            if (listener != null) listener.onServiceStatus(true);
            Log.d(tag, "Service Connected. Starting auto-scan (21-500)...");
            
            autoScanModules();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceConnected = false;
            activeModules.clear();
            if (listener != null) listener.onServiceStatus(false);
        }
    };

    private void autoScanModules() {
        new Thread(() -> {
            try {
                for (int m = 21; m <= 500; m++) {
                    if (!isServiceConnected || remoteToolkit == null) break;
                    
                    try {
                        IRemoteModule mod = remoteToolkit.getRemoteModule(m);
                        if (mod != null) {
                            activeModules.put(m, mod);
                            if (listener != null) listener.onNewModuleDiscovered(m);
                            
                            // Register 0-1000 for this module
                            for (int i = 0; i <= 1000; i++) {
                                mod.register(new GlobalCallback(m), i, 1);
                            }
                            Log.d(tag, "Auto-connected and registered Module: " + m);
                        }
                    } catch (Exception e) {
                        // Module not available or permission denied
                    }
                    // Small sleep to avoid overloading the IPC system during heavy registration
                    Thread.sleep(10); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void connect(OnExplorerListener listener) {
        this.listener = listener;
        if (isServiceConnected) {
            autoScanModules();
        }
    }

    private class GlobalCallback extends IModuleCallback.Stub {
        private final int moduleId;
        public GlobalCallback(int id) { this.moduleId = id; }

        @Override
        public void update(int code, int[] ints, float[] flts, String[] strs) {
            if (ints != null && ints.length > 0 && listener != null) {
                listener.onDataUpdate(moduleId, code, ints[0]);
            }
        }
    }
}
