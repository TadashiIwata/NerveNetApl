package jp.co.nassua.nervenet.groupchatmain;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import java.io.StringWriter;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.groupchatmain.ChatItem;
import jp.co.nassua.nervenet.groupchatmain.GroupCommon;

public class CursorLoaderChatBoxShare implements Loader.OnLoadCompleteListener<Cursor> {

    private static ActMain actMain;
    private static CursorLoader cursorLoader;
    private static Context loader_context;
    private static ChatCommon chatCommon = null;
    private static String currentId = null;
    private static ChatBoxShare chatBoxShare;
    private static GroupCommon groupCommon;
    private static ChatItem chatItem;

    public CursorLoaderChatBoxShare() {
        if (actMain == null) {
            actMain = new ActMain();
        }
        if (chatItem == null) {
            chatItem = new ChatItem();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
    }

    // CursorLoader作成 (Chat Message用)
    public void createLoader(Context context, String gruupname) {
        cursorLoader = new CursorLoader(context);
        cursorLoader.setUri(DbDefineShare.BoxShare.CONTENT_URI);

        byte[] boxName;
        StringWriter sw = new StringWriter();

        boxName = gruupname.getBytes();
        sw.write("(");
        //無効フラグ
        sw.write(DbDefineShare.Common.whereValid(System.currentTimeMillis(), 0));
        //論理積
        sw.write(")AND(");
        //ボックスID
        sw.write("id_box=x'");
        sw.write(toHex(boxName));
        sw.write("')");
        cursorLoader.setSelection(sw.toString());

        String[] projection = {};
        cursorLoader.setProjection(projection);
        cursorLoader.registerListener(0, this);
        cursorLoader.startLoading();

        loader_context = context;
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }

        //return cursorLoader;
        return;
    }

    private String toHex(byte[] data) {
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        for (byte d : data) {
            stringBuilder.append(String.format("%02X", d));
        }
        str = stringBuilder.toString();
        return str;
    }

    // CursorLoader破棄
    public void destroyLoader() {
        //if (prefLog.logCamera >= PrefLog.LV_DEBUG) {
        //    Log.d( logLabel, "destroyLoader");
        // }
        if (cursorLoader != null) {
            cursorLoader.stopLoading();
            cursorLoader.unregisterListener(this);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        /* refresfviewを呼び出す。*/
        if (chatCommon  == null) {
            chatCommon = new ChatCommon();
        }
        Context context = chatCommon.getChatContext();
        Intent intent = new Intent();
        intent.setAction(ContentChat.ACT_NOTIFY);
        intent.putExtra(ContentChat.EXTRA_EVENT, ContentChat.EVENT_LOAD_COMPLETE);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        context.sendBroadcast(intent);
    }
}
