
// BOAT API操作
//
// Copyright (C) 2015 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.vmphonedepend;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.phone.DbDefinePhone;

/**
 * BOAT API操作
 */
public class BoatApi {
    //定数
    private static final String logLabel = "PhoneDepend";
    
    /**
     * 端末諸元 取得
     */
    public static DbDefineBoat.ConfNode readConfNode( Context cont) {
        //端末諸元テーブル
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        DbDefineBoat.ConfNode ret = null;
        //コンテントプロバイダへの接続
        ContentResolver resolver = cont.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient( uri_tbl );
        if (client != null) try {
            //端末諸元 読み取り
            Cursor cursor = client.query( uri_tbl, null, null, null, null );
            if (cursor != null) {
                //先頭のレコードに移動
                if (cursor.moveToFirst()) {
                    //取り出し
                    ret = new DbDefineBoat.ConfNode();
                    ret.setFromQuery( cursor );
                }
                //カーソルの後始末
                cursor.close();
            }
        } catch (RemoteException e) {
            Log.e( logLabel, "readConfNode "+e.getMessage() );
        } finally {
            //コンテントプロバイダの後始末
            client.release();
        }
        //取得した情報
        return ret;
    }
    /**
     * 端末状態 取得
     */
    public static DbDefineBoat.StatNode readStatNode( Context cont) {
        //端末状態テーブル
        Uri uri_tbl = DbDefineBoat.StatNode.CONTENT_URI;
        DbDefineBoat.StatNode ret = null;
        //コンテントプロバイダへの接続
        ContentResolver resolver = cont.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient( uri_tbl );
        if (client != null) try {
            //端末状態 読み取り
            Cursor cursor = client.query( uri_tbl, null, null, null, null );
            if (cursor != null) {
                //先頭のレコードに移動
                if (cursor.moveToFirst()) {
                    //取り出し
                    ret = new DbDefineBoat.StatNode();
                    ret.setFromQuery( cursor );
                }
                //カーソルの後始末
                cursor.close();
            }
        } catch (RemoteException e) {
            Log.e( logLabel, "readStatNode "+e.getMessage() );
        } finally {
            //コンテントプロバイダの後始末
            client.release();
        }
        //取得した情報
        return ret;
    }
    /**
     * 通話諸元 取得
     */
    public static DbDefinePhone.ConfPhone readConfPhone( Context cont) {
        //通話諸元テーブル
        Uri uri_tbl = DbDefinePhone.ConfPhone.CONTENT_URI;
        DbDefinePhone.ConfPhone ret = null;
        //コンテントプロバイダへの接続
        ContentResolver resolver = cont.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient( uri_tbl );
        if (client != null) try {
            //通話諸元 読み取り
            Cursor cursor = client.query( uri_tbl, null, null, null, null );
            if (cursor != null) {
                //先頭のレコードに移動
                if (cursor.moveToFirst()) {
                    //取り出し
                    ret = new DbDefinePhone.ConfPhone();
                    ret.setFromQuery( cursor );
                }
                //カーソルの後始末
                cursor.close();
            }
        } catch (RemoteException e) {
            Log.e( logLabel, "readConfPhone "+e.getMessage() );
        } finally {
            //コンテントプロバイダの後始末
            client.release();
        }
        //取得した情報
        return ret;
    }
    /**
     * 通話状態 取得
     */
    public static DbDefinePhone.StatPhone readStatPhone( Context cont) {
        //通話状態テーブル
        Uri uri_tbl = DbDefinePhone.StatPhone.CONTENT_URI;
        DbDefinePhone.StatPhone ret = null;
        //コンテントプロバイダへの接続
        ContentResolver resolver = cont.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient( uri_tbl );
        if (client != null) try {
            //通話状態 読み取り
            Cursor cursor = client.query( uri_tbl, null, null, null, null );
            if (cursor != null) {
                //先頭のレコードに移動
                if (cursor.moveToFirst()) {
                    //取り出し
                    ret = new DbDefinePhone.StatPhone();
                    ret.setFromQuery( cursor );
                }
                //カーソルの後始末
                cursor.close();
            }
        } catch (RemoteException e) {
            Log.e( logLabel, "readStatPhone "+e.getMessage() );
        } finally {
            //コンテントプロバイダの後始末
            client.release();
        }
        //取得した情報
        return ret;
    }
}
