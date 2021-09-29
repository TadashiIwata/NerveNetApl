package jp.co.nassua.nervenet.groupchatmain;

import android.os.Parcel;
import android.os.Parcelable;

public class TerminalItem  implements Parcelable {
    public String sipuri;
    public String name;

    public TerminalItem() {
        this.sipuri = null;
        this.name = null;
    }

    private TerminalItem(Parcel in) {
        sipuri = in.readString();
        name = in.readString();
    }

    /**
     * ParcelからこのParcelableのインスタンスを作るためのCreator
     */
    public static final Parcelable.Creator<TerminalItem> CREATOR = new Parcelable.Creator<TerminalItem>() {
        public TerminalItem createFromParcel(Parcel in) {
            return new TerminalItem(in);
        }

        public TerminalItem[] newArray(int size) {
            return new TerminalItem[size];
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
