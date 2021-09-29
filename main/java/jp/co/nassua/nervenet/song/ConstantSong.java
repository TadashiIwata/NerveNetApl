
// NerveNet基地局データ 外部定数一覧
//
// Copyright (C) 2015 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.song;

/**
 * NerveNet基地局データ 外部定数一覧
 * @author takumi
 * @version 1.3.12
 */
public final class ConstantSong {
    public static final String PKG_SONG = "jp.co.nassua.nervenet.song";
    /// アクション ///
    //一覧取得
    public static final String ACT_GETLIST_NOTIFY = PKG_SONG+".GETLIST_NOTIFY";
    public static final String ACT_GETLIST_REQUEST = PKG_SONG+".GETLIST_REQUEST";
    //監視予約
    public static final String ACT_SUBSCRIBE_REQUEST = PKG_SONG+".SUBSCRIBE_REQUEST";
    //生存確認
    public static final String ACT_ALIVE = PKG_SONG+".ALIVE";

    /// 通知諸元 ///
    public static final String EXTRA_EVENT      = "EVENT";  //イベント
    public static final String EXTRA_SONG       = "SONG";   //ソング種類
    //イベント
    public static final String EVENT_START      = "START";  //予約登録
    public static final String EVENT_STOP       = "STOP";   //予約取り消し
    //ソング種類
    public static final String SONG_LINKSTAT    = "LINKSTAT";   //リンク状態
    public static final String SONG_PATHTREE    = "PATHTREE";   //経路木
    public static final String SONG_TSGINF      = "TSGINF";     //TSG情報
    public static final String SONG_BOATLIST    = "BOATLIST";   //BOAT一覧
    public static final String SONG_LINKORG     = "LINKORG";    //経路木作成時のリンク状態
    public static final String SONG_PARTICULAR  = "PARTICULAR"; //基地局ごとの固有情報

    // コンストラクタ
    //
    private ConstantSong() {
    }
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static String getVersionName() {
        final String v_name = "1.3.12";
        return v_name;
    }
    public static int getVersionCode() {
        final int v_code = 2;
        return v_code;
    }
}
