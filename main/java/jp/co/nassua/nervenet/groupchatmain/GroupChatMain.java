package jp.co.nassua.nervenet.groupchatmain;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

// QR Code用
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class GroupChatMain extends AppCompatActivity {

    static ActMain actMain = ActMain.actMyself;
    static VoiceMessageCommon voiceMessageCommon;
    private static DbDefineBoat.ConfNode confNode;
    private static ContentResolver contentResolver;
    private static ChatCommon chatCommon;
    private static GroupCommon groupCommon;
    private static CursorLoaderBoxMember cursorLoaderBoxMember;
    private static ContentChat contentChat;
    static android.support.v4.app.FragmentManager fm;

    public static final int OPTION_SUBMENU_CREATE_GROUP = Menu.FIRST;
    public static final int OPTION_SUBMENU_DELETE_GROUP = OPTION_SUBMENU_CREATE_GROUP + 1;
    public static final int OPTION_SUBMENU_REG_MYNAME = OPTION_SUBMENU_DELETE_GROUP + 1;
    public static final int OPTION_SUBMENUE_MSGCOUNT = OPTION_SUBMENU_REG_MYNAME + 1;
    public static final int OPTION_SUBMENUE_QUALITY = OPTION_SUBMENUE_MSGCOUNT + 1;
    public static final int OPTION_SUBMENUE_SENDSIZE = OPTION_SUBMENUE_QUALITY + 1;
    public static final int OPTION_SUBMENUE_SENDRATIO = OPTION_SUBMENUE_SENDSIZE + 1;
    public static final int OPTION_SUBMENUE_DISPLAYRATIO = OPTION_SUBMENUE_SENDRATIO + 1;
    public static final int OPTION_SUBMENUE_CHAT_AUTO_RECEIVE = OPTION_SUBMENUE_DISPLAYRATIO + 1;
    public static final int OPTION_SUBMENUE_CHAT_TEST_MODE = OPTION_SUBMENUE_CHAT_AUTO_RECEIVE + 1;
    public static final int OPTION_SUBMENUE_CREATE_QRCODE = OPTION_SUBMENUE_CHAT_TEST_MODE + 1;

    public static final int OPTION_CREATE_CHAT_GROUP_DIALOG = 0;
    public static final int OPTION_DELETE_CHAT_GROUP_DIALOG = OPTION_CREATE_CHAT_GROUP_DIALOG + 1;
    public static final int OPTION_REG_CHAT_MYNAME_DIALOG = OPTION_DELETE_CHAT_GROUP_DIALOG + 1;
    public static final int OPTION_MESSAGE_COUNT_DIALOG = OPTION_REG_CHAT_MYNAME_DIALOG + 1;
    public static final int OPTION_QUALITY_DIALOG = OPTION_MESSAGE_COUNT_DIALOG + 1;
    public static final int OPTION_SENDSIZE_DIALOG = OPTION_QUALITY_DIALOG + 1;
    public static final int OPTION_SENDRATIO_DIALOG = OPTION_SENDSIZE_DIALOG + 1;
    public static final int OPTION_DISPLAYRATIO_DIALOG = OPTION_SENDRATIO_DIALOG + 1;
    public static final int OPTION_CHAT_AUTORECEIVE_DIALOG = OPTION_DISPLAYRATIO_DIALOG + 1;
    public static final int OPTION_CREATE_QRCODE_DIALOG = OPTION_CHAT_AUTORECEIVE_DIALOG + 1;

    private static boolean refreshFlag;
    private static Context GCMcontext;

    // Debug用
    public static final long BoxLimitTime   = 60 * 10 * 1000;  // ボックスの有効時間 10分

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_tab);

        GrouplistAdapter terminallistAdapter = new GrouplistAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.terminallistPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(terminallistAdapter);

        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }

        fm = getSupportFragmentManager();
        GCMcontext = this;


        TabLayout tabLayout = findViewById(R.id.grouptabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (voiceMessageCommon == null) {
                    voiceMessageCommon = new VoiceMessageCommon();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // 環境ファイル読み込み
        groupCommon.readConf();
        // BoadId読み込み
        groupCommon.readBoatId(this);
        // boxmemberを作る。
        //groupCommon.makeTableBoxmember();

        // ブロードキャストレシーバー登録 (Boxmember用)
        BroadcastReceiver broadcastReceiver = new GroupMemberNotify();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstantGroup.ACT_NOTIFY);
        filter.addAction(ConstantGroup.ACT_REQUEST);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);

        // Box Member用
        if (cursorLoaderBoxMember == null) {
            cursorLoaderBoxMember = new CursorLoaderBoxMember();
            cursorLoaderBoxMember.createLoader(this);
        }

        refreshFlag = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // タイトル設定
        setTitle(getString(R.string.group_chat_title) + " " + ActMain.appversion);
        if (!groupCommon.readConf()) {
            // チャット名を登録を促すダイアログを表示する。
            groupCommon.alertMessage(this, contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
        }
        if (chatCommon.getAutoReceive()) {
            // チャットメッセージ自動受信が許可されている。
            voiceMessageCommon.startChatMessageService(this);
        }
        // グループを作成
        createNerveNetGroup();
        if (refreshFlag) {
            Context context = this;
            Intent intent = new Intent();
            intent.setAction(ConstantGroup.ACT_REQUEST);
            intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_TERMINALLIST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        refreshFlag = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursorLoaderBoxMember != null) {
            cursorLoaderBoxMember.destroyLoader();
        }
    }


    public void addSuccessMessage(String type) {
        String alertMessage = null;
        final Context context = GCMcontext;
        final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        final String alertType = type;

        String wkmsg = context.getResources().getString(R.string.alert_dialog_add_group_name);
        alertDialog.setTitle(wkmsg + ": " + groupCommon.getNewGroupName());
        if (alertType.equals(ConstantGroup.EVENT_GROUP_JOINED)) {
            alertMessage =context.getResources().getString(R.string.alert_dialog_group_joined);
        } else if (alertType.equals(ConstantGroup.EVENT_GROUPNAME_ALREADY)) {
            alertMessage =context.getResources().getString(R.string.alert_dialog_groupname_already);
        } else {
            alertMessage = context.getResources().getString(R.string.alert_dialog_create_success);
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alertType.equals(ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST)) {
                    refreshGrouplist();
                }
            }
        });
        alertDialog.create();
        alertDialog.show();

    }

    public void refreshGrouplist() {
        GroupChatMainFragment groupChatMainFragment = new GroupChatMainFragment();
        groupChatMainFragment.RefreshView();
        /*
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        fragment = new GroupChatMainFragment();
        ft.replace(R.id.grouptabfragment, fragment);
        ft.commit();
        */
    }

    // 処理用のグループリスト作成
    private boolean createNerveNetGroup() {
        Uri uri = null;

        boolean bret = false;
        int tlistcnt = 0;
        int glistcnt = 0;
        // XMLを読み込んで端末リストを作成する。
        tlistcnt = groupCommon.ParseTerminalList();
        // XMLを読み込んでグループリストを作成する。
        glistcnt = groupCommon.ParseGroupList();

        if ((tlistcnt > 0) && (glistcnt > 0)) {
            bret = true;
        }
        return bret;

        /* 当面、BoxMasterは使用しない */
        /*
        readConfNode();
        GroupBoxMaster.idMyself = confNode.mIdBoat;
        GroupBoxMaster.uriMyself = confNode.mUriBoat;
        GroupBoxMaster.idBox = groupCommon.getDefaultGroupName().getBytes();
        // Boxmasterに DEFALUT_GROUP_NAME が登録されているかチェックする。
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        uri = DbDefineShare.BoxMaster.CONTENT_URI;
        //uri = DbDefineShare.BoxShare.CONTENT_URI;
        ContentResolver resolver = this.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        String[] projection = null;
        String selection = "( name='" + groupCommon.getDefaultGroupName() + "' ) and ( flag_invalid=0 )";
        try {
            //Cursor cursor = client.query(uri, null, selection, null, null);
            //Cursor cursor = client.query(uri, null, "( name='NerveNetChat' ) and ( flag_invalid=0 )", null, null);
            Cursor cursor = client.query(uri, null, null, null, null);
            if (cursor != null) {
                int cnt = cursor.getCount();
                if (cnt < 1) {
                    // デフォルトグループ無し。BoxMasterにグループを作成する。
                    groupBoxMaster = GroupBoxMaster.newInstance(confNode.mUriBoat);
                    groupBoxMaster.idRecord = groupCommon.getDefaultGroupName().getBytes();
                    groupBoxMaster.boxName = groupCommon.getDefaultGroupName();
                    groupBoxMaster.timeUpdate = System.currentTimeMillis();
                    groupBoxMaster.timeDiscard = groupBoxMaster.timeUpdate + BoxLimitTime;
                    DbDefineShare.BoxMaster boxMaster = groupBoxMaster.toRecord();
                    boxMaster.mPermit = (short) (boxMaster.PERMIT_SHARE_READ | boxMaster.PERMIT_SHARE_WRITE);
                    ContentResolver cont = resolver;
                    ContentProviderClient contentProviderClient = cont.acquireContentProviderClient(uri);
                    if (contentProviderClient != null) {
                        contentProviderClient.insert(uri, boxMaster.getForInsert());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.release();
        }
        */

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        /*
        // グループ設定
        SubMenu groupSubMenu = menu.addSubMenu(R.string.option_menu_group);
        // グループ作成
        groupSubMenu.add(menu.NONE, OPTION_SUBMENU_CREATE_GROUP, menu.NONE, R.string.option_submenu_create_group);
        // グループ削除
        groupSubMenu.add(menu.NONE, OPTION_SUBMENU_DELETE_GROUP, menu.NONE, R.string.option_submenu_delete_group);
        */

        // チャット設定
        SubMenu chatSubMenu = menu.addSubMenu(R.string.option_menu_chat);
        // 名前登録
        chatSubMenu.add(menu.NONE, OPTION_SUBMENU_REG_MYNAME, menu.NONE, R.string.option_submenu_reg_chat_myname);
        // メッセージ表示件数
        chatSubMenu.add(menu.NONE, OPTION_SUBMENUE_MSGCOUNT, menu.NONE, R.string.option_submenu_count);
        // 自動受信設定
        chatSubMenu.add(menu.NONE, OPTION_SUBMENUE_CHAT_AUTO_RECEIVE, menu.NONE, R.string.option_submenu_chat_auto_receive);
        // テストモード
        chatSubMenu.add(menu.NONE, OPTION_SUBMENUE_CHAT_TEST_MODE, menu.NONE, R.string.option_submenu_chat_test_mode);
        // QRコード生成
        chatSubMenu.add(menu.NONE, OPTION_SUBMENUE_CREATE_QRCODE, menu.NONE, R.string.alert_dialog_create_qrcode);

        // 画像設定・送信系
        SubMenu sendImageSubMenu = menu.addSubMenu(R.string.option_menu_sendimage);
        // 品質
        sendImageSubMenu.add(menu.NONE, OPTION_SUBMENUE_QUALITY, menu.NONE, R.string.option_submenu_quality);
        // 最大サイズ
        sendImageSubMenu.add(menu.NONE, OPTION_SUBMENUE_SENDSIZE, menu.NONE, R.string.option_submenu_maxsize);
        // 縮小倍率
        sendImageSubMenu.add(menu.NONE, OPTION_SUBMENUE_SENDRATIO, menu.NONE, R.string.option_submenu_sendratio);

        // 画像設定・受信系
        SubMenu displayImageSubMenu = menu.addSubMenu(R.string.option_menu_recvimage);
        // 縮小倍率
        displayImageSubMenu.add(menu.NONE, OPTION_SUBMENUE_DISPLAYRATIO, menu.NONE, R.string.option_submenu_recvratio);

        return true;
    }

    // オプションメニュー更新
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // グループ作成
        MenuItem creategroup = menu.findItem(OPTION_SUBMENU_CREATE_GROUP);
        // グループ削除
        MenuItem deletegroup = menu.findItem(OPTION_SUBMENU_DELETE_GROUP);
        // 名前登録
        MenuItem regmymane = menu.findItem(OPTION_SUBMENU_REG_MYNAME);
        // メッセージ表示件数
        MenuItem msgcount = menu.findItem(OPTION_SUBMENUE_MSGCOUNT);
        // 自動受信
        MenuItem autorecv = menu.findItem(OPTION_SUBMENUE_CHAT_AUTO_RECEIVE);
        autorecv.setCheckable(true).setChecked(chatCommon.getAutoReceive());
        // テストモード
        MenuItem testmode = menu.findItem(OPTION_SUBMENUE_CHAT_TEST_MODE);
        testmode.setCheckable(true).setChecked(chatCommon.getTestMode());
        // QRコード生成
        MenuItem qrcode = menu.findItem(OPTION_SUBMENUE_CREATE_QRCODE);

        // 画像・品質
        MenuItem quality = menu.findItem(OPTION_SUBMENUE_QUALITY);
        // 最大サイズ
        MenuItem sendsize = menu.findItem(OPTION_SUBMENUE_SENDSIZE);
        // 縮小倍率・送信系
        MenuItem sendratio = menu.findItem(OPTION_SUBMENUE_SENDRATIO);
        // 縮小倍率・受信系
        MenuItem displayratio = menu.findItem(OPTION_SUBMENUE_DISPLAYRATIO);

        return true;
    }

        @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        boolean ret;
        MenuCustomDialogFragment mcdf = new MenuCustomDialogFragment();
        MenuCustomDialogFragment menuCustomDialogFragment;
        switch (item.getItemId()) {
            case OPTION_SUBMENU_CREATE_GROUP:
                // グループ作成用ダイアログ表示
                mcdf.setShowType(OPTION_CREATE_CHAT_GROUP_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_DELETE_GROUP:
                // グループ削除用ダイアログ表示
                mcdf.setShowType(OPTION_DELETE_CHAT_GROUP_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENU_REG_MYNAME:
                // チャット名登録ダイアログ表示
                mcdf.setShowType(OPTION_REG_CHAT_MYNAME_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_MSGCOUNT:
                // メッセージ表示件数ダイアログ表示
                mcdf.setShowType(OPTION_MESSAGE_COUNT_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_QUALITY:
                // 画像品質ダイアログ表示
                mcdf.setShowType(OPTION_QUALITY_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_SENDSIZE:
                // 画像ファイル最大サイズダイアログ表示
                mcdf.setShowType(OPTION_SENDSIZE_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_SENDRATIO:
                // 送信画像縮小倍率ダイアログ表示
                mcdf.setShowType(OPTION_SENDRATIO_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_DISPLAYRATIO:
                // 送信画像縮小倍率ダイアログ表示
                mcdf.setShowType(OPTION_DISPLAYRATIO_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            case OPTION_SUBMENUE_CHAT_AUTO_RECEIVE:
                // 自動受信設定
                String yesno = null;
                boolean autoFlag = !chatCommon.getAutoReceive();
                if (autoFlag)
                    yesno = "Yes";
                else {
                    yesno = "No";
                }
                menuCustomDialogFragment = new MenuCustomDialogFragment();
                menuCustomDialogFragment.writeConf("AutoReceive=" + yesno, yesno);
                break;
            case OPTION_SUBMENUE_CHAT_TEST_MODE:
                // テストモード
                // 設定ファイルには書き込まない。
                boolean testmodeFlag = !chatCommon.getTestMode();
                chatCommon.setTestModee(testmodeFlag);
                break;
            case OPTION_SUBMENUE_CREATE_QRCODE:
                // 送信画像縮小倍率ダイアログ表示
                mcdf.setShowType(OPTION_CREATE_QRCODE_DIALOG);
                mcdf.show(getFragmentManager(), "test");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static class MenuCustomDialogFragment extends DialogFragment {

        private static int showType;
        private static ChatCommon chatCommon;

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            if (chatCommon == null) {
                chatCommon = new ChatCommon();
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle saveInstanceState) {
            int st;
            Integer intValue;
            Long longValue;
            String strValue;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            st = getShowType();
            switch (st) {
                case OPTION_CREATE_CHAT_GROUP_DIALOG:
                    LayoutInflater inflater1 = LayoutInflater.from(getActivity());
                    final View inputdisacrd1 = inflater1.inflate(R.layout.input_groupname_dialog2, null);
                    /* 作成、削除の実装は認証をどうするか決めてからにする。当面は未サポート
                    LayoutInflater inflater1 = LayoutInflater.from(getActivity());
                    final View inputdisacrd1 = inflater1.inflate(R.layout.input_groupname_dialog, null);
                    final EditText value1 = (EditText) (inputdisacrd1.findViewById(R.id.input_create_group_name));
                    value1.setFilters(filters);
                    */
                    builder.setTitle(getString(R.string.option_submenu_create_group));
                    builder.setView(inputdisacrd1);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*
                            // BoxMasterにグループを登録する。
                            String gname = value1.getText().toString();
                            String Glist = gname;
                            }
                            */
                        }
                    });
                    /*
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    */
                    builder.create();
                    return builder.show();
                case OPTION_DELETE_CHAT_GROUP_DIALOG:
                    LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                    final View inputdisacrd2 = inflater2.inflate(R.layout.delete_groupname_dialog2, null);
                    /* 作成、削除の実装は認証をどうするか決めてからにする。当面は未サポート
                    LayoutInflater inflater2 = LayoutInflater.from(getActivity());
                    final View inputdisacrd2 = inflater2.inflate(R.layout.delete_groupname_dialog, null);
                    final EditText value2 = (EditText) (inputdisacrd2.findViewById(R.id.input_delete_group_name));
                    value2.setFilters(filters);
                    */
                    builder.setTitle(getString(R.string.option_submenu_delete_group));
                    builder.setView(inputdisacrd2);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*
                            // グループを削除する。
                            String gname = value2.getText().toString();
                            String Glist = gname;
                            */
                        }
                    });
                    /*
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    */
                    builder.create();
                    return builder.show();
                case OPTION_REG_CHAT_MYNAME_DIALOG:
                    LayoutInflater inflater3 = LayoutInflater.from(getActivity());
                    final View inputdisacrd3 = inflater3.inflate(R.layout.input_chat_myname_dialog, null);
                    final EditText value3 = (EditText) (inputdisacrd3.findViewById(R.id.input_chat_myname));
                    value3.setText(chatCommon.getMyChatName());
                    builder.setTitle(getString(R.string.option_submenu_reg_chat_myname));
                    builder.setView(inputdisacrd3);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /****
                            *   XMLを検索してニックネームが重複していないか確認する。  *
                            *   重複していたら、警告を表示して再入力。
                            ****/
                            String myname = value3.getText().toString();
                            if (checkNickname(myname)) {
                                // チャットで使用する名前を登録する。
                                writeConf("ChatName=" + myname);
                            }
                            // ToDo: BoxMemberを検索してグループリストを作成する。
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();
                case OPTION_MESSAGE_COUNT_DIALOG:
                    LayoutInflater inflater4 = LayoutInflater.from(getActivity());
                    final View inputdisacrd4 = inflater4.inflate(R.layout.input_chat_message_count, null);
                    final EditText value4 = (EditText) (inputdisacrd4.findViewById(R.id.input_chat_message_count));
                    intValue = chatCommon.getMaxMessageCount();
                    strValue = intValue.toString();
                    value4.setText(strValue);
                    builder.setTitle(getString(R.string.option_submenu_count));
                    builder.setView(inputdisacrd4);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        // チャットのメッセージ表示件数を設定する。
                        String count = value4.getText().toString();
                        int icount = Integer.parseInt(count);
                        // 下限 10個、上限 30個
                        if (icount < 10) {
                            count = "10";
                        } else if (icount > 30) {
                            count = "30";
                        }
                        writeConf("MessageCount=" + count, count);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return builder.show();
                case OPTION_QUALITY_DIALOG:
                    LayoutInflater inflater5 = LayoutInflater.from(getActivity());
                    final View inputdisacrd5 = inflater5.inflate(R.layout.input_chat_image_quality_dialog, null);
                    final EditText value5 = (EditText) (inputdisacrd5.findViewById(R.id.input_chat_image_quality));
                    intValue = chatCommon.getImageQuality();
                    strValue = intValue.toString();
                    value5.setText(strValue);
                    builder.setTitle(getString(R.string.option_submenu_quality));
                    builder.setView(inputdisacrd5);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 送信する画像の品質を設定する。
                            String quality = value5.getText().toString();
                            int icount = Integer.parseInt(quality);
                            // 下限 10、上限 100
                            if (icount < 10) {
                                quality = "10";
                            } else if (icount > 100) {
                                quality = "100";
                            }
                            writeConf("ImageQuality=" + quality, quality);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_SENDSIZE_DIALOG:
                    LayoutInflater inflater6 = LayoutInflater.from(getActivity());
                    final View inputdisacrd6 = inflater6.inflate(R.layout.input_chat_image_size_dialog, null);
                    final EditText value6 = (EditText) (inputdisacrd6.findViewById(R.id.input_chat_image_size));
                    longValue = chatCommon.getImageMaxSize();
                    strValue = longValue.toString();
                    value6.setText(strValue);
                    builder.setTitle(getString(R.string.option_submenu_maxsize));
                    builder.setView(inputdisacrd6);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 送信する画像の品質を設定する。
                            String sendsize = value6.getText().toString();
                            int icount = Integer.parseInt(sendsize);
                            // 下限 1024、上限 100
                            if (icount < 1024) {
                                sendsize = "1024";  // 1Kbytes
                            } else if (icount > 2097152) {
                                sendsize = "2097152"; // 2Mbytes
                            }
                            writeConf("ImageSize=" + sendsize, sendsize);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_SENDRATIO_DIALOG:
                    LayoutInflater inflater7 = LayoutInflater.from(getActivity());
                    final View inputdisacrd7 = inflater7.inflate(R.layout.input_chat_image_send_ratio_dialog, null);
                    final RadioGroup sendRatioGroup = (RadioGroup) inputdisacrd7.findViewById(R.id.chat_image_send_ratio_group);
                    builder.setTitle(getString(R.string.option_submenu_sendratio));
                    builder.setView(inputdisacrd7);
                    float wkratio = chatCommon.getImageSendRatio();
                    if (wkratio == chatCommon.IMAGE_RATIO_100) {
                        sendRatioGroup.check(R.id.send_ratio_100);
                    } else if (wkratio == chatCommon.IMAGE_RATIO_75) {
                        sendRatioGroup.check(R.id.send_ratio_75);
                    } else if (wkratio == chatCommon.IMAGE_RATIO_50) {
                        sendRatioGroup.check(R.id.send_ratio_50);
                    } else {
                        sendRatioGroup.check(R.id.send_ratio_25);
                    }
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = sendRatioGroup.getCheckedRadioButtonId();
                            RadioButton radioButton7 = (RadioButton) inputdisacrd7.findViewById(id);
                            String ratioValue = radioButton7.getText().toString();
                            if (ratioValue.equals("100%")) {
                                ratioValue = "100";
                            } else if (ratioValue.equals("75%")) {
                                ratioValue = "75";
                            } else if (ratioValue.equals("50%")) {
                                ratioValue = "50";
                            } else {
                                ratioValue = "25";
                            }
                            writeConf("ImageSendRatio=" + ratioValue, ratioValue);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_DISPLAYRATIO_DIALOG:
                    LayoutInflater inflater8 = LayoutInflater.from(getActivity());
                    final View inputdisacrd8 = inflater8.inflate(R.layout.input_chat_image_display_ratio_dialog, null);
                    final RadioGroup displayRatioGroup = (RadioGroup) inputdisacrd8.findViewById(R.id.chat_image_display_ratio_group);
                    builder.setTitle(getString(R.string.option_submenu_recvratio));
                    builder.setView(inputdisacrd8);
                    float wkdisratio = chatCommon.getImageDisplayRatio();
                    if (wkdisratio == chatCommon.IMAGE_RATIO_100) {
                        displayRatioGroup.check(R.id.display_ratio_100);
                    } else if (wkdisratio == chatCommon.IMAGE_RATIO_75) {
                        displayRatioGroup.check(R.id.display_ratio_75);
                    } else if (wkdisratio == chatCommon.IMAGE_RATIO_50) {
                        displayRatioGroup.check(R.id.display_ratio_50);
                    } else {
                        displayRatioGroup.check(R.id.display_ratio_25);
                    }
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = displayRatioGroup.getCheckedRadioButtonId();
                            RadioButton radioButton8 = (RadioButton) inputdisacrd8.findViewById(id);
                            String ratioValue = radioButton8.getText().toString();
                            if (ratioValue.equals("100%")) {
                                ratioValue = "100";
                            } else if (ratioValue.equals("75%")) {
                                ratioValue = "75";
                            } else if (ratioValue.equals("50%")) {
                                ratioValue = "50";
                            } else {
                                ratioValue = "25";
                            }
                            writeConf("ImageDisplayRatio=" + ratioValue, ratioValue);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    return  builder.show();
                case OPTION_CREATE_QRCODE_DIALOG:
                    LayoutInflater inflater9 = LayoutInflater.from(getActivity());
                    final View inputdisacrd9 = inflater9.inflate(R.layout.create_qrcode_dialog, null);
                    final ImageView QrCode = (ImageView) inputdisacrd9.findViewById(R.id.qrcode_image);
                    String myQrCode = chatCommon.getMySIPURI();
                    int size = 400;
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(myQrCode, BarcodeFormat.QR_CODE, size, size);
                        QrCode.setImageBitmap(bitmap);
                    } catch (WriterException w) {
                        w.printStackTrace();
                    }
                    builder.setView(inputdisacrd9);
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 何もしない
                        }
                    });
                    builder.create();
                    return builder.show();
            }
            return null;
        }

        public static void writeConf(String... params) {
            File envFile;
            String envFilename = "voicemessage.conf";
            String envfilename = Environment.getExternalStorageDirectory() + "/" + envFilename;
            String wkfilename;
            File workFile;
            wkfilename = envfilename + "work";
            envFile = new File(envfilename);
            workFile = new File(wkfilename);
            try {
                FileInputStream fis = new FileInputStream(envFile);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufread = new BufferedReader(isr);
                FileOutputStream fos = new FileOutputStream(workFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                PrintWriter bufwrite = new PrintWriter(osw);

                String Param;
                String str = null;
                boolean groupFlag = false;
                boolean writeFlag = false;
                while ((Param = bufread.readLine()) != null) {
                    str = Param;
                    if (!groupFlag) {
                        if (Param.toLowerCase().indexOf("[group]") != -1) {
                            groupFlag = true;
                        }
                    }
                    if (groupFlag) {
                        if (Param.toLowerCase().indexOf("chatname") != -1) {
                            // チャット名登録
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("chatname") != -1) {
                                    str = params[0];
                                    writeFlag = true;
                                    chatCommon.setMyChatName(str);
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("messagecount") != -1) {
                            // 表示件数設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("messagecount") != -1) {
                                    str = params[0];
                                    String count = params[1];
                                    chatCommon.setMaxMessageCount(Integer.parseInt(count));
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("imagequality") != -1) {
                            // 画像品質設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("imagequality") != -1) {
                                    str = params[0];
                                    String quality = params[1];
                                    chatCommon.setImageQuality(Integer.parseInt(quality));
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("imagesize") != -1) {
                            // 送信画像最大サイズ設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("imagesize") != -1) {
                                    str = params[0];
                                    String maxsize = params[1];
                                    chatCommon.setImageMaxSize(Integer.parseInt(maxsize));
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("imagesendratio") != -1) {
                            // 送信画像縮小倍率設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("imagesendratio") != -1) {
                                    str = params[0];
                                    String sendratio = params[1];
                                    chatCommon.setImageSendRatio(Integer.parseInt(sendratio));
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("imagedisplayratio") != -1) {
                            // 表示画像縮小倍率設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("imagedisplayratio") != -1) {
                                    str = params[0];
                                    String displayratio = params[1];
                                    chatCommon.setImageDisplayRatio(Integer.parseInt(displayratio));
                                }
                            }
                        }
                        if (Param.toLowerCase().indexOf("autoreceive") != -1) {
                            // チャットメッセージ自動受信設定
                            if (params.length > 0) {
                                if (params[0].toLowerCase().indexOf("autoreceive") != -1) {
                                    str = params[0];
                                    if (params[1].equalsIgnoreCase("yes")) {
                                        chatCommon.setAutoReceive(true);
                                        voiceMessageCommon.startChatMessageService(chatCommon.getChatContext());
                                    } else {
                                        chatCommon.setAutoReceive(false);
                                        voiceMessageCommon.stopChatMessageService(chatCommon.getChatContext());
                                    }
                                }
                            }
                        }
                    } else {
                        if (Param.toLowerCase().indexOf("chatname") != -1) {
                            str = "";  // [GROUP]無しで "ChatName"の行は削除する。
                        }
                    }
                    bufwrite.println(str);
                    str = null;
                }
                // チャット名の記述が見つからなかった。
                if ((!groupFlag) || (!writeFlag)) {
                    if (params[0].toLowerCase().indexOf("chatname") != -1) {
                        str = params[0];
                    }
                    if (!groupFlag) {
                        bufwrite.println("[GROUP]");
                    }
                    if (str != null) {
                        bufwrite.println(str);
                        chatCommon.setMyChatName(str);
                    }
                }
                bufwrite.flush();
                envFile.delete();
                workFile.renameTo(envFile);
                bufread.close();
                bufwrite.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setShowType(int type) {
            showType = type;
        }

        private int getShowType() {
            return showType;
        }

        private boolean checkNickname(String nickname) {
            boolean bret = true;

            return bret;
        }

        private InputFilter[] filters = { new AlphanumericFilter() };

        class AlphanumericFilter implements InputFilter {
            public CharSequence filter( CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[a-zA-Z0-9]+$")) {
                    return source;
                }
                return "";
            }
        }
    }

}
