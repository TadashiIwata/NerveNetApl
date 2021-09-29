package jp.co.nassua.nervenet.voicerecorder;
// Voice Message GUI処理
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.EventListener;
import java.util.TimeZone;

import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.phone.DbDefinePhone;
import jp.co.nassua.nervenet.playmessage.PlayMessageCommon;
import jp.co.nassua.nervenet.vmphonedepend.ActRinging;
import jp.co.nassua.nervenet.vmphonedepend.BoatApi;
import jp.co.nassua.nervenet.vmphonedepend.UserProfilePermission;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.PrefApp;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class VoiceMessage extends AppCompatActivity implements View.OnClickListener {
    static public VoiceMessage actMyself = null; //プロセス上の単一インスタンス
    public PrefApp pref;           //アプリケーション設定
    public static boolean recflag;
    public static boolean btnflag;
    private BroadcastReceiver rcvNotify;
    private ImageButton recbutton;
    private VoiceMessageSubFunctions voiceMessageSubFunctions;
    PlayMessageCommon pmc;
    VoiceMessageCommon vmc;
    VoiceRecorder voiceRecorder;
    AudioManager audioManager;
    UserProfilePermission userProfilePermission;
    private int saveVolume;

    Handler recHandle;
    public ContentResolver resolver;
    //録音状態
    private static final short REC_STAT_IDLE = 0; // 録音開始
    private static final short REC_STAT_EXEC = 1; // 録音中
    private static final short REC_STAT_SEND = 2; // 送信中
    private static final short REC_STAT_END = 3; // 録音終了
    private static final int   SEND_WAIT_MAX = 3;
    private static boolean recLock;

    SimpleDateFormat recSdf = new SimpleDateFormat("yyyyMMddHHmmss");
    public String myname = null;

    // 送信完了イベント
    public VoiceMessageNotify vmn = null;

    public void VoiceMessage() {
        rcvNotify = null;
        myname = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_message);
        //自身のインスタンスを公開
        actMyself = this;
        voiceMessageSubFunctions = new VoiceMessageSubFunctions();

        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 使用者の名前取得
        Cursor mycur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        mycur.moveToFirst();
        int nidx = mycur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
        int cnt = mycur.getCount();
        if (cnt > 0) {
            myname = mycur.getString(nidx);
        } else {
            myname = null;
        }
        mycur.close();
        if ((myname == null)
                || (myname.indexOf("名前なし") != -1)) {
            myname = null;  // mynameを nullで初期化
            userProfilePermission = new UserProfilePermission();
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (tm != null) {
                switch(tm.getSimState()) {
                    default:
                        // 電話番号読み取り
                        myname = tm.getLine1Number();
                        break;
                    case TelephonyManager.SIM_STATE_ABSENT:
                        break;
                }
            }
            if (myname == null) {
                if (userProfilePermission.getAndroid6Status()) {
                    // Android 6以上
                    myname = voiceMessageSubFunctions.getUriBoart();
                } else {
                    // SIMが無い場合、NerveNet端末の設定から番号を取ってくる。
                    myname = voiceMessageSubFunctions.getPhoneNumber();
                    if (myname == null) {
                        myname = voiceMessageSubFunctions.getUriBoart();
                    }
                }
            }
        }

        recflag = false;
        recLock = false;
        // 録音ボタンによる録音
        recbutton = (ImageButton) findViewById(R.id.recButton);
        recbutton.setOnClickListener(this);
        if (voiceMessageSubFunctions.getVolumeKeyStatus()) {
            // 音量ボタンによる録音設定あり
            // recbutton = (ImageButton) findViewById(R.id.recButton);
            // recbutton.setEnabled(false);  // 録音ボタンを無効化
            voiceMessageSubFunctions.initVolumeKeyCount();
            voiceMessageSubFunctions.initVolumeKeyUp();
            // 音量をミュート
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            saveVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }

        recHandle = new Handler();
        recSdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        resolver = getContentResolver();

        // VoiceMessageイベント登録
        vmn = new VoiceMessageNotify();
        VoiceMessageEventListener vmel = new VoiceMessageEventListener();
        vmn.setSendMessageListener(vmel);

        // メッセージ再生
        pmc = new PlayMessageCommon();
        // メッセージ受信
        vmc = new VoiceMessageCommon();
        // メッセージ録音
        voiceRecorder = new VoiceRecorder();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        if (!voiceMessageSubFunctions.getVolumeKeyStatus()) {
            if (vmc.getSendWaitRecordIdsCount() >= SEND_WAIT_MAX) {
                // 録音禁止状態を設定する。
                if (!recLock) {
                    // 録音ボタン無効化
                    recbutton.setEnabled(false);
                    recbutton.setColorFilter(0xaa808080);
                    // 未接続、未送信メッセージが上限に達したダイアログを表示する。
                    vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE4);
                    recLock = true;
                }
            } else {
                // 録音禁止状態なら解除する。
                if (recLock) {
                    recbutton.setEnabled(true);
                    recLock = false;
                }
                // 録音ボタン(ICON)による録音
                if (!recflag) {
                    recbutton.setImageResource(R.drawable.recstop);
                    recflag = true;
                    btnflag = true;
                    // メッセージ受信停止
                    // vmc.stopShareAlive(this);
                    // メッセージ再生ロック
                    pmc.lockMessaggPlay();
                    // 録音サービスを開始する。
                    UpdateText(REC_STAT_EXEC);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Android 8.0以降
                        intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                        intent.setAction(ConstantRecorder.ACT_REQUEST);
                        intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_START);
                        actMyself.sendBroadcast(intent);
                    } else {
                        // Android 7.1.1以前
                        intent = new Intent(ConstantRecorder.ACT_REQUEST);
                        intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_START);
                        actMyself.sendBroadcast(intent);
                    }
                } else {
                    if (vmc.getSendWaitRecordIdsCount() >= SEND_WAIT_MAX) {
                        // ボタンを無効化
                        recbutton.setEnabled(false);
                    }
                    // 録音終了、送信メッセージ
                    if (btnflag) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // Android 8.0以降
                            intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                            intent.setAction(ConstantRecorder.ACT_REQUEST);
                            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                            actMyself.sendBroadcast(intent);
                        } else {
                            // Android 7.1.1以前
                            intent = new Intent(ConstantRecorder.ACT_REQUEST);
                            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                            actMyself.sendBroadcast(intent);
                        }
                    } else {
                        SendCompleteNotify();
                    }
                    // メッセージ再生アンロック
                    pmc.unlockMessagePlay();
                    // メッセージ受信開始
                    //vmc.startShareAlive(this);
                    recflag = false;
                    btnflag = false;
                }
            }
        } else {
            // 音量ボタンによる録音、録音ボタンは無視してメッセージを表示する。
            Toast toast = Toast.makeText(this, R.string.option_submenu_recbutton_alert, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        // タイトル設定
        setTitle(getString(R.string.voicerec_title) + " " + ActMain.appversion);
        //自身のインスタンスを公開
        actMyself = this;

        Intent intent;
        recbutton.setEnabled(true);
        // 基地局と接続されているかをチェック
        if (!(vmc.checkNodeStatus(this))) {
            if (vmc.getSendWaitRecordIdsCount() > SEND_WAIT_MAX) {
                // 録音ボタン無効化
                recbutton.setEnabled(false);
                recbutton.setColorFilter(0xaa808080);
                // 未接続、未送信メッセージが上限に達したダイアログを表示する。
                vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE4);
                voiceRecorder.StartSendWaitTimer();
            } else if (vmc.getSendWaitRecordIdsCount() > 0) {
                // 未接続、未送信メッセージ有りのダイアログを表示する
                vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE3);
                voiceRecorder.StartSendWaitTimer();
            } else {
                // 未接続ダイアログを表示する。
                vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE2);
            }
        } else {
            if (vmc.getSendWaitRecordIdsCount() > 0) {
                // 保存されているメッセージを送信する。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0以降
                    intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                    intent.setAction(ConstantRecorder.ACT_REQUEST);
                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_BSCHECK);
                    VoiceMessage.actMyself.sendBroadcast(intent);
                } else {
                    // Android 7.1.1以前
                    intent = new Intent(ConstantRecorder.ACT_REQUEST);
                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_BSCHECK);
                    VoiceMessage.actMyself.sendBroadcast(intent);
                }
            }
            if (vmc.getSendWaitRecordIdsCount() > 1) {
                // 複数ある時は送信タイマーを設定する。
                voiceRecorder.StartSendWaitTimer();
            }
        }

        super.onResume();
    }

    @Override
    public void onUserLeaveHint() {
        // Homeまたは Historyが押されたら録音を中断してメイン画面に戻る。
        if (recflag) {
            // 録音中の時は録音を中断する。
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0以降
                intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                intent.setAction(ConstantRecorder.ACT_REQUEST);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_CANCEL);
                actMyself.sendBroadcast(intent);
            } else {
                // Android 7.1.1以前
                intent = new Intent(ConstantRecorder.ACT_REQUEST);
                intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_CANCEL);
                actMyself.sendBroadcast(intent);
            }
            Toast toast = Toast.makeText(this, R.string.voicerec_cancel, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();
        }
        voiceRecorder.CancelSendWaitTimer();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Backボタンが押されたら録音を中断する。
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recflag) {
                // 録音中の時は録音を中断する。
                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0以降
                    intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                    intent.setAction(ConstantRecorder.ACT_REQUEST);
                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_CANCEL);
                    actMyself.sendBroadcast(intent);
                } else {
                    // Android 7.1.1以前
                    intent = new Intent(ConstantRecorder.ACT_REQUEST);
                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_CANCEL);
                    actMyself.sendBroadcast(intent);
                }
                Toast toast = Toast.makeText(this, R.string.voicerec_cancel, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                toast.show();
            }
            voiceRecorder.CancelSendWaitTimer();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean bret = super.dispatchKeyEvent(event);
        Intent intent;
        // 音量ボタンによる録音有効
        if (voiceMessageSubFunctions.getVolumeKeyStatus()) {
            if (vmc.getSendWaitRecordIdsCount() > 9) {
                // 録音禁止状態にする。
                if (!recLock) {
                    // 録音ボタン無効化
                    recbutton.setEnabled(false);
                    recbutton.setColorFilter(0xaa808080);
                    // 未接続、未送信メッセージが上限に達したダイアログを表示する。
                    vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE4);
                    recLock = true;
                }
            } else {
                // 録音禁止状態なら解除する
                if (recLock) {
                    recbutton.setEnabled(true);
                    recLock = false;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            // 録音開始
                            if ((!recflag)
                                    && (voiceMessageSubFunctions.getVolumeKeyCount() == 0)) {
                                // 録音タイマー開始、録音開始
                                recflag = true;
                                btnflag = true;
                                // メッセージ受信停止
                                //vmc.stopShareAlive(this);
                                // メッセージ再生ロック
                                pmc.lockMessaggPlay();
                                // 録音サービスを開始する。
                                UpdateText(REC_STAT_EXEC);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    // Android 8.0以降
                                    intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
                                    intent.setAction(ConstantRecorder.ACT_REQUEST);
                                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_START);
                                    actMyself.sendBroadcast(intent);
                                } else {
                                    // Android 7.1.1以前
                                    intent = new Intent(ConstantRecorder.ACT_REQUEST);
                                    intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_START);
                                    actMyself.sendBroadcast(intent);
                                }
                            }
                            return true;
                    }
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            if (recflag) {
                                // 録音タイマー終了、録音停止
                                // 録音終了、送信メッセージ
                                if (btnflag) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        // Android 8.0以降
                                        intent = new Intent(actMyself, RecorderRcvNotify.class);
                                        intent.setAction(ConstantRecorder.ACT_REQUEST);
                                        intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                                        actMyself.sendBroadcast(intent);
                                    } else {
                                        // Android 7.1.1以前
                                        intent = new Intent(ConstantRecorder.ACT_REQUEST);
                                        intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_STOP);
                                        actMyself.sendBroadcast(intent);
                                    }
                                } else {
                                    SendCompleteNotify();
                                }
                                // メッセージ再生アンロック
                                pmc.unlockMessagePlay();
                                // メッセージ受信開始
                                //vmc.startShareAlive(this);
                                recflag = false;
                                btnflag = false;
                            }
                            if (voiceMessageSubFunctions.getVolumeKeyUpStatus()) {
                                voiceMessageSubFunctions.VolumeKeyCountDown();
                                if (voiceMessageSubFunctions.getVolumeKeyCount() < 1) {
                                    voiceMessageSubFunctions.initVolumeKeyUp();
                                }
                            } else {
                                voiceMessageSubFunctions.initVolumeKeyCount();
                                voiceMessageSubFunctions.initVolumeKeyUp();
                            }
                            //return true;
                            break;
                    }
                }
            }
        }
        return bret;
    }

    @Override
    public void onDestroy() {
        // メッセージ再生アンロック
        pmc.unlockMessagePlay();
        // メッセージ受信開始
        //vmc.startShareAlive(this);
        if (voiceMessageSubFunctions.getVolumeKeyStatus()) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, saveVolume, 0);
        }
        voiceRecorder.CancelSendWaitTimer();
        actMyself = null;
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
   }


    private void UpdateText(final int cmd) {
        final TextView tv = (TextView) findViewById(R.id.chronometer);

        recHandle.post(new Runnable() {
            @Override
            public void run() {
                if (cmd == REC_STAT_EXEC) {
                    tv.setText(R.string.voicerec_exec, TextView.BufferType.NORMAL);
                } else if (cmd == REC_STAT_END) {
                    tv.setText(R.string.voicerec_end, TextView.BufferType.NORMAL);
                } else if (cmd == REC_STAT_SEND) {
                    //tv.setText(R.string.voicerec_send, TextView.BufferType.NORMAL);
                } else {
                    tv.setText(R.string.voicerec_start, TextView.BufferType.NORMAL);
                }
            }
        });
    }

    public class VoiceMessageEventListener implements EventListener {
        public void SendMessageComplete() {
            // RECボタンが(STOP)が押されたことを疑似的に発生させる。
            if (btnflag) {
                btnflag = false;
                if (voiceMessageSubFunctions.getVolumeKeyStatus()) {
                    voiceMessageSubFunctions.VolumeKeyCountDownStart();
                    voiceMessageSubFunctions.setVolumeKeyUp();
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP));
                } else {
                    recbutton.performClick();
                }
            } else {
                SendCompleteNotify();
            }
        }

        public void SendMessageExec() {
            UpdateText(REC_STAT_SEND);
        }

    }

    private void SendCompleteNotify() {
        UpdateText(REC_STAT_SEND);
        Intent intent;
        // タイムアウト、録音終了指示を発行する。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降
            intent = new Intent(VoiceMessage.actMyself, RecorderRcvNotify.class);
            intent.setAction(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_SHOW);
            sendBroadcast(intent);
        } else {
            // Android 7.1.1以前
            intent = new Intent(ConstantRecorder.ACT_REQUEST);
            intent.putExtra(ConstantRecorder.EXTRA_EVENT, ConstantRecorder.EVENT_SHOW);
            sendBroadcast(intent);
        }
    }

    public void showSendComplete() {
        // RECボタンに入れ替えて有効化
        recbutton.setImageResource(R.drawable.recstart);
        recbutton.setEnabled(true);
        UpdateText(REC_STAT_IDLE);
        if (vmc.checkNodeStatus(this)) {
            // メッセージ送信完了
            Toast toast = Toast.makeText(this, R.string.dialog_message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
            toast.show();
        }

    }

    public void Recording_Called_cancel() {
        // 録音中は着信を拒否する。
        Log.i("nassua", "Voice Message Recording now. Cancel Telephone call.");
        // 着信音の停止
        MediaPlayer mMediaPlyer = new MediaPlayer();
        mMediaPlyer.stop();
        // コンテンツプロバイダから情報取得
        DbDefinePhone.StatPhone statPhone = new DbDefinePhone.StatPhone();
        statPhone = BoatApi.readStatPhone(this);
        String uri = statPhone.mUriThere;

        //通話サービス 切断
        Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
        intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_DISCONNECT);
        intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
        sendBroadcast(intent_bcast);
    }

    public void EnableRecButton() {
        // RECボタンに入れ替えて有効化
        recbutton.setEnabled(true);
    }

}
