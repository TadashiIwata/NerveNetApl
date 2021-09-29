package jp.co.nassua.nervenet.voicerecorder;

/**
 * Created by I.Tadshi on 2016/08/01.
 */
public final class ConstantRecorder {
    public static final String PKG_RECORDER = "jp.co.nassua.nervenet.voicerecorder";
    /// アクション ///
    //録音通知
    public static final String ACT_NOTIFY = PKG_RECORDER +".NOTIFY";
    //録音操作
    public static final String ACT_REQUEST = PKG_RECORDER +".REQUEST";

    /// 通知・操作諸元 ///
    public static final String EXTRA_EVENT      = "EVENT";      //イベント
    public static final String EVENT_START      = "START";      //録音開始
    public static final String EVENT_STOP       = "STOP";       //録音停止
    public static final String EVENT_CANCEL     = "CANCEL";       //録音中断
    public static final String EVENT_SEND       = "SEND";       //送信中
    public static final String EVENT_SHOW       = "SHOW";       // 送信完了表示
    public static final String EVENT_READ       = "READ";       //読み取り開始
    public static final String EVENT_SEND_END     = "SEND_END";  // 再送信完了
    public static final String EVENT_BSCHECK    = "BSCHECK";  // 基地局状態チェック

    // コンストラクタ
    //
    private ConstantRecorder() {
    }
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static String getVersionName() {
        final String v_name = "1.0.0";
        return v_name;
    }
}
