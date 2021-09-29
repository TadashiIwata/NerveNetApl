package jp.co.nassua.nervenet.service;

import org.json.JSONException;

import jp.co.nassua.nervenet.voicemessage.PrefApp;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicerecorder.VoiceMessage;
import jp.co.nassua.net.utlproto.NetData;

/**
 * Created by I.Tadshi on 2016/07/06.
 */
public class BoxVoice {
    public static byte[] idBox;  // ボックスID
    public static byte[] idMyself; //自局ID
    public static String uriMyself; //自局URI

    public Long recTime;        // 録音時刻
    public String userName;     // 作成者名
    public String uriVoice;     //  録音端末
    public String uriFile;      // 音声データURI
    public byte[] idVoice;     //  メッセージID
    public byte[] bytesVoice;  //音声データ
    private PrefApp pref;

    // コンストラクタ
    private BoxVoice() {
        recTime = null;
        bytesVoice = null;
        idVoice = null;
        uriVoice = null;
    }

    // データベースへの変換
    public DbDefineShare.BoxShare toRecord() {
        DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
        rec.mIdBox = idBox;
        rec.mIdMsg = idVoice;
        rec.mUriBoat = uriVoice;
        rec.mUriAttached = uriFile;
        //rec.mIdLink = idOrder;
        rec.mTimeCalibrate = recTime;
        // メッセージ作成者名
        if ((VoiceMessage.actMyself.myname == null)
        ||  (VoiceMessage.actMyself.myname.equals(""))) {
            pref = new PrefApp();
            pref.readPreference(ActMain.actMyself);
            rec.mBody = ActMain.actMyself.conf_node.mUriBoat;
        } else {
            rec.mBody = VoiceMessage.actMyself.myname;
        }
        //基地局時刻の使用有無
        /*
        if (isTsgTime) {
            rec.mCommon.mIsTsgTime = 1;
        }else{
            rec.mCommon.mIsTsgTime = 0;
        }
        */
        return rec;
    }

    // データベースからの変換
    //
    public boolean fromRecord( DbDefineShare.BoxShare rec ) throws JSONException {
        idVoice = rec.mIdMsg;
        uriVoice = rec.mUriBoat;
         // メッセージ作成者名
        if (rec.mBody != null) {
            userName = rec.mBody;
        }
        // メッセージ添付情報
        if (rec.mAttached != null) {
            bytesVoice = rec.mAttached;
        }
        // 更新時刻
        if (rec.mTimeCalibrate != null) {
            recTime = rec.mTimeCalibrate;
        }
        // 音声ファイル名
        if (rec.mUriAttached != null) {
            uriFile = rec.mUriAttached;
        }
        return true;
    }

    // 新しいインスタンスの取得 (boat由来)
    //
    public static BoxVoice newInstance( String boat) {
        BoxVoice inst = new BoxVoice();
        inst.uriVoice = uriMyself;

        byte[] bytes = idMyself;
        NetData data = new NetData(bytes.length + 8);
        data.encodeByteArray(bytes, bytes.length);
        data.encodeLong(System.currentTimeMillis());
        inst.idVoice = data.areaTgt;

        return inst;
    }

    // 新しいインスタンスの取得 (データベース由来)
    //
    public static BoxVoice newInstance( DbDefineShare.BoxShare rec) throws JSONException {
        BoxVoice inst = new BoxVoice();
        inst.fromRecord( rec );
        return inst;
    }

    public boolean fromRecordByDate( DbDefineShare.BoxShare rec, long recDate ) {
        if (rec.mTimeCalibrate == recDate) {
            return true;
        }
        return false;
    }

}
