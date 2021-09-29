package jp.co.nassua.nervenet.playmessage;

// Voice Message再生
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.android.listview.EnhancedListView;
import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.voicemessage.PrefApp;
import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.net.utlproto.NetData;
import jp.co.nassua.utlcontent.resolver.FileResolveHelper;

public class PlayMessage extends AppCompatActivity implements Loader.OnLoadCompleteListener<Cursor> {
    private String[] mDate;
    private String[] username;
    private CursorLoader cursorLoader;
    private EnhancedListView listView;
    private RandomAccessFile raf;
    private long StartUpTime;
    private boolean firstflag;
    private Context context;
    private PlayMessageAsyncTask pmd;
    private static PlayMessageCommon pmc;
    private PlayMessageService pms;
    private PrefApp prefApp;
    public boolean autoPlay;
    ArrayAdapter<String> adapter;

    // 環境ファイル
    private File envFile;
    // 管理用DB
    private SQLiteDatabase db;
    private static final String SELECT_DATE = "select * from " + VoiceDbHelper.TABLE_NAME + " where " + VoiceDbHelper.COLUMN_RECDATE + "=";
    private static final String SELECT_MESSAGE_ID = "select * from " + VoiceDbHelper.TABLE_NAME + " where " + VoiceDbHelper.COLINUM_MESSAGEID + "=x";
    //定数
    public static PlayMessage actMyself = null;  //プロセス上の単一インスタンス
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    PlayMessageAsyncTask playMessageAsyncTask;
    static List<Map<String, byte[]>> mIdList;

    public PlayMessage() {
        //rcvNotify = null;
        cursorLoader = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_message);
        StartUpTime = System.currentTimeMillis();
        pmc = new PlayMessageCommon();
        pms = new PlayMessageService();

        firstflag = true;
    }

    @Override
    protected void onResume() {
        // タイトル設定
        setTitle(getString(R.string.msgplay_title) + " " + ActMain.appversion);
        // 非同期再生タスク
        pmd = new PlayMessageAsyncTask();
        // バックで起動中のサービスを停止
        context = getBaseContext();
        stopService(new Intent(context, PlayMessageService.class));

        // 自身のインスタンスを公開
        actMyself = this;

        //CursorLoader初期化
        if (cursorLoader == null) {
            cursorLoader = createLoader();
        }

        // チェックボックス
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        if (ActMain.actMyself.AutoPlay) {
            // 自動再生設定あり。
            checkBox.setChecked(true);
            autoPlay = true;
        }
        // チェックボックスイベントリスナー
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autoPlay = true;
                    ActMain.actMyself.AutoPlay = true;
                    // 自動再生機能起動。
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(context, PlayMessageService.class));
                    } else {
                        startService(new Intent(context, PlayMessageService.class));
                    }
                    //pmc.setPlayMessageServiceStatus(true);
                } else {
                    autoPlay = false;
                    ActMain.actMyself.AutoPlay = false;
                    // 自動再生機能停止。
                    stopService(new Intent(context, PlayMessageService.class));
                    //pmc.setPlayMessageServiceStatus(false);
                }
                // 環境ファイルへ書き込む
                writeAutoPlayMode();
            }
        });

        super.onResume();
    }

    // サービス廃棄
    //
    @Override
    public void onDestroy() {
        /*
        if (prefLog.logCamera >= PrefLog.LV_DEBUG) {
            Log.d( logLabel, "onDestroy" );
        }
        */
        destroyLoader(cursorLoader);
        cursorLoader = null;
        actMyself = null;

        // メッセージ自動再生機能起動
        //context.startService(new Intent(context, PlayMessageService.class));
        super.onDestroy();
    }

    private void createMessageList() {
        String name;
        String recDate;
        byte[] msgId;

        // 作成者と作成日時を追加する。
        final List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
        mIdList = new ArrayList<Map<String, byte[]>>();
        try {
            for (int midx = pmd.MessageTotal - 1; midx >= 0; midx--) {
                name = pmd.messageList.get(midx).userName;
                recDate = new SimpleDateFormat(DATE_PATTERN).format(pmd.messageList.get(midx).recTime);
                msgId = pmd.messageList.get(midx).idVoice;
                Map<String, String> map = new HashMap<String, String>();
                map.put("Name", name);
                map.put("Date", recDate);
                mList.add(map);
                Map<String, byte[]> map2 = new HashMap<String, byte[]>();
                map2.put("MsgID", msgId);
                mIdList.add(map2);
            }
        } catch (Exception e) {
            Log.i("nassua", "createMessageList Exception");
        }
        // ListViewを表示する
        final SimpleAdapter adapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String[]{"Name", "Date"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        listView = (EnhancedListView) findViewById(R.id.MessageList);
        listView.setAdapter(adapter);

        // Clickされたら再生
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                // 再生中は無視する。
                if (!(pmc.getLockMessagePlay())) {
                    // メッセージIDを取得する。
                    byte[] msgId;
                    msgId = getMessageIdByIndex(pos);
                    playMessage(msgId);
                }
                return;
            }
        });

        // スワイプされたら表示から消して無効データを設定する
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView nlistView, final int position) {
                long ldate;
                String str_sql;
                byte[] bmsgId;
                // メッセージ管理DBアクセス開始
                VoiceDbHelper voiceDbHelper = new VoiceDbHelper(PlayMessage.actMyself);
                db = voiceDbHelper.getWritableDatabase();
                try {
                    // 無効フラグを設定する。
                    bmsgId = getMessageIdByIndex(position);

                    for (int idx = 0; idx < pmd.MessageTotal; idx++) {
                        if (Arrays.equals(pmd.msgMng[idx].msgId, bmsgId)) {
                            pmd.msgMng[idx].msgInvalid = true;
                            // メッセージ管理DBに登録済みのデータか調べる
                            str_sql = SELECT_MESSAGE_ID + "'" + pmc.ByteToHex(bmsgId) + "'";
                            Cursor cvm = db.rawQuery(str_sql, null);
                            int cnt = cvm.getCount();
                            if (cnt != 0) {
                                // データベースのレコードに無効フラグを設定する。
                                cvm.moveToFirst();
                                ContentValues values = new ContentValues();
                                values.put(voiceDbHelper.COLUMN_INVALID, 1);
                                db.update(voiceDbHelper.TABLE_NAME, values, voiceDbHelper.COLINUM_MESSAGEID + "=x'" + pmc.ByteToHex(bmsgId) + "'", null);
                            }
                            cvm.close();
                        }
                    }
                    mList.remove(position);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.i("nassua", "setDismissCallback Exception.");
                } finally {
                    db.close();
                    voiceDbHelper.close();
                }
                return null;
            }
        });

        listView.enableSwipeToDismiss();

    }

    // メッセージID取得
    private byte[] getMessageIdByIndex(int idx) {
        byte[] mid;

        Map<String, byte[]> map = new HashMap<String, byte[]>();
        map = mIdList.get(idx);
        mid = map.get("MsgID");
        return mid;
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
            e.printStackTrace();
        } finally {
            resolver.release();
        }
        return stat_node.mRagLast;
    }

    public boolean fromDb(Cursor cursor, long rag) {
        String str_sql;
        Cursor cvm;
        int cnt;
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
                        //str_sql = SELECT_DATE + voice.recTime.toString();
                        str_sql = SELECT_MESSAGE_ID + "'" + pmc.ByteToHex(voice.idVoice) + "'";
                        cvm = db.rawQuery(str_sql, null);
                        cnt = cvm.getCount();

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
                    } catch (Exception e) {
                        Log.i("nassua", "fromDb cvm Exception");
                    }
                } while (cursor.moveToNext());

                /* 登録されているメッセージ数を取得 */
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
                        pmd.msgMng[idx].msgDate = pmd.messageList.get(idx).recTime;
                        pmd.msgMng[idx].msgId = pmd.messageList.get(idx).idVoice;
                    }
                }
                bret = true;
            }
        } catch (Exception e) {
            Log.i("nassua", "fromDB db Exception.");
        } finally {
            db.close();
            voiceDbHelper.close();
        }
        return bret;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        Log.i("nassua","onLoadComplete start." );

        pmd.messageList = null;
        pmd.messageList = new ArrayList<>();
        fromDb(cursor, getRagLast());
        createMessageList();
        // 自動再生設定があれば再生を開始する。
        if (autoPlay) {
            // PlayMessageServiceが未起動なら起動する。
            if (!pmc.getPlayMessageServiceStatus()) {
                // 自動再生機能起動。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(context, PlayMessageService.class));
                } else {
                    startService(new Intent(context, PlayMessageService.class));
                }
            }
        } else {
            pmc.dbCleanup(cursor);
        }
        Log.i("nassua","onLoadComplete end." );
        return;
    }

    private void playMessage(byte[] msgId) {
        String str_sql;
        String FilePath;
        Cursor cvm;
        int bufsz;
        byte[] message = null;
        // メッセージ管理DBアクセス開始
        VoiceDbHelper voiceDbHelper = new VoiceDbHelper(this);
        db = voiceDbHelper.getWritableDatabase();
        try {
            if (pmd.MessageTotal > 0) {
                // メッセージIDをキーにしてデータを検索する。
                for (int idx = 0; idx < pmd.MessageTotal; idx++) {
                    if ((!pmd.msgMng[idx].msgInvalid)
                            && (Arrays.equals(pmd.msgMng[idx].msgId, msgId))) {
                        if (Arrays.equals(pmd.messageList.get(idx).idVoice, msgId)) {
                            // 再生済みを設定する。
                            //str_sql = SELECT_DATE + msgDate;
                            str_sql = SELECT_MESSAGE_ID + "'" + pmc.ByteToHex(msgId) + "'";
                            cvm = db.rawQuery(str_sql, null);
                            cvm.moveToFirst();
                            ContentValues values = new ContentValues();
                            values.put(voiceDbHelper.COLUMN_PLAY, 1);
                            db.update(voiceDbHelper.TABLE_NAME, values, voiceDbHelper.COLINUM_MESSAGEID + "=x'" + pmc.ByteToHex(msgId) + "'", null);
                            pmd.msgMng[idx].msgplay = true;
                            // 音声データを取得する。
                            message = getMessageDate(idx);
                            // 音声ファイルへ変換する
                            FilePath = pmc.ConvertToAudioFile(message);
                            if (FilePath != null) {
                                // 音声ファイルを再生する。
                                Log.i("nassua", "Get Voice Message success." );
                                pmc.VoiceMessagePlay(FilePath);
                            } else {
                                Log.i("nassua", "Get Voice Message failed." );
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.i("nassua", "playMessage Exception.");
        } finally {
            db.close();
            voiceDbHelper.close();
        }
    }

    private void writeAutoPlayMode() {
        String wkfilename;
        File workFile;
        wkfilename = ActMain.actMyself.envfilename + "work";
        envFile = new File(ActMain.actMyself.envfilename);
        workFile = new File(wkfilename);
        try {
            FileInputStream fis = new FileInputStream(envFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufread = new BufferedReader(isr);
            FileOutputStream fos = new FileOutputStream(workFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            PrintWriter bufwrite = new PrintWriter(osw);

            String Param;
            while ((Param = bufread.readLine()) != null) {
                if (Param.toLowerCase().indexOf("autoplay") != -1) {
                    // 自動再生モード設定なら書きかえる
                    try {
                        if (autoPlay) {
                            bufwrite.println("AutoPlay=yes");
                        } else {
                            bufwrite.println("AutoPlay=no");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // そのまま上書きする。
                    bufwrite.println(Param);
                }
            }
            envFile.delete();
            workFile.renameTo(envFile);
            bufread.close();
            bufwrite.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getMessageDate(int idx) {
        Context context;
        byte[] msgDate = null;

        context = getApplicationContext();
        try {
            FileResolveHelper file_resolver = FileResolveHelper.newInstance(context);
            ParcelFileDescriptor fd = file_resolver.openForRead(Uri.parse(pmd.messageList.get(idx).uriFile));
            ParcelFileDescriptor.AutoCloseInputStream stm = new ParcelFileDescriptor.AutoCloseInputStream(fd);
            long msgsize = fd.getStatSize();
            msgDate = new byte[(int)msgsize];
            stm.read(msgDate);
            stm.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgDate;
    }

 }