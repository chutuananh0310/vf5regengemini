package com.syu.ipc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteModule extends IInterface {
    void cmd(int i, int[] iArr, float[] fArr, String[] strArr) throws RemoteException;
    ModuleObject get(int i, int[] iArr, float[] fArr, String[] strArr) throws RemoteException;
    void register(IModuleCallback iModuleCallback, int i, int i2) throws RemoteException;
    void unregister(IModuleCallback iModuleCallback, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteModule {
        private static final String DESCRIPTOR = "com.syu.ipc.IRemoteModule";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteModule asInterface(IBinder obj) {
            if (obj == null) return null;
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IRemoteModule)) return (IRemoteModule) iin;
            return new Proxy(obj);
        }

        @Override public IBinder asBinder() { return this; }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            // Minimal implementation for IPC
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IRemoteModule {
            private IBinder mRemote;
            Proxy(IBinder remote) { mRemote = remote; }
            @Override public IBinder asBinder() { return mRemote; }

            @Override public void cmd(int cmdCode, int[] ints, float[] flts, String[] strs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(cmdCode);
                    _data.writeIntArray(ints);
                    _data.writeFloatArray(flts);
                    _data.writeStringArray(strs);
                    mRemote.transact(1, _data, null, 1);
                } finally { _data.recycle(); }
            }

            @Override public ModuleObject get(int getCode, int[] ints, float[] flts, String[] strs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(getCode);
                    _data.writeIntArray(ints);
                    _data.writeFloatArray(flts);
                    _data.writeStringArray(strs);
                    mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt() != 0 ? ModuleObject.CREATOR.createFromParcel(_reply) : null;
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override public void register(IModuleCallback callback, int updateCode, int update) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(updateCode);
                    _data.writeInt(update);
                    mRemote.transact(3, _data, null, 1);
                } finally { _data.recycle(); }
            }

            @Override public void unregister(IModuleCallback callback, int updateCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(updateCode);
                    mRemote.transact(4, _data, null, 1);
                } finally { _data.recycle(); }
            }
        }
    }
}
