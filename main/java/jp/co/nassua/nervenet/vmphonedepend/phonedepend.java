package jp.co.nassua.nervenet.vmphonedepend;

// NerveNet電話　発呼メイン処理
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.co.nassua.nervenet.Dialing.dialing;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.PrefApp;
import jp.co.nassua.nervenet.voicemessage.R;
//import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.phone.ConstantPhone;
//import jp.co.nassua.nervenet.phone.DbDefinePhone;
import jp.co.nassua.nervenet.song.ConstantSong;
import jp.co.nassua.nervenet.song.DbDefineSong;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class phonedepend extends AppCompatActivity {
    public Handler handPhone = null;      //外部操作イベント受信ハンドラー
    private Intent intent;
    public static phonedepend actMyself = null;  //プロセス上の単一インスタンス

    public PrefApp pref;           //アプリケーション設定
    private RcvNotify rcvNotify;    //通知待ち受け
    private Handler handNotify = null;      //NerveNet端末機能 通知受信ハンドラー
    private Button btnCall, btnBye, btnBoatList;//ボタン
    private TextView tvMsg;         //テキスト表示領域
    private TextView tvVersion;     //バージョン表示
    private ListView listView;

    public String boatMaine;
    public String uriMine;     //自局SIP-URI
    public String callnum;
    public boolean useSubscribe;    //通知予約の利用有無
    public boolean usePhonenumber;    //通知予約の利用有無

    private short boatPhase;  //端末状態
    public static String phonePhase; //通話状態
    //端末状態
    private static final short BoatPhase_INIT = 0; //初期状態
    private static final short BoatPhase_WATCH = 1; //基地局 探索中
    private static final short BoatPhase_DISCOVER = 2; //基地局 捕捉完了
    //定数
    private static final String logLabel = "PhoneDepend";
    private static final int WHAT_TSG = 1;
    private static final int WHAT_PHONE = 2;

    //DB操作
    private ContentProviderClient contClient;
    private Cursor dbCursor;
    ArrayAdapter<String> adapter;
    UserProfilePermission userProfilePermission;

    private List<String> AddressList = new ArrayList<>();

    public static CallUri callUri;
    ContentResolver mCResolver = null;

     public phonedepend() {
        uriMine = null;
        contClient = null;
        dbCursor = null;
        usePhonenumber = false;

        boatPhase = BoatPhase_INIT;
        phonePhase = ConstantPhone.PHASE_INIT;
        // ユーザプロファイルアクセス権限
        userProfilePermission = new UserProfilePermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonedepend);
        //startSongAlive();
     }

    protected void onResume() {
        // タイトル設定
        setTitle(getString(R.string.phone_title) + " " + ActMain.appversion);
        //自身のインスタンスを公開
        actMyself = this;

        if ((phonePhase.equals(ConstantPhone.PHASE_INIT))
                || (phonePhase.equals(ConstantPhone.PHASE_IDLE))) {
            // 設定読み取り
            //pref = new PrefApp();
            //pref.readPreference(this);
            //諸元
            boatMaine = ActMain.actMyself.conf_node.mUriBoat;
            uriMine = ActMain.actMyself.conf_phone.mUriMine;
            // BOAT一覧作成
            setContentView(R.layout.activity_phonedepend);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            createBoatList();
        }
        super.onResume();
    }

    protected void onDestroy() {
        //
        actMyself = null;
        //通知用ハンドラーの後始末
        //stopSongAlive();
        super.onDestroy();
    }

    /* Commonへ移動
    // NerveNet基地局間同期データの生存確認 開始
    //
    private void startSongAlive() {
        if (rcvNotify == null) {
            rcvNotify = new RcvNotify();
            //ブロードキャストレシーバー登録
            IntentFilter filter = new IntentFilter();
            filter.addAction( ConstantSong.ACT_ALIVE );
            registerReceiver( rcvNotify, filter );
        }
    }
    // NerveNet基地局間同期データの生存確認 停止
    //
    private void stopSongAlive() {
        if (rcvNotify != null) {
            //ブロードキャストレシーバー登録解除
            unregisterReceiver( rcvNotify );
            rcvNotify = null;
        }
    }
    */

    // BOAT一覧作成
    private void createBoatList() {
        int curidx, curcnt, curidx2;
        String fieldname = "sipuri";
        String fieldname2 = "phonenumber";
        String strd, strd2;
        String phonenum;
        Cursor addrcur = null;
        boolean readflag, osflag;

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
        curidx = dbCursor.getColumnIndex(fieldname);
        curcnt = dbCursor.getCount();
        curidx2 = dbCursor.getColumnIndex(fieldname2);
        dbCursor.moveToFirst();
        // BOATをリストに追加する。
        listView = (ListView) findViewById(R.id.boatList);
        try {
            for (int i = 0; i < curcnt; i++) {
                CharArrayBuffer boatList = new CharArrayBuffer(256);
                CharArrayBuffer boatList2 = new CharArrayBuffer(256);
                dbCursor.copyStringToBuffer(curidx, boatList);
                dbCursor.copyStringToBuffer(curidx2, boatList2);
                strd = String.valueOf(boatList.data).trim();
                strd2 = String.valueOf(boatList2.data).trim();
                // 自分の番号は表示しない。
                if (!strd.equals(boatMaine)) {
                    // 表示優先順位：名前 > 電話番号 > SIP-URL
                    // OSが Android 5.1以前か連絡先にアクセス許可がある場合のみ電話番号検索を実行。
                    if (!strd2.equals("") && (readflag) && (addrcur != null)) {
                        if (addrcur.moveToFirst()) {
                            do {
                                // 登録されている電話番号から "-" を取り除く。
                                phonenum = addrcur.getString(addrcur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("-", "");
                                // 電話番号が一致するかチェックする。
                                if (strd2.equals(phonenum)) {
                                    // 電話番号が一致したら名前を取得して抜ける。
                                    strd2 = addrcur.getString(addrcur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                    break;
                                }
                            } while (addrcur.moveToNext());
                        }
                        adapter.add(strd2);
                    } else {
                        adapter.add(strd);
                    }
                    // 接続先リストに追加
                    AddressList.add(strd);
                }
                dbCursor.moveToNext();
            }
            // 接続先リストを表示
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((readflag) && (addrcur != null)) {
            addrcur.close();
        }

        // コールバックリスナー登録
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] uri_items;
                callUri = new CallUri();

                ListView listView1 = (ListView) parent;
                callUri.setDisplayDialingNumber((String)listView1.getItemAtPosition(position));  // 表示用番号
                callnum = AddressList.get(position);                    // 実際に使う番号
                uri_items = uriMine.split("@");
                callUri.setCallSipUri(uri_items[0] + "@" + callnum);

                // 接続先の情報を取得する
                if (dbCursor.moveToPosition(position + 1)) {
                    DbDefineSong.BoatList mBoat = new DbDefineSong.BoatList();
                    mBoat.setFromQuery(dbCursor);
                    // 発呼処理の呼び出し
                    intent = new Intent(phonedepend.this, dialing.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * カーソル生成
     *
     * @return
     */
    private Cursor getCursor() {
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

    @Override
    public void onStart() {
        super.onStart();
     }

    @Override
    public void onStop() {
        super.onStop();
     }
}