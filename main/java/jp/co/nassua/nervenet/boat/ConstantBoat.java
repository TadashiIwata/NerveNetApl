
// NerveNet端末機能 外部定数一覧
//
// Copyright (C) 2012-2019 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.boat;

import android.content.ComponentName;

/**
 * NerveNet端末機能 外部定数一覧
 * @version 2.1.1
 */
public final class ConstantBoat {
    public static final String PKG_BOAT = "jp.co.nassua.nervenet.boat";
    public static final String CLS_BOAT = PKG_BOAT+".RcvOperate";
    /// アクション ///
    //端末共通機能
    public static final String ACT_NODE_REQUEST = PKG_BOAT+".NODE_REQUEST";
    //基地局通知
    public static final String ACT_TSG_NOTIFY   = PKG_BOAT+".TSG_NOTIFY";
    //Moorクライアント
    public static final String ACT_MOOR_NOTIFY  = PKG_BOAT+".MOOR_NOTIFY";
    public static final String ACT_MOOR_REQUEST = PKG_BOAT+".MOOR_REQUEST";
    //SIPプロキシ
    public static final String ACT_SIP_NOTIFY   = PKG_BOAT+".SIP_NOTIFY";
    public static final String ACT_SIP_REQUEST  = PKG_BOAT+".SIP_REQUEST";

    /// 通知諸元 ///
    public static final String EXTRA_EVENT      = "EVENT";      //イベント
    public static final String EXTRA_RESULT     = "RESULT";     //結果
    public static final String EXTRA_ID_TSG     = "ID_TSG";     //基地局ID
    public static final String EXTRA_IP_TSG     = "IP_TSG";     //IPアドレス (基地局)
    public static final String EXTRA_URI_TSG    = "URI_TSG";    //SIP-URI (基地局)
    public static final String EXTRA_TIME_TSG   = "TIME_TSG";   //基地局時刻
    public static final String EXTRA_ID_BOAT    = "ID_BOAT";    //端末ID
    public static final String EXTRA_IP_BOAT    = "IP_BOAT";    //IPアドレス (端末)
    public static final String EXTRA_URI_BOAT   = "URI_BOAT";   //SIP-URI (端末)
    public static final String EXTRA_TIME_UPDATE= "TIME_UPDATE";//更新時刻
    
    public static final String EVENT_START      = "START";      //起動
    public static final String EVENT_STOP       = "STOP";       //停止
    public static final String EVENT_CONF_SIM   = "CONF_SIM";   //端末諸元 内蔵SIM読み取り
    public static final String EVENT_CONF_AUTO  = "CONF_AUTO";  //端末諸元 自動構成
    public static final String EVENT_GETTIME    = "GETTIME";    //基地局時刻 取得
    public static final String EVENT_SETTIME    = "SETTIME";    //基地局時刻 設定
    
    public static final String RESULT_OK        = "OK";         //成功
    public static final String RESULT_NG        = "NG";         //失敗
    
    /// 通知予約諸元 ///
    //通知先
    public static final String EXTRA_SUBSCRIBER = "COMPONENT";
    
    /**
     * コンストラクタ
     */
    private ConstantBoat() {
    }
    
    /**
     * コンポーネント名を返します。
     * ブロードキャスト宛先の明示的指定に必要です。
     * @return コンポーネント名
     */
    public static ComponentName getComponent() {
        return new ComponentName( PKG_BOAT, CLS_BOAT);
    }
    
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static int getVersionCode() {
        //Version 2.1.1
        final int v_code = 0x020101;
        return v_code;
    }
}
