package com.syu.ipc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IModuleCallback extends IInterface {
    void update(int updateCode, int[] ints, float[] flts, String[] strs) throws RemoteException;

    public static abstract class Stub extends Binder implements IModuleCallback {
        private static final String DESCRIPTOR = "com.syu.ipc.IModuleCallback";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IModuleCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IModuleCallback) {
                return (IModuleCallback) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    update(data.readInt(), data.createIntArray(), data.createFloatArray(), data.createStringArray());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IModuleCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void update(int updateCode, int[] ints, float[] flts, String[] strs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(updateCode);
                    _data.writeIntArray(ints);
                    _data.writeFloatArray(flts);
                    _data.writeStringArray(strs);
                    mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
