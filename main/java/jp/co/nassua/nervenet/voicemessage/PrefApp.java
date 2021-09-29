
// Pinger アプリケーション設定
//
// Copyright (C) 2015 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.voicemessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PrefApp {
    //設定ファイル名
    public static final String FILE_NAME = "PhoneDepend PrefApp";

    //設定項目
    public boolean isLog;   //ログ出力有無
    public int levelLog;    //ログ出力レベル
    
    public boolean useSubscribe;    //通知予約 使用有無
    public boolean usePhonenumber;  //電話番号 使用有無
    public String call_boatURI = "boat-99";	// 発信先URI
    public boolean useBlob;     //画像をBLOBに保存

    public static final int LV_EMERG = 0;
    public static final int LV_ERROR = 3;
    public static final int LV_WARN = 4;
    public static final int LV_NOTICE= 5;
    public static final int LV_INFO= 6;
    public static final int LV_DEBUG = 7;
    public static final int LV_DUMP = 8;

    // コンストラクタ
    //
    public PrefApp() {
        isLog = false;
        levelLog = LV_NOTICE;
    
        useSubscribe = false;
        usePhonenumber = false;
        useBlob = false;
    }

    // 設定読み取り
    // 
    public void readPreference( Context context) {
        //他プロセスから読み書き可能
        int mode = Context.MODE_PRIVATE;

        //読み取り
        SharedPreferences pref = context.getSharedPreferences( FILE_NAME, mode);
        if (pref != null) {
            String name;
            //ログ出力有無
            name = "LogOutput";
            if (pref.contains( name)) {
                isLog = pref.getBoolean(name, false);
            }
            //ログ出力レベル
            name = "LogLevel";
            if (pref.contains( name)) {
                levelLog = this.getInt( pref, name, LV_EMERG );
            }
    
            //通知予約 使用有無
            name = "UseSubscribe";
            if (pref.contains( name)) {
                useSubscribe = pref.getBoolean( name, false );
            }
            //電話番号 使用有無
            name = "UsePhonenumber";
            if (pref.contains( name)) {
                usePhonenumber = pref.getBoolean( name, false );
            }
            //URI (自身)
            name = "UriCall";
            if (pref.contains( name)) {
                call_boatURI = pref.getString( name, "boat-99");
            }
            //データをFILEに保存
            name = "UseBlob";
            if (pref.contains( name)) {
                useBlob = pref.getBoolean(name, false);
            }
        }
    }
    // 数値取り出し
    //
    private int getInt( SharedPreferences pref, String name, int defval) {
        if (pref.contains( name)) try {
            //文字列形式で取り出し
            String str = pref.getString( name, null);
            if (str == null) {
                //設定値がないので、デフォルト値を参照
                return defval;
            }else{
                //文字列を数値として解析
                return Integer.parseInt( str);
            }
        }catch (ClassCastException e) {
            //解析に失敗したので、数値形式で取り出し
            return pref.getInt( name, defval);
        }catch (Exception e) {
        }
        //詳細不明のエラーが発生したので、デフォルト値を参照
        return defval;
    }
    // 設定書き込み
    //
    public boolean savePreference( Context context) {
        //他プロセスから読み書き可能
        int mode = Context.MODE_PRIVATE;

        //読み取り
        SharedPreferences pref = context.getSharedPreferences( FILE_NAME, mode);
        if (pref != null) {
            Editor editor = pref.edit();
            //ログ出力有無
            editor.putBoolean("LogOutput", isLog);
            //ログ出力レベル
            editor.putString( "LogLevel", String.valueOf( levelLog ) );
    
            //通知予約 使用有無
            editor.putBoolean("UseSubscribe", useSubscribe);
            //電話番号 使用有無
            editor.putBoolean("UsePhonenumber", usePhonenumber);
            //音声をFILEに保存
            editor.putBoolean( "UseBlob", useBlob );
            //保存
            editor.commit();
            return true;
        }
        return false;
    }

    // ログ出力有無 判断
    //
    public boolean wantedLog( int level) {
        if (isLog && level <= levelLog) {
            return true;
        }
        return false;
    }
}
