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

    // VinFast 5 Data Indexes (Referenced from com.syu.module.canbus.Callback_0453)
    public static final int U_SPEED = 7;          
    public static final int U_REGEN_LEVEL = 110;  // U_CARSET_D26_D4_B5
    public static final int U_BATTERY_SOC = 114;  // U_CARSET_D40_D2_B70
    public static final int U_BRAKE = 101;        // U_CARSET_D26_D2_B40 (Potential Brake/Gear)
    public static final int U_RANGE = 113;        // U_CARSET_D40_D0_D1 (Range)

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
        Log.d("VF5Regen", "Attempting to bind to service...");
        Intent intent = new Intent("com.syu.ms.toolkit");
        intent.setPackage("com.syu.ms");
        boolean bound = context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        Log.d("VF5Regen", "Binding result: " + bound);
        if (!bound) {
            isConnecting = false;
            retryConnection();
        }
    }

    private void retryConnection() {
        if (listener != null) listener.onConnectionStatus(false);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d("VF5Regen", "Retrying connection...");
            bindToService();
        }, 3000);
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("VF5Regen", "Service Connected");
            isConnecting = false;
            if (listener != null) listener.onConnectionStatus(true);
            remoteToolkit = IRemoteToolkit.Stub.asInterface(service);
            try {
                // Module 7: Canbus Info (SOC, Range, Regen)
                canbusModule = remoteToolkit.getRemoteModule(7); 
                if (canbusModule != null) {
                    for (int i = 0; i <= 1000; i++) {
                        canbusModule.register(mCallback, i, 1);
                    }
                }

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
            Log.d("VF5Regen", "Service Disconnected");
            canbusModule = null;
            isConnecting = false;
            retryConnection();
        }
    };

    private IModuleCallback mCallback = new IModuleCallback.Stub() {
        @Override
        public void update(int code, int[] ints, float[] flts, String[] strs) {
            if (ints != null && ints.length > 0) {
                Log.d("VF5Regen_Data", "Code: " + code + " | Value: " + ints[0]);
                if (listener != null) {
                    listener.onDataUpdate(code, ints[0]);
                }
            }
        }
    };
}
