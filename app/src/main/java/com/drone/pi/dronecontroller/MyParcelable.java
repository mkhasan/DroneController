package com.drone.pi.dronecontroller;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hasan on 17. 8. 27.
 */

// simple class that just has one member property as an example
public class MyParcelable implements Parcelable {
    private int mData;

    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this

    public MyParcelable(int _mData) {
        mData = _mData;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<MyParcelable> CREATOR = new Parcelable.Creator<MyParcelable>() {
        public MyParcelable createFromParcel(Parcel in) {
            return new MyParcelable(in);
        }

        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private MyParcelable(Parcel in) {
        mData = in.readInt();
    }


    public int Get() {
        return mData;
    }

    public void Set(int k) {
        mData = k;
    }

}