package jp.co.nassua.nervenet.groupchatmain;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ChatItem implements Parcelable {

    public String id;
    public String name;
    public String comment;
    public Long time;
    public Uri fileuri;
    public String filepath;

    public ChatItem() {
        this.id = null;
        this.name = null;
        this.comment = null;
        this.time = null;
        this.fileuri = null;
        this.filepath = null;
    }

    /**
     * コンストラクタ（ローカル）
     * @param in
     */
    private ChatItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        comment = in.readString();
        time = in.readLong();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>() {
        public ChatItem createFromParcel(Parcel in) {
            return new ChatItem(in);
        }

        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };

    /**
     *
     * @return
     */
    /*
    @Override
    public String toString() {
        String ret = "id: " + this.id + ", name: " + name + ", comment: " + comment + ", time: " + time;
        return ( ret );
    }
    */

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( id );
        dest.writeString( name );
        dest.writeString( comment );
        dest.writeLong( time );
    }
}
