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
    private boolean isConnecting = false;
    private final SparseArray<IRemoteModule> activeModules = new SparseArray<>();

    private static final String tag = "VF5Regen_Scanner";

    public interface OnGlobalDataListener {
        void onDataUpdate(int moduleId, int code, int value);
        void onConnectionStatus(boolean connected);
        void onNewModuleDiscovered(int moduleId);
    }

    private OnGlobalDataListener listener;

    public static synchronized Module5Manager getInstance(Context ctx) {
        if (instance == null) instance = new Module5Manager(ctx);
        return instance;
    }

    private Module5Manager(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    public void connect(OnGlobalDataListener listener) {
        this.listener = listener;
        bindToService();
    }

    private void bindToService() {
        if (isConnecting) return;
        isConnecting = true;
        Intent intent = new Intent("com.syu.ms.toolkit");
        intent.setPackage("com.syu.ms");
        context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void retryConnection() {
        if (listener != null) listener.onConnectionStatus(false);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::bindToService, 3000);
    }

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConnecting = false;
            if (listener != null) listener.onConnectionStatus(true);
            remoteToolkit = IRemoteToolkit.Stub.asInterface(service);
            
            try {
                // Quét từ module 1 đến 20 (bỏ qua 0 và 7 vì đã có manager riêng)
                for (int m = 1; m <= 20; m++) {
                    if (m == 7) continue; 
                    
                    IRemoteModule mod = remoteToolkit.getRemoteModule(m);
                    if (mod != null) {
                        activeModules.put(m, mod);
                        if (listener != null) listener.onNewModuleDiscovered(m);
                        
                        // Đăng ký dải ID rộng cho mỗi module tìm thấy
                        Log.d(tag, "Registering Module " + m + " IDs 0-1000");
                        for (int i = 0; i <= 1000; i++) {
                            mod.register(new GlobalCallback(m), i, 1);
                        }
                        Log.d(tag, "Attached to Module: " + m);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            activeModules.clear();
            isConnecting = false;
            retryConnection();
        }
    };

    // Callback class riêng để biết dữ liệu đến từ module nào
    private class GlobalCallback extends IModuleCallback.Stub {
        private final int moduleId;
        public GlobalCallback(int id) { this.moduleId = id; }

        @Override
        public void update(int code, int[] ints, float[] flts, String[] strs) {
            if (ints != null && ints.length > 0) {
                if (listener != null) {
                    listener.onDataUpdate(moduleId, code, ints[0]);
                }
            }
        }
    }
}
