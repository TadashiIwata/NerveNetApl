
// データベース用コンテントプロバイダ 構成定義
//
// Copyright (C) 2016-2019 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.share;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.io.PrintWriter;
import java.io.StringWriter;

//// データベース定義 端末間情報共有 ////

public class DbDefineShare {
    //公式名
    public static final String AUTHORITY = "jp.co.nassua.nervenet.share";
    //履歴
    public static final int VCODE_2_2_1 = 0x020201;
    // BoxShare.mIsAutosend 利用可能

    // コンストラクタ
    //
    public DbDefineShare() {
    }
    
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static int getVersionCode() {
        final int v_code = VCODE_2_2_1;
        return v_code;
    }
    
    // 16進ダンプ
    //
    public static String toHex( byte[] data) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw);
        
        int size = data.length;
        for (int off = 0; off < size; off++) {
            pw.format( "%02x", data[off] & 0x0ff);
        }
        return sw.toString();
    }
    // フラグ有効/無効の検索条件
    //
    public static String whereFlag( String column_name, boolean is_on) {
        StringBuilder sb = new StringBuilder();
        sb.append( "(");
        if (is_on) {
            sb.append( "(");
            sb.append( column_name); sb.append( " IS NOT NULL");
            sb.append( ")" );
            sb.append( " AND ");
            sb.append( column_name); sb.append( "<>0");
        }else{
            sb.append( "(");
            sb.append( column_name); sb.append( " IS NULL");
            sb.append( ")" );
            sb.append( " OR ");
            sb.append( column_name); sb.append( "=0");
        }
        sb.append( ")" );
        return sb.toString();
    }

    // 列名一覧 取得
    //
    public static String[] getColumnNames( String column_types[][]) {
        String names[] = new String[ column_types.length + Common.COLUMN_TYPES.length ];
        
        int dst = 0;
        //共通項目
        for (String[] field: Common.COLUMN_TYPES) {
            names[dst++] = field[0];
        }
        //個別項目
        for (String[] field: column_types) {
            names[dst++] = field[0];
        }
        return names;
    }
    
    // 列形式一覧(連結済み) 取得
    //
    public static String getColumnDefs( String column_types[][]) {
        StringBuilder sb = new StringBuilder();
        //列形式一覧 合成 (共通項目)
        for (String[] field: Common.COLUMN_TYPES) {
            if (field[1] == null) {
                continue;
            }
            sb.append( "," );
            //↑列区切り
            sb.append( field[0] );
            sb.append( " " );
            sb.append( field[1] );
        }
        //列形式一覧 合成
        for (String[] field: column_types) {
            if (field[1] == null) {
                continue;
            }
            sb.append( "," );
            //↑列区切り
            sb.append( field[0] );
            sb.append( " " );
            sb.append( field[1] );
        }
        //余計な列区切りを排除
        if (sb.length() > 0 && sb.charAt( 0 ) == ',') {
            sb.deleteCharAt( 0 );
        }
        return sb.toString();
    }
    
    //// 列定義 共通フィールド ////
    
    public static class Common {
        //列値
        public byte[] mNodeUpdate;  //レコード更新ノード
        public long   mTimeUpdate;  //レコード更新時刻
        public long   mTimeDiscard; //レコード廃棄時刻
        public long   mTimeSync;    //レコード同期完了時刻
        public short  mFlagInvalid; //レコード無効フラグ
        public byte   mIsTsgTime;   //基地局時刻の使用有無
    
        //列定義
        public static final String COLUMN_NODE_UPDATE   = "node_update";
        public static final String COLUMN_TIME_UPDATE   = "time_update";
        public static final String COLUMN_TIME_DISCARD  = "time_discard";
        public static final String COLUMN_TIME_SYNC     = "time_sync";
        public static final String COLUMN_FLAG_INVALID  = "flag_invalid";
        public static final String COLUMN_IS_TSGTIME    = "is_tsgtime";
        public static final String COLUMN_TYPES[][] = {
                { COLUMN_NODE_UPDATE,    "BLOB" },   //レコード更新ノード
                { COLUMN_TIME_UPDATE,    "LONG" },   //レコード更新時刻
                { COLUMN_TIME_DISCARD,   "LONG" },   //レコード廃棄時刻
                { COLUMN_TIME_SYNC,      "LONG" },   //レコード同期完了時刻
                { COLUMN_FLAG_INVALID,   "SMALLINT"},//レコード無効フラグ
                { COLUMN_IS_TSGTIME,     "TINYINT"}, //基地局時刻の使用有無
        };
    
        // コンストラクタ (クラス外からの使用は禁止)
        //
        private Common() {
            mNodeUpdate = null;
            mTimeUpdate = 0;
            mTimeDiscard = 0;
            mTimeSync = 0;
            mFlagInvalid = 0;
            mIsTsgTime = 0;
        }
        // 新しいインスタンスの生成
        //
        public static Common newInstance() {
            Common common = new Common();
            return common;
        }
        
        // insert用values取得
        //
        public void getForInsert( ContentValues values) {
            //レコード更新ノード
            values.put( COLUMN_NODE_UPDATE, mNodeUpdate);
            //レコード更新時刻
            values.put( COLUMN_TIME_UPDATE, mTimeUpdate);
            //レコード廃棄時刻
            values.put( COLUMN_TIME_DISCARD, mTimeDiscard);
            //レコード同期完了時刻
            values.put( COLUMN_TIME_SYNC, mTimeSync);
            //レコード無効フラグ
            values.put( COLUMN_FLAG_INVALID, mFlagInvalid);
            //基地局時刻の使用有無
            values.put( COLUMN_IS_TSGTIME, mIsTsgTime);
        }
        
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //レコード更新ノード
                if (name.equalsIgnoreCase( COLUMN_NODE_UPDATE )) {
                    mNodeUpdate = cursor.getBlob( index );
                }
                //レコード更新時刻
                if (name.equalsIgnoreCase( COLUMN_TIME_UPDATE )) {
                    mTimeUpdate = cursor.getLong( index);
                }
                //レコード廃棄時刻
                if (name.equalsIgnoreCase( COLUMN_TIME_DISCARD )) {
                    mTimeDiscard = cursor.getLong( index );
                }
                //レコード同期完了時刻
                if (name.equalsIgnoreCase( COLUMN_TIME_SYNC )) {
                    mTimeSync = cursor.getLong( index );
                }
                //レコード無効フラグ
                if (name.equalsIgnoreCase( COLUMN_FLAG_INVALID )) {
                    mFlagInvalid = cursor.getShort( index);
                }
                //基地局時刻の使用有無
                if (name.equalsIgnoreCase( COLUMN_IS_TSGTIME )) {
                    mIsTsgTime = (byte)cursor.getInt( index);
                }
            }
        }
        // 廃棄されていないレコードの検索条件
        //
        public static String whereNotDiscarded( long time_current, long tag_tsg) {
            StringBuilder sb = new StringBuilder();
            //廃棄時刻
            sb.append( whereFlag( COLUMN_TIME_DISCARD, false ));
            sb.append( "OR");
            if (tag_tsg != 0) {
                sb.append( "(is_tsgtime=0 AND time_discard>=" );
                sb.append( Long.toString( time_current, 10) );
                sb.append( ")" );
                sb.append( "OR");
                sb.append( "(is_tsgtime<>0 AND time_discard>=" );
                sb.append( Long.toString( time_current+tag_tsg, 10) );
                sb.append( ")" );
            }else{
                sb.append( "(time_discard>=" );
                sb.append( Long.toString( time_current, 10) );
                sb.append( ")" );
            }
            return sb.toString();
        }
        // 有効なレコードの検索条件
        //
        public static String whereValid( long time_current, long lag_tsg) {
            StringBuilder sb = new StringBuilder();
            sb.append( "(" );
            {
                //無効フラグ
                sb.append( whereFlag( COLUMN_FLAG_INVALID, false ));
            }
            //論理積
            sb.append( ")AND(");
            {
                //廃棄時刻
                sb.append( whereNotDiscarded( time_current, lag_tsg ));
            }
            sb.append( ")" );
            return sb.toString();
        }
    }
    
    //// 列定義 ボックスマスタ ////
    
    public static class BoxMaster {
        //URI
        public static final String PATH = "boxmaster";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;         //ID
        //列値 (外部情報由来)
        public Common mCommon;  //共通フィールド
        public byte[] mIdBox;   //ボックスID
        public String mName;    //ボックス名
        public short  mPermit;  //アクセス許可
        public String mUriOwner;//所有者URI
        
        //列定義
        public static final String COLUMN_ID_BOX    = "id_box";
        public static final String COLUMN_NAME      = "name";
        public static final String COLUMN_PERMIT    = "permit";
        public static final String COLUMN_URI_OWNER = "uri_owner";
        public static final String COLUMN_TYPES[][] = {
                { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
                { COLUMN_ID_BOX,    "BLOB" },       //ボックスID
                { COLUMN_NAME,      "TEXT" },       //ボックス名
                { COLUMN_PERMIT,    "SMALLINT" },   //アクセス許可
                { COLUMN_URI_OWNER, "TEXT" },       //所有者URI
        };
        public static final String COLUMN_ALIAS[][] = {
        };
        
        //非参加端末へのアクセス許可
        public static final short PERMIT_SHARE_READ = 0x0001;   //共有情報 読取
        public static final short PERMIT_SHARE_WRITE = 0x0002;  //共有情報 書込
        
        // コンストラクタ (クラス外からの使用は禁止)
        //
        private BoxMaster() {
            mId = 0;
            mCommon = null;
            mIdBox = null;
            mName = null;
            mPermit = 0;
            mUriOwner = null;
        }
        // 新しいインスタンスの生成
        //
        public static BoxMaster newInstance() {
            BoxMaster inst = new BoxMaster();
            inst.mCommon = Common.newInstance();
            return inst;
        }
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //共通フィールド
            if (mCommon != null) {
                mCommon.getForInsert( values );
            }
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //ボックスID
            if (mIdBox != null) {
                values.put( COLUMN_ID_BOX, mIdBox );
            }
            //ボックス名
            if (mName != null) {
                values.put( COLUMN_NAME, mName );
            }
            //アクセス許可
            values.put( COLUMN_PERMIT, mPermit );
            //所有者URI
            if (mUriOwner != null) {
                values.put( COLUMN_URI_OWNER, mUriOwner );
            }
            return values;
        }
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            //共通フィールド
            mCommon = Common.newInstance();
            mCommon.setFromQuery( cursor );
    
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //ボックスID
                if (name.equalsIgnoreCase( COLUMN_ID_BOX )) {
                    mIdBox = cursor.getBlob( index );
                }
                //ボックス名
                if (name.equalsIgnoreCase( COLUMN_NAME )) {
                    mName = cursor.getString( index );
                }
                //アクセス許可
                if (name.equalsIgnoreCase( COLUMN_PERMIT )) {
                    mPermit = cursor.getShort( index );
                }
                //所有者URI
                if (name.equalsIgnoreCase( COLUMN_URI_OWNER )) {
                    mUriOwner = cursor.getString( index );
                }
            }
        }
        // DB同期のレコードIDの検索条件
        //
        public String whereIdRecord() {
            StringBuilder sb = new StringBuilder();
            sb.append( COLUMN_ID_BOX );
            sb.append( "=x'");
            sb.append(toHex( mIdBox));
            sb.append( "'" );
            return sb.toString();
        }
    }
    
    //// 列定義 ボックス参加テーブル ////
    
    public static class BoxMember {
        //URI
        public static final String PATH = "boxmember";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;         //ID
        //列値 (外部情報由来)
        public Common mCommon;  //共通フィールド
        public byte[] mIdRecord;//レコードID
        public byte[] mIdBox;   //ボックスID
        public String mUriBoat; //端末URI
        public String mName;    //端末名
        public short  mAuthority;//権限
        
        //列定義
        public static final String COLUMN_ID_RECORD = "id_record";
        public static final String COLUMN_ID_BOX    = "id_box";
        public static final String COLUMN_URI_BOAT  = "uri_boat";
        public static final String COLUMN_NAME      = "name";
        public static final String COLUMN_AUTHORITY = "authority";
        public static final String COLUMN_TYPES[][] = {
                { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
                { COLUMN_ID_RECORD,  "BLOB" },       //レコードID
                { COLUMN_ID_BOX,     "BLOB" },       //ボックスID
                { COLUMN_URI_BOAT,   "TEXT" },       //端末URI
                { COLUMN_NAME,       "TEXT" },       //端末名
                { COLUMN_AUTHORITY,  "SMALLINT" },   //権限
        };
        public static final String COLUMN_ALIAS[][] = {
        };
        
        //権限
        public static final short AUTHORITY_SHARE_READ = 0x0001;    //共有情報 読取
        public static final short AUTHORITY_SHARE_WRITE = 0x0002;   //共有情報 書込
        public static final short AUTHORITY_MEMBER_READ = 0x0004;   //参加 読取
        public static final short AUTHORITY_MEMBER_WRITE = 0x0008;  //参加 書込
        public static final short AUTHORITY_MASTER_UPDATE = 0x0020; //マスタ 更新
        
        // コンストラクタ (クラス外からの使用は禁止)
        //
        private BoxMember() {
            mId = 0;
            mCommon = null;
            mIdRecord = null;
            mIdBox = null;
            mUriBoat = null;
            mName = null;
            mAuthority = AUTHORITY_SHARE_READ | AUTHORITY_SHARE_WRITE |
                    AUTHORITY_MEMBER_READ;
        }
        // 新しいインスタンスの生成
        //
        public static BoxMember newInstance() {
            BoxMember inst = new BoxMember();
            inst.mCommon = Common.newInstance();
            return inst;
        }
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //共通フィールド
            if (mCommon != null) {
                mCommon.getForInsert( values );
            }
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //レコードID
            if (mIdRecord != null) {
                values.put( COLUMN_ID_RECORD, mIdRecord );
            }
            //ボックスID
            if (mIdBox != null) {
                values.put( COLUMN_ID_BOX, mIdBox );
            }
            //端末URI
            if (mUriBoat != null) {
                values.put( COLUMN_URI_BOAT, mUriBoat );
            }
            //端末名
            if (mName != null) {
                values.put( COLUMN_NAME, mName );
            }
            //権限
            values.put( COLUMN_AUTHORITY, mAuthority );
            
            return values;
        }
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            //共通フィールド
            mCommon = Common.newInstance();
            mCommon.setFromQuery( cursor );
            
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //レコードID
                if (name.equalsIgnoreCase( COLUMN_ID_RECORD )) {
                    mIdRecord = cursor.getBlob( index );
                }
                //ボックスID
                if (name.equalsIgnoreCase( COLUMN_ID_BOX )) {
                    mIdBox = cursor.getBlob( index );
                }
                //端末URI
                if (name.equalsIgnoreCase( COLUMN_URI_BOAT )) {
                    mUriBoat = cursor.getString( index );
                }
                //端末名
                if (name.equalsIgnoreCase( COLUMN_NAME )) {
                    mName = cursor.getString( index );
                }
                //権限
                if (name.equalsIgnoreCase( COLUMN_AUTHORITY )) {
                    mAuthority = cursor.getShort( index );
                }
            }
        }
        // DB同期のレコードIDの検索条件
        //
        public String whereIdRecord() {
            StringBuilder sb = new StringBuilder();
            sb.append( COLUMN_ID_RECORD );
            sb.append( "=x'");
            sb.append(toHex( mIdRecord));
            sb.append( "'" );
            return sb.toString();
        }
    }
    
    //// 列定義 ボックス共有情報テーブル ////
    
    public static class BoxShare {
        //URI
        public static final String PATH = "boxshare";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        //列値 (データベース固有)
        public int mId;         //ID
        //列値 (外部情報由来)
        public Common mCommon;  //共通フィールド
        public byte[] mIdMsg;   //メッセージID
        public byte[] mIdBox;   //ボックスID
        public String mUriBoat; //端末URI
        public String mBody;    //メッセージ本文
        public String mUriAttached; //メッセージ添付情報のURI
        public byte[] mAttached;    //メッセージ添付情報
        public byte[] mIdLink;      //リンクID
        public Long mTimeCalibrate; //較正対象時刻
        public Byte   mIsAutoSend;  //自動送信フラグ
    
        //列定義
        public static final String COLUMN_ID_MSG    = "id_msg";
        public static final String COLUMN_ID_BOX    = "id_box";
        public static final String COLUMN_URI_BOAT  = "uri_boat";
        public static final String COLUMN_BODY      = "body";
        public static final String COLUMN_URI_ATTACH= "uri_attached";
        public static final String COLUMN_ATTACHED  = "attached";
        public static final String COLUMN_ID_LINK   = "id_link";
        public static final String COLUMN_CALIBRATE = "time_calibrate";
        public static final String COLUMN_AUTOSEND  = "is_autosend";
        public static final String COLUMN_TYPES[][] = {
                { "_id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
                { COLUMN_ID_MSG,    "BLOB" },   //メッセージID
                { COLUMN_ID_BOX,    "BLOB" },   //ボックスID
                { COLUMN_URI_BOAT,  "TEXT" },   //端末URI
                { COLUMN_BODY,      "TEXT" },   //メッセージ本文
                { COLUMN_URI_ATTACH,"TEXT" },   //メッセージ添付情報のURI
                { COLUMN_ATTACHED,  "BLOB" },   //メッセージ添付情報
                { COLUMN_ID_LINK,   "BLOB" },   //リンクID
                { COLUMN_CALIBRATE, "LONG" },   //較正対象時刻
                { COLUMN_AUTOSEND,  "TINYINT"}, //自動送信フラグ
        };
        public static final String COLUMN_ALIAS[][] = {
        };
        
        // コンストラクタ (クラス外からの使用は禁止)
        //
        private BoxShare() {
            mId = 0;
            mCommon = null;
            mIdMsg = null;
            mIdBox = null;
            mUriBoat = null;
            mBody = null;
            mUriAttached = null;
            mAttached = null;
            mIdLink = null;
            mTimeCalibrate = null;
            mIsAutoSend = null;
        }
        // 新しいインスタンスの生成
        //
        public static BoxShare newInstance() {
            BoxShare inst = new BoxShare();
            inst.mCommon = Common.newInstance();
            return inst;
        }
        
        // insert用values取得
        //
        public ContentValues getForInsert() {
            ContentValues values = new ContentValues();
            //共通フィールド
            if (mCommon != null) {
                mCommon.getForInsert( values );
            }
            //ID
            //values.put( "_id", mId); AUTOINCREMENTなので不要
            //メッセージID
            if (mIdMsg != null) {
                values.put( COLUMN_ID_MSG, mIdMsg );
            }
            //ボックスID
            if (mIdBox != null) {
                values.put( COLUMN_ID_BOX, mIdBox );
            }
            //端末URI
            if (mUriBoat != null) {
                values.put( COLUMN_URI_BOAT, mUriBoat );
            }
            //メッセージ本文
            if (mBody != null) {
                values.put( COLUMN_BODY, mBody );
            }
            //メッセージ添付情報のURI
            if (mUriAttached != null) {
                values.put( COLUMN_URI_ATTACH, mUriAttached );
            }
            //メッセージ添付情報
            if (mAttached != null) {
                values.put( COLUMN_ATTACHED, mAttached );
            }
            //リンクID
            if (mIdLink != null) {
                values.put( COLUMN_ID_LINK, mIdLink );
            }
            //較正対象時刻
            if (mTimeCalibrate != null) {
                values.put( COLUMN_CALIBRATE, mTimeCalibrate );
            }
            //自動送信フラグ
            if (mIsAutoSend != null) {
                values.put( COLUMN_AUTOSEND, mIsAutoSend);
            }
            
            return values;
        }
        // queryから項目設定
        //
        public void setFromQuery( Cursor cursor) {
            //共通フィールド
            mCommon = Common.newInstance();
            mCommon.setFromQuery( cursor );
    
            int index, count = cursor.getColumnCount();
            for (index = 0; index < count; index++) {
                String name = cursor.getColumnName( index);
                //ID
                if (name.equalsIgnoreCase( "_id" )) {
                    mId = cursor.getInt( index);
                }
                //メッセージID
                if (name.equalsIgnoreCase( COLUMN_ID_MSG )) {
                    mIdMsg = cursor.getBlob( index );
                }
                //ボックスID
                if (name.equalsIgnoreCase( COLUMN_ID_BOX )) {
                    mIdBox = cursor.getBlob( index );
                }
                //端末URI
                if (name.equalsIgnoreCase( COLUMN_URI_BOAT )) {
                    mUriBoat = cursor.getString( index );
                }
                //メッセージ本文
                if (name.equalsIgnoreCase( COLUMN_BODY )) {
                    mBody = cursor.getString( index );
                }
                //メッセージ添付情報のURI
                if (name.equalsIgnoreCase( COLUMN_URI_ATTACH )) {
                    mUriAttached = cursor.getString( index );
                }
                //メッセージ添付情報
                if (name.equalsIgnoreCase( COLUMN_ATTACHED )) {
                    mAttached = cursor.getBlob( index );
                }
                //リンクID
                if (name.equalsIgnoreCase( COLUMN_ID_LINK )) {
                    mIdLink = cursor.getBlob( index );
                }
                //較正対象時刻
                if (name.equalsIgnoreCase( COLUMN_CALIBRATE ) && ! cursor.isNull( index )) {
                    mTimeCalibrate = cursor.getLong( index );
                }
                //自動送信フラグ
                if (name.equalsIgnoreCase( COLUMN_AUTOSEND ) && ! cursor.isNull( index )) {
                    mIsAutoSend = (byte)cursor.getInt( index);
                }
            }
        }
        // DB同期のレコードIDの検索条件
        //
        public String whereIdRecord() {
            StringBuilder sb = new StringBuilder();
            sb.append( COLUMN_ID_MSG );
            sb.append( "=x'");
            sb.append(toHex( mIdMsg));
            sb.append( "'" );
            return sb.toString();
        }
    }
    //// 列定義 ファイル操作 ////
    public static class File {
        //URI
        public static final String PATH = "file";
        public static final Uri CONTENT_URI = Uri.parse( "content://" + DbDefineShare.AUTHORITY + "/" + PATH );
    }
}
