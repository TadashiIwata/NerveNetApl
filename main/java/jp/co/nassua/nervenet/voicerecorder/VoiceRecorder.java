package jp.co.nassua.nervenet.voicerecorder;

// Voice Message録音処理
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
//import android.content.ContentProvider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
//import jp.co.nassua.nervenet.phonedepend.PrefApp;
import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.net.utlproto.ExceptionDecode;
import jp.co.nassua.utlcontent.resolver.FileResolveHelper;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

import static java.lang.Thread.sleep;

public class VoiceRecorder extends VoiceMessage {
    public static VoiceMessageSubFunctions voiceMessageSubFunctions;
    public static String recFileName = "test.wav";
    private DbDefineBoat.ConfNode confNode;
    private static AudioRecord myAR;
    final static int SAMPLING_RATE = 44100;
    private WaveFile wav1;
    private int bufSize;
    private int frameSize;
    private short[] shortData1;
    private BoxVoice boxVoice;
    private static Context mycontext;
    public static VoiceMessageCommon vmc;
    private static int RecAudioFormat;
    private static int RecAudioEncoder;
    private static SendWaitTimer sendWaitTimer;
    private static boolean sendTimerFlag = false;

    /* 3gp対応 */
    SimpleDateFormat recSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private String fileName = "test.3gp";
    private MediaRecorder recorder;
    /* <----- ここまで -----> */

     private static String VoiceBoxName = "VoiceMessage";

    public void startRecording(Context context) {
        // 録音開始
        boolean recstart = true;
        confNode = null;
        boxVoice = null;
        mycontext = context;
        vmc = new VoiceMessageCommon();
        voiceMessageSubFunctions = new VoiceMessageSubFunctions();
        RecAudioFormat = vmc.getAudioFormat();
        RecAudioEncoder = vmc.getAudioEncoder();
        if (RecAudioFormat == vmc.AUDIO_FORMAT_PCM) {
            /* PCM録音 */
            wav1 = new WaveFile();
            NewRecFile();
            myAR.startRecording();
            Intent intent;
            // タイムアウト、録音終了指示を発行する。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0以降
                intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                intent.setAction(ConstantRecorder.ACT_REQUEST);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_READ);
                VoiceMessage.actMyself.sendBroadcast(intent);
            } else {
                // Android 7.1.1以前
                intent = new Intent(ConstantRecorder.ACT_REQUEST);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_READ);
                VoiceMessage.actMyself.sendBroadcast(intent);
            }
        } else {
            /* 3GP録音 */
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            if (RecAudioEncoder == vmc.AUDIO_ENCODER_DEFAULT) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            } else if (RecAudioEncoder == vmc.AUDIO_ENCODER_AAC) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            } else if (RecAudioEncoder == vmc.AUDIO_ENCODER_HE_AAC) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            } else if (RecAudioEncoder == vmc.AUDIO_ENCODER_AAC_ELD) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
            } else if (RecAudioEncoder == vmc.AUDIO_ENCODER_AMR_NB) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            } else if (RecAudioEncoder == vmc.AUDIO_ENCODER_AMR_WB) {
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            }
            // 録音用ファイル名を生成
            Date recDate = new Date();
            fileName = "Vm" + recSdf.format(recDate).toString();
            recFileName = fileName;
            fileName = Environment.getExternalStorageDirectory() + "/" + fileName + ".3gp";
            recorder.setOutputFile(fileName);
            try {
                recorder.prepare();
            } catch (Exception e) {
                recstart = false;
            }
            if (recstart) {
                voiceMessageSubFunctions.setPcmFileName(fileName);
                recorder.start();
            } else {
                // 録音失敗の Toastを表示して処理を中断する。
                Log.i("nassua", "VoiceMessage 3GP Recording Start Failed.");
            }
        }
     }

    public void stopRecording(Context context, boolean dboutput) {
        mycontext = context;
        RecFileClose();
        if (dboutput) {
            // 録音終了。DBへ書き込む。
            MessageDbWrite();
        } else {
            String filename = voiceMessageSubFunctions.getPcmFileName();
            File pcmfile = new File(filename);
            // 録音キャンセル。PCMファイル削除
            pcmfile.delete();
            CancelSendWaitTimer();
        }
    }

    public void readRequest() {
        myAR.read(shortData1, 0, frameSize);
    }

    // 送信処理中通知
    private void MessageDbWrite() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降;
            intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
            intent.setAction(ConstantRecorder.ACT_NOTIFY);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_SEND);
            VoiceMessage.actMyself.sendBroadcast(intent);
        } else {
            // Android 7.1.1以前
            intent = new Intent(ConstantRecorder.ACT_NOTIFY);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_SEND);
            VoiceMessage.actMyself.sendBroadcast(intent);
        }
        // 設定読み取り
        readConfNode();
        BoxVoice.idMyself = confNode.mIdBoat;
        BoxVoice.uriMyself = confNode.mUriBoat;
        BoxVoice.idBox = VoiceBoxName.getBytes();
        boxVoice = BoxVoice.newInstance(confNode.mUriBoat);
        // PCMファイルを読み込む。
        saveVoiceMessage();
        // 送信終了通知;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降;
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
            VoiceMessage.actMyself.sendBroadcast(intent);
        } else {
            // Android 7.1.1以前
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
            VoiceMessage.actMyself.sendBroadcast(intent);
        }
        /*
        if (saveVoiceMessage()) {
            // 送信終了通知;
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
            VoiceMessage.actMyself.sendBroadcast(intent);
        } else {
            // 録音ボタンを有効化する。
            VoiceMessage.actMyself.EnaleRecButton();
        }
        */
    }

    private void initAudioRecord() {
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        frameSize = bufSize / 2;
        myAR = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize);

        shortData1 = new short[frameSize];

        myAR.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener()  {

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                if ((myAR != null) && (wav1 != null)) {
                    myAR.read(shortData1, 0, frameSize);
                    wav1.addBigEndianData(shortData1);
                }
            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
            }
        });
        myAR.setPositionNotificationPeriod(frameSize);
    }

    private void NewRecFile() {
        // PCMファイル作成
        wav1.createFile();
        initAudioRecord();
    }

    private void RecFileClose() {
        if (RecAudioFormat == vmc.AUDIO_FORMAT_PCM) {
            /* PCM */
            myAR.stop();
            wav1.close();
            myAR.release();
            wav1 = null;
            myAR = null;
        } else {
            /* 3GP */
            recorder.stop();
            recorder.reset();
            recorder.release();
        }
    }

    private boolean saveVoiceMessage() {
        boolean bret = true;
        byte id_record[] = { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };

        if (boxVoice == null) {
            return false;
        }
        boxVoice.recTime = System.currentTimeMillis();
        id_record = boxVoice.idVoice;
        boxVoice.uriFile = fileCopy(voiceMessageSubFunctions.getPcmFileName());

        DbDefineShare.BoxShare rec = boxVoice.toRecord();
        rec.mTimeCalibrate = boxVoice.recTime;
        rec.mCommon.mNodeUpdate = confNode.mIdBoat;
        rec.mCommon.mTimeUpdate = boxVoice.recTime;
        rec.mCommon.mTimeDiscard = rec.mCommon.mTimeUpdate + (ActMain.actMyself.MessageLife * 1000);

        Uri uri_tbl = DbDefineShare.BoxShare.CONTENT_URI;

        ContentResolver cont = VoiceMessage.actMyself.resolver;

        ContentProviderClient resolver = cont.acquireContentProviderClient( uri_tbl );
        if (resolver != null) {
            try {
                Log.i("nassua", "VoiceMessage Send Request");
                // レコード追加
                resolver.insert( uri_tbl, rec.getForInsert() );
                // 能動配信要求
                Context context = VoiceMessage.actMyself;
                if (vmc.checkNodeStatus(context)) {
                    // 基地局と接続されていれば送信する。
                    Intent intent = new Intent(ConstantShare.ACT_PUBLISH);
                    Bundle extras = new Bundle();
                    extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
                    extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxShare.PATH);
                    extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, id_record);
                    intent.putExtras(extras);
                    context.sendBroadcast(intent);
                } else {
                    // 基地局と接続されていない時は送信待ちリストに積む。
                    vmc.addSendWaitRecordIds(id_record);
                    if ((sendWaitTimer == null) && (getSendTimerFlag())) {
                        sendWaitTimer.cancel();
                        resetSendTimerFlag();
                    }
                    StartSendWaitTimer();
                    bret = false;
                }
            } catch (RemoteException e) {
                Log.i("nassua", "VoiceMessage Send Failed.");
                e.printStackTrace();
            } finally {
                Log.i("nassua", "VoiceMessage Send Finished.");
                resolver.release();
            }
        }
        boxVoice = null;
        return bret;
    }

    public void StartSendWaitTimer() {
        // 未初期化または、待機レコード数が 0ならばタイマー値を設定しなおす。
        if ((sendWaitTimer == null) || (vmc.getSendWaitRecordIdsCount() < 1)) {
            sendWaitTimer =  new SendWaitTimer(5000, 1000);
        }
        setSendTimerFlag();
        sendWaitTimer.start();
    }

    public void SendWaitMessages(Context context) {
        int count;
        Intent intent;
        byte[] id_record;

        resetSendTimerFlag();
        count = vmc.getSendWaitRecordIdsCount();
        if (count > 0) {
            id_record = vmc.getSendWaitRecordId(0);
            intent = new Intent(ConstantShare.ACT_PUBLISH);
            Bundle extras = new Bundle();
            extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
            extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxShare.PATH);
            extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, id_record);
            intent.putExtras(extras);
            context.sendBroadcast(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0以降
                intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                intent.setAction(ConstantRecorder.ACT_NOTIFY);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                VoiceMessage.actMyself.sendBroadcast(intent);
            } else {
                // Android 7.1.1以前
                intent = new Intent(ConstantRecorder.ACT_NOTIFY);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                VoiceMessage.actMyself.sendBroadcast(intent);
            }




            count--;
            if (count > 0) {
                // 次レコード送信タイマ設定。
                if ((sendWaitTimer == null) || (vmc.getSendWaitRecordIdsCount() > 0)) {
                    sendWaitTimer = new SendWaitTimer(1000, 500);
                }
            } else {
                intent = new Intent(ConstantRecorder.ACT_NOTIFY);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_SEND_END);
                VoiceMessage.actMyself.sendBroadcast(intent);
            }
        }
    }

    public void CancelSendWaitTimer() {
        if (sendWaitTimer != null) {
            sendWaitTimer.cancel();
            sendWaitTimer = null;
        }
    }

    // 端末諸元 読み取り
    //
    private void readConfNode() {
        confNode = new DbDefineBoat.ConfNode();
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        ContentResolver cont = VoiceMessage.actMyself.resolver;
        ContentProviderClient resolver = cont.acquireContentProviderClient( uri_tbl );
        if (resolver != null ) try {
            Cursor cursor = resolver.query( uri_tbl, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    confNode.setFromQuery(cursor);
                }
                cursor.close();
            }
        } catch (RemoteException e) {
            //if (prefLog.logCamera >= PrefLog.LV_WARN) {
            //    Log.w(logLabel, "no conf-node err=" + e.getMessage());
            //}
            e.printStackTrace();
        } finally {
            resolver.release();
        }
    }

    // 音声ファイルをコピーする。
    private String fileCopy(String filename) {
        Context context;
        boolean playflag;
        byte[] message;

        playflag = false;
        File pcmfile = new File(filename);
        message = new byte[(int)pcmfile.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(pcmfile);
            fileInputStream.read(message);
            fileInputStream.close();
            playflag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (playflag) {
            //context = getBaseContext();
            FileResolveHelper file_helper = FileResolveHelper.newInstance(mycontext);
            try {
                Uri uri_file = file_helper.newFileUri(DbDefineShare.File.CONTENT_URI);
                ParcelFileDescriptor fd = file_helper.openForWrite(uri_file, false);
                ParcelFileDescriptor.AutoCloseOutputStream stm = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                stm.write(message);
                pcmfile.delete();
                return uri_file.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // PCMファイル削除
        pcmfile.delete();
        return null;
    }

    private void setSendTimerFlag() {
        sendTimerFlag = true;
    }

    private void resetSendTimerFlag() {
        sendTimerFlag = false;
    }

    private boolean getSendTimerFlag() {
        return sendTimerFlag;
    }
}
