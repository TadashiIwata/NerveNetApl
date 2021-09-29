
// データベース用コンテントプロバイダ 構成定義
//
// Copyright (C) 2013-2014 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.song;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * データベース定義 NerveNet基地局データ
 * @author takumi
 * @version 1.1.1
 */
public final class DbDefineSong {
    //公式名
    public static final String AUTHORITY = "jp.co.nassua.nervenet.song";
    
    // コンストラクタ
    //
    private DbDefineSong() {
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
    
    // 列名一覧 取得
    //
    public static String[] getColumnNames( String column_types[][]) {
        String names[] = new String[ column_types.length ];
        
        for (int off = 0; off < column_types.length; off++) {
            names[off] = column_types[off][0];
        }
        return names;
    }
    
    // 列形式一覧(連結済み) 取得
    //
    public static String getColumnDefs( String column_types[][]) {
        String defs = new String();
        
        //列形式一覧 合成
        for (int off = 0; off < column_types.length; off++) {
            if (off > 0) {
                //列区切り
                defs += ", ";
            }
            defs += column_types[off][0];
            defs += " ";
            defs += column_types[off][1];
        }
        
        return defs;
    }
    
    //// 列定義 リンク状態 ////
    
    public static class LinkStat {
        //URI
        public static final String PATH = "linkstat";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        
        //列値 (データベース固有)
        public int mId;                 //ID
        //列値 (外部情報由来)
        public short mNodeId = 0;       //ノードID
        public short mThereId = 0;      //対向ノードID
        public short mPortId = 0;       //ポート番号
        public byte  mCost = 0;         //コスト
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "nodeid",     "SMALLINT" },   //ノードID
            { "thereid",    "SMALLINT" },   //対向ノードID
            { "portid",     "SMALLINT" },   //ポート番号
            { "cost",       "TINYINT" },    //コスト
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ノードID
            values.put( "nodeid", mNodeId);
            //対向ノードID
            values.put( "thereid", mThereId);
            //ポート番号
            values.put( "portid", mPortId);
            //コスト
            values.put( "cost", mCost);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid" )) {
                    mNodeId = cursor.getShort( index);
                }
                //対向ノードID
                if (name.equalsIgnoreCase( "thereid" )) {
                    mThereId = cursor.getShort( index);
                }
                //ポート番号
                if (name.equalsIgnoreCase( "portid" )) {
                    mPortId = cursor.getShort( index);
                }
                //コスト
                if (name.equalsIgnoreCase( "cost" )) {
                    mCost = (byte)cursor.getInt( index);
                }
            }
        }
    }
    
    //// 列定義 経路木 ////
    
    public static class PathTree {
        //URI
        public static final String PATH = "pathtree";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;             //ID
        //列値 (外部情報由来)
        public short mTreeId = 0;   //経路木の識別子
        public short mNodeId1 = 0;  //リンク端ノード1
        public short mNodeId2 = 0;  //リンク端ノード2
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "treeid",    "SMALLINT" },    //経路木の識別子
            { "nodeid1",    "SMALLINT" },   //リンク端ノード1
            { "nodeid2",    "SMALLINT" },   //リンク端ノード2
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //経路木の識別子
            values.put( "treeid", mTreeId);
            //リンク端ノード1
            values.put( "nodeid1", mNodeId1);
            //リンク端ノード2
            values.put( "nodeid2", mNodeId2);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //経路木の識別子
                if (name.equalsIgnoreCase( "treeid" )) {
                    mTreeId = cursor.getShort( index);
                }
                //リンク端ノード1
                if (name.equalsIgnoreCase( "nodeid1" )) {
                    mNodeId1 = cursor.getShort( index);
                }
                //リンク端ノード2
                if (name.equalsIgnoreCase( "nodeid2" )) {
                    mNodeId2 = cursor.getShort( index);
                }
            }
        }
    }
    
    //// 列定義 TSG情報 ////
    
    public static class TsgInf {
        //URI
        public static final String PATH = "tsginf";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;                 //ID
        //列値 (外部情報由来)
        public short mNodeId = 0;       //ノードID
        public String mSipUri = null;   //SIP-URI
        public String mIpAddr = null;   //IPアドレス
        public String mMacAddr = null;  //MACアドレス
        public short mSipPort = 0;      //SIPポート番号
        public String mTunNet = null;   //トンネルIPネットワーク
        public byte  mTunMask = 0;      //トンネルIPネットマスク
        public String mTunRoute = null; //トンネルIPルーティング情報
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "nodeid",     "SMALLINT" },   //ノードID
            { "sipuri",     "TEXT" },       //SIP-URI
            { "ipaddr",     "TEXT" },       //IPアドレス
            { "macaddr",    "TEXT" },       //MACアドレス
            { "sipport",    "SMALLINT" },   //SIPポート番号
            { "tunnet",     "TEXT" },       //トンネルIPネットワーク
            { "tunmask",    "TINYINT" },    //トンネルIPネットマスク
            { "tunroute",   "TEXT" },       //トンネルIPルーティング情報
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ノードID
            values.put( "nodeid", mNodeId);
            //SIP-URI
            if (mSipUri != null) {
                values.put( "sipuri", mSipUri);
            }
            //IPアドレス
            if (mIpAddr != null) {
                values.put( "ipaddr", mIpAddr);
            }
            //MACアドレス
            if (mMacAddr != null) {
                values.put( "macaddr", mMacAddr);
            }
            //SIPポート番号
            values.put( "sipport", mSipPort);
            //トンネルIPネットワーク
            if (mTunNet != null) {
                values.put( "tunnet", mTunNet);
            }
            //トンネルIPネットマスク
            values.put( "tunmask", mTunMask);
            //トンネルIPルーティング情報
            if (mTunRoute != null) {
                values.put( "tunroute", mTunRoute);
            }
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid" )) {
                    mNodeId = cursor.getShort( index);
                }
                //SIP-URI
                if (name.equalsIgnoreCase( "sipuri" )) {
                    mSipUri = cursor.getString( index);
                }
                //IPアドレス
                if (name.equalsIgnoreCase( "ipaddr" )) {
                    mIpAddr = cursor.getString( index);
                }
                //MACアドレス
                if (name.equalsIgnoreCase( "macaddr" )) {
                    mMacAddr = cursor.getString( index);
                }
                //SIPポート番号
                if (name.equalsIgnoreCase( "sipport" )) {
                    mSipPort = cursor.getShort( index);
                }
                //トンネルIPネットワーク
                if (name.equalsIgnoreCase( "tunnet" )) {
                    mTunNet = cursor.getString( index);
                }
                //トンネルIPネットマスク
                if (name.equalsIgnoreCase( "tunmask" )) {
                    mTunMask = (byte)cursor.getInt( index);
                }
                //トンネルIPルーティング情報
                if (name.equalsIgnoreCase( "tunroute" )) {
                    mTunRoute = cursor.getString( index);
                }
            }
        }
    }
    
    //// 列定義 BOAT一覧 ////
    
    public static class BoatList {
        //URI
        public static final String PATH = "boatlist";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;                 //ID
        //列値 (外部情報由来)
        public byte[] mNodeId = null;   //ノードID
        public String mSipUri = null;   //SIP-URI
        public String mPhoneNumber = null;//電話番号
        public String mIpAddr = null;   //IPアドレス
        public long   mUpdateTime = 0;  //更新時刻
        public short  mTsgId = 0;       //基地局ID
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "nodeid",     "BLOB" },       //ノードID
            { "sipuri",     "TEXT" },       //SIP-URI
            { "phonenumber","TEXT" },       //電話番号
            { "ipaddr",     "TEXT" },       //IPアドレス
            { "updatetime", "LONG" },       //更新時刻
            { "tsgid",      "SMALLINT" },   //基地局ID
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ノードID
            if (mNodeId != null) {
                values.put( "nodeid", mNodeId);
            }
            //SIP-URI
            if (mSipUri != null) {
                values.put( "sipuri", mSipUri);
            }
            //電話番号
            if (mPhoneNumber != null) {
                values.put( "phonenumber", mPhoneNumber);
            }
            //IPアドレス
            if (mIpAddr != null) {
                values.put( "ipaddr", mIpAddr);
            }
            //更新時刻
            values.put( "updatetime", mUpdateTime);
            //基地局ID
            values.put( "tsgid", mTsgId);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid" )) {
                    mNodeId = cursor.getBlob( index);
                }
                //SIP-URI
                if (name.equalsIgnoreCase( "sipuri" )) {
                    mSipUri = cursor.getString( index);
                }
                //電話番号
                if (name.equalsIgnoreCase( "phonenumber" )) {
                    mPhoneNumber = cursor.getString( index);
                }
                //IPアドレス
                if (name.equalsIgnoreCase( "ipaddr" )) {
                    mIpAddr = cursor.getString( index);
                }
                //更新時刻
                if (name.equalsIgnoreCase( "updatetime" )) {
                    mUpdateTime = cursor.getLong( index);
                }
                //基地局ID
                if (name.equalsIgnoreCase( "tsgid" )) {
                    mTsgId = cursor.getShort( index);
                }
            }
        }
    }
    
    //// 列定義 経路木生成時のリンク状態 ////
    
    public static class LinkOrg {
        //URI
        public static final String PATH = "linkorg";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        
        //列値 (データベース固有)
        public int mId;                 //ID
        //列値 (外部情報由来)
        public short mLinkId = 0;       //リンクID
        public short mNodeId1 = 0;      //ノードID
        public short mPortId1 = 0;      //ポート番号
        public byte  mCost1 = 0;        //コスト
        public short mNodeId2 = 0;      //ノードID
        public short mPortId2 = 0;      //ポート番号
        public byte  mCost2 = 0;        //コスト
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "linkid",    "SMALLINT" },    //リンクID
            { "nodeid1",    "SMALLINT" },   //ノードID
            { "portid1",    "SMALLINT" },   //ポート番号
            { "cost1",       "TINYINT" },   //コスト
            { "nodeid2",    "SMALLINT" },   //ノードID
            { "portid2",    "SMALLINT" },   //ポート番号
            { "cost2",       "TINYINT" },   //コスト
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //リンクID
            values.put( "linkid", mLinkId);
            //ノードID
            values.put( "nodeid1", mNodeId1);
            //ポート番号
            values.put( "portid1", mPortId1);
            //コスト
            values.put( "cost1", mCost1);
            //ノードID
            values.put( "nodeid2", mNodeId2);
            //ポート番号
            values.put( "portid2", mPortId2);
            //コスト
            values.put( "cost2", mCost2);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //リンクID
                if (name.equalsIgnoreCase( "linkid" )) {
                    mLinkId = cursor.getShort( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid1" )) {
                    mNodeId1 = cursor.getShort( index);
                }
                //ポート番号
                if (name.equalsIgnoreCase( "portid1" )) {
                    mPortId1 = cursor.getShort( index);
                }
                //コスト
                if (name.equalsIgnoreCase( "cost1" )) {
                    mCost1 = (byte)cursor.getInt( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid2" )) {
                    mNodeId2 = cursor.getShort( index);
                }
                //ポート番号
                if (name.equalsIgnoreCase( "portid2" )) {
                    mPortId2 = cursor.getShort( index);
                }
                //コスト
                if (name.equalsIgnoreCase( "cost2" )) {
                    mCost2 = (byte)cursor.getInt( index);
                }
            }
        }
    }
    
    //// 列定義 基地局固有情報 ////
    
    public static class Particular {
        //URI
        public static final String PATH = "particular";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        
        //列値 (データベース固有)
        public int mId;             //ID
        //列値 (外部情報由来)
        public short mNodeId = 0;   //ノードID
        public int  mLatitude = 0;  //緯度
        public int  mLongitude = 0; //経度
        public int  mAltitude = 0;  //高度
        public int  mDirection = 0; //方位
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "nodeid",     "SMALLINT" },   //ノードID
            { "latitude",   "INTEGER" },    //緯度
            { "longitude",  "INTEGER" },    //経度
            { "altitude",   "INTEGER" },    //高度
            { "direction",  "INTEGER" },    //方位
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ノードID
            values.put( "nodeid", mNodeId);
            //緯度
            values.put( "latitude", mLatitude);
            //経度
            values.put( "longitude", mLongitude);
            //高度
            values.put( "altitude", mAltitude);
            //方位
            values.put( "direction", mDirection);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ノードID
                if (name.equalsIgnoreCase( "nodeid" )) {
                    mNodeId = cursor.getShort( index);
                }
                //緯度
                if (name.equalsIgnoreCase( "latitude" )) {
                    mLatitude = cursor.getInt( index);
                }
                //経度
                if (name.equalsIgnoreCase( "longitude" )) {
                    mLongitude = cursor.getInt( index);
                }
                //高度
                if (name.equalsIgnoreCase( "altitude" )) {
                    mAltitude = cursor.getInt( index);
                }
                //方位
                if (name.equalsIgnoreCase( "direction" )) {
                    mDirection = cursor.getInt( index);
                }
            }
        }
    }
    
    //// 列定義 経路 ////
    
    public static class Path {
        //URI
        public static final String PATH = "path";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        
        //列値 (データベース固有)
        public int mId;                 //ID
        //列値 (外部情報由来)
        public short mNodeId1 = 0;      //ノードID1
        public short mNodeId2 = 0;      //ノードID2
        public short mTreeId = 0;       //経路木の識別子
        public short mCost = 0;         //コスト
        
        //列定義
        public static final String COLUMN_TYPES[][] = {
            { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { "nodeid1",    "SMALLINT" },   //ノードID1
            { "nodeid2",    "SMALLINT" },   //ノードID2
            { "treeid",     "SMALLINT" },   //経路木の識別子
            { "cost",       "SMALLINT" },   //コスト
        };
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ノードID1
            values.put( "nodeid1", mNodeId1);
            //ノードID2
            values.put( "nodeid2", mNodeId2);
            //経路木の識別子
            values.put( "treeid", mTreeId);
            //コスト
            values.put( "cost", mCost);
            
            return values;
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ノードID1
                if (name.equalsIgnoreCase( "nodeid1" )) {
                    mNodeId1 = cursor.getShort( index);
                }
                //ノードID2
                if (name.equalsIgnoreCase( "nodeid2" )) {
                    mNodeId2 = cursor.getShort( index);
                }
                //経路木の識別子
                if (name.equalsIgnoreCase( "treeid" )) {
                    mTreeId = cursor.getShort( index);
                }
                //コスト
                if (name.equalsIgnoreCase( "cost" )) {
                    mCost = cursor.getShort( index);
                }
            }
        }
    }
}
