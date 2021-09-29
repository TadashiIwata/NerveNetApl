package jp.co.nassua.nervenet.groupchatmain;

import android.os.Parcel;
import android.os.Parcelable;

public class SelectGroupItem implements Parcelable {

    public String groupname;

    public SelectGroupItem() {
        this.groupname = null;
    }

    private SelectGroupItem(Parcel in) {
        groupname = in.readString();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<SelectGroupItem> CREATOR = new Parcelable.Creator<SelectGroupItem>() {
        public SelectGroupItem createFromParcel(Parcel in) {
            return new SelectGroupItem(in);
        }

        public SelectGroupItem[] newArray(int size) {
            return new SelectGroupItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( groupname );
    }

}
