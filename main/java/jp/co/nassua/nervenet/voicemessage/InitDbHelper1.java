package jp.co.nassua.nervenet.voicemessage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by I.Tadshi on 2017/10/06.
 */

class InitDbHelper1 extends SQLiteOpenHelper {
    // DB定義
    private static final String DB_NAME = "voicemessage.db";
    private static final int DB_VERSION = 2;

    public InitDbHelper1(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("nassua", "No create table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("nassua", "No upgrade DB tabele.");
    }
}
