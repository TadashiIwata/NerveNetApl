package jp.co.nassua.nervenet.playmessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicerecorder.RecordingTimer;

/**
 * Created by I.Tadshi on 2016/08/18.
 */
public class PlayMessageCommon {
    private static final String SELECT_ALL = "select * from " + VoiceDbHelper.TABLE_NAME;
    private static final int NOTIFICATION_ID = R.layout.activity_act_main;
    private static final int REQUEST_CODE = 0;
    private static MediaPlayer mediaPlayer;
    private static PlaybackTimer playbackTimer = null;
    private static PlayMessageService pms;

    public class PlayMessageData {
        long messageDate;
        String toastMessage;
    }
    static PlayMessageData playMessageData;

    private static String filepath;
    private static String toastMessage;
    private static String playState;
    private static boolean startpms;
    private static boolean playlocked;
    private static LinkedList<Long> messageDateList = new LinkedList<Long>();
    private static LinkedList<String> toastMessageList = new LinkedList<String>();

    public void setPlayMessageServiceStatus(boolean flag) {
        startpms = flag;
        if (!flag) {
            if (pms == null) {
                pms = new PlayMessageService();
            }
            pms.resetstartForeground();
        }
    }

    public boolean getPlayMessageServiceStatus() {
        return startpms;
    }

    public boolean enqueueMessage(long messageDate, String msg) {
        boolean bret;
        bret = messageDateList.offer(messageDate);
        if (bret) {
            bret = toastMessageList.offer(msg);
            if (!bret) {
                messageDateList.pollLast();
            }
        }
        return bret;
    }

    public PlayMessageData dequeueMessage() {
        Long messageDate;
        String message;
        playMessageData = new PlayMessageData();

        messageDate = messageDateList.poll();
        if (messageDate != null) {
            playMessageData.messageDate = messageDate;
        } else {
            playMessageData.messageDate = 0;
        }
        message = toastMessageList.poll();
        if (message != null) {
            playMessageData.toastMessage = message;
        } else {
            playMessageData.toastMessage = null;
        }
        return playMessageData;
    }

    public int getMessageCount() {
        return toastMessageList.size();
    }

    public void setPlayStatus(String state) {
        playState = state;
    }

    public String getPlayStatus() {
        return playState;
    }

    public void setToastMessage(String message) {
        toastMessage = message;
    }

    public String getToastMessage() {
        return toastMessage;
    }

    public void dbCleanup(Cursor cursor) {
        long recDate;
        boolean validflag;
        VoiceDbHelper voiceDbHelper;

        // 不要レコードを削除する。
        // メッセージ管理DBアクセス開始
        if (PlayMessageService.actMyself != null) {
            voiceDbHelper = new VoiceDbHelper(PlayMessageService.actMyself.getApplicationContext());
        } else if (PlayMessage.actMyself != null) {
            voiceDbHelper = new VoiceDbHelper(PlayMessage.actMyself.getApplicationContext());
        } else {
            Log.i("nassua", "DB not cleanup.");
            return;
        }

        SQLiteDatabase db = voiceDbHelper.getWritableDatabase();
        try {
            Cursor cvmm = db.rawQuery(SELECT_ALL, null);
            int cvmmcnt = cvmm.getCount();
            if (cvmmcnt > 0) {
                try {
                    cvmm.moveToFirst();
                    do {
                        recDate = cvmm.getLong(cvmm.getColumnIndex(voiceDbHelper.COLUMN_RECDATE));
                        cursor.moveToFirst();
                        do {
                            DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
                            rec.setFromQuery(cursor);
                            BoxVoice voice = BoxVoice.newInstance(rec);
                            validflag = voice.fromRecordByDate(rec, recDate);
                            if (validflag) break;
                        } while (cursor.moveToNext());
                        if (!validflag) {
                            Log.i("nassua", "To remove because it is an invalid record.");
                            db.delete(VoiceDbHelper.TABLE_NAME, voiceDbHelper.COLUMN_RECDATE + "=" + recDate, null);
                        }
                    } while (cvmm.moveToNext());
                } catch (Exception e) {
                    Log.i("nassua", "dbCleanup cvmm Exception.");
                } finally {
                    cvmm.close();
                }
            }
        } catch (Exception e) {
            Log.i("nassua", "dbCleanup db Exception.");
        } finally {
            db.close();
            voiceDbHelper.close();
        }
    }

    public void lockMessaggPlay() {
        // 自動再生を一時停止する。
        playlocked = true;
    }

    public void unlockMessagePlay() {
        // 自動再生を再開する。
        playlocked = false;

    }

    public boolean getLockMessagePlay() {
        // 自動再生の可否を返す。
        return playlocked;
    }

    public String ByteToHex(byte[] data) {
        StringBuffer strbuf = new StringBuffer(data.length * 2);

        for(int idx=0; idx < data.length; idx++) {
            int bt = data[idx] & 0xff;
            if (bt < 0x10) {
                strbuf.append("0");
            }
            strbuf.append(Integer.toHexString(bt));
        }
        return strbuf.toString();
    }

    public void PlayMessageServiceStartNotification(Context context) {
        // 起動表示
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Voice Message");
        builder.setContentText("Play Message Service Start.");
        builder.setAutoCancel(true);
        builder.build().flags = Notification.FLAG_AUTO_CANCEL;
        Intent intentx = new Intent(context, PlayMessageNoAction.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, REQUEST_CODE, intentx, 0);
        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // 音声データを音声ファイルに出力する。
    public String ConvertToAudioFile(byte[] message) {
        int idx, idx2;
        int pcmidx[] = { 0, 0, 0, 0 };
        int pcmposition[] = { 8, 9, 10, 11 };
        byte[] wave = {'W', 'A', 'V', 'E'};

        filepath = null;
        // "WAVE"を検索して PCMか 3GPか判定する。
        for(idx=0; idx < 4; idx++) {
            // "WAVE"の文字があるかチェック
            for(idx2=8; idx2 < 12; idx2++) {
                if (message[idx2] == wave[idx]) {
                    // 一致したら位置を取得する
                    pcmidx[idx] = idx2;
                }
            }
        }

        // 音声ファイル作成
        FileOutputStream fos = null;
        try {
            // "WAVE"の位置が一致したら PCMファイル
            if (Arrays.equals(pcmidx, pcmposition)) {
                // PCMファイル作成
                filepath = Environment.getExternalStorageDirectory() + "/" + "VoiceMessage.wav";
                fos = new FileOutputStream(filepath);
            } else {
                // 3GPファイル作成
                filepath = Environment.getExternalStorageDirectory() + "/" + "VoiceMessage.3gp";
                fos = new FileOutputStream(filepath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fos != null) {
            try {
                fos.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filepath;
    }

    // 音声ファイル再生処理
    public void VoiceMessagePlay(String filepath) {
        mediaPlayer = new MediaPlayer();

        // 音声ファイルを設定
        try {
            mediaPlayer.setDataSource(filepath);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 音声ファイル再生準備
        try {
            mediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 音声ファイル再生
        mediaPlayer.start();
        lockMessaggPlay();
        // 再生監視タイマー開始要求
        if (PlayMessageService.actMyself != null) {
            Intent intent = new Intent(ConstantPlayMessage.ACT_REQUEST);
            intent.putExtra(ConstantPlayMessage.EXTRA_EVENT, ConstantPlayMessage.TIMER_START);
            PlayMessageService.actMyself.sendBroadcast(intent);
        }
    }

    public void PlaybackTimerStart() {
        // タイマー(1秒間隔、35秒)を設定する。
        // 再生カウントダウンタイマー開始
        Log.i("nassua", "Voice Message Playback Count Down Timer Start.");
        if (playbackTimer == null) {
            playbackTimer = new PlaybackTimer(35000, 1000);
            playbackTimer.start();
        }
    }

    public void PlaybackTimerStop() {
        if (playbackTimer != null) {
            playbackTimer.cancel();
            playbackTimer = null;
        }
    }

    public void checkPlaybackStatus() {
        if (!(mediaPlayer.isPlaying())) {
            Log.i("nassua", "Voice Message Playback is Finished.");
            // 再生カウントダウンタイマー停止
            PlaybackTimerStop();
            // 再生が終了していればファイルを削除する。
            File audiofile = new File(filepath);
            audiofile.delete();
            if (PlayMessageService.actMyself !=  null) {
                PlayMessageService.actMyself.playStopNotify();
            }
            unlockMessagePlay();
        }
    }

    public void VoiceMessagePlaybackCancel() {
        mediaPlayer.stop();
        if (PlayMessageService.actMyself != null) {
            Log.i("nassua", "Voice Message Playback is Canceled.");
            PlayMessageService.actMyself.playStopNotify();
        }
        playbackTimer = null;
        unlockMessagePlay();
    }

}
