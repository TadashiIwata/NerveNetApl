package jp.co.nassua.nervenet.voicemessage;

// NerveNet通話、Voice Message メイン画面
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import jp.co.nassua.nervenet.boat.ConstantBoat;
import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.groupchatmain.ChatCommon;
import jp.co.nassua.nervenet.groupchatmain.ChatMessageService;
import jp.co.nassua.nervenet.groupchatmain.ConstantGroup;
import jp.co.nassua.nervenet.groupchatmain.GroupChatMain;
import jp.co.nassua.nervenet.groupchatmain.GroupCommon;
import jp.co.nassua.nervenet.groupchatmain.GroupMemberNotify;
import jp.co.nassua.nervenet.phone.DbDefinePhone;
import jp.co.nassua.nervenet.playmessage.PlayMessageAsyncTask;
import jp.co.nassua.nervenet.playmessage.PlayMessageCommon;
import jp.co.nassua.nervenet.playmessage.VoiceDbHelper;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.song.ConstantSong;
import jp.co.nassua.nervenet.vmphonedepend.BoatApi;
import jp.co.nassua.nervenet.vmphonedepend.RcvNotify;
import jp.co.nassua.nervenet.vmphonedepend.UserProfilePermission;
import jp.co.nassua.nervenet.vmphonedepend.phonedepend;
import jp.co.nassua.nervenet.playmessage.PlayMessage;
import jp.co.nassua.nervenet.playmessage.PlayMessageService;
import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.voicerecorder.VoiceMessageSubFunctions;
import jp.co.nassua.nervenet.voicerecorder.VoiceMessage;

public class ActMain extends AppCompatActivity {
    static public ActMain actMyself = null; //プロセス上の単一インスタンス
    private DbDefineBoat.ConfNode confNode;
    private ContentResolver contentResolver;
    Intent intent;
    private VoiceMessageSubFunctions voiceMessageSubFunctions;
    private static VoiceMessageCommon vmc;
    private PlayMessageAsyncTask pmd;
    private PlayMessageCommon pmc;
    private VoiceDbHelper voiceDbHelper;
    private static GroupCommon groupCommon;
    private static ChatCommon chatCommon;
    private static boolean chatAutoReceive = false;

    public DbDefinePhone.StatPhone rec;
    public DbDefineBoat.ConfNode conf_node;
    public DbDefinePhone.ConfPhone conf_phone;
    // 環境設定
    private static String VoiceBoxName = "VoiceMessage";
    private File envFile;
    public static final String envFilename = "voicemessage.conf";
    public String envfilename;
    public static String appversion;
    private static boolean connectFlag;
    // パーミッションリクエスト
    private static boolean osflag;
    UserProfilePermission userProfilePermission;
    private static final int PERMISSIONS_REQUEST = 1;
    private static int requestCnt;
    AlertDialog.Builder alertDialog;
    private final Handler alertHandle = new Handler();
    // 通話機能
    private RcvNotify rcvNotify;    //通知待ち受け
    public PrefApp pref;           //アプリケーション設定
    // オプションメニュー
    public static final int OPTION_SUBMENU_DISCARD = Menu.FIRST;
    public static final int OPTION_SUBMENU_RECMAX = OPTION_SUBMENU_DISCARD + 1;
    public static final int OPTION_SUBMENU_RECBUTTON = OPTION_SUBMENU_RECMAX + 1;
    public static final int OPTION_SUBMENU_PLAYMODE = OPTION_SUBMENU_RECBUTTON + 1;
    public static final int OPTION_SUBMENU_AUDIOFORMAT = OPTION_SUBMENU_PLAYMODE + 1;
    public static final int OPTION_SUBMENU_AUDIOENCODER = OPTION_SUBMENU_AUDIOFORMAT + 1;
    public static final int OPTION_SUBMENU_PASTPLAY = OPTION_SUBMENU_AUDIOENCODER + 1;
    public static final int OPTION_SUBMENU_TIME_CORRECTION = OPTION_SUBMENU_PASTPLAY + 1;
    public static final int OPTION_SUBMENU_INITDB = OPTION_SUBMENU_TIME_CORRECTION + 1;
    // ダイアログ
    public static final int OPTION_DISCARD_DIALOG = 0;
    public static final int OPTION_RECTIME_DIALOG = OPTION_DISCARD_DIALOG + 1;
    public static final int OPTION_AUDIOFORMAT_DIALOG = OPTION_RECTIME_DIALOG + 1;
    public static final int OPTION_AUDIOENCODER_DIALOG = OPTION_AUDIOFORMAT_DIALOG + 1;
    public static final int OPTION_BS_TIME_CORRECTION_DIALOG = OPTION_AUDIOENCODER_DIALOG + 1;
    public static final int OPTION_INITDB_DIALOG = OPTION_BS_TIME_CORRECTION_DIALOG + 1;
    // 基地局設定
    public final static int TIME_CORRECTION_NONE = 0;
    public final static int TIME_CORRECTION_NOTICE = TIME_CORRECTION_NONE + 1;
    public final static int TIME_CORRECTION_YES = TIME_CORRECTION_NOTICE + 1;
    // オプション設定
    public static boolean AutoPlay;
    public static boolean PastPlay;
    public static boolean RecButton;
    public static int MaxRecTime;
    public static long MessageLife;
    public static int DiscardType;
    public static int AudioFormat;
    public static int AudioEncoder;
    public static int TimeCorrection;
    // オプションパラメータ
    //private static long MESSAGE_LIFE = 3600 * 24 * 7;  // メッセージの有効期間はとりあえず一週間
    //private static long MESSAGE_LIFE = 3600 * 24;  // メッセージの有効期間 24時間 デバッグ用
    //private static long MESSAGE_LIFE = 3600 * 2;  // メッセージの有効期間 2時間　デバッグ用
    private static long MESSAGE_LIFE = 3600;  // メッセージの有効期間 1時間　デバッグ用
    //private static long MESSAGE_LIFE = 1800;  // メッセージの有効期間 30分 デバッグ用
    //private static long MESSAGE_LIFE = 600;  // メッセージの有効期間 10分 デバッグ用
    private static int RECMAX_TIME = 30;       // 録音時間：最大 30秒
    private final static int DISCARD_TYPE_DAYS = 0;
    private final static int DISCARD_TYPE_HOURS = DISCARD_TYPE_DAYS + 1;
    private final static int DISCARD_TYPE_MINUTES = DISCARD_TYPE_HOURS + 1;
    private static String sAutoPlay = "AutoPlay=yes";
    private static String sPastPlay = "PastMessage=no";
    private static String sMaxRecTime = "MaxRecTime=30";
    private static String sRecButton = "VolumeRec=no";
    private static String sDiscardType = "DiscardType=hours";
    private static String sDiscardValue = "DiscardValue=1";
    private static String sAudioFormat = "AudioFormat=3GP";
    private static String sAudioEncoder = "AudioEncoder=Default";
    private static String sTimeCorrection = "TimeCorrection=Notice";

    private static final int NOTIFICATION_ID = R.layout.activity_act_main;

    // Moor用
    private static MoorRcvNotify moorRcvNotify;
    private static boolean BsGetTimeFlag;
    private static boolean BsSetTimeFlag;
    private static long bsDate;
    public LayoutInflater inflater;

    // Group Chat用
    private static GroupMemberNotify groupMemberNotify;

    public ActMain() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_act_main);

        userProfilePermission = new UserProfilePermission();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Android 5.1 以前
            osflag = false;
        } else {
            // Android 6.0 以降
            osflag = true;
        }
        userProfilePermission.setAndroid6Status(osflag);
        // パーミッションチェック
        if (osflag) {
            checkPermissions();
        } else {
            // Android 5.0以前は連絡帳等へアクセスを許可する。
            userProfilePermission.setReadProfileStatus(true);
            userProfilePermission.setStorageWriteStatus(true);
            userProfilePermission.setInputMicStatus(true);
            userProfilePermission.setCameraStatus(true);
            userProfilePermission.setVibrateStatus(true);
        }

        //バージョン表示
        String pkg_name = getPackageName();
        try {
            PackageInfo pkg_info = getPackageManager().getPackageInfo(pkg_name, PackageManager.GET_META_DATA);
            appversion = pkg_info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // no version name
        }

        // DB作成
        voiceDbHelper = new VoiceDbHelper(this);

        //自身のインスタンスを公開
        actMyself = this;
        // 設定読み取り
        pref = new PrefApp();
        pref.readPreference(this);
        if (pref.useBlob) {
            // 音声データの出力先を FILEにする。(BLOBは使用しない)
            pref.useBlob = false;
            pref.savePreference(this);
        }

        if (vmc == null) {
            vmc = new VoiceMessageCommon();
        }
        // OS Versionフラグ設定
        //vmc.setOsFlag(osflag);
        // 通話機能を開始
        vmc.startPhoneNotify(this);
        // 基地局間データ同期の開始
        vmc.startSongAlive(this);

        // Voice Message 機能開始
        rec = BoatApi.readStatPhone(this);
        //諸元
        conf_node = BoatApi.readConfNode(this);
        conf_phone = BoatApi.readConfPhone(this);

        // 設定読み取り
        readConfNode();
        BoxVoice.idMyself = confNode.mIdBoat;
        BoxVoice.uriMyself = confNode.mUriBoat;
        BoxVoice.idBox = VoiceBoxName.getBytes();
        voiceMessageSubFunctions = new VoiceMessageSubFunctions();
        voiceMessageSubFunctions.setPhoneNumber(confNode.mPhoneNumber);
        voiceMessageSubFunctions.setUriBoart(confNode.mUriBoat);

        //  端末間共有を開始
        //vmc.startShareAlive(this);


        IntentFilter filter1 = new IntentFilter();
        // Moor用ブロードキャストレシーバ登録
        moorRcvNotify = new MoorRcvNotify();
        filter1.addAction(ConstantBoat.ACT_MOOR_NOTIFY);
        getApplicationContext().registerReceiver(moorRcvNotify, filter1);

        // テストモード無効
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        chatCommon.setTestModee(false);

        super.onCreate(savedInstanceState);
    }

    public void onClick(View view) {
        boolean alertflag;

        if (!connectFlag) {
            // 基地局と接続されているかをチェック
            if (!(vmc.checkNodeStatus(this))) {
                // 未接続ダイアログを表示する。
                vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE1);
                connectFlag = false;
            } else {
                connectFlag = true;
            }
        }
        switch (view.getId()) {
            case R.id.button:
                if (connectFlag) {
                    // NerveNet通話画面へ遷移
                    intent = new Intent(this, phonedepend.class);
                    startActivity(intent);
                } else {
                    // 未接続ダイアログを表示する。
                    vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE1);
                }
                break;
            case R.id.button2:
                alertflag = true;
                if ((userProfilePermission.getStorageWriteStatus())) {
                    if ((userProfilePermission.getInputMicStatus())) {
                        // メッセージ録音、送信画面へ遷移
                        alertflag = false;
                        intent = new Intent(this, VoiceMessage.class);
                        startActivity(intent);
                    } else {
                        alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setTitle(R.string.alert_mic_title);
                        alertDialog.setMessage(R.string.alert_mic_message);
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkPermissions();
                            }
                        });
                        alertDialog.create();
                    }
                } else {
                    alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle(R.string.alert_strage_title);
                    alertDialog.setMessage(R.string.alert_strage_message);
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkPermissions();
                        }
                    });
                    alertDialog.create();
                }
                if (alertflag) {
                    alertHandle.post(new Runnable() {
                        @Override
                        public void run() {
                            Date dd = new Date();
                            alertDialog.show();
                        }
                    });
                }
                break;
            case R.id.button3:
                // メッセージリスト表示、再生画面へ遷移
                intent = new Intent(this, PlayMessage.class);
                startActivity(intent);
                break;
            case R.id.button4:
                //if (osflag) {
                    if (connectFlag) {
                        // チャットグループ画面へ遷移
                        intent = new Intent(this, GroupChatMain.class);
                        startActivity(intent);
                    } else {
                        // 未接続ダイアログを表示する。
                        vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE1);
                    }
                //} else {
                    // Android 5以前では使用不可のメッセージを出す。
                //}
                break;
        }
    }

    @Override
    public void onResume() {
        // 通知を消す
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        Context context = null;
        //  端末間共有を開始
        vmc.startShareAlive(this);
        // タイトル設定
        //setTitle("VoiceMessage " + appversion);
        setTitle("NerveNet Application " + appversion);
        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 自身のインスタンスを公開
        actMyself = this;
        // BOATリスト取得
        getBoatList();
        // 環境設定ファイル読込
        readConf();
        if (AutoPlay) {
            // 自動再生設定されていて、サービス未起動ならサービスを起動する。
            if (pmc == null) {
                pmc = new PlayMessageCommon();
            }
            if ((PlayMessageService.actMyself == null) || (!pmc.getPlayMessageServiceStatus())) {
                context = getBaseContext();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(context, PlayMessageService.class));
                } else {
                    startService(new Intent(context, PlayMessageService.class));
                }
            }
        }
        // 基地局と接続されているかをチェック。
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!(vmc.checkNodeStatus(this))) {
            // 未接続ダイアログを表示する。
            vmc.alertBaseStation(this, vmc.ALERT_MESSAGE_TYPE1);
            connectFlag = false;
        } else {
            connectFlag = true;
        }
        if (connectFlag) {
            RequestBaseStationDate();
        }
        // チャットメッセージをバックグランドで受信するためのサービスを起動する。
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (chatAutoReceive) {
            vmc.startChatMessageService(getBaseContext());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        actMyself = null;
        super.onDestroy();
    }

    // データベース初期化
    public void InitDB() throws RemoteException {
        Cursor cursor;
        // SQL文
        final String DELETE_RECORDS = "delete from voice_message";

        // 1. 作業用DBの初期化(全レコード削除)
        InitDbHelper1 initDbHelper1;
        initDbHelper1 = new InitDbHelper1(getBaseContext());
        SQLiteDatabase db = initDbHelper1.getWritableDatabase();
        cursor = db.rawQuery(DELETE_RECORDS, null);
        cursor.moveToFirst();
        cursor.close();
        /* for debug
        int cnt;
        final String SELECT_ALL = "select * from voice_message";

        cnt = cursor.getCount();
        Log.i("nassua", "delete count: " + cnt);
        cursor = db.rawQuery(SELECT_ALL, null);
        cnt = cursor.getCount();
        Log.i("nassua", "delete faild count: " + cnt);
        cursor.close();
        */
        // 2. 音声ファイルの削除
        pmd = new PlayMessageAsyncTask();
        for(int idx=0; idx < pmd.MessageTotal; idx++) {
            String filename = pmd.messageList.get(idx).uriFile;
            Log.i("nassua", "delete filename: " + filename);
            File pcmfile = new File(filename);
            pcmfile.delete();
        }
        // 3. 端末間情報共有テーブルから VoiceMessageのレコードを削除
        String column = "id_box like ?";
        String[] VoiceBox = {"VoiceMessage"};
        ContentResolver resolver1;
        Uri uri_tbl = DbDefineShare.BoxShare.CONTENT_URI;
        resolver1 = getContentResolver();
        ContentResolver cont = resolver1;
        ContentProviderClient resolver2 = cont.acquireContentProviderClient( uri_tbl );
        try {
            //resolver2.delete(uri_tbl, null, null);
            //resolver2.delete(uri_tbl, "id_box='VoiceMessage'", null);
            resolver2.delete(uri_tbl, column, VoiceBox);
        } catch (RemoteException e) {
            Log.i("nassua", "Record delete faild.");
        }
    }

    private void readConf() {
        envfilename = Environment.getExternalStorageDirectory() + "/" + envFilename;
        envFile = new File(envfilename);
        int discardtm = 0;
        int paramCheck = 0;
        String wkautoplay = null;
        String wkpastplay = null;
        String wkrectime = null;
        String wkrecvol = null;
        String wkdiscardtype = null;
        String wkdiscardvalue = null;
        String wkaudioformat = null;
        String wkaudioencoder = null;
        String wktimecorrection = null;
        // 既定値を設定しておく。
        AutoPlay = true;
        PastPlay = false;
        MaxRecTime = RECMAX_TIME;
        MessageLife = MESSAGE_LIFE;
        DiscardType = DISCARD_TYPE_HOURS;
        RecButton = false;
        if (vmc == null) {
            vmc = new VoiceMessageCommon();
        }
        String SaveBuff = null;
        if (envFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(envFile);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufread = new BufferedReader(isr);
                String Param, Values;
                boolean rflag = false, wkflag = false;
                int sidx, eidx;
                while ((Param = bufread.readLine()) != null) {
                    if (!rflag) {
                        if (Param.toLowerCase().indexOf("recmode") != -1) {
                            rflag = true;
                            paramCheck = 1;
                            continue;
                        }
                    } else {
                        // 自動再生モード設定
                        if (Param.toLowerCase().indexOf("autoplay") != -1) {
                            sidx = Param.toLowerCase().indexOf("autoplay=") + 9;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.toLowerCase().indexOf("no") != -1) {
                                AutoPlay = false;
                            } else {
                                AutoPlay = true;
                            }
                            paramCheck = paramCheck + 2;
                            wkautoplay = Param;
                            continue;
                        }
                        // 録音時間設定
                        if (Param.toLowerCase().indexOf("maxrectime") != -1) {
                            sidx = Param.toLowerCase().indexOf("maxrectime=") + 11;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            MaxRecTime = Integer.parseInt(Values);
                            voiceMessageSubFunctions = new VoiceMessageSubFunctions();
                            voiceMessageSubFunctions.setRecTimeValue(MaxRecTime);
                            paramCheck = paramCheck + 4;
                            wkrectime = Param;
                            continue;
                        }
                        // 音量ボタンによる録音
                        if (Param.toLowerCase().indexOf("volumerec") != -1) {
                            sidx = Param.toLowerCase().indexOf("volumerec=") + 10;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.toLowerCase().indexOf("no") != -1) {
                                RecButton = false;
                                voiceMessageSubFunctions.UnLockVolumeKey();
                            } else {
                                RecButton = true;
                                voiceMessageSubFunctions.LockVolumeKey();
                            }
                            paramCheck = paramCheck + 8;
                            wkrecvol = Param;
                            continue;
                        }
                        // メッセージ有効期限単位
                        if (Param.toLowerCase().indexOf("discardtype") != -1) {
                            sidx = Param.toLowerCase().indexOf("discardtype=") + 12;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            // 日数
                            if (Values.toLowerCase().indexOf("days") != -1) {
                                discardtm = 24 * 3600;
                                DiscardType = DISCARD_TYPE_DAYS;
                            } else if (Values.toLowerCase().indexOf("hours") != -1) {
                                discardtm = 3600;
                                DiscardType = DISCARD_TYPE_HOURS;
                            } else if (Values.toLowerCase().indexOf("minutes") != -1) {
                                discardtm = 60;
                                DiscardType = DISCARD_TYPE_MINUTES;
                            }
                            paramCheck = paramCheck + 16;
                            wkdiscardtype = Param;
                            continue;
                        }
                        // メッセージ有効期限値
                        if (discardtm != 0) {
                            if (Param.toLowerCase().indexOf("discardvalue") != -1) {
                                sidx = Param.toLowerCase().indexOf("discardvalue=") + 13;
                                eidx = Param.length();
                                Values = Param.substring(sidx, eidx);
                                MessageLife = Long.parseLong(Values) * discardtm;
                                paramCheck = paramCheck + 32;
                                wkdiscardvalue = Param;
                                continue;
                            }
                        }
                        // 録音形式
                        if (Param.toLowerCase().indexOf("audioformat") != -1) {
                            sidx = Param.toLowerCase().indexOf("audioformat=") + 12;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.equalsIgnoreCase("pcm")) {
                                AudioFormat = vmc.AUDIO_FORMAT_PCM;
                            } else if (Values.equalsIgnoreCase("3gp")) {
                                AudioFormat = vmc.AUDIO_FORMAT_3GP;
                             }
                            vmc.setAudioFormat(AudioFormat);
                            paramCheck = paramCheck + 64;
                            wkaudioformat = Param;
                            continue;
                        }
                        // エンコーダ
                        if (Param.toLowerCase().indexOf("audioencoder") != -1) {
                            sidx = Param.toLowerCase().indexOf("audioencoder=") + 13;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.equalsIgnoreCase("default")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_DEFAULT;
                            } else if (Values.equalsIgnoreCase("aac")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_AAC;
                            } else if (Values.equalsIgnoreCase("he_aad")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_HE_AAC;
                            } else if (Values.equalsIgnoreCase("aac_eld")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_AAC_ELD;
                            } else if (Values.equalsIgnoreCase("amr_nb")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_AMR_NB;
                            } else if (Values.equalsIgnoreCase("amr_wb")) {
                                AudioEncoder = vmc.AUDIO_ENCODER_AMR_WB;
                            }
                            vmc.setAudioEncoder(AudioEncoder);
                            paramCheck = paramCheck + 128;
                            wkaudioencoder = Param;
                            continue;
                        }
                        // 起動前に録音されたメッセージ再生設定
                        if (Param.toLowerCase().indexOf("pastmessage") != -1) {
                            sidx = Param.toLowerCase().indexOf("pastmessage=") + 12;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.toLowerCase().indexOf("no") != -1) {
                                PastPlay = false;
                            } else {
                                PastPlay = true;
                            }
                            paramCheck = paramCheck + 256;
                            wkpastplay = Param;
                            continue;
                        }
                        // 基地局時刻補正設定
                        if (Param.toLowerCase().indexOf("timecorrection") !=  -1) {
                            sidx = Param.toLowerCase().indexOf("timecorrection=") + 15;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.toLowerCase().indexOf("none") != -1) {
                                TimeCorrection = TIME_CORRECTION_NONE;
                            } else if (Values.toLowerCase().indexOf("notice") != -1) {
                                TimeCorrection = TIME_CORRECTION_NOTICE;
                            } else if (Values.toLowerCase().indexOf("yes") != -1) {
                                TimeCorrection = TIME_CORRECTION_YES;
                            }
                            paramCheck = paramCheck + 512;
                            wktimecorrection = Param;
                            continue;
                        }
                        /* チャットメッセージの設定は既定値書き込みの対象外 */
                        if (Param.toLowerCase().indexOf("autoreceive") != -1) {
                            // チャットメッセージ自動受信設定
                            sidx = Param.toLowerCase().indexOf("autoreceive=") + 12;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.equalsIgnoreCase("yes")) {
                                chatAutoReceive = true;  // Chat Message Auto Receive 起動許可
                            } else {
                                chatAutoReceive = false; // Chat Message Auto Receive 起動不許可
                            }
                        }
                        /* この下にオプションチェックを追加したら
                           OPTION_CHECK_VALUE の値も修正すること。
                           次の paramCheckの値は 1024。
                           OPTION_CHECK_VALUE は 2048 - 1 にする。
                           また、下の既定値の書き込みも追加する。
                         */
                    }
                    if (!wkflag) {
                        SaveBuff = Param + "\n";
                        wkflag = true;
                    } else {
                        SaveBuff = SaveBuff + Param + "\n";
                    }
                }
                bufread.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        final int OPTION_CHECK_VALUE = 1024 - 1;

        if (paramCheck != OPTION_CHECK_VALUE) {
            try {
                envFile.delete();
                envFile.createNewFile();
            } catch (IOException e) {
                Log.i("nassua", "Configuration file not created.");
                return;
            }
            // ファイルに既定値を書き込む
            try {
                FileOutputStream fos = new FileOutputStream(envFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                PrintWriter bufwrite = new PrintWriter(osw);
                bufwrite.println("[RECMODE]");
                if ((paramCheck & 2) == 2) {
                    bufwrite.println(wkautoplay);
                } else {
                    bufwrite.println(sAutoPlay);
                }
                if ((paramCheck & 4) == 4) {
                    bufwrite.println(wkrectime);
                } else {
                    bufwrite.println(sMaxRecTime);
                }
                if ((paramCheck & 8) == 8) {
                    bufwrite.println(wkrecvol);
                } else {
                    bufwrite.println(sRecButton);
                }
                if (((paramCheck & 16) == 16)
                        && ((paramCheck & 32) == 32)) {
                    bufwrite.println(wkdiscardtype);
                    bufwrite.println(wkdiscardvalue);
                } else {
                    bufwrite.println(sDiscardType);
                    bufwrite.println(sDiscardValue);
                }
                if ((paramCheck & 64) == 64) {
                    bufwrite.println(wkaudioformat);
                } else {
                    bufwrite.println(sAudioFormat);
                }
                if ((paramCheck & 128) == 128) {
                    bufwrite.println(wkaudioencoder);
                } else {
                    bufwrite.println(sAudioEncoder);
                }
                if ((paramCheck & 256) == 256) {
                    bufwrite.println(wkpastplay);
                } else {
                    bufwrite.println(sPastPlay);
                }
                if ((paramCheck & 512) == 512) {
                    bufwrite.println(wktimecorrection);
                } else {
                    bufwrite.println(sTimeCorrection);
                }
                bufwrite.println(SaveBuff);  // その他のパラメータの書き込み
                bufwrite.close();
            } catch (Exception e) {
                Log.i("nassua", "Configration file write error.");
            }
        }
    }

    public boolean getPlayMode() {
        readConf();
        return AutoPlay;
    }

    public boolean getPastPlayMode() {
        readConf();
        return PastPlay;
    }

    public boolean getMsgRecvMode() {
        readConf();
        return chatAutoReceive;
    }

    // 端末諸元 読み取り
    //
    private void readConfNode() {
        confNode = new DbDefineBoat.ConfNode();
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        contentResolver = getContentResolver();
        ContentProviderClient resolver = contentResolver.acquireContentProviderClient(uri_tbl);
        if (resolver != null) try {
            Cursor cursor = resolver.query(uri_tbl, null, null, null, null);
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

    // パーミッションチェック
    private void checkPermissions() {
        int idx, reqflag;
        String[] sRequestPermission = new String[0];

        idx = 0;
        reqflag = 0x00;
        requestCnt = 0x00;
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestCnt++;
            reqflag = 0x01;
        } else {
            userProfilePermission.setReadProfileStatus(true);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestCnt++;
            reqflag = reqflag | 0x02;
        } else {
            userProfilePermission.setStorageWriteStatus(true);
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestCnt++;
            reqflag = reqflag | 0x04;
        } else {
            userProfilePermission.setInputMicStatus(true);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCnt++;
            reqflag = reqflag | 0x08;
        } else {
            userProfilePermission.setCameraStatus(true);
        }
        if (checkSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            requestCnt++;
            reqflag = reqflag | 0x10;
        } else {
            userProfilePermission.setVibrateStatus(true);
        }
        if (requestCnt > 0) {
            sRequestPermission = new String[requestCnt];
            if ((reqflag & 0x01) != 0) {
                sRequestPermission[idx] = Manifest.permission.READ_CONTACTS;
                idx++;
            }
            if ((reqflag & 0x02) != 0) {
                sRequestPermission[idx] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                idx++;
            }
            if ((reqflag & 0x04) != 0) {
                sRequestPermission[idx] = Manifest.permission.RECORD_AUDIO;
                idx++;
            }
            if ((reqflag & 0x08) != 0) {
                sRequestPermission[idx] = Manifest.permission.CAMERA;
                idx++;
            }
            if ((reqflag & 0x10) != 0) {
                sRequestPermission[idx] = Manifest.permission.VIBRATE;
            }
            requestPermissions(sRequestPermission, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResult) {
        boolean flag;
        int idx;

        // Android 5.0以前 OSでは実行しない。
        flag = userProfilePermission.getAndroid6Status();
        if (!flag) {
            return;
        }
        // パーミッション設定
        if (requestCode == PERMISSIONS_REQUEST) {
            for (idx = 0; idx < requestCnt; idx++) {
                if (permissions[idx].equalsIgnoreCase(Manifest.permission.READ_CONTACTS)) {
                    if (grantResult[idx] == PackageManager.PERMISSION_GRANTED) {
                        userProfilePermission.setReadProfileStatus(true);
                    } else {
                        userProfilePermission.setReadProfileStatus(false);
                    }
                }
                if (permissions[idx].equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult[idx] == PackageManager.PERMISSION_GRANTED) {
                        userProfilePermission.setStorageWriteStatus(true);
                    } else {
                        userProfilePermission.setStorageWriteStatus(false);
                    }
                }
                if (permissions[idx].equalsIgnoreCase(Manifest.permission.RECORD_AUDIO)) {
                    if (grantResult[idx] == PackageManager.PERMISSION_GRANTED) {
                        userProfilePermission.setInputMicStatus(true);
                    } else {
                        userProfilePermission.setInputMicStatus(false);
                    }
                }
                if (permissions[idx].equalsIgnoreCase(Manifest.permission.CAMERA)) {
                    if (grantResult[idx] == PackageManager.PERMISSION_GRANTED) {
                        userProfilePermission.setCameraStatus(true);
                    } else {
                        userProfilePermission.setCameraStatus(false);
                    }
                }
                if (permissions[idx].equalsIgnoreCase(Manifest.permission.VIBRATE)) {
                    if (grantResult[idx] == PackageManager.PERMISSION_GRANTED) {
                        userProfilePermission.setVibrateStatus(true);
                    } else {
                        userProfilePermission.setVibrateStatus(false);
                    }
                }
            }
        }
    }

    // BOAT一覧 受信
    //
    private void getBoatList() {
        Intent intent_bcast = new Intent(ConstantSong.ACT_GETLIST_REQUEST);
        intent_bcast.putExtra(ConstantSong.EXTRA_SONG, ConstantSong.SONG_BOATLIST);
        sendBroadcast(intent_bcast);
    }

    // オプションメニュー作成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 録音設定
        SubMenu recSunMenu = menu.addSubMenu(R.string.option_menu_rec);
        // 録音設定・メッセージ有効期限設定
        recSunMenu.add(menu.NONE, OPTION_SUBMENU_DISCARD, menu.NONE, R.string.option_submenu_time_discard);
        // 録音設定・録音時間設定
        recSunMenu.add(menu.NONE, OPTION_SUBMENU_RECMAX, menu.NONE, R.string.option_submenu_recmax);
        // 録音設定・音量ボタンによる録音
        recSunMenu.add(menu.NONE, OPTION_SUBMENU_RECBUTTON, menu.NONE, R.string.option_submenu_recbutton);
        // 録音設定・オーディオフォーマット選択
        recSunMenu.add(menu.NONE, OPTION_SUBMENU_AUDIOFORMAT, menu.NONE, R.string.option_submenu_audioformat);
        // 録音設定・オーディオエンコーダ選択
        recSunMenu.add(menu.NONE, OPTION_SUBMENU_AUDIOENCODER, menu.NONE, R.string.option_submenu_audioencoder);
        // 再生設定
        SubMenu playSubMenu = menu.addSubMenu(R.string.option_menu_play);
        // 再生設定・自動再生設定
        playSubMenu.add(menu.NONE, OPTION_SUBMENU_PLAYMODE, menu.NONE, R.string.option_submenu_autoplay);
        // 再生設定・起動前に録音されたメッセージの自動再生設定
        playSubMenu.add(menu.NONE, OPTION_SUBMENU_PASTPLAY, menu.NONE, R.string.option_submenu_pastplay);

        // 基地局設定
        SubMenu bsSubMenu = menu.addSubMenu(R.string.option_menu_bssetting);
        // 基地局時刻補正
        bsSubMenu.add(menu.NONE, OPTION_SUBMENU_TIME_CORRECTION, menu.NONE, R.string.option_submenu_time_correction);

        // データベース初期化
        //playSubMenu.add(menu.NONE, OPTION_SUBMENU_INITDB, menu.NONE, R.string.option_submenu_init_db);
        return true;
    }

    // オプションメニュー更新
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // 録音設定
        // 録音設定・メッセージ有効期限設定
        MenuItem msgdiacard = menu.findItem(OPTION_SUBMENU_DISCARD);
        // 録音設定・録音時間設定
        MenuItem recmax = menu.findItem(OPTION_SUBMENU_RECMAX);
        // 録音設定・音量ボタンによる録音
        MenuItem recbutton = menu.findItem(OPTION_SUBMENU_RECBUTTON);
        recbutton.setCheckable(true).setChecked(RecButton);
        // 録音設定・オーディオフォーマット
        MenuItem audioformat = menu.findItem(OPTION_SUBMENU_AUDIOFORMAT);
        // 録音設定・オーディオエンコーダ
        MenuItem audioencoder = menu.findItem(OPTION_SUBMENU_AUDIOENCODER);
        // 再生設定
        // 再生設定・自動再生設定
        MenuItem playmode = menu.findItem(OPTION_SUBMENU_PLAYMODE);
        playmode.setCheckable(true).setChecked(AutoPlay);
        // 再生設定・起動前に録音されたメッセージの自動再生設定
        MenuItem pastmode = menu.findItem(OPTION_SUBMENU_PASTPLAY);
        pastmode.setCheckable(true).setChecked(PastPlay);
        // 基地局設定
        // 基地局時刻補正
        MenuItem bstime = menu.findItem(OPTION_SUBMENU_TIME_CORRECTION);
        return true;
    }

    // サブメニューが選択された時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuCustomDialogFragment mcdf = new MenuCustomDialogFragment();
        boolean updateFlag = false;

        switch (item.getItemId()) {
            case OPTION_SUBMENU_DISCARD:
                mcdf.setShowType(OPTION_DISCARD_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_RECMAX:
                mcdf.setShowType(OPTION_RECTIME_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_RECBUTTON:
                RecButton = !RecButton;
                updateFlag = true;
                break;
            case OPTION_SUBMENU_AUDIOFORMAT:
                mcdf.setShowType(OPTION_AUDIOFORMAT_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_AUDIOENCODER:
                mcdf.setShowType(OPTION_AUDIOENCODER_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_PLAYMODE:
                AutoPlay = !AutoPlay;
                updateFlag = true;
                break;
            case OPTION_SUBMENU_PASTPLAY:
                PastPlay = !PastPlay;
                updateFlag = true;
                break;
            case OPTION_SUBMENU_TIME_CORRECTION:
                mcdf.setShowType(OPTION_BS_TIME_CORRECTION_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_INITDB:
                mcdf.setShowType(OPTION_INITDB_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        if (updateFlag) {
            // 環境ファイルへの書き込み。
            writeConf();
        }
        // 環境パラメータの再読み込み。
        readConf();
        return updateFlag;
    }

    private void writeConf(String... params) {
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
            String str;
            Toast toast = null;
            boolean toastOn = false;
            while ((Param = bufread.readLine()) != null) {
                str = Param;
                if (Param.toLowerCase().indexOf("autoplay") != -1) {
                    // 自動再生モード設定なら書きかえる
                    if (AutoPlay) {
                        str = "AutoPlay=yes";
                        toast = Toast.makeText(this, R.string.option_submenu_autoplay_true, Toast.LENGTH_LONG);
                    } else {
                        str = "AutoPlay=no";
                        toast = Toast.makeText(this, R.string.option_submenu_autoplay_false, Toast.LENGTH_LONG);
                    }
                    if (!(str.equalsIgnoreCase(Param))) {
                        toastOn = true;
                    }
                }
                if (Param.toLowerCase().indexOf("pastmessage") != -1) {
                    // 過去メッセージ再生設定なら書き換える。
                    if (PastPlay) {
                        str = "PastMessage=yes";
                        toast = Toast.makeText(this, R.string.option_submenu_pastplay_true, Toast.LENGTH_LONG);
                    } else {
                        str = "PastMessage=no";
                        toast = Toast.makeText(this, R.string.option_submenu_pastplay_false, Toast.LENGTH_LONG);
                    }
                    if (!(str.equalsIgnoreCase(Param))) {
                        toastOn = true;
                    }
                }
                if (Param.toLowerCase().indexOf("volumerec") != -1) {
                    // 録音ボタン設定なら書きかえる
                    if (RecButton) {
                        str = "VolumeRec=yes";
                        voiceMessageSubFunctions.LockVolumeKey();
                        toast = Toast.makeText(this, R.string.option_submenu_recbutton_true, Toast.LENGTH_LONG);
                    } else {
                        str = "VolumeRec=no";
                        voiceMessageSubFunctions.UnLockVolumeKey();
                        toast = Toast.makeText(this, R.string.option_submenu_recbutton_false, Toast.LENGTH_LONG);
                    }
                    if (!(str.equalsIgnoreCase(Param))) {
                        toastOn = true;
                    }
                }
                if (Param.toLowerCase().indexOf("discardtype") != -1) {
                    // レコード有効期限単位
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("discardtype") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (Param.toLowerCase().indexOf("discardvalue") != -1) {
                    // レコード有効期限値
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("discardvalue") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (Param.toLowerCase().indexOf("audioformat") != -1) {
                    // オーディオフフォーマット
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("audioformat") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (Param.toLowerCase().indexOf("audioencoder") != -1) {
                    // オーディオエンコーダ
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("audioencoder") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (Param.toLowerCase().indexOf("maxrectime") != -1) {
                    // 録音時間
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("maxrectime") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (Param.toLowerCase().indexOf("timecorrection") != -1){
                    // 時刻補正設定
                    if (params.length > 0) {
                        if (params[0].toLowerCase().indexOf("timecorrection") != -1) {
                            str = params[0];
                        }
                    }
                }
                if (toastOn) {
                    toastOn = false;
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                    toast.show();
                }
                bufwrite.println(str);
            }
            envFile.delete();
            workFile.renameTo(envFile);
            bufread.close();
            bufwrite.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bootStart(Context context) {
        Log.i("nassua", "bootStart.");
        //Context context = getBaseContext();
        // Android 6.0以上はパーミッションをチェックする。
        userProfilePermission = new UserProfilePermission();
        userProfilePermission.setReadProfileStatus(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            // パーミッションチェック。　許可が無いときは起動しない。
            Log.i("nassua", "bootStart:Android 6.0 permission check.");
            Log.i("nassua", "bootStart:Profile permission check.");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                return;
            Log.i("nassua", "bootStart:Storage permission check.");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                return;
            Log.i("nassua", "bootStart:MIC permission check.");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                return;
        }
        userProfilePermission.setReadProfileStatus(true);
        Log.i("nassua", "bootStart:Permission status ok.");
        // テーブルの有無をチェックしてテーブルがなければ作る。

        // PrefLog pref_log = new PrefLog();
        // pref_log.readPreference( context);
        // Moorプロトコル設定
        Log.i("nassua", "bootStart:readPreference");
        PrefApp pref = new PrefApp();
        pref.readPreference(context);
        if (pref.useBlob) {
            // 音声データの出力先を FILEにする。(BLOBは使用しない)
            pref.useBlob = false;
            Log.i("nassua", "bootStart:savePreference");
            pref.savePreference(context);
        }

        vmc = new VoiceMessageCommon();
        // 通話機能を開始
        Log.i("nassua", "bootStart:Phone, Boat start");
        vmc.startPhoneNotify(context);
        rec = BoatApi.readStatPhone(context);
        // 基地局間データ同期の開始
        Log.i("nassua", "bootStart:Phone, Boat start");
        vmc.startSongAlive(context);
        //諸元
        conf_node = BoatApi.readConfNode(context);
        conf_phone = BoatApi.readConfPhone(context);
        // 設定読み取り
        Log.i("nassua", "bootStart:BoxVoice request");
        readConfNode(context);
        BoxVoice.idMyself = confNode.mIdBoat;
        BoxVoice.uriMyself = confNode.mUriBoat;
        BoxVoice.idBox = VoiceBoxName.getBytes();
        voiceMessageSubFunctions = new VoiceMessageSubFunctions();
        voiceMessageSubFunctions.setPhoneNumber(confNode.mPhoneNumber);

        //  端末間共有を開始
        Log.i("nassua", "bootStart:Share start");
        vmc.startShareAlive(context);
        // メッセージ自動再生起動
        if (getPlayMode()) {
            Log.i("nassua", "bootStart:VoiceMessage auto play mode.");
            if ((PlayMessageService.actMyself == null) || (!pmc.getPlayMessageServiceStatus())) {
                Log.i("nassua", "bootStart:PlayMessageService start. ");
                Intent intent3 = new Intent(context, PlayMessageService.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0以降
                    context.startForegroundService(intent3);
                } else {
                    // Android 7.1.1以前
                    context.startService(intent3);
                }
            }
        }
        // チャットメッセージ自動受信起動
        if (getMsgRecvMode()) {
            Log.i("nassua", "bootStart:Chat Message Auto Receive Mode.");
            if (ChatMessageService.actMyself == null) {
                Log.i("nassua", "bootStart:ChatMessageService start.");
                Intent intent4 = new Intent(context, ChatMessageService.class);
                intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0以降
                    context.startForegroundService(intent4);
                } else {
                    // Android 7.1.1以前
                    context.startService(intent4);
                }
            }
        }
    }

    private void readConfNode(Context context) {
        confNode = new DbDefineBoat.ConfNode();
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        contentResolver = context.getContentResolver();
        ContentProviderClient resolver = contentResolver.acquireContentProviderClient(uri_tbl);
        if (resolver != null) try {
            Cursor cursor = resolver.query(uri_tbl, null, null, null, null);
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // 基地局の時刻取得要求
    public void RequestBaseStationDate() {
        if (TimeCorrection != TIME_CORRECTION_NONE) {
            if ((!getBsGetTimeFlag() && !getBsSetTimeFlag())) {  // 時刻取得、時刻設定未実行
                Context context = actMyself;
                Intent intentact = new Intent(ConstantBoat.ACT_MOOR_REQUEST);
                intentact.putExtra(ConstantBoat.EXTRA_EVENT, ConstantBoat.EVENT_GETTIME);
                intentact.setComponent(ConstantBoat.getComponent());
                context.sendBroadcast(intentact);
                setBsGetTimeFlag();  // 基地局時刻要求中
            }
        }
    }

    // 基地局への時刻設定要求
    public void SetRequestBaseStationDate() {
        long nowDate = System.currentTimeMillis();
        if ((!getBsGetTimeFlag() && !getBsSetTimeFlag())) {  // 時刻取得、時刻設定未実行
            Context context = actMyself;
            Intent intentact = new Intent(ConstantBoat.ACT_MOOR_REQUEST);
            intentact.putExtra(ConstantBoat.EXTRA_EVENT, ConstantBoat.EVENT_SETTIME);
            intentact.putExtra(ConstantBoat.EXTRA_TIME_TSG, nowDate);
            intentact.setComponent( ConstantBoat.getComponent());
            context.sendBroadcast(intentact);
            setBsSetTimeFlag();  // 基地局時刻設定中
        }
    }

    // 基地局時刻要求中
    public void setBsGetTimeFlag() {
        BsGetTimeFlag = true;
    }

    // 基地局時刻要求解除
    public void resetBsGetTimeFlag() {
        BsGetTimeFlag = false;
    }

    // // 基地局時刻要求状態取得
    public boolean getBsGetTimeFlag() {
        return BsGetTimeFlag;
    }

    // 基地局時刻設定中
    public void setBsSetTimeFlag() {
        BsSetTimeFlag = true;
    }

    // 基地局時刻設定中解除
    public void resetBsSetTimeFlag() {
        BsSetTimeFlag = false;
    }

    // 基地局時刻要求状態取得
    public boolean getBsSetTimeFlag() {
        return BsSetTimeFlag;
    }

    public void saveBaseStationDate(long bsNowDate) {
        bsDate = bsNowDate;
    }

    public long getBaseStationDate() {
        return bsDate;
    }

    public static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static boolean alertFlag = false;
    public void alertNowDate() {
        if (TimeCorrection == TIME_CORRECTION_NONE) {
            // 通知、補正無し。
            return;
        }
        if (!alertFlag) {
            Context context = actMyself;
            inflater = (LayoutInflater) LayoutInflater.from(context);
            //View alertview = (View) inflater.inflate(R.layout.alert_date_dialog, (ViewGroup) findViewById(R.id.alert_date_dialog));
            View alertview = (View) inflater.inflate(R.layout.alert_date_dialog, null);
            TextView BsDate = (TextView) alertview.findViewById(R.id.bsdate);
            TextView TerminalDate = (TextView) alertview.findViewById(R.id.terminaldate);

            final long bsDate, nowDate;
            bsDate = getBaseStationDate();
            nowDate = System.currentTimeMillis();
            String BsDateStr = new SimpleDateFormat(DATE_PATTERN).format(bsDate);
            String nowDateStr = new SimpleDateFormat(DATE_PATTERN).format(nowDate);
            BsDate.setText(BsDateStr);
            TerminalDate.setText(nowDateStr);
            TextView AlertMessage = (TextView) alertview.findViewById(R.id.alert_date_message);
            if (TimeCorrection == TIME_CORRECTION_NOTICE) {
                AlertMessage.setText(R.string.alert_date_dialog_message2);
            } else {
                AlertMessage.setText(R.string.alert_date_dialog_message1);
            }
            // 文字サイズ変更
            /*
            TextView BsDataTitle = (TextView) view.findViewById(R.id.bsdatetext);
            TextView TerminalDataTitle = (TextView) view.findViewById(R.id.terminaltext);
            BsDataTitle.setTextSize(warehouseCommon.getTextSize(24));
            BsDate.setTextSize(warehouseCommon.getTextSize(24));
            TerminalDataTitle.setTextSize(warehouseCommon.getTextSize(24));
            TerminalDate.setTextSize(warehouseCommon.getTextSize(24));
            */
            alertDialog = new AlertDialog.Builder(actMyself);
            String title = context.getResources().getString(R.string.alert_date_dialog_title);
            alertDialog.setTitle(title);
            alertDialog.setView(alertview);
            if (TimeCorrection == TIME_CORRECTION_NOTICE) {
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // 表示のみ時刻補正は行わない。
                        alertFlag = false;
                    }
                });
            } else if (TimeCorrection == TIME_CORRECTION_YES) {
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // 基地局へ時刻補正要求
                        SetRequestBaseStationDate();
                        alertFlag = false;
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    // なにもしない。
                    alertFlag = false;
                }
                });
            }
            // 表示
            alertDialog.create();
            alertHandle.post(new Runnable() {
                @Override
                public void run() {
                    alertDialog.show();
                }
            });
            alertFlag = true;
        }

    }

    public static class MenuCustomDialogFragment extends DialogFragment {
        private String sDiscardValue;
        private static long iDiscardValue;
        private String sRecTime;
        private static int iRecTime;
        private static int showType;
        private static long discardtm;
        private static String discardtype;
        private static String audioformattype;
        private static String audioencodertype;
        private static VoiceMessageCommon vmCommon;
        private static int audiofmt;
        private static int audioenc;
        private static String timecorrection;

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            vmCommon = new VoiceMessageCommon();
        }

        @Override
        public Dialog onCreateDialog(Bundle saveInstanceState) {
            int st;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            discardtm = 0;
            st = getShowType();
            switch (st) {
                case OPTION_DISCARD_DIALOG:
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    final View inputdisacrd = inflater.inflate(R.layout.input_discard_dialog, null);
                    final EditText value = (EditText) (inputdisacrd.findViewById(R.id.input_discard));
                    final RadioGroup radioGroup = (RadioGroup) inputdisacrd.findViewById(R.id.radiogroup);
                    builder.setTitle(getString(R.string.option_submenu_time_discard));
                    builder.setView(inputdisacrd);
                    value.setText(String.valueOf(ActMain.actMyself.MessageLife));
                    switch (DiscardType) {
                        case DISCARD_TYPE_DAYS:
                            radioGroup.check(R.id.discard_day);
                            discardtm = 24 * 3600;
                            break;
                        case DISCARD_TYPE_HOURS:
                            radioGroup.check(R.id.discard_hour);
                            discardtm = 3600;
                            break;
                        case DISCARD_TYPE_MINUTES:
                            radioGroup.check(R.id.discard_minute);
                            discardtm = 60;
                            break;
                    }
                    value.setText(String.valueOf(ActMain.actMyself.MessageLife / discardtm));
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String discardParam = "DiscardType=unknown";
                            sDiscardValue = value.getText().toString();
                            int id = radioGroup.getCheckedRadioButtonId();
                            RadioButton radioButton = (RadioButton) inputdisacrd.findViewById(id);
                            discardtype = radioButton.getText().toString();
                            if (discardtype.equalsIgnoreCase(getString(R.string.option_submenu_discard_day))) {
                                discardtm = 24 * 3600;
                                DiscardType = DISCARD_TYPE_DAYS;
                                discardParam = "DiscardType=days";
                            } else if (discardtype.equalsIgnoreCase(getString(R.string.option_submenu_discard_hour))) {
                                discardtm = 3600;
                                DiscardType = DISCARD_TYPE_HOURS;
                                discardParam = "DiscardType=hours";
                            } else if (discardtype.equalsIgnoreCase(getString(R.string.option_submenu_discard_minute))) {
                                discardtm = 60;
                                DiscardType = DISCARD_TYPE_MINUTES;
                                discardParam = "DiscardType=minutes";
                            }
                            iDiscardValue = Long.parseLong(sDiscardValue);
                            // 10分未満は不可
                            if ((DiscardType == DISCARD_TYPE_MINUTES)
                                    && (iDiscardValue < 10)) {
                                iDiscardValue = 10;
                            }
                            ActMain.actMyself.writeConf(discardParam);
                            ActMain.actMyself.writeConf("DiscardValue=" + String.valueOf(iDiscardValue));
                            ActMain.actMyself.MessageLife = iDiscardValue * discardtm;
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();
                case OPTION_RECTIME_DIALOG:
                    LayoutInflater inflater1 = LayoutInflater.from(getActivity());
                    final View inputdisacrd1 = inflater1.inflate(R.layout.input_rectime_dialog, null);
                    final EditText value1 = (EditText) (inputdisacrd1.findViewById(R.id.input_rectime));
                    builder.setTitle(getString(R.string.option_submenu_recmax));
                    builder.setView(inputdisacrd1);
                    value1.setText(String.valueOf(ActMain.actMyself.MaxRecTime));
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sRecTime = value1.getText().toString();
                            iRecTime = Integer.parseInt(sRecTime);
                            // 10秒未満、31秒以上は不可
                            if (iRecTime < 10) {
                                iRecTime = 10;
                            } else if (iRecTime > 30) {
                                iRecTime = 30;
                            }
                            ActMain.actMyself.writeConf("MaxRecTime=" + String.valueOf(iRecTime));
                            ActMain.actMyself.MaxRecTime = iRecTime;
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();
                case OPTION_AUDIOFORMAT_DIALOG:
                    audiofmt = vmCommon.getAudioFormat();
                    LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                    final View inputdisacrd2 = inflater2.inflate(R.layout.input_audioformat_dialog, null);
                    final RadioGroup audiofromatGroup = (RadioGroup) inputdisacrd2.findViewById(R.id.audioformatradiogroup);
                    builder.setTitle(getString(R.string.option_submenu_audioformat));
                    builder.setView(inputdisacrd2);
                    if (audiofmt == vmCommon.AUDIO_FORMAT_PCM) {
                        audiofromatGroup.check(R.id.audioformat_pcm);
                    } else if (audiofmt == vmCommon.AUDIO_FORMAT_3GP) {
                        audiofromatGroup.check(R.id.audioformat_3gp);
                    }
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String AudioFormatParam = "AudioFormat=unknown";
                            int id = audiofromatGroup.getCheckedRadioButtonId();
                            RadioButton radioButton2 = (RadioButton) inputdisacrd2.findViewById(id);
                            audioformattype = radioButton2.getText().toString();
                            if (audioformattype.equalsIgnoreCase(getString(R.string.option_submenu_audioformat_pcm))) {
                                audiofmt = vmCommon.AUDIO_FORMAT_PCM;
                                AudioFormatParam = "AudioFormat=PCM";
                            } else if (audioformattype.equalsIgnoreCase(getString(R.string.option_submenu_audioformat_3gp))) {
                                audiofmt = vmCommon.AUDIO_FORMAT_3GP;
                                AudioFormatParam = "AudioFormat=3GP";
                            }
                            ActMain.actMyself.writeConf(AudioFormatParam);
                            vmCommon.setAudioFormat(audiofmt);
                         }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_AUDIOENCODER_DIALOG:
                    audioenc = vmCommon.getAudioEncoder();
                    LayoutInflater inflater3 = LayoutInflater.from(getActivity());
                    final View inputdisacrd3 = inflater3.inflate(R.layout.input_audioencoder_dialog, null);
                    final RadioGroup audioencodertGroup = (RadioGroup) inputdisacrd3.findViewById(R.id.audioencoderdiogroup);
                    builder.setTitle(getString(R.string.option_submenu_audioformat));
                    builder.setView(inputdisacrd3);
                    if (audioenc == vmCommon.AUDIO_ENCODER_DEFAULT) {
                        audioencodertGroup.check(R.id.audioencoder_default);
                    } else if (audioenc == vmCommon.AUDIO_ENCODER_AAC) {
                        audioencodertGroup.check(R.id.audioencoder_aac);
                    } else if (audioenc == vmCommon.AUDIO_ENCODER_HE_AAC) {
                        audioencodertGroup.check(R.id.audioencoder_he_aac);
                    } else if (audioenc == vmCommon.AUDIO_ENCODER_AAC_ELD) {
                        audioencodertGroup.check(R.id.audioencoder_aac_eld);
                    } else if (audioenc == vmCommon.AUDIO_ENCODER_AMR_NB) {
                        audioencodertGroup.check(R.id.audioencoder_amr_nb);
                    } else if (audioenc == vmCommon.AUDIO_ENCODER_AMR_WB) {
                        audioencodertGroup.check(R.id.audioencoder_amr_wb);
                    }
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String AudioEcoderParam = "AudioEncoder=unknown";
                            int id = audioencodertGroup.getCheckedRadioButtonId();
                            RadioButton radioButton3 = (RadioButton) inputdisacrd3.findViewById(id);
                            audioencodertype = radioButton3.getText().toString();
                            if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_default))) {
                                audioenc = vmCommon.AUDIO_ENCODER_DEFAULT;
                                AudioEcoderParam = "AudioEncoder=DEFAULT";
                            } else if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_aac))) {
                                audioenc = vmCommon.AUDIO_ENCODER_AAC;
                                AudioEcoderParam = "AudioEncoder=AAC";
                            } else if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_he_aac))) {
                                audioenc = vmCommon.AUDIO_ENCODER_HE_AAC;
                                AudioEcoderParam = "AudioEncoder=HE_AAC";
                            } else if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_aac_eld))) {
                                audioenc = vmCommon.AUDIO_ENCODER_AAC_ELD;
                                AudioEcoderParam = "AudioEncoder=AAC_ELD";
                            } else if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_amr_nb))) {
                                audioenc = vmCommon.AUDIO_ENCODER_AMR_NB;
                                AudioEcoderParam = "AudioEncoder=AMR_NB";
                            } else if (audioencodertype.equalsIgnoreCase(getString(R.string.option_submenu_audioencoder_amr_wb))) {
                                audioenc = vmCommon.AUDIO_ENCODER_AMR_WB;
                                AudioEcoderParam = "AudioEncoder=AMR_WB";
                            }
                            ActMain.actMyself.writeConf(AudioEcoderParam);
                            vmc.setAudioEncoder(audioenc);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_BS_TIME_CORRECTION_DIALOG:
                    LayoutInflater inflater4 = LayoutInflater.from(getActivity());
                    final View inputdisacrd4 = inflater4.inflate(R.layout.input_time_correction_dialog, null);
                    final RadioGroup timecorrectionGroup = (RadioGroup) inputdisacrd4.findViewById(R.id.timecorrectiondiogroup);
                    builder.setTitle(getString(R.string.option_submenu_time_correction));
                    builder.setView(inputdisacrd4);
                    if (TimeCorrection == TIME_CORRECTION_NONE) {
                        timecorrectionGroup.check(R.id.timecorrection_none);
                    } else if (TimeCorrection == TIME_CORRECTION_YES) {
                        timecorrectionGroup.check(R.id.timecorrection_yes);
                    } else {
                        timecorrectionGroup.check(R.id.timecorrection_notice);
                    }
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = timecorrectionGroup.getCheckedRadioButtonId();
                            RadioButton radioButton4 = (RadioButton) inputdisacrd4.findViewById(id);
                            String correctType = radioButton4.getText().toString();
                            if (correctType.equalsIgnoreCase(getResources().getString(R.string.bssetting_time_correction_none))) {
                                TimeCorrection = TIME_CORRECTION_NONE;
                                timecorrection = "TimeCorrection=none";
                            } else if (correctType.equalsIgnoreCase(getResources().getString(R.string.bssetting_time_correction_yes))) {
                                TimeCorrection = TIME_CORRECTION_YES;
                                timecorrection = "TimeCorrection=Yes";
                            } else {
                                TimeCorrection = TIME_CORRECTION_NOTICE;
                                timecorrection = "TimeCorrection=Notice";
                            }
                            ActMain.actMyself.writeConf(timecorrection);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();
                case OPTION_INITDB_DIALOG:
                    LayoutInflater inflater5 = LayoutInflater.from(getActivity());
                    final View inputdisacrd5 = inflater5.inflate(R.layout.init_db_dialog, null);
                    builder.setTitle(getString(R.string.option_submenu_init_db));
                    builder.setView(inputdisacrd5);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // データベース初期化処理
                            try {
                                ActMain.actMyself.InitDB();
                            } catch (RemoteException e) {
                                Log.i("nassua", "DB init failed.");
                            }
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();

            }
            return null;
        }

        public void setShowType(int type) {
            showType = type;
        }

        private int getShowType() {
            return showType;
        }

    }
}
