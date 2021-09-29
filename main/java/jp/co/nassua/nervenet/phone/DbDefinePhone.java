
// コンテントプロバイダ 構成定義
//
// Copyright (C) 2015 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.phone;

import android.database.Cursor;
import android.net.Uri;

/**
 * データベース定義 NerveNet通話機能
 * @author takumi
 * @version 1.1.1
 */
public class DbDefinePhone {
    //公式名
    public static final String AUTHORITY = "jp.co.nassua.nervenet.phone";

    // コンストラクタ
    //
    private DbDefinePhone() {
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
    // 真偽値に変換
    //
    public static Boolean booleanFromQuery( Short value) {
        if (value != null) {
            if (value.shortValue() == 1) {
                //真
                return true;
            }
            //偽
            return false;
        }
        //値なし
        return null;
    }
    public static Short booleanToQuery( Boolean value) {
        if (value != null) {
            if (value.booleanValue()) {
                //真
                return 1;
                //偽
            }
            return 0;
        }
        //値なし
        return null;
    }
    // Byteに変換
    //
    public static Byte byteFromQuery( Short value) {
        if (value != null) {
            return value.byteValue();
        }
        //値なし
        return null;
    }
    public static Short byteToQuery( Byte value) {
        if (value != null) {
            return value.shortValue();
        }
        //値なし
        return null;
    }
    
    //// 列定義 通話諸元 ////
    public static class ConfPhone {
        //URI
        public static final String PATH = "conf_phone";
        public static final Uri CONTENT_URI = Uri.parse( "content://" + DbDefinePhone.AUTHORITY + "/" + PATH );
        //列値
        public String mAddrProxy = null;    //IPアドレス (SIPプロキシ)
        public Short mPortProxy = null;     //ポート番号 (SIPプロキシ)
        public String mUriProxy = null;     //SIP-URI (SIPプロキシ)
        public String mUriMine = null;      //SIP-URI (自局)
        //列定義
        public static final String COLUMN_ADDR_PROXY    = "addr_proxy";    //IPアドレス (SIPプロキシ)
        public static final String COLUMN_PORT_PROXY    = "port_proxy";    //ポート番号 (SIPプロキシ)
        public static final String COLUMN_URI_PROXY     = "uri_proxy";      //SIP-URI (SIPプロキシ)
        public static final String COLUMN_URI_MINE      = "uri_mine";       //SIP-URI (自局)
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_ADDR_PROXY, COLUMN_PORT_PROXY,
                COLUMN_URI_PROXY, COLUMN_URI_MINE,
        };
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //IPアドレス (SIPプロキシ)
                if (name.equalsIgnoreCase( COLUMN_ADDR_PROXY )) {
                    mAddrProxy = cursor.getString( index );
                }
                //ポート番号 (SIPプロキシ)
                if (name.equalsIgnoreCase( COLUMN_PORT_PROXY )) {
                    mPortProxy = cursor.getShort( index );
                }
                //SIP-URI (SIPプロキシ)
                if (name.equalsIgnoreCase( COLUMN_URI_PROXY )) {
                    mUriProxy = cursor.getString ( index );
                }
                //SIP-URI (自局)
                if (name.equalsIgnoreCase( COLUMN_URI_MINE )) {
                    mUriMine = cursor.getString( index );
                }
            }
        }
    }
    //// 列定義 通話状態 ////
    public static class StatPhone {
        //URI
        public static final String PATH = "stat_phone";
        public static final Uri CONTENT_URI = Uri.parse( "content://" + DbDefinePhone.AUTHORITY + "/" + PATH );
        //列値
        public String mPhase = null;    //通話状態
        public String mIpThere = null;  //IPアドレス
        public String mUriThere = null; //SIP-URI
        public Boolean mRunSip = null;  //SIPプロキシとの接続
        public Boolean mRunPhone = null;//通話機能の稼働
        //列定義
        public static final String COLUMN_PHASE     = "phase";      //通話状態
        public static final String COLUMN_IP_THERE  = "ip_there";   //IPアドレス
        public static final String COLUMN_URI_THERE = "uri_there";  //SIP-URI
        public static final String COLUMN_RUN_SIP   = "run_sip";    //SIPプロキシとの接続
        public static final String COLUMN_RUN_PHONE = "run_phone";  //通話機能の稼働
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_PHASE, COLUMN_IP_THERE, COLUMN_URI_THERE,
                COLUMN_RUN_SIP, COLUMN_RUN_PHONE,
        };

        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //IPアドレス
                if (name.equalsIgnoreCase( COLUMN_PHASE )) {
                    mPhase = cursor.getString( index );
                }
                //IPアドレス
                if (name.equalsIgnoreCase( COLUMN_IP_THERE )) {
                    mIpThere = cursor.getString( index );
                }
                //SIP-URI
                if (name.equalsIgnoreCase( COLUMN_URI_THERE )) {
                    mUriThere = cursor.getString( index);
                }
                //SIPプロキシとの接続
                if (name.equalsIgnoreCase( COLUMN_RUN_SIP)) {
                    mRunSip = booleanFromQuery( cursor.getShort( index ) );
                }
                //通話機能の稼働
                if (name.equalsIgnoreCase( COLUMN_RUN_PHONE)) {
                    mRunPhone = booleanFromQuery( cursor.getShort( index ) );
                }
            }
        }
    }
}
