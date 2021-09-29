package jp.co.nassua.nervenet.playmessage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.utlcontent.resolver.FileResolveHelper;

import static java.util.Arrays.binarySearch;

/**
 * Created by I.Tadshi on 2016/07/20.
 */
public class PlayMessageAsyncTask extends AsyncTask<String, Void, Boolean> {
    private SQLiteDatabase db;
    private static final String SELECT_DATE = "select * from " + VoiceDbHelper.TABLE_NAME + " where " + VoiceDbHelper.COLUMN_RECDATE + "=";
    public static PlayMessageAsyncTask actMyself = null;  //プロセス上の単一インスタンス
    private PlayMessageCommon pmc;

    public static int MessageTotal;
    // messageListは排他制御する。
    public static List<BoxVoice> messageList = Collections.synchronizedList(new ArrayList<BoxVoice>());
    public class MessageManager {
        boolean msgInvalid;
        boolean msgplay;
        long msgDate;
        byte[] msgId;
    }
    static MessageManager[] msgMng;


    public PlayMessageAsyncTask() {
        super();
    }

    @Override
    protected Boolean doInBackground(String... Params) {
        Log.i("nassua","doInBackground PlayMessageAsyncTask");
        Context context;
        String str_sql;
        String ToastMessage;
        String FilePath;
        Cursor cvm;
        int bufsz, idx;
        boolean msgflag;
        byte[] message;
        //long msgDate = Params[0];
        long msgDate;

        // メッセージ管理DBアクセス開始
        actMyself = this;
        if (PlayMessage.actMyself != null) {
            context = PlayMessage.actMyself;
        } else {
            context = PlayMessageService.actMyself;
        }

        pmc = new PlayMessageCommon();
        PlayMessageCommon.PlayMessageData pmcPmd;

        VoiceDbHelper voiceDbHelper = new VoiceDbHelper(context);
        db = voiceDbHelper.getWritableDatabase();
        try {
            // 積まれているメッセージを全て再生する。
            while(pmc.getMessageCount() > 0) {
                // 更新日時をキーにしてデータを検索する。
                pmcPmd = pmc.dequeueMessage();
                msgDate = pmcPmd.messageDate;
                ToastMessage = pmcPmd.toastMessage;
                if (msgDate != 0) {
                    msgflag = true;
                    messageSearch:
                    while (msgflag) {
                        for (idx = 0; idx < MessageTotal; idx++) {
                            if ((!msgMng[idx].msgInvalid)
                                    && (msgMng[idx].msgDate == msgDate)) {
                                if (!checkListsize(idx)) continue messageSearch;
                                if (messageList.get(idx).recTime == msgDate) {
                                    // 再生済みを設定する。
                                    str_sql = SELECT_DATE + msgDate;
                                    cvm = db.rawQuery(str_sql, null);
                                    try {
                                        cvm.moveToFirst();
                                        ContentValues values = new ContentValues();
                                        values.put(voiceDbHelper.COLUMN_PLAY, 1);
                                        db.update(voiceDbHelper.TABLE_NAME, values, voiceDbHelper.COLUMN_RECDATE + "=" + msgDate, null);
                                        Log.i("nassua", "DB update success." );
                                    } catch (Exception e) {
                                        Log.i("nassua", "DB update exception." );
                                    } finally {
                                        Log.i("nassua", "DB(cvm) close." );
                                        cvm.close();
                                    }
                                    msgMng[idx].msgplay = true;
                                    Log.i("nassua", "doInBackground msgMng[" + idx + "].msgplay=" + msgMng[idx].msgplay);
                                    // 音声データを取得する。
                                    if (!checkListsize(idx)) continue messageSearch;
                                    message = getMessageDate(idx, context);
                                    // 音声ファイルへ変換する
                                    FilePath = pmc.ConvertToAudioFile(message);
                                    if (FilePath != null) {
                                        Log.i("nassua", "Get Voice Message success." );
                                        // メッセージ受信を表示する。
                                        pmc.setToastMessage(ToastMessage);
                                        if (PlayMessageService.actMyself != null) {
                                            PlayMessageService.actMyself.playStartNotify();
                                        }
                                        // 音声ファイルを再生する。
                                        pmc.VoiceMessagePlay(FilePath);
                                    } else {
                                        Log.i("nassua", "Get Voice Message failed." );
                                    }
                                    msgflag = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            db.close();
            voiceDbHelper.close();
        }
        /*
        if (PlayMessageService.actMyself != null) {
            PlayMessageService.actMyself.playStopNotify();
        }
        */
        actMyself = null;
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        return;
    }

    private boolean checkListsize(int idx) {
        // messageListのサイズが idxより小さいか、0になっていないかチェックする。
        if ((idx >= messageList.size())
         || (messageList.size() == 0)) {
            android.util.Log.i("nassua", "messageList empty.");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;

    }

    private byte[] getMessageDate(int idx, Context context) {
        byte[] msgDate = null;

        try {
            FileResolveHelper file_resolver = FileResolveHelper.newInstance(context);
            ParcelFileDescriptor fd = file_resolver.openForRead(Uri.parse(messageList.get(idx).uriFile));
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
