
// NerveNet通話機能 外部定数一覧
//
// Copyright (C) 2012-2014 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.phone;

/**
 * NerveNet通話機能 外部定数一覧
 * @author takumi
 * @version 1.1.1
 */
public final class ConstantPhone {
    public static final String PKG_PHONE = "jp.co.nassua.nervenet.phone";
    /// アクション ///
    //通話通知
    public static final String ACT_NOTIFY = PKG_PHONE +".NOTIFY";
    //通話操作
    public static final String ACT_REQUEST = PKG_PHONE +".REQUEST";
    
    /// 通知・操作諸元 ///
    public static final String EXTRA_EVENT      = "EVENT";      //イベント
    public static final String EXTRA_PHASE      = "PHASE";      //通話状態
    public static final String EXTRA_URI_THERE  = "URI_THERE";  //SIP-URI (対向局)
    
    public static final String EVENT_START      = "START";      //起動
    public static final String EVENT_STOP       = "STOP";       //停止
    public static final String EVENT_CONNECT    = "CONNECT";    //接続確立
    public static final String EVENT_DISCONNECT = "DISCONNECT"; //接続解放

    /// 通話状態 ///
    public static final String PHASE_INIT        = "INIT";      //初期状態
    public static final String PHASE_IDLE        = "IDLE";      //待機中
    public static final String PHASE_CALLING     = "CALLING";   //発呼中
    public static final String PHASE_INCOMMING   = "INCOMMING"; //着呼中
    public static final String PHASE_CONNECTED   = "CONNECTED"; //通話中
    
    // コンストラクタ
    //
    private ConstantPhone() {
    }
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static String getVersionName() {
        final String v_name = "1.1.1";
        return v_name;
    }
    public static int getVersionCode() {
        final int v_code = 1;
        return v_code;
    }
}
