package jp.co.nassua.nervenet.Dialing;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.phone.DbDefinePhone;
import jp.co.nassua.nervenet.playmessage.PlayMessageCommon;
import jp.co.nassua.nervenet.vmphonedepend.BoatApi;
import jp.co.nassua.nervenet.vmphonedepend.RcvNotify;
import jp.co.nassua.nervenet.vmphonedepend.phonedepend;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class dialing extends AppCompatActivity {
    static public dialing actMyself = null; //プロセス上の単一インスタンス
    private Handler handNotify = null;      //NerveNet端末機能 通知受信ハンドラ
    public Handler handPhone = null;      //外部操作イベント受信ハンドラー

    private String callText;
    phonedepend phonedepend = new phonedepend();
    MediaPlayer mMediaPlyer;
    PlayMessageCommon pmc;
    VoiceMessageCommon vmc;
    public final short STAT_UNKNOWN = 0;    // 状態不明
    public final short STAT_CALLING = 1;    // 呼び出し中
    public final short STAT_CONNECT = 2;    // 通話中
    public final short STAT_DISCONNECT = 3; // 終話

    private final Handler callHandle = new Handler();
    private static RcvNotify rcvNotify;    // 通知待ち受け
    private static String callnum;         // 発信用番号
    private static String displaynum;      // 表示用番号

    private short boatPhase;  //端末状態
    private String phonePhase; //通話状態
    //端末状態
    private static final short BoatPhase_INIT = 0; //初期状態
    private static final short BoatPhase_WATCH = 1; //基地局 探索中
    private static final short BoatPhase_DISCOVER = 2; //基地局 捕捉完了
    //定数
    private static final String logLabel = "PhoneDepend";
    private static final int WHAT_TSG = 1;
    private static final int WHAT_PHONE = 2;

    //TextView tvnum;
    //TextView tvstat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_calling);
        final ImageButton endbutton = (ImageButton) findViewById(R.id.imageButton1);
        callnum = phonedepend.callUri.getCallSipUri();
        displaynum = phonedepend.callUri.getDisplayDialingNumber();

        endbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通話終了
                if ((phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_CALLING))
                || (mMediaPlyer.isPlaying())){
                    mMediaPlyer.stop();
                }
                CallTemination();
                // メイン画面に戻る
                Intent intent = new Intent(dialing.this, ActMain.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // メッセージ受信停止
        if (vmc == null) {
            vmc = new VoiceMessageCommon();
        }
        vmc.stopShareAlive(this);
        vmc.stopChatMessageService(this);
        // メッセージ再生ロック
        pmc = new PlayMessageCommon();
        pmc.lockMessaggPlay();

        UpdateText(STAT_CALLING);
        //operateCalling();
        // 呼び出し処理
        //通話サービス 接続確立
        Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
        intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_CONNECT);
        intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, callnum);
        sendBroadcast(intent_bcast);
        startHandler();
    }

    public void UpdateText(final int cmd) {
        final TextView tvnum = (TextView) findViewById(R.id.tVCallNum);
        final TextView tvstat = (TextView) findViewById(R.id.tVCallNormalInfo);

        callHandle.post(new Runnable() {
            @Override
            public void run() {
                switch (cmd) {
                    case STAT_CALLING:
                        tvstat.setText(R.string.phone_dialing, TextView.BufferType.NORMAL);
                        break;
                    case STAT_CONNECT:
                        tvstat.setText(R.string.phone_talking, TextView.BufferType.NORMAL);
                        break;
                    case STAT_DISCONNECT:
                        tvstat.setText(R.string.phone_term, TextView.BufferType.NORMAL);
                        break;
                    default:
                        break;
                }
                tvnum.setMovementMethod(ScrollingMovementMethod.getInstance());
                tvnum.setText(displaynum, TextView.BufferType.NORMAL);
            }
        });
    }

    /**
     * 通知の受信を開始
     */
    private void startHandler() {
        //ハンドラーの準備
        handNotify = new HandNotify();
        //公開
        actMyself = this;

        // 呼び出し音再生
        Uri uri = null;
        try {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMediaPlyer = new MediaPlayer();
        try {
            mMediaPlyer.setDataSource(this, uri);
            mMediaPlyer.setLooping(true);
            mMediaPlyer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlyer.setVolume(0.5f, 0.5f);
            mMediaPlyer.prepare();
            mMediaPlyer.start();
        } catch (IllegalArgumentException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        // 発信音再生中なら停止させる。
        if (mMediaPlyer.isPlaying()) {
            mMediaPlyer.start();
        }
        // 通話状態で画面が破棄されたら通話を終了させる。
        if (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_CONNECTED)) {
            CallTemination();
        }
        // メッセージ再生ロック解除
        pmc.unlockMessagePlay();
        // メッセージ受信開始
        if (vmc == null) {
            vmc = new VoiceMessageCommon();
        }
        vmc.startShareAlive(this);
        vmc.startChatMessageService(this);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_INIT)
                || (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_IDLE))) {
            // 復帰時に切断されていたらメイン画面に戻る。
            Intent intent = new Intent(dialing.this, ActMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onUserLeaveHint() {
        if (phonePhase != null) {
            if ((phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_CALLING))
            || (mMediaPlyer.isPlaying())) {
                mMediaPlyer.stop();
                CallTemination();
            }
        }
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        if (phonePhase != null) {
            if (!phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_IDLE)
                    && (!phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_INIT))) {
                if (KeyCode == KeyEvent.KEYCODE_BACK) {
                    if ((phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_CALLING))
                    || (mMediaPlyer.isPlaying())) {
                        mMediaPlyer.stop();
                    }
                    CallTemination();
                }
            }
        }
        return super.onKeyDown(KeyCode, event);
    }

    /**
     * 通知の受信を停止
     */
    private void stopHandler() {
        //隠蔽
        actMyself = null;
        //ハンドラーの後始末
        handPhone = null;
        handNotify = null;
    }

    /**
     * 画面消滅
     */
    private void CallTemination() {
        if (actMyself != null) {
            //通知用ハンドラーの後始末
            stopHandler();
            // コンテンツプロバイダから情報取得
            DbDefinePhone.StatPhone conf2 = new DbDefinePhone.StatPhone();
            conf2 = BoatApi.readStatPhone(this);
            String uri = conf2.mUriThere;
            //通話サービス 切断
            Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
            intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_DISCONNECT);
            intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
            sendBroadcast(intent_bcast);
            phonePhase = ConstantPhone.PHASE_IDLE;
            UpdateText(STAT_DISCONNECT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //// TSG通知 イベント受信ハンドラー ////
    class HandNotify extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle extras = msg.getData();
            switch (msg.what) {
                case WHAT_TSG:
                    updateTsg(extras);
                    break;
                case WHAT_PHONE:
                    updatePhone(extras);
                    break;
            }
        }
    }

    /**
     * TSG通知 受信
     *
     * @param extras 添付情報
     */
    public void actTsg(Bundle extras) {
        Message msg = handNotify.obtainMessage(WHAT_TSG);
        msg.setData(extras);
        handNotify.sendMessage(msg);
    }

    /**
     * 通話通知 受信
     *
     * @param extras 添付情報
     */
    public void actPhone(Bundle extras) {
        Message msg = handNotify.obtainMessage(WHAT_PHONE);
        msg.setData(extras);
        handNotify.sendMessage(msg);
    }

    /**
     * 端末状態(基地局監視) 反映
     *
     * @param extras 添付情報
     */
    private void updateTsg(Bundle extras) {
        DbDefineBoat.StatNode rec = BoatApi.readStatNode(this);
        //内部状態 更新
        if (rec != null && rec.mIdTsg != null) {
            boatPhase = BoatPhase_DISCOVER;
            phonePhase = ConstantPhone.PHASE_IDLE;
        } else {
            boatPhase = BoatPhase_WATCH;
            phonePhase = ConstantPhone.PHASE_INIT;
        }
    }

    /**
     * 通話状態 反映
     *
     * @param extras 添付情報
     */
    private void updatePhone(Bundle extras) {
        DbDefinePhone.StatPhone rec = BoatApi.readStatPhone(this);
        //内部状態 更新
        if (rec != null) {
            if (phonePhase != null) {
                if (phonePhase.equals(ConstantPhone.PHASE_CALLING)) {
                    // 接続中→通話中
                    if (rec.mPhase.equals(ConstantPhone.PHASE_CONNECTED)) {
                        // 呼び出し音停止
                        mMediaPlyer.stop();
                        UpdateText(STAT_CONNECT);
                        // 接続中→終話(着信拒否)
                    } else if (rec.mPhase.equals(ConstantPhone.PHASE_IDLE)) {
                        // 呼び出し音停止
                        mMediaPlyer.stop();
                        UpdateText(STAT_DISCONNECT);
                    }
                }
                // 通話中→終話
                if (phonePhase.equals(ConstantPhone.PHASE_CONNECTED)
                        && (rec.mPhase.equals(ConstantPhone.PHASE_IDLE))) {
                    CallTemination();
                    phonePhase = rec.mPhase;
                    Intent intent = new Intent(dialing.this, ActMain.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
            //通話状態が変化していれば
            if (rec.mPhase != null && !rec.mPhase.equalsIgnoreCase(phonePhase)) {
                phonePhase = rec.mPhase;
            }
        }
    }

}
