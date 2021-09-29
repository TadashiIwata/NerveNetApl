package jp.co.nassua.nervenet.playmessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONException;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.vmphonedepend.UserProfilePermission;
import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.net.utlproto.NetData;

/**
 * Created by I.Tadshi on 2016/07/21.
 */
public class PlayMessageService extends Service implements Loader.OnLoadCompleteListener<Cursor> {
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String SELECT_DATE = "select * from " + VoiceDbHelper.TABLE_NAME + " where " + VoiceDbHelper.COLUMN_RECDATE + "=";
    private static final String SELECT_MESSAGE_ID = "select * from " + VoiceDbHelper.TABLE_NAME + " where " + VoiceDbHelper.COLINUM_MESSAGEID + "=x";
    private static long StartUpTime;
    private static boolean autoplay;
    private static boolean pastplay;
    private static boolean startForeground = false;
    private static String ToastText;
    private static String myname;
    private CursorLoader cursorLoader;
    private BroadcastReceiver playNotify;
    private SQLiteDatabase db;
    public static PlayMessageService actMyself = null;  //プロセス上の単一インスタンス
    PlayMessageAsyncTask playMessageAsyncTask;
    private static PlayMessageAsyncTask pmd;
    private static PlayMessageCommon pmc;
    UserProfilePermission userProfilePermission;

    //定数
    private static final String logLabel = "Play Message Service";
    private static final int NOTIFICATION_ID = R.layout.activity_act_main;

    public PlayMessageService() {
        // ユーザプロファイルアクセス権限
        userProfilePermission = new UserProfilePermission();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("nassua","onCreate Service PlayMessageService");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!startForeground) {
                Notification notification = new Notification();
                Intent intent = new Intent(getApplicationContext(), ActMain.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                notification = builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setTicker("")
                        .setAutoCancel(true).setContentTitle("VoiceMessage")
                        .setContentText("PlayMessageService").build();
                startForeground(1, notification);
                startForeground = true;
                Log.i("nassua", "onCreate: PlayMessageService Start Foreground.");
            }
        }
        // Toast用メッセージ取得
        ToastText = getResources().getString(R.string.msgplay_receive);
        pmc = new PlayMessageCommon();
        Intent intent = new Intent(ConstantPlayMessage.ACT_NOTIFY);
        intent.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.EVENT_INIT);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("nassua", "PlayMessageService service end.");
        destroyLoader(cursorLoader);
        pmc.setPlayMessageServiceStatus(false);
        super.onDestroy();
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId) {
        Log.i("nassua","onStartCommand Service PlayMessageService");
        if (pmc.getPlayMessageServiceStatus()) {
            // サービス起動済みなら終了する。
            Log.i("nassua", "PlayMessageService is already started.");
            stopSelf();
            return START_NOT_STICKY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!startForeground) {
                Log.i("nassua", "PlayMessageService is already start.");
                Notification notification = new Notification();
                Intent intent1 = new Intent(getApplicationContext(), ActMain.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                notification = builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setTicker("")
                        .setAutoCancel(true).setContentTitle("VoiceMessage")
                        .setContentText("PlayMessageService").build();
                startForeground(1, notification);
                startForeground = true;
                Log.i("nassua", "onStartCommand: PlayMessageService Start Foreground.");
            }
        }
        // 連絡表へのアクセスが許可されているかチェックする。
        boolean readflag;
        readflag = userProfilePermission.getReadProfileStatus();
        if (!readflag) {
            // 連絡表へのアクセスが許可されていない時はサービスを終了する。
            Log.i("nassua", "Access to the user profile is not allowed.");
            stopSelf();
            return START_NOT_STICKY;
        }


        Cursor mycur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        mycur.moveToFirst();
        int nidx = mycur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
        int cnt = mycur.getCount();
        if (cnt > 0) {
            myname = mycur.getString(nidx);
        } else {
            myname = null;
        }

        ActMain actMain = new ActMain();
        if (actMain.actMyself != null) {
            autoplay = actMain.getPlayMode();
            // 自動再生モードが設定されていなければ終了する。
            if (!autoplay) {
                Log.i("nassua", "Disable automatic playback mode.");
                stopSelf();
                return START_NOT_STICKY;
            }
        } else {
            //端末間情報共有の生存確認を開始
            startAliveShare();
        }
        actMyself = this;
        pmd = new PlayMessageAsyncTask();
        StartUpTime = System.currentTimeMillis();
        // メッセージ一覧が表示されていなければ
        //CursorLoader初期化
        if (cursorLoader == null) {
            cursorLoader = createLoader();
        }
        pmc.setPlayMessageServiceStatus(true);

        // サービス開始通知
        Intent intentact = new Intent(ConstantPlayMessage.ACT_NOTIFY);
        intentact.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.EVENT_EXEC);
        sendBroadcast(intentact);

        return START_NOT_STICKY;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        pmd.messageList = null;
        pmd.messageList = new ArrayList<>();
        fromDb(cursor, getRagLast());
        // 自動再生設定有り
        ActMain actMain = new ActMain();
        if (actMain != null) {
            autoplay = actMain.getPlayMode();
            pastplay = actMain.getPastPlayMode();
            if (autoplay) {
                pmc.dbCleanup(cursor);
                // 自動再生が許可されていれば再生を行う。
                if (!pmc.getLockMessagePlay()) {
                    //if ((VoiceMessage.actMyself == null) && (phonedepend.actMyself == null) && (ActRinging.actMyself == null)) {
                    Intent intent = new Intent(ConstantPlayMessage.ACT_REQUEST);
                    intent.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.EVENT_START);
                    sendBroadcast(intent);
                } else {
                    Log.i("nassus", "Message playback is locked.");
                }
            } else {
                Log.i("nassua", "Disable automatic playback mode.");
            }
        } else {
            Log.i("nassua", "Instance acquisition failure of ActMain.");
        }
        return;
    }

    // CursorLoader作成
    //
    private CursorLoader createLoader() {
        /*
        if (prefLog.logCamera >= PrefLog.LV_DEBUG) {
            Log.d( logLabel, "createLoader");
        }
        */
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(DbDefineShare.BoxShare.CONTENT_URI);

        StringWriter sw = new StringWriter();
        sw.write("(");
        //無効フラグ
        sw.write(DbDefineShare.Common.whereValid(System.currentTimeMillis(), getRagLast()));
        //論理積
        sw.write(")AND(");
        //ボックスID
        sw.write("id_box=x'");
        sw.write(NetData.toHex(BoxVoice.idBox));
        sw.write("')");
        loader.setSelection(sw.toString());

        String[] projection = {};
        loader.setProjection(projection);

        loader.registerListener(0, this);
        loader.startLoading();
        return loader;
    }

    private void destroyLoader(CursorLoader loader) {
        /*
        if (prefLog.logCamera >= PrefLog.LV_DEBUG) {
            Log.d( logLabel, "destroyLoader");
        }
        */
        if (loader != null) {
            loader.stopLoading();
            loader.unregisterListener(this);
        }
    }
    // Load完了

    // 基地局時差 取得
    //
    private long getRagLast() {
        //端末状態 取得
        DbDefineBoat.StatNode stat_node = new DbDefineBoat.StatNode();
        Uri uri_tbl = DbDefineBoat.StatNode.CONTENT_URI;
        ContentProviderClient resolver = getContentResolver().acquireContentProviderClient(uri_tbl);
        if (resolver != null) try {
            Cursor cursor = resolver.query(uri_tbl, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    stat_node.setFromQuery(cursor);
                }
                cursor.close();
            }
        } catch (RemoteException e) {
        /*
        if (prefLog.logCamera >= PrefLog.LV_WARN) {
            Log.w(logLabel, "no stat-node err=" + e.getMessage());
        }
        */
            e.printStackTrace();
        } finally {
            resolver.release();
        }
    /*
    if (prefLog.logCamera >= PrefLog.LV_INFO) {
        StringWriter sw = new StringWriter();
        if (stat_node.mRagTsg != null) {
            sw.write( "rag_tsg=" );
            sw.write( stat_node.mRagTsg.toString() );
        }
        sw.write( " rag_last=" );
        sw.write( Long.toString( stat_node.mRagLast ) );
        Log.i( logLabel, sw.toString());
    }
    */
        return stat_node.mRagLast;
    }
    // 端末間情報共有の生存確認を開始
    //
    private void startAliveShare() {
        //端末間情報共有 起動
        Intent intent = new Intent( ConstantShare.ACT_START);
        sendBroadcast( intent );
    }

    public boolean fromDb(Cursor cursor, long rag) {
        String str_sql;
        Cursor cvm;
        PlayMessageAsyncTask.MessageManager wkmsgMng[];
        boolean bret;

        // メッセージ管理DBアクセス開始
        bret = false;
        VoiceDbHelper voiceDbHelper = new VoiceDbHelper(this);
        db = voiceDbHelper.getWritableDatabase();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                pmd.MessageTotal = 0;
                do {
                    DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
                    rec.setFromQuery(cursor);
                    try {
                        BoxVoice voice = BoxVoice.newInstance(rec);
                        // メッセージ管理DBに登録済みのデータか調べる
                        str_sql = SELECT_MESSAGE_ID + "'" + pmc.ByteToHex(voice.idVoice) + "'";
                        cvm = db.rawQuery(str_sql, null);
                        int cnt = cvm.getCount();
                        if (cnt != 0) {
                            cvm.moveToFirst();
                            // 有効なデータかチェックする。
                            int invalid_flag = cvm.getInt(cvm.getColumnIndex(voiceDbHelper.COLUMN_INVALID));
                            if (invalid_flag == 0) {
                                pmd.MessageTotal++;
                                pmd.messageList.add(voice);
                            }
                        } else {
                            // 新規は一覧に追加
                            pmd.MessageTotal++;
                            pmd.messageList.add(voice);
                            ContentValues values = new ContentValues();
                            values.put(voiceDbHelper.COLINUM_MESSAGEID, voice.idVoice);
                            values.put(voiceDbHelper.COLUMN_RECDATE, voice.recTime);
                            values.put(voiceDbHelper.COLUMN_PLAY, 0);
                            values.put(voiceDbHelper.COLUMN_INVALID, 0);
                            db.insert(voiceDbHelper.TABLE_NAME, null, values);
                        }
                        cvm.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());

                /* 確認用 */
                Cursor cvmm = db.rawQuery("select * from " + VoiceDbHelper.TABLE_NAME, null);
                int cvmmcnt = cvmm.getCount();
                Log.i("nassua", "Total record counts=" + cvmmcnt);

                if (pmd.MessageTotal > 0) {
                    if (pmd.msgMng == null) {
                        wkmsgMng = null;
                    } else {
                        wkmsgMng = new PlayMessageAsyncTask.MessageManager[pmd.msgMng.length];
                        wkmsgMng = pmd.msgMng;
                    }
                    pmd.msgMng = new PlayMessageAsyncTask.MessageManager[pmd.MessageTotal];
                    for (int idx = 0; idx < pmd.MessageTotal; idx++) {
                        pmd.msgMng[idx] = pmd.new MessageManager();
                        pmd.msgMng[idx].msgInvalid = false;
                        pmd.msgMng[idx].msgplay = false;
                        if (wkmsgMng != null) {
                            for (int idx2 = 0; idx2 < wkmsgMng.length; idx2++) {
                                if (wkmsgMng[idx2].msgId == pmd.messageList.get(idx).idVoice) {
                                    pmd.msgMng[idx].msgplay = wkmsgMng[idx2].msgplay;
                                }
                            }
                        }
                        pmd.msgMng[idx].msgId = pmd.messageList.get(idx).idVoice;
                        pmd.msgMng[idx].msgDate = pmd.messageList.get(idx).recTime;
                    }
                }
                bret = true;
            }
        } finally {
            db.close();
            voiceDbHelper.close();
        };
        return bret;
    }

    public boolean MessageEntry(Context context) {
        String str_sql;
        Cursor cvm;
        String MsgName;
        String MsgDate;
        String Message;
        boolean autostart;
        boolean retq;

        // メッセージ管理DBアクセス開始
        autostart = false;
        VoiceDbHelper voiceDbHelper = new VoiceDbHelper(context);
        db = voiceDbHelper.getWritableDatabase();
        try {
            Log.i("nassua", "Message count:" + pmd.MessageTotal);
            if (pmd.MessageTotal > 0) {
                for (int idx = 0; idx < pmd.MessageTotal; idx++) {
                    // 端末起動時間後に登録されたメッセージで、自端末以外の未再生のメッセージを再生する。
                    // または、起動前に登録されたメッセージも再生対象の場合、未再生のメッセージを再生する。
                    if ((pmd.messageList.get(idx).recTime > StartUpTime) || (pastplay)) {
                        // 自局uri以外のメッセージを再生する。
                        if (!(pmd.messageList.get(idx).uriVoice.equalsIgnoreCase(BoxVoice.uriMyself))) {
                            // 再生済みを設定する。
                            //str_sql = SELECT_DATE + pmd.messageList.get(idx).recTime;
                            str_sql = SELECT_MESSAGE_ID + "'" + pmc.ByteToHex(pmd.messageList.get(idx).idVoice) + "'";
                            cvm = db.rawQuery(str_sql, null);
                            try {
                                cvm.moveToFirst();
                                int cidx = cvm.getColumnIndex(voiceDbHelper.COLUMN_PLAY);
                                int playflag = cvm.getInt(cidx);

                                Log.i("nassua", "MessageEntry pmd.msgMng[" + idx + "].msgplay=" + pmd.msgMng[idx].msgplay);
                                Log.i("nassua", "MessageEntry playflag=" + playflag);

                                if ((!pmd.msgMng[idx].msgplay) && (playflag != 1)) {
                                    MsgName = pmd.messageList.get(idx).userName;
                                    MsgDate = new SimpleDateFormat(DATE_PATTERN).format(pmd.messageList.get(idx).recTime);
                                    Message = ToastText + "\n" + MsgName + "\n" + MsgDate;
                                    // メッセージ受信通知
                                    autostart = true;
                                    retq = pmc.enqueueMessage(pmd.messageList.get(idx).recTime, Message);
                                    Log.i("nassua", "Enqueue Message Result=" + retq);
                                } else {
                                    if (playflag != 1) {
                                        ContentValues values = new ContentValues();
                                        values.put(voiceDbHelper.COLUMN_PLAY, 1);
                                        //db.update(voiceDbHelper.TABLE_NAME, values, voiceDbHelper.COLUMN_RECDATE + "=" + pmd.messageList.get(idx).recTime, null);
                                        db.update(voiceDbHelper.TABLE_NAME, values, voiceDbHelper.COLINUM_MESSAGEID + "=" + pmd.messageList.get(idx).idVoice, null);
                                    } else if (!pmd.msgMng[idx].msgplay) {
                                        pmd.msgMng[idx].msgplay = true;
                                    }
                                }
                            } catch (Exception e) {
                                Log.i("nassua", "Message enqueue failed. " + e.getMessage());
                            } finally {
                                cvm.close();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.i("nassua", "MessageEntry Error. " + e.getMessage());
        } finally {
            db.close();
            voiceDbHelper.close();
        }

        return autostart;
    }

    public void AutoPlay() {
        // 非同期再生タスク起動
        playMessageAsyncTask = new PlayMessageAsyncTask();
        playMessageAsyncTask.execute();
    }

    public void playStopNotify() {
        // 再生終了を通知する
        Intent intent = new Intent(ConstantPlayMessage.ACT_NOTIFY);
        intent.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.EVENT_STOP);
        sendBroadcast(intent);
    }

    public void playStartNotify() {
        // 再生開始を通知する
        Intent intent = new Intent(ConstantPlayMessage.ACT_NOTIFY);
        intent.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.EVENT_PLAY);
        sendBroadcast(intent);
    }


    public void showPlayToast(Context context) {
        String ToastMessage;

        ToastMessage = pmc.getToastMessage();
        Toast toast = Toast.makeText(context, ToastMessage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public void resetstartForeground() {
        startForeground = false;
    }
}
