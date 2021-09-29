package jp.co.nassua.nervenet.groupchatmain;

import android.content.ComponentName;

public class ConstantGroup {
    public static final String PKG_GROUP = "jp.co.nassua.nervenet.groupchatmain";
    public static final String CLS_GROUP = PKG_GROUP+".RcvOperate";
    /// アクション ///
    // グループ管理通知
    public static final String ACT_NOTIFY = PKG_GROUP +".NOTIFY";
    // グループ管理要求
    public static final String ACT_REQUEST = PKG_GROUP +".REQUEST";

    /// 要求 ///
    public static final String EXTRA_EVENT = "EVENT";      // イベント
    public static final String EVENT_LOAD_COMPLETE = "COMPLETE"; // DBロード完了
    public static final String EVENT_LOAD_REFLESH_GROUPLIST = "REFLESH_GROUPLIST";  // 画面更新通知
    public static final String EVENT_LOAD_REFLESH_GROUPLIST2 = "REFLESH_GROUPLIST2";  // 画面更新通知
    public static final String EVENT_LOAD_REFLESH_TERMINALLIST = "REFLESH_TERMINALLIST";  // 画面更新通知
    public static final String EVENT_LOAD_REFLESH_TERMINALLIST2 = "EVENT_LOAD_REFLESH_TERMINALLIST2";  // 画面更新通知
    public static final String EVENT_UPDATE_TERMINAL = "EVENT_UPDATE_TERMINAL";  // ユーザ情報更新

    // ダイアログ tag
    public static final String DIALOG_TAG_ADD_GROUP = "addgroup";
    public static final String DIALOG_TAG_ADD_TERMINAL_1 = "addterminal1";
    public static final String DIALOG_TAG_ADD_TERMINAL_2 = "addterminal2";
    public static final String DIALOG_TAG_CONFIRM_TERMINAL = "confirmterminal";

    // アラート種別
    public static final String EVENT_GROUP_JOINED = "GROUP_JOINED";
    public static final String EVENT_GROUPNAME_ALREADY = "GROUPNAME_ALREADY";

    // Query 結果
    public static final int QUERY_STATUS_INVALID = -1;
    public static final int QUERY_STATUS_NOT_FOUND = QUERY_STATUS_INVALID + 1;
    public static final int QUERY_STATUS_JOINED = QUERY_STATUS_NOT_FOUND + 1;
    public static final int QUERY_STATUS_ALREADY = QUERY_STATUS_JOINED + 1;

    private ConstantGroup() {
    }

    /**
     * コンポーネント名を返します。
     * ブロードキャスト宛先の明示的指定に必要です。
     * @return コンポーネント名
     */
    public static ComponentName getComponent() {
        return new ComponentName( PKG_GROUP, CLS_GROUP);
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
