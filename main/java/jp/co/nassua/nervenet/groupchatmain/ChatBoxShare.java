package jp.co.nassua.nervenet.groupchatmain;

import org.json.JSONException;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.net.utlproto.NetData;

public class ChatBoxShare {
    public static byte[] idBox;  // ボックスID
    public static byte[] idMyself;  //自局ID
    public static String uriMyself; //自局URI

    public Long timeCalibrate;   // 更新時刻
    public Long timeUpdate;     // レコード更新時刻
    public Long timeDiscard;    // レコード廃棄日時
    public String boxName;      // ボックス名
    public String MyBoatUrl;   // boat URL (attached)
    public String MessageInfo;    // メッセージ情報(body)
    public String Attached;
    public String uriAttached;
    public byte[] idRecord;    // メッセージID(レコードID

    // コンストラクタ
    public ChatBoxShare() {
        timeCalibrate = null;
        timeUpdate = null;
        timeDiscard = null;
        idRecord = null;
    }

    // データベースへの変換
    public DbDefineShare.BoxShare toRecord() {
        DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
        rec.mIdMsg = idRecord;
        rec.mBody = MessageInfo;
        rec.mIdBox = boxName.getBytes();
        rec.mUriBoat = MyBoatUrl;
        rec.mUriAttached = uriAttached;
        rec.mTimeCalibrate = timeCalibrate;
        //rec.mAttached = Attached;
        return rec;
    }

    // データベースからの変換
    //
    public boolean fromRecord( DbDefineShare.BoxShare rec ) throws JSONException {
        idBox = rec.mIdMsg;
        //
        if (rec.mBody != null) {
            MessageInfo = rec.mBody;
        }
        // ボックス名
        if (rec.mIdBox != null) {
            boxName = new String(rec.mIdBox);
        }
        // メッセージ本体
        if (rec.mUriAttached != null) {
            uriAttached = rec.mUriAttached;
        }
        if (rec.mUriBoat != null) {
            MyBoatUrl = rec.mUriBoat;
        }
        // 更新時刻
        timeCalibrate = rec.mTimeCalibrate;
        // レコード更新日時
        timeUpdate = rec.mCommon.mTimeUpdate;
        // レコード廃棄日時
        timeDiscard = rec.mCommon.mTimeDiscard;

        return true;
    }

    // 新しいインスタンスの取得 (boat由来)
    public static ChatBoxShare newInstance(String boat) {
        ChatBoxShare inst = new ChatBoxShare();
        inst.MyBoatUrl = uriMyself;

        byte[] bytes = idMyself;
        NetData data = new NetData(bytes.length + 8);
        data.encodeByteArray(bytes, bytes.length);
        data.encodeLong(System.currentTimeMillis());
        inst.idRecord = data.areaTgt;

        return inst;
    }

    // 新しいインスタンスの取得 (データベース由来)
    public static ChatBoxShare newInstance(DbDefineShare.BoxShare rec) throws JSONException {
        ChatBoxShare inst = new ChatBoxShare();
        inst.fromRecord( rec );
        return inst;
    }
}
