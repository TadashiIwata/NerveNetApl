package jp.co.nassua.nervenet.groupchatmain;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupViewItem implements Parcelable {
    public String groupname;

    public GroupViewItem() {
        this.groupname = null;
    }

    private GroupViewItem(Parcel in) {
        groupname = in.readString();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<GroupViewItem> CREATOR = new Parcelable.Creator<GroupViewItem>() {
        public GroupViewItem createFromParcel(Parcel in) {
            return new GroupViewItem(in);
        }

        public GroupViewItem[] newArray(int size) {
            return new GroupViewItem[size];
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
