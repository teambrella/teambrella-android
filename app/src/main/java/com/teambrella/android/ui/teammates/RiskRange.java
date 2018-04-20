package com.teambrella.android.ui.teammates;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Risk Range
 */
public class RiskRange implements Parcelable {

    private float mLeftRange;
    private float mRightRange;


    public RiskRange(float leftRange, float rightRange) {
        this.mLeftRange = leftRange;
        this.mRightRange = rightRange;
    }

    private RiskRange(Parcel in) {
        mLeftRange = in.readFloat();
        mRightRange = in.readFloat();
    }

    public static final Creator<RiskRange> CREATOR = new Creator<RiskRange>() {
        @Override
        public RiskRange createFromParcel(Parcel in) {
            return new RiskRange(in);
        }

        @Override
        public RiskRange[] newArray(int size) {
            return new RiskRange[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(mLeftRange);
        parcel.writeFloat(mRightRange);
    }


    public float getLeftRange() {
        return mLeftRange;
    }

    public float getRightRange() {
        return mRightRange;
    }
}
