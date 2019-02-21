package com.wcy.client12306.util;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageUtil implements Parcelable {
    private String messStr;
    private int messInt;

    public MessageUtil(){}

    protected MessageUtil(Parcel in) {
        messStr = in.readString();
        messInt = in.readInt();
    }

    public static final Creator<MessageUtil> CREATOR = new Creator<MessageUtil>() {
        @Override
        public MessageUtil createFromParcel(Parcel in) {
            return new MessageUtil(in);
        }

        @Override
        public MessageUtil[] newArray(int size) {
            return new MessageUtil[size];
        }
    };

    public String getMessStr() {
        return messStr;
    }

    public void setMessStr(String messStr) {
        this.messStr = messStr;
    }

    public int getMessInt() {
        return messInt;
    }

    public void setMessInt(int messInt) {
        this.messInt = messInt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messStr);
        dest.writeInt(messInt);
    }
}
