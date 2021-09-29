package jp.co.nassua.nervenet.groupchatmain;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupItem implements Parcelable {

    //public String boxnodeid;
    public String boxname;

    public GroupItem() {
        //this.boxnodeid = null;
        this.boxname = null;
    }

    private GroupItem(Parcel in) {
        //boxnodeid = in.readString();
        boxname = in.readString();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<GroupItem> CREATOR = new Parcelable.Creator<GroupItem>() {
        public GroupItem createFromParcel(Parcel in) {
            return new GroupItem(in);
        }

        public GroupItem[] newArray(int size) {
            return new GroupItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(boxnodeid);
        dest.writeString( boxname );
    }

}
