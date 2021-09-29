
// コンテントプロバイダ 構成定義
//
// Copyright (C) 2015-2017 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.boat;

import android.database.Cursor;
import android.net.Uri;

/**
 * データベース定義 NerveNet端末
 * @author takumi
 * @version 1.8.1
 */
public class DbDefineBoat {
    //公式名
    public static final String AUTHORITY = "jp.co.nassua.nervenet.boat";

    // コンストラクタ
    //
    private DbDefineBoat() {
    }
    
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static int getVersionCode() {
        //Version 1.8.1
        final int v_code = 0x010801;
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

    //// 列定義 端末諸元 ////
    public static class ConfNode {
        //URI
        public static final String PATH;
        public static final Uri CONTENT_URI;
        //列値
        public byte[] mIdBoat;      //BOAT-ID
        public String mUriBoat;     //SIP-URI
        public String mPhoneNumber; //電話番号
        //列値 ネットワーク判断
        public String mDomainNervenet;  //Nervenet DNSドメイン名
        public String mSubnetNervent;   //NerveNet サブネット
        public String mSsidNervenet;    //NerveNet SSID
        //列定義
        public static final String COLUMN_ID_BOAT       = "id_boat";        //BOAT-ID
        public static final String COLUMN_URI_BOAT      = "uri_boat";       //SIP-URI
        public static final String COLUMN_PHONE_NUMBER  = "phone_number";   //電話番号
        //列定義 ネットワーク判断
        public static final String COLUMN_DOMAIN_NERVENET   = "domain_nervenet";//Nervenet DNSドメイン名
        public static final String COLUMN_SUBNET_NERVENET   = "subnet_nervenet";//Nervenet サブネット
        public static final String COLUMN_SSID_NERVENET     = "ssid_nervenet";  //Nervenet SSID
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_ID_BOAT, COLUMN_URI_BOAT, COLUMN_PHONE_NUMBER,
                COLUMN_DOMAIN_NERVENET, COLUMN_SUBNET_NERVENET, COLUMN_SSID_NERVENET,
        };
    
        // コンストラクタ
        //
        public ConfNode () {
            mIdBoat = null;
            mUriBoat = null;
            mPhoneNumber = null;
            mDomainNervenet = null;
            mSubnetNervent = null;
            mSsidNervenet = null;
        }
        // 初期化ブロック
        //
        static {
            //URI
            PATH = "conf_node";
            CONTENT_URI = Uri.parse( "content://" + DbDefineBoat.AUTHORITY + "/" + PATH );
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //BOAT-ID
                if (name.equalsIgnoreCase( COLUMN_ID_BOAT )) {
                    mIdBoat = cursor.getBlob( index);
                }
                //SIP-URI
                if (name.equalsIgnoreCase( COLUMN_URI_BOAT )) {
                    mUriBoat = cursor.getString( index);
                }
                //電話番号
                if (name.equalsIgnoreCase( COLUMN_PHONE_NUMBER )) {
                    mPhoneNumber = cursor.getString( index);
                }
                //Nervenet DNSドメイン名
                if (name.equalsIgnoreCase( COLUMN_DOMAIN_NERVENET )) {
                    mDomainNervenet = cursor.getString( index);
                }
                //Nervenet サブネット
                if (name.equalsIgnoreCase( COLUMN_SUBNET_NERVENET )) {
                    mSubnetNervent = cursor.getString( index);
                }
                //Nervenet SSID
                if (name.equalsIgnoreCase( COLUMN_SSID_NERVENET )) {
                    mSsidNervenet = cursor.getString( index);
                }
            }
        }
    }
    //// 列定義 端末状態 ////
    public static class StatNode {
        //URI
        public static final String PATH;
        public static final Uri CONTENT_URI;
        //列値
        public Short mIdTsg;    //収容先基地局 ID
        public String mIpTsg;   //収容先基地局 IPアドレス
        public String mUriTsg;  //収容先基地局 SIP-URI
        public Long mRagTsg;    //収容先基地局の時差 (端末時刻基準の差分)
        public long mRagLast;   //収容先基地局の時差 (最後に検出した値)
        public String mIpBoat;  //対基地局の 自局IPアドレス
        public Boolean mRunMoor;//Moorクライアント機能の稼働
        public Boolean mRunSip; //SIPプロキシ機能の稼働
        public Integer mBoatVcode;//Boat.apk バージョン番号
        //列定義
        public static final String COLUMN_ID_TSG    = "id_tsg";     //収容先基地局 ID
        public static final String COLUMN_IP_TSG    = "ip_tsg";     //収容先基地局 IPアドレス
        public static final String COLUMN_URI_TSG   = "uri_tsg";    //収容先基地局 SIP-URI
        public static final String COLUMN_RAG_TSG   = "rag_tsg";    //収容先基地局の時差 (端末時刻基準の差分)
        public static final String COLUMN_RAG_LAST  = "rag_last";   //収容先基地局の時差 (最後に検出した値)
        public static final String COLUMN_IP_BOAT   = "ip_boat";    //対基地局の 自局IPアドレス
        public static final String COLUMN_RUN_MOOR  = "run_moor";   //Moorクライアント機能の稼働
        public static final String COLUMN_RUN_SIP   = "run_sip";    //SIPプロキシ機能の稼働
        public static final String COLUMN_BOAT_VCODE= "boat_vcode"; //Boat.apk バージョン番号
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_ID_TSG, COLUMN_IP_TSG, COLUMN_URI_TSG,
                COLUMN_RAG_TSG, COLUMN_RAG_LAST,
                COLUMN_IP_BOAT, COLUMN_RUN_MOOR, COLUMN_RUN_SIP,
                COLUMN_BOAT_VCODE,
        };
    
        // コンストラクタ
        //
        public StatNode () {
            mIdTsg = null;
            mIpTsg = null;
            mUriTsg = null;
            mRagTsg = null;
            mRagLast = 0;
            mIpBoat = null;
            mRunMoor = null;
            mRunSip = null;
            mBoatVcode = null;
        }
        // 初期化ブロック
        //
        static {
            PATH = "stat_node";
            CONTENT_URI = Uri.parse( "content://" + DbDefineBoat.AUTHORITY + "/" + PATH );
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //収容先基地局 ID
                if (name.equalsIgnoreCase( COLUMN_ID_TSG )) {
                    if (cursor.isNull( index )) {
                        mIdTsg = null;
                    }else {
                        mIdTsg = cursor.getShort( index );
                    }
                }
                //収容先基地局 IPアドレス
                if (name.equalsIgnoreCase( COLUMN_IP_TSG )) {
                    mIpTsg = cursor.getString( index );
                }
                //収容先基地局 SIP-URI
                if (name.equalsIgnoreCase( COLUMN_URI_TSG )) {
                    mUriTsg = cursor.getString( index);
                }
                //収容先基地局の時差 (端末時刻基準の差分)
                if (name.equalsIgnoreCase( COLUMN_RAG_TSG )) {
                    if (cursor.isNull( index )) {
                        mRagTsg = null;
                    }else {
                        mRagTsg = cursor.getLong( index );
                    }
                }
                //収容先基地局の時差 (最後に検出した値)
                if (name.equalsIgnoreCase( COLUMN_RAG_LAST )) {
                    if (cursor.isNull( index )) {
                        mRagLast = 0;
                    }else {
                        mRagLast = cursor.getLong( index );
                    }
                }
                //対基地局の 自局IPアドレス
                if (name.equalsIgnoreCase( COLUMN_IP_BOAT )) {
                    mIpBoat = cursor.getString( index );
                }
                //Moorクライアント機能の稼働
                if (name.equalsIgnoreCase( COLUMN_RUN_MOOR )) {
                    mRunMoor = booleanFromQuery( cursor.getShort( index ) );
                }
                //SIPプロキシ機能の稼働
                if (name.equalsIgnoreCase( COLUMN_RUN_SIP )) {
                    mRunSip = booleanFromQuery( cursor.getShort( index ) );
                }
                //Boat.apk バージョン番号
                if (name.equalsIgnoreCase( COLUMN_BOAT_VCODE )) {
                    if (cursor.isNull( index )) {
                        mBoatVcode = null;
                    }else {
                        mBoatVcode = cursor.getInt( index );
                    }
                }
            }
        }
    }
    //// 列定義 Moor諸元 ////
    public static class ConfMoor {
        //URI
        public static final String PATH;
        public static final Uri CONTENT_URI;
        //列値
        public Boolean mAutoStart;      //自動起動
        public String mAddrServer;      //サーバアドレス
        public Short mPortServer;       //サーバポート番号
        public Short mPortUdp;          //UDPポート番号
        public Integer mPeriodConnect;  //接続待ち時間
        public Integer mIntvlConnect;   //接続試行間隔
        public Integer mPeriodAlive;    //生存確認時間
        public Integer mIntvlAlive;     //生存確認送信間隔
        //列定義
        public static final String COLUMN_IS_AUTOSTART  = "is_autostart";   //自動起動
        public static final String COLUMN_ADDR_SERVER   = "addr_server";    //サーバアドレス
        public static final String COLUMN_PORT_SERVER   = "port_server";    //サーバポート番号
        public static final String COLUMN_PORT_UDP      = "port_udp";       //UDPポート番号
        public static final String COLUMN_PERIOD_CONNECT= "period_connect"; //接続待ち時間
        public static final String COLUMN_INTVL_CONNECT = "intvl_connect";  //接続試行間隔
        public static final String COLUMN_PERIOD_ALIVE  = "period_alive";   //生存確認時間
        public static final String COLUMN_INTVL_ALIVE   = "intvl_alive";    //生存確認送信間隔
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_IS_AUTOSTART,
                COLUMN_ADDR_SERVER, COLUMN_PORT_SERVER, COLUMN_PORT_UDP,
                COLUMN_PERIOD_CONNECT, COLUMN_INTVL_CONNECT,
                COLUMN_PERIOD_ALIVE, COLUMN_INTVL_ALIVE,
        };
        
        // コンストラクタ
        //
        public ConfMoor () {
            mAutoStart = null;
            mAddrServer = null;
            mPortServer = null;
            mPortUdp = null;
            mPeriodConnect = null;
            mIntvlConnect = null;
            mPeriodAlive = null;
            mIntvlAlive = null;
        }
        // 初期化ブロック
        //
        static {
            PATH = "conf_moor";
            CONTENT_URI = Uri.parse( "content://" + DbDefineBoat.AUTHORITY + "/" + PATH );
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //自動起動
                if (name.equalsIgnoreCase( COLUMN_IS_AUTOSTART )) {
                    mAutoStart = booleanFromQuery( cursor.getShort( index ) );
                }
                //サーバアドレス
                if (name.equalsIgnoreCase( COLUMN_ADDR_SERVER )) {
                    mAddrServer = cursor.getString( index );
                }
                //サーバポート番号
                if (name.equalsIgnoreCase( COLUMN_PORT_SERVER )) {
                    mPortServer = cursor.getShort( index );
                }
                //UDPポート番号
                if (name.equalsIgnoreCase( COLUMN_PORT_UDP )) {
                    mPortUdp = cursor.getShort( index );
                }
                //接続待ち時間
                if (name.equalsIgnoreCase( COLUMN_PERIOD_CONNECT )) {
                    mPeriodConnect = cursor.getInt( index );
                }
                //接続試行間隔
                if (name.equalsIgnoreCase( COLUMN_INTVL_CONNECT )) {
                    mIntvlConnect = cursor.getInt( index );
                }
                //生存確認時間
                if (name.equalsIgnoreCase( COLUMN_PERIOD_ALIVE )) {
                    mPeriodAlive = cursor.getInt( index );
                }
                //生存確認送信間隔
                if (name.equalsIgnoreCase( COLUMN_INTVL_ALIVE )) {
                    mIntvlAlive = cursor.getInt( index );
                }
            }
        }
    }
    //// 列定義 SIP諸元 ////
    public static class ConfSip {
        //URI
        public static final String PATH;
        public static final Uri CONTENT_URI;
        //列値
        public Boolean mAutoStart;  //自動起動
        public Short mPortUdp;      //対基地局UDP待ち受けポート番号
        public Short mPortServer;   //端末内TCP待ち受けポート番号
        public String mAddrServer;  //端末内TCP待ち受けアドレス
        public String mUriLocal;    //端末内REGIST先SIP-URI
        //列定義
        public static final String COLUMN_IS_AUTOSTART      = "is_autostart";   //自動起動
        public static final String COLUMN_PORT_UDP          = "port_udp";       //対基地局UDP待ち受けポート番号
        public static final String COLUMN_PORT_TCPSERVER    = "port_server";    //端末内TCP待ち受けポート番号
        public static final String COLUMN_ADDR_TCPSERVER    = "addr_server";    //端末内TCP待ち受けアドレス
        public static final String COLUMN_URI_LOCAL         = "uri_local";      //端末内REGIST先SIP-URI
        //列定義一覧
        public static final String[] COLUMNS = {
                "_id", COLUMN_IS_AUTOSTART,
                COLUMN_PORT_UDP, COLUMN_PORT_TCPSERVER, COLUMN_ADDR_TCPSERVER,
                COLUMN_URI_LOCAL,
        };
    
        // コンストラクタ
        //
        public ConfSip () {
            mAutoStart = null;
            mPortUdp = null;
            mPortServer = null;
            mAddrServer = null;
            mUriLocal = null;
        }
        // 初期化ブロック
        //
        static {
            PATH = "conf_sip";
            CONTENT_URI = Uri.parse( "content://" + DbDefineBoat.AUTHORITY + "/" + PATH );
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //自動起動
                if (name.equalsIgnoreCase( COLUMN_IS_AUTOSTART )) {
                    mAutoStart = booleanFromQuery( cursor.getShort( index ) );
                }
                //対基地局UDP待ち受けポート番号
                if (name.equalsIgnoreCase( COLUMN_PORT_UDP )) {
                    mPortUdp = cursor.getShort( index );
                }
                //端末内TCP待ち受けポート番号
                if (name.equalsIgnoreCase( COLUMN_PORT_TCPSERVER )) {
                    mPortServer = cursor.getShort( index );
                }
                //端末内TCP待ち受けアドレス
                if (name.equalsIgnoreCase( COLUMN_ADDR_TCPSERVER )) {
                    mAddrServer = cursor.getString( index );
                }
                //端末内REGIST先SIP-URI
                if (name.equalsIgnoreCase( COLUMN_URI_LOCAL )) {
                    mUriLocal = cursor.getString( index );
                }
            }
        }
    }
}
