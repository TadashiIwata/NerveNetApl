package jp.co.nassua.nervenet.groupchatmain;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import java.io.StringWriter;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;

public class CursorLoaderBoxMember implements Loader.OnLoadCompleteListener<Cursor> {

    private static ActMain actMain = null;
    private static CursorLoader cursorLoader;
    private static Context loader_context;
    private static ChatCommon chatCommon = null;
    private static GroupCommon groupCommon = null;
    private GroupList groupList;
    private GroupItem groupItem;
    //公式名
    private static final String AUTHORITY = "jp.co.nassua.nervenet.share";
    private static final String PATH = "boxmember";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    public CursorLoaderBoxMember() {
        if (actMain == null) {
            actMain = new ActMain();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupItem == null) {
            groupItem = new GroupItem();
        }
    }

    // CursorLoader作成 (BoxMember用)
    public void createLoader(Context context) {

        String mysipuri = chatCommon.getMySIPURI();

        cursorLoader = new CursorLoader(context);
        cursorLoader.setUri(DbDefineShare.BoxMember.CONTENT_URI);

        StringWriter sw = new StringWriter();
        // 有効なレコードのみ対称
        sw.write("(");
        //無効フラグ
        sw.write(DbDefineShare.Common.whereValid(System.currentTimeMillis(), 0));
        //論理積
        //sw.write(")");  //デバッグ用
        sw.write(")AND(");
        //ボックスID
        sw.write("uri_boat='");
        sw.write(mysipuri);
        sw.write("')");

        String sql = sw.toString();
        cursorLoader.setSelection(sql);

        String[] projection = {};
        cursorLoader.setProjection(projection);
        cursorLoader.registerListener(0, this);
        cursorLoader.startLoading();

        loader_context = context;
        //return cursorLoader;
        return;
    }

    // CursorLoader破棄
    public void destroyLoader() {
        if (cursorLoader != null) {
            cursorLoader.stopLoading();
           // cursorLoader.unregisterListener(this);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        loader_context = loader.getContext();
        Context context;
        context = loader_context;
        //context = actMain.actMyself.getBaseContext();
        if (cursor != null) {
            // DBから XML にグループを登録する。
            groupCommon.setBoxMembercursor(cursor);
            Intent intent = new Intent();
            intent.setAction(ConstantGroup.ACT_NOTIFY);
            intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_COMPLETE);
            //context.sendBroadcast(intent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
