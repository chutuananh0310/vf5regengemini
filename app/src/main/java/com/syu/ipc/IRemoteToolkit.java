package com.syu.ipc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteToolkit extends IInterface {
    IRemoteModule getRemoteModule(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteToolkit {
        private static final String DESCRIPTOR = "com.syu.ipc.IRemoteToolkit";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteToolkit asInterface(IBinder obj) {
            if (obj == null) return null;
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IRemoteToolkit)) return (IRemoteToolkit) iin;
            return new Proxy(obj);
        }

        @Override public IBinder asBinder() { return this; }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IRemoteToolkit {
            private IBinder mRemote;
            Proxy(IBinder remote) { mRemote = remote; }
            @Override public IBinder asBinder() { return mRemote; }

            @Override public IRemoteModule getRemoteModule(int moduleCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(moduleCode);
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return IRemoteModule.Stub.asInterface(_reply.readStrongBinder());
                } finally { _reply.recycle(); _data.recycle(); }
            }
        }
    }
}
