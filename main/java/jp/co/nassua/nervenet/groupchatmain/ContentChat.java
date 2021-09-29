package jp.co.nassua.nervenet.groupchatmain;

public final class ContentChat {
    public static final String PKG_RECORDER = "jp.co.nassua.nervenet.groupchatmain";
    /// アクション ///
    // 通知
    public static final String ACT_NOTIFY = PKG_RECORDER +".NOTIFY";
    // 操作
    public static final String ACT_REQUEST = PKG_RECORDER +".REQUEST";

    /// 要求 ///
    public static final String EXTRA_EVENT = "EVENT";      // イベント
    public static final String EVENT_LOAD_COMPLETE          = "COMPLETE";      //
    public static final String EVENT_REQUEST_REFRESH_VIEW  = "REFRESH";       //

    // アラート種別
    public static final int ALERT_NICKNAME_NOT_REGISTRATION = 0;
    public static final int ALERT_NOT_SUPPORTED_CAMERA = ALERT_NICKNAME_NOT_REGISTRATION + 1;
    public static final int ALERT_ADD_TERMINAL_SUCCESS = ALERT_NOT_SUPPORTED_CAMERA + 1;


    private ContentChat() {
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
