package com.thamkhao;

import android.os.RemoteException;

import com.syu.canbus.TheApp;
import com.syu.ipc.IModuleCallback;
import com.syu.ui.air.AirHelper;
import com.syu.ui.air.Air_0453_Rzc_VinFast_Plus_23;
import com.syu.ui.door.DoorHelper;

/* JADX INFO: loaded from: classes.dex */
public class Callback_0453_RZC_VinFast5_Plus_23 extends CallbackCanbusBase {
    public static final int U_CARSET_D26_D0_B30 = 95;
    public static final int U_CARSET_D26_D0_B74 = 94;
    public static final int U_CARSET_D26_D1_B30 = 97;
    public static final int U_CARSET_D26_D1_B7 = 96;
    public static final int U_CARSET_D26_D2_B40 = 101;
    public static final int U_CARSET_D26_D2_B5 = 100;
    public static final int U_CARSET_D26_D2_B6 = 99;
    public static final int U_CARSET_D26_D2_B7 = 98;
    public static final int U_CARSET_D26_D3_B2 = 107;
    public static final int U_CARSET_D26_D3_B3 = 106;
    public static final int U_CARSET_D26_D3_B4 = 105;
    public static final int U_CARSET_D26_D3_B5 = 104;
    public static final int U_CARSET_D26_D3_B6 = 103;
    public static final int U_CARSET_D26_D3_B7 = 102;
    public static final int U_CARSET_D26_D4_B20 = 118;
    public static final int U_CARSET_D26_D4_B3 = 117;
    public static final int U_CARSET_D26_D4_B5 = 110;
    public static final int U_CARSET_D26_D4_B6 = 109;
    public static final int U_CARSET_D26_D4_B7 = 108;
    public static final int U_CARSET_D26_D5_70 = 111;
    public static final int U_CARSET_D26_D6_70 = 112;
    public static final int U_CARSET_D40_D0_D1 = 113;
    public static final int U_CARSET_D40_D2_B70 = 114;
    public static final int U_CARSET_D40_D3_B0 = 115;
    public static final int U_CARSET_D40_D4_D5 = 116;
    public static final int U_CNT_MAX = 119;

    @Override // com.syu.module.canbus.CallbackCanbusBase
    public void in() {
        IModuleCallback callback = ModuleCallbackCanbusProxy.getInstance();
        for (int i = 0; i < 119; i++) {
            DataCanbus.PROXY.register(callback, i, 1);
        }
        DoorHelper.sUcDoorEngine = 0;
        DoorHelper.sUcDoorFl = 1;
        DoorHelper.sUcDoorFr = 2;
        DoorHelper.sUcDoorRl = 3;
        DoorHelper.sUcDoorRr = 4;
        DoorHelper.sUcDoorBack = 5;
        DoorHelper.getInstance().buildUi();
        for (int i2 = 0; i2 < 6; i2++) {
            DataCanbus.NOTIFY_EVENTS[i2].addNotify(DoorHelper.getInstance(), 0);
        }
        AirHelper.getInstance().buildUi(new Air_0453_Rzc_VinFast_Plus_23(TheApp.getInstance()));
        for (int i3 = 10; i3 < 93; i3++) {
            DataCanbus.NOTIFY_EVENTS[i3].addNotify(AirHelper.SHOW_AND_REFRESH, 0);
        }
    }

    @Override // com.syu.module.canbus.CallbackCanbusBase
    public void out() {
        for (int i = 0; i < 6; i++) {
            DataCanbus.NOTIFY_EVENTS[i].removeNotify(DoorHelper.getInstance());
        }
        DoorHelper.getInstance().destroyUi();
        for (int i2 = 10; i2 < 93; i2++) {
            DataCanbus.NOTIFY_EVENTS[i2].removeNotify(AirHelper.SHOW_AND_REFRESH);
        }
        AirHelper.getInstance().destroyUi();
    }

    @Override // com.syu.ipc.IModuleCallback
    public void update(int updateCode, int[] ints, float[] flts, String[] strs) throws RemoteException {
        if (updateCode >= 0 && updateCode < 119) {
            HandlerCanbus.update(updateCode, ints);
        }
    }
}
