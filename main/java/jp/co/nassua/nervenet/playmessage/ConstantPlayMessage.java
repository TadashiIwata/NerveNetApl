package jp.co.nassua.nervenet.playmessage;

/**
 * Created by I.Tadshi on 2016/08/17.
 */
public class ConstantPlayMessage {
    public static final String PKG_RECORDER = "jp.co.nassua.nervenet.playmessage";
    /// アクション ///
    //録音通知
    public static final String ACT_NOTIFY = PKG_RECORDER +".NOTIFY";
    //録音操作
    public static final String ACT_REQUEST = PKG_RECORDER +".REQUEST";

    /// 通知・操作諸元 ///
    public static final String EXTRA_EVENT      = "EVENT";      // イベント
    public static final String EVENT_START      = "START";      // 再生開始
    public static final String EVENT_STOP       = "STOP";       // 再生終了
    public static final String EVENT_PLAY       = "PLAY";       // 再生中
    public static final String EVENT_INIT       = "INIT";       // 初期化
    public static final String EVENT_EXEC       = "EXEC";       // サービス開始
    // タイマー制御
    public static final String TIMER_START      = "TM_START";  // 再生監視タイマー開始
    public static final String TIMER_STOP       = "TM_STOP";   // 再生監視タイマー停止
    //  状態
    public static final String STAT_PLAY        = "PLAY";       // 再生中
    public static final String STAT_IDLE        = "IDLE";       // 未再生
    public static final String STATE_INIT       = "INIT";       // 初期状態

    // コンストラクタ
    //
    private ConstantPlayMessage() {
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
