package jp.co.nassua.nervenet.groupchatmain;

import android.os.Parcel;
import android.os.Parcelable;

public class SelectTerminalItem implements Parcelable {
    public String sipuri;
    public String name;

    public SelectTerminalItem() {
        this.sipuri = null;
        this.name = null;
    }

    private SelectTerminalItem(Parcel in) {
        sipuri = in.readString();
        name = in.readString();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<SelectTerminalItem> CREATOR = new Parcelable.Creator<SelectTerminalItem>() {
        public SelectTerminalItem createFromParcel(Parcel in) {
            return new SelectTerminalItem(in);
        }

        public SelectTerminalItem[] newArray(int size) {
            return new SelectTerminalItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( sipuri );
        dest.writeString( name );
    }
}