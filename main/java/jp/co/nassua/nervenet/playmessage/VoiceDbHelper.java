package jp.co.nassua.nervenet.playmessage;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by I.Tadshi on 2016/07/15.
 */
public class VoiceDbHelper extends SQLiteOpenHelper {
    // DB定義
    private static final String DB_NAME = "voicemessage.db";
    private static final int DB_VERSION = 2;
    private static final int newDBVersion = DB_VERSION;
    private static final int oldDBVersion = DB_VERSION - 1;
    // テーブル、カラム定義
    public static final String TABLE_NAME = "voice_message";
    public static final String COLUMN_ID = "_id";                  //
    public static final String COLINUM_MESSAGEID = "message_id"; // メッセージID
    public static final String COLUMN_RECDATE = "recdate";       // 録音日時
    public static final String COLUMN_PLAY = "play_flag";        // 再生済み
    public static final String COLUMN_INVALID = "invalid_flag"; // 無効
    // カラムプロパティ
    private String idrecord = null;
    private long recDate_id = 0;
    private int play_flag = 0;
    private int invalid_flag = 0;
    // SQL文
    static final String CREATE_TABLE = "create table voice_message( _id integer primary key autoincrement, message_id blob, recdate integer, play_flag integer, invalid_flag integer )";
    //static final String CREATE_TABLE = "create table voice_message( _id integer primary key autoincrement, message_id text, recdate integer, play_flag integer, invalid_flag integer )";

    public VoiceDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("nassua", "Create Table");
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.i("nassua", "DB create Exception.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("nassua", "Upgrade DB Tabele: Old version=" + oldVersion + " New version=" + newVersion);
        if ((oldVersion == oldDBVersion) && (newVersion == newDBVersion)) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) {
                Log.i("nassua", "DB create Exception.");
            }
        }
    }
}
