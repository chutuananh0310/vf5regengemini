package com.syu.ipc;

import android.os.Parcel;
import android.os.Parcelable;

public class ModuleObject implements Parcelable {
    public float[] flts;
    public int[] ints;
    public String[] strs;

    public ModuleObject() {}

    protected ModuleObject(Parcel in) {
        ints = in.createIntArray();
        flts = in.createFloatArray();
        strs = in.createStringArray();
    }

    public static final Creator<ModuleObject> CREATOR = new Creator<ModuleObject>() {
        @Override public ModuleObject createFromParcel(Parcel in) { return new ModuleObject(in); }
        @Override public ModuleObject[] newArray(int size) { return new ModuleObject[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(ints);
        dest.writeFloatArray(flts);
        dest.writeStringArray(strs);
    }
}
