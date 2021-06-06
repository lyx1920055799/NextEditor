package com.starrysky.nextor;

import android.os.Parcel;
import android.os.Parcelable;

public class FragmentData implements Parcelable {

    private String text;
    private String filename;
    private String path;
    private boolean selected;

    public FragmentData() {

    }

    public FragmentData(String text, String filename, String path, boolean selected) {
        this.text = text;
        this.filename = filename;
        this.path = path;
        this.selected = selected;
    }

    protected FragmentData(Parcel in) {
        text = in.readString();
        filename = in.readString();
        path = in.readString();
        selected = in.readBoolean();
    }

    public static final Creator<FragmentData> CREATOR = new Creator<FragmentData>() {
        @Override
        public FragmentData createFromParcel(Parcel in) {
            return new FragmentData(in);
        }

        @Override
        public FragmentData[] newArray(int size) {
            return new FragmentData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(filename);
        parcel.writeString(path);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public boolean isSelected() {
        return selected;
    }
}
