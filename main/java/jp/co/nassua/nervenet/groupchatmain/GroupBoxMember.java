package jp.co.nassua.nervenet.groupchatmain;

import org.json.JSONException;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.net.utlproto.NetData;

public class GroupBoxMember {

    public static final short AUTHORITY_SETTING_ALL = DbDefineShare.BoxMember.AUTHORITY_SHARE_READ | DbDefineShare.BoxMember.AUTHORITY_SHARE_WRITE
            | DbDefineShare.BoxMember.AUTHORITY_MEMBER_READ | DbDefineShare.BoxMember.AUTHORITY_MEMBER_WRITE | DbDefineShare.BoxMember.AUTHORITY_MASTER_UPDATE;

    public static byte[] idBox;  // ボックスID
    public static byte[] idMyself;  // 自局ID (Common mNodeUpdate)
    public static String uriMyself; // ボックス参加端末URI (insert時Boxmember mUriBoat)
    public static String uriBoat; // 自局URI (select時Boxmember mUriBoat)
    public static String uriMember; // ボックス参加端末URI (insert時Boxmember mUriBoat)

    // Common
    public short flagInvalid;  // レコード無効フラグ (Common mFlagInvalid)
    public Long timeUpdate;     // レコード更新時刻 (Common mTimeUpdate)
    public Long timeDiscard;    // レコード廃棄日時 (Common mTimeDiscard)
    // Boxmember
    public String name;          // 端末名 (Boxmember mName, Apl nickName)
    public String boxName;      // ボックス名 (Boxmember mIdBox)
    public short authority;    // 権限 (Boxmamber mAutority)
    public byte[] idRecord;    // メッセージID(Boxmember mIdRecord)

    // コンストラクタ
    public GroupBoxMember() {
        timeUpdate = null;
        timeDiscard = null;
        idRecord = null;
    }

    // データベースへの変換
    public DbDefineShare.BoxMember toRecord() {
        // BoxMember
        DbDefineShare.BoxMember rec = DbDefineShare.BoxMember.newInstance();
        rec.mAuthority = authority;
        rec.mIdRecord = idRecord;
        rec.mIdBox = boxName.getBytes();
        rec.mName = name;
        rec.mUriBoat = uriMember;
        return rec;
    }

    // データベースからの変換
    public boolean fromRecord( DbDefineShare.BoxMember rec) throws JSONException {
        // Boxmember
        if (rec.mUriBoat != null) {
            uriBoat = rec.mUriBoat;
        }
        if (rec.mName != null) {
            name = rec.mName;
        }
        if (rec.mIdBox != null) {
            boxName = rec.mIdBox.toString();
        }
        if (rec.mIdRecord != null) {
            idRecord = rec.mIdRecord;
        }
        authority = rec.mAuthority;
        // Common
        flagInvalid = rec.mCommon.mFlagInvalid;
        timeDiscard = rec.mCommon.mTimeDiscard;
        timeUpdate = rec.mCommon.mTimeUpdate;

        return true;
    }

    // 新しいインスタンスの取得 (boat由来)
    public static GroupBoxMember newInstance(String boat) {
        GroupBoxMember inst = new GroupBoxMember();
        inst.uriMyself = boat;

        byte[] bytes = idMyself;
        NetData data = new NetData(bytes.length + 8);
        data.encodeByteArray(bytes, bytes.length);
        data.encodeLong(System.currentTimeMillis());
        inst.idRecord = data.areaTgt;

        return inst;
    }

    // 新しいインスタンスの取得 (データベース由来)
    public static GroupBoxMember newInstance(DbDefineShare.BoxMember rec) throws JSONException {
        GroupBoxMember inst = new GroupBoxMember();
        inst.fromRecord( rec );
        return inst;
    }
}
