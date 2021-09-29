package jp.co.nassua.nervenet.vmphonedepend;

// 着呼通話処理
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
import android.app.Activity;
import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.StringWriter;

import jp.co.nassua.nervenet.boat.ConstantBoat;
import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.Dialing.dialing;
import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.phone.DbDefinePhone;

import jp.co.nassua.nervenet.playmessage.PlayMessageCommon;
import jp.co.nassua.nervenet.song.DbDefineSong;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.PrefApp;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

/**
 * 着信表示Activityクラス
 *
 * @author Mori Hideaki
 */
public class ActRinging extends Activity implements OnClickListener {

    final String TAG = "ActRinging";
    private Handler handNotify = null;      //NerveNet端末機能 通知受信ハンドラ
    public Handler handPhone = null;      //外部操作イベント受信ハンドラー

    // 変数宣言
    private PrefApp pref;                //アプリケーション設定
    private ImageButton btRinging;            // 着信応答ボタン
    private ImageButton btCancel;           // 着信拒否ボタン
    private static boolean btcancel;
    public Handler handOperate = null;      //外部操作イベント受信ハンドラー
    private static String callnum;
    private String phonePhase; //通話状態
    private short boatPhase;  //端末状態
    private String callText;

    private final Handler callHandle = new Handler();
    //private static RcvNotify rcvNotify;    //通知待ち受け
    private Cursor dbCursor;
    private static String dialerName;
    private Ringtone mRingtone;
    public static CallUri callUri;
    UserProfilePermission userProfilePermission;
    ContentResolver mCResolver = null;
    PlayMessageCommon pmc;
    VoiceMessageCommon vmc;

    //定数
    public static ActRinging actMyself = null;  //プロセス上の単一インスタンス
    private static final String logLabel = "ActRinging";
    private static final int WHAT_TSG = 1;
    private static final int WHAT_PHONE = 2;
    //端末状態
    private static final short BoatPhase_INIT = 0; //初期状態
    private static final short BoatPhase_WATCH = 1; //基地局 探索中
    private static final short BoatPhase_DISCOVER = 2; //基地局 捕捉完了
    // 通話状態
    public final short STAT_UNKNOWN = 0;    // 状態不明
    public final short STAT_CALLING = 1;    // 呼び出し中
    public final short STAT_CONNECT = 2;    // 通話中
    public final short STAT_DISCONNECT = 3; // 終話
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * インスタンス生成
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor addrcur = null;
        boolean osflag, readflag;
        int curcnt, curidx2;
        String fieldname2 = "phonenumber";
        String dialerNumber;
        String phonenum;

        //自身のインスタンスを公開
        actMyself = this;
        callUri = new CallUri();
        //リソースからレイアウト指定
        setContentView(R.layout.activity_act_ringing);
        //設定読み取り
        pref = new PrefApp();
        pref.readPreference(this);
        if (pref.wantedLog(PrefApp.LV_INFO)) {
            Log.d(logLabel, "onCreate");
        }

        // URI格納
        Intent intent = (Intent) getIntent();
        String sAction = intent.getAction();

        // コンテンツプロバイダから情報取得;
        DbDefinePhone.StatPhone statPhone = new DbDefinePhone.StatPhone();
        statPhone = BoatApi.readStatPhone(this);
        callnum = statPhone.mUriThere;
        callUri.setDisplayCallNumber(getCallingNumber(callnum));

        // 発信者名を取得する
        dialerName = null;
        dialerNumber = callUri.getDisplayCallNumber().toString();
        userProfilePermission = new UserProfilePermission();
        osflag = userProfilePermission.getAndroid6Status();
        readflag = userProfilePermission.getReadProfileStatus();
        if (osflag) {
            if (readflag) {
                // Android 6.0以降の OSは連絡先読み取り許可がある時だけ実行可。
                mCResolver = getContentResolver();
                addrcur = mCResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            }
        } else {
            // Android 5.1以前の OSは実行可。
            mCResolver = getContentResolver();
            addrcur = mCResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        }
        dbCursor = getCursor();
        curcnt = dbCursor.getCount();
        dbCursor.moveToFirst();
        try {
            for (int i = 0; i < curcnt; i++) {
                // 表示優先順位：名前 > 電話番号 > SIP-URL
                // OSが Android 5.1以前か連絡先にアクセス許可がある場合のみ電話番号検索を実行。
                if ((readflag) && (addrcur != null)) {
                    if (addrcur.moveToFirst()) {
                        do {
                            // 登録されている電話番号から "-" を取り除く。
                            phonenum = addrcur.getString(addrcur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("-", "");
                            // 電話番号が一致するかチェックする。
                            if (dialerNumber.equals(phonenum)) {
                                // 電話番号が一致したら名前を取得して抜ける。
                                dialerName = addrcur.getString(addrcur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                break;
                            }
                        } while (addrcur.moveToNext());
                    }
                }
                if (dialerName != null) break;
                dbCursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((readflag) && (addrcur != null)) {
            addrcur.close();
        }
        // 発信者名が登録されていなければ番号を表示する。
        if (dialerName == null) {
            dialerName = dialerNumber;
        }
        TextView tview = (TextView) findViewById(R.id.textView2);
        tview.setMovementMethod(ScrollingMovementMethod.getInstance());
        tview.setText(dialerName);

        //ボタン作成 with クリック設定
        btRinging = (ImageButton) findViewById(R.id.imageButton2);
        btRinging.setOnClickListener(this);

        //拒否ボタン1度受けてから切る
        btCancel = (ImageButton) findViewById(R.id.imageButton1);
        btCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btcancel = true;
                execRingingStop(true);
            }
        });

        // メッセージ受信解除
        vmc = new VoiceMessageCommon();
        vmc.stopShareAlive(this);
        vmc.stopChatMessageService(this);
        // メッセージ再生機能ロック
        pmc = new PlayMessageCommon();
        pmc.lockMessaggPlay();
        // 着信音鳴動
        Uri uri = null;
        try {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            mRingtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        phonePhase = ConstantPhone.PHASE_INCOMMING;
    }



    /**
     * Activity再起動時のインテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        //アクション指定
        // ログの記録
        if (pref.wantedLog(PrefApp.LV_INFO)) {
            Log.d(logLabel, "onNewIntent");
        }
        super.onNewIntent(intent);

    }

    /**
     * UIの最前面へ
     */
    @Override
    protected void onResume() {
        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // タイトル設定
        setTitle(getString(R.string.phone_title) + " " + ActMain.appversion);
        actMyself = this;
        btcancel = false;

        // ログの記録
        if (pref.wantedLog(PrefApp.LV_INFO)) {
            Log.d(logLabel, "onResume");
        }
        super.onResume();
    }

    /**
     * 画面消滅
     */
    @Override
    protected void onDestroy() {
        if (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_CONNECTED)) {
            // 通話状態なら切断する。
            stopHandler();
            // コンテンツプロバイダから情報取得
            DbDefinePhone.StatPhone statPhone = new DbDefinePhone.StatPhone();
            statPhone = BoatApi.readStatPhone(this);
            final String uri = statPhone.mUriThere;

            Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
            intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_DISCONNECT);
            intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
            sendBroadcast(intent_bcast);
            phonePhase = ConstantPhone.PHASE_IDLE;
        }
        // 着信音停止(着信音鳴動中の時)
        stopRingtone();
        // メッセージ再生ロック解除
        pmc.unlockMessagePlay();
        // メッセージ受信開始
        vmc.startShareAlive(this);
        vmc.startChatMessageService(this);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (phonePhase != null) {
            if (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_INIT)
                    || (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_IDLE))) {
                // 復帰時に切断されていたら画面を消す。
                Log.i("nassua", "onRestart: Disconnected.");
                finish();
            }
        }
    }

    /**
     * Homeボタンが押された
      */
    @Override
    public void onUserLeaveHint() {
        // 着信音停止(着信音が鳴動していた場合)
        stopRingtone();
        if (phonePhase != null) {
            // 着呼中ならば着呼拒否
            if (phonePhase.equalsIgnoreCase(ConstantPhone.PHASE_INCOMMING)) {
                cancelCalled();
            }
        }
    }

    /**
     * ボタンクリック時処理
     *
     * @param view ビュー
     */
    @Override
    public void onClick(View view) {
        // 処理の呼出し
        //通話ボタン
        if (view == btRinging) {
            execRinging(false);
        }
    }

    /**
     * 通話停止時呼出し
     */
    private void execRingingStop(boolean bRefuse) {
        StringWriter sw = new StringWriter();
        sw.write("Unknown operation");

        // 着信音の停止
        stopRingtone();
        // コンテンツプロバイダから情報取得
        DbDefinePhone.StatPhone statPhone = new DbDefinePhone.StatPhone();
        statPhone = BoatApi.readStatPhone(this);
        String uri = statPhone.mUriThere;

        //通話サービス 切断
        Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
        intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_DISCONNECT);
        intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
        sendBroadcast(intent_bcast);
        stopHandler();
        // メイン画面へ戻る
        if (ActMain.actMyself != null) {
            returnActMainTop();
        } else {
            finish();
        }
    }

    /**
     * 通話開始時呼出し
     */
    private void execRinging(boolean bRefuse) {
        StringWriter sw = new StringWriter();
        sw.write("Unknown operation");

        // 着信音の停止
        stopRingtone();
        // コンテンツプロバイダから情報取得
        DbDefinePhone.StatPhone statPhone = new DbDefinePhone.StatPhone();
        statPhone = BoatApi.readStatPhone(this);
        final String uri = statPhone.mUriThere;

        //通話サービス 接続確立
        Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
        intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_CONNECT);
        intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
        sendBroadcast(intent_bcast);
        startHandler();

        // 通話画面表示
        LinearLayout layouut = (LinearLayout) findViewById(R.id.ringing);
        layouut.removeAllViews();
        getLayoutInflater().inflate(R.layout.called_layout, layouut);
        final TextView tvnum = (TextView) findViewById(R.id.tVCallingNum);
        //tvnum.setText(callUri.getDisplayCallNumber(), TextView.BufferType.NORMAL);
        tvnum.setText(dialerName, TextView.BufferType.NORMAL);

        final ImageButton endbutton = (ImageButton) findViewById(R.id.imageEndButton);
        endbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通話サービス 切断
                // 着信後、通話状態からすぐに切断すると発呼側に切断されたことが通知されない。
                try {
                    // 対策として 1000ミリ秒スリープする。
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent_bcast = new Intent(ConstantPhone.ACT_REQUEST);
                intent_bcast.putExtra(ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_DISCONNECT);
                intent_bcast.putExtra(ConstantPhone.EXTRA_URI_THERE, uri);
                sendBroadcast(intent_bcast);
                if (ActMain.actMyself == null) {
                    finish();
                } else {
                    returnActMainTop();
                }
            }
        });
    }

    /**
     * 着信音停止
     */
    private void stopRingtone() {
        if (mRingtone != null) {
            if (mRingtone.isPlaying()) {
                mRingtone.stop();
                mRingtone = null;
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
        // 着信拒否時はハンドラガー登録されていないので無視する
        if (handNotify != null) {
            Message msg = handNotify.obtainMessage(WHAT_PHONE);
            msg.setData(extras);
            handNotify.sendMessage(msg);
        } else {
            DbDefinePhone.StatPhone rec = BoatApi.readStatPhone(this);
            if (rec != null) {
                if (rec.mPhase != null && !rec.mPhase.equalsIgnoreCase(phonePhase)) {
                    phonePhase = rec.mPhase;
                }
            }
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
                // 通話中→終話
                if (phonePhase.equals(ConstantPhone.PHASE_CONNECTED)
                        && (rec.mPhase.equals(ConstantPhone.PHASE_IDLE))) {
                    UpdateText(STAT_DISCONNECT);
                    // ハンドラー解除
                    stopHandler();
                    // メイン画面に戻る
                    if (ActMain.actMyself != null) {
                        phonePhase = rec.mPhase;
                        returnActMainTop();
                    } else {
                        finish();
                    }
                }
            }
            //通話状態が変化していれば
            if (rec.mPhase != null && !rec.mPhase.equalsIgnoreCase(phonePhase)) {
                phonePhase = rec.mPhase;
            }
        }
    }

    private void returnActMainTop() {
        Intent intent = new Intent(ActRinging.this, ActMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * 通知の受信を開始
     */
    private void startHandler() {
        //ハンドラーの準備
        handNotify = new HandNotify();
        //公開
        actMyself = this;
    }

    /**
     * 通知の受信を停止
     */
    private void stopHandler() {
        //ブロードキャストレシーバー後始末
        //unregisterReceiver(rcvNotify );
        //rcvNotify = null;
        //隠蔽
        actMyself = null;
        //ハンドラーの後始末
        handPhone = null;
        handNotify = null;
    }

    public void UpdateText(final int cmd) {
        final TextView tvnum = (TextView) findViewById(R.id.tVCallingNum);
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
                tvnum.setText(callUri.getDisplayCallNumber(), TextView.BufferType.NORMAL);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // ダイアログ表示など特定の処理を行いたい場合はここに記述
                    // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void cancelCalled() {
        final TextView tvstat = (TextView) findViewById(R.id.textView1);
        if (!btcancel) {
            tvstat.setText(R.string.phone_cancel, TextView.BufferType.NORMAL);
            // 着信拒否ボタンが押されたことを疑似的に発生させる。
            // ToDo: 一時的に応答ボタンを無効化することも検討する。
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            btCancel.performClick();
            btcancel = true;
        }
    }

    protected String getCallingNumber(String callnum) {
        String wknum = callnum.replaceAll("SipPhone@", "");
        int curidx, curcnt, curidx2;
        String fieldname = "sipuri";
        String fieldname2 = "phonenumber";
        String strd, strd2;
        Cursor dbCursor;

        dbCursor = getCursor();
        curidx = dbCursor.getColumnIndex(fieldname);
        curcnt = dbCursor.getCount();
        curidx2 = dbCursor.getColumnIndex(fieldname2);
        dbCursor.moveToFirst();
        // 発呼したBOATを探す。
        try {
            for (int i = 0; i < curcnt; i++) {
                CharArrayBuffer boatList = new CharArrayBuffer(256);
                CharArrayBuffer boatList2 = new CharArrayBuffer(256);
                dbCursor.copyStringToBuffer(curidx, boatList);
                dbCursor.copyStringToBuffer(curidx2, boatList2);
                strd = String.valueOf(boatList.data).trim();
                strd2 = String.valueOf(boatList2.data).trim();
                // 発呼側の BOATと同じか？
                if ((strd.equals(wknum))
                        && (!strd2.equals(""))) {
                    wknum = strd2;
                    break;
                }
                dbCursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wknum;
    }

    /**
     * カーソル生成
     *
     * @return
     */
    private Cursor getCursor() {
        ContentProviderClient contClient;
        //final String label = logCls+" getCursor";
        //外部端末状態テーブル
        Uri uri_tbl = DbDefineSong.BoatList.CONTENT_URI;
        //コンテントプロバイダへの接続
        ContentResolver resolver = getContentResolver();
        contClient = resolver.acquireContentProviderClient(uri_tbl);
        //端末一覧 読み取り
        try {
            //カーソルを返す
            return contClient.query(uri_tbl, null, null, null, null);
        } catch (RemoteException e) {
            //Log.e( logTag, label+" err="+e.getMessage() );
        }
        //失敗
        return null;
    }
}
