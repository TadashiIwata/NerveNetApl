package jp.co.nassua.nervenet.groupchatmain;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;

public class UnjoinedTerminalLists extends FragmentActivity {

    static ChatCommon chatCommon;
    static GroupCommon groupCommon;
    static TerminalList terminalList;
    static SelectTerminalItem selectTerminalItem;
    static SelectTerminalList selectTerminalList;
    static GroupBoxMember groupBoxMember;

    static String groupname;
    static final ArrayList<TerminalItem> gtList = new ArrayList<>();  // グループに参加している端末のリスト
    static final ArrayList<TerminalItem> tList = new ArrayList<>();   // グループに未参加の端末のリスト
    static final ArrayList<String> mList = new ArrayList<>();        // 表示用の未参加端末リスト
    //static final ArrayList<TerminalItem> selectList = new ArrayList<>();  // 選択された端末のリスト
    static private ArrayAdapter arrayAdapter;
    static ListView listView;

    private static Context mContext;

    private static final int STATUS_NO_RECORD = 0;
    private static final int STATUS_INVALID_RECORD = STATUS_NO_RECORD + 1;
    private static final int STATUS_VALID_RECORD = STATUS_INVALID_RECORD + 1;

    private static byte[] id_Record;

    public UnjoinedTerminalLists() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unjoined_terminal_list);

        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (selectTerminalItem == null) {
            selectTerminalItem = new SelectTerminalItem();
        }
        if (selectTerminalList == null) {
            selectTerminalList = new SelectTerminalList();
        }
        if (groupBoxMember == null) {
            groupBoxMember = new GroupBoxMember();
        }
        if (terminalList == null) {
            terminalList = new TerminalList();
        }
        if (mContext == null) {
            mContext = getBaseContext();
        }
        Intent intent = getIntent();
        groupname = intent.getStringExtra("GROUP");

    }


    @Override
    public void onResume() {
        super.onResume();

        // タイトル設定
        setTitle(getString(R.string.group_chat_title) + " " + ActMain.appversion);
        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView textView = (TextView)findViewById(R.id.addterminalfromgroup_title);
        textView.setText(groupname);

        Button button = (Button)findViewById(R.id.addterminaltogroup);

        // BoxMemberを検索してグループに未参加の端末リストを作る。
        listView = findViewById(R.id.add_terminal_list_to_group);
        if (createUnjoinedTerminalList(groupname)) {
            // 未登録端末有り
            arrayAdapter = new ArrayAdapter(
                    this.getBaseContext(),
                    android.R.layout.simple_list_item_checked,
                    mList
            );
            //listView = findViewById(R.id.add_terminal_list_to_group);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(arrayAdapter);
        } else {
            // 未登録端末無し
            // ToDo:全員参加のメッセージ、またはダイアログを表示する。
            // グループ参加ボタン無効化
            //button.setClickable(false);
        }

        // プラスボタンが押されたら新規端末の登録を行う。
        ImageButton imageButton = (ImageButton)findViewById(R.id.addnewterminal);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // QRコード読み取りに変更する。
                readQRCode();
            }
        });
        // グループへの参加
        //button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択された端末をグループに登録する。
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                boolean ret = addSelectedTerminalToGroup(checked, groupname);
                if (ret) {
                } else {
                    // ToDo:端末が選択されていないメッセージ
                }
            }
        });
    }

    private void readQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(QrcodeCaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    // QRコードを読み取ったら端末登録画面を出す。
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String code;
        String QRCode = null;
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && (code = result.getContents()) != null) {
            QRCode = result.getContents();
        }
        groupCommon.setQrcodeSipUri(QRCode);
        // Layout作成
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // ユーザ名
        TextView textView_name = new TextView(this);
        textView_name.setText(R.string.alert_dialog_add_terminal_name);
        final EditText editText_name = new EditText(this);
        // 文字数制限設定
        InputFilter[] filters_name = new InputFilter[1];
        filters_name[0] = new InputFilter.LengthFilter(10);
        // 入力容設定
        editText_name.setFilters(filters_name);
        editText_name.setInputType(InputType.TYPE_CLASS_TEXT);
        editText_name.setEnabled(true);
        // Layout設定
        linearLayout.addView(textView_name);
        linearLayout.addView(editText_name);

        // SIP URI設定
        TextView sipuri_label = new TextView(this);
        final TextView textView_sipuri = new TextView(this);
        // SIP URI設定：表示内容設定
        sipuri_label.setText(R.string.alert_dialog_add_terminal_sipuri);
        String qrsipuri = groupCommon.getQrcodeSipUri();
        if (qrsipuri != null) {
            textView_sipuri.setText(qrsipuri); }
        textView_sipuri.setTextColor(Color.parseColor("black"));
        linearLayout.addView(sipuri_label);
        linearLayout.addView(textView_sipuri);

        // ダイアログ作成
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(linearLayout);
        builder.setTitle(R.string.alert_dialog_add_terminal);
        // 登録ボタン
        builder.setPositiveButton(R.string.alert_dialog_add_terminal_ok, null);
        builder.create();
        final AlertDialog alertDialog = builder.show();
        Button OkButton = alertDialog.getButton( DialogInterface.BUTTON_POSITIVE);
        OkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 端末登録処理
                String name = editText_name.getText().toString();
                String sipuri = textView_sipuri.getText().toString();
                if (!(name.equals(""))) {
                    groupCommon.addTerminal(sipuri, name);
                    Context context = getApplicationContext();
                    Intent intent = new Intent();
                    intent.setAction(ConstantGroup.ACT_REQUEST);
                    intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_TERMINALLIST2);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    alertDialog.dismiss();
                } else {
                    editText_name.setError(getResources().getString(R.string.error_message_invlied_name));
                }
            }
        });
        // 取消ボタン
        builder.setNegativeButton(R.string.alert_dialog_add_terminal_ng, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 何もしない
                dialog.cancel();
            }
        });
    }


    public void addNreTerminal() {
        RefreshView();
    }

    private void RefreshView() {
        Context context = mContext;
        createUnjoinedTerminalList(groupname);
        arrayAdapter = new ArrayAdapter(
                context,
                android.R.layout.simple_list_item_checked,
                mList
        );
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        listView.invalidateViews();
    }

    private boolean addSelectedTerminalToGroup(SparseBooleanArray checked, String gname) {
        boolean bret = false;
        int tcnt = terminalList.getTerminalCount();
        String selectNickname = null;
        String selectSipuri = null;
        String wknickname = null;
        String wksipuri = null;

        if (tcnt > 0) {
            for (int position = 0; position < tcnt; position++) {
                if (checked.get(position) == true) {
                    selectNickname = tList.get(position).name;
                    selectSipuri = tList.get(position).sipuri;
                    for (int idx = 0; idx < tcnt; idx++) {
                        wknickname = terminalList.ITEMS.get(idx).name;
                        wksipuri = terminalList.ITEMS.get(idx).sipuri;
                        if (selectNickname.equals(wknickname) && selectSipuri.equals(wksipuri)) {
                            bret = true;
                            break;
                        }
                    }
                    if (bret) {
                        int stat = checkTeminalInaaGroup(gname, selectSipuri);
                        if ((stat == STATUS_NO_RECORD) || (stat == STATUS_INVALID_RECORD)) {
                            if (stat == STATUS_NO_RECORD) {  // グループ未参加
                                // グループへの参加
                                addTerminalToGrpup(gname, selectNickname, selectSipuri);
                            } else {  // グループ未参加(無効レコード)
                                updateTerminalToGroup();
                            }
                        }
                    }
                }
            }
            // グループ未参加端末リスト更新
            RefreshView();
        }
        return bret;
    }

    // グループに端末を追加する。
    private void addTerminalToGrpup(String groupname, String nickname, String sipuri) {
        Uri result;
        Context context = getApplicationContext();

        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentResolver contentResolver1 = context.getContentResolver();
        ContentProviderClient contentProviderClient = contentResolver1.acquireContentProviderClient(uri);
        String strBortId = new String(chatCommon.getMyBoatId());
        String recordId = strBortId + groupCommon.getNowDayTimeStr();
        long timeUpdate  = System.currentTimeMillis();
        long timeDiscard = System.currentTimeMillis() + chatCommon.getGroupLimitTime();
        // GroupBoxMember
        GroupBoxMember.idMyself = chatCommon.getMyBoatId();
        GroupBoxMember.uriMember = sipuri;
        GroupBoxMember.idBox = recordId.getBytes();
        // Common
        groupBoxMember = GroupBoxMember.newInstance(chatCommon.getMySIPURI());
        groupBoxMember.timeUpdate = timeUpdate;
        groupBoxMember.timeDiscard = timeDiscard;
        groupBoxMember.flagInvalid = 0;
        // Boxmember
        groupBoxMember.authority = groupBoxMember.AUTHORITY_SETTING_ALL;
        groupBoxMember.idRecord = recordId.getBytes();
        groupBoxMember.boxName = groupname;
        groupBoxMember.name = nickname;
        DbDefineShare.BoxMember rec = groupBoxMember.toRecord();
        rec.mCommon.mNodeUpdate = chatCommon.getMyBoatId();
        rec.mCommon.mTimeDiscard = timeDiscard;
        rec.mCommon.mTimeUpdate = timeUpdate;
        int ret = 0;
        if (contentProviderClient != null) {
            try {
                result = contentProviderClient.insert(uri, rec.getForInsert());
                String strResult = result.getLastPathSegment();
                if (strResult != null) {
                    switch (Integer.parseInt(strResult)) {
                        case -1:
                            // エラー
                            ret = -1;
                            break;
                        default:
                            // 追加成功
                            ret = 1;
                            break;
                    }
                } else {
                    ret = 2;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                contentProviderClient.release();
            }
            if (ret == 1) {
                pushBoxMember(recordId);
            } else {
                ret = 5;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTerminalToGroup() {
        String hexIdRecord;
        String idRecord;
        int result;

        Long newTImeUpdate = System.currentTimeMillis();  // 新しいレコード更新日時を設定する
        Long newTimeDiscard = System.currentTimeMillis() + chatCommon.getGroupLimitTime(); // 新しいレコード廃棄日時を設定する。
        hexIdRecord = chatCommon.bin2hex(id_Record);
        ContentResolver contentResolver1 = mContext.getContentResolver();
        // レコードを更新する
        ContentValues contentValues = new ContentValues();
        contentValues.put("time_update", newTImeUpdate);
        contentValues.put("time_discard", newTimeDiscard);
        contentValues.put("flag_invalid", 0);
        String selection = "id_record=x'" + hexIdRecord + "'";
        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentProviderClient contentProviderClient = contentResolver1.acquireContentProviderClient(uri);
        int ret = 0;
        if (contentProviderClient != null) {
            try {
                result = contentProviderClient.update(uri, contentValues, selection, null);
                if (0 < result) {
                    // 更新成功
                    ret = 1;
                } else {
                    // エラー
                    ret = -1;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                contentProviderClient.release();
            }
        }
        if (ret == 1) {
            idRecord = new String(id_Record);
            pushBoxMember(idRecord);
        }
    }

    private int checkTeminalInaaGroup(String groupname, String sipuri) {
        int bret = STATUS_NO_RECORD;  // 未参加：0(レコード無し)、未参加：(無効レコード)  参加中：2;

        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentResolver resolver = getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        String selection = ( "(id_box=x'" + groupCommon.toHex(groupname.getBytes()) + "') AND (uri_boat='" + sipuri + "')" );

        if (client != null) {
            try {  // グループ参加中かチェックする。
                Cursor cursor = client.query(uri, null, selection, null, null);
                if (cursor != null) {
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        DbDefineShare.BoxMember rec = DbDefineShare.BoxMember.newInstance();
                        long nowDate = System.currentTimeMillis();
                        rec.setFromQuery(cursor);
                        try {
                            GroupBoxMember boxMember = GroupBoxMember.newInstance(rec);
                            // 登録されている端末かチェックする
                            if (nowDate < boxMember.timeDiscard) { // 有効期間
                                if (boxMember.flagInvalid != 1) {  // 有効レコード
                                    bret = STATUS_VALID_RECORD; //　グループ参加中
                                }
                            }
                            if (bret == 0) {
                                bret = STATUS_INVALID_RECORD;  // 未参加：無効レコード
                                id_Record = boxMember.idRecord;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("nassua", "GroupBoxMember fromDB record Exception");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.release();
            }
        }
        return bret;
    }

    // グループ未参加の端末リストを作成する。
    private boolean createUnjoinedTerminalList(String gname) {
        String MySIPURI;
        boolean bret = false;  // false：未参加端末無し、true：未参加端末有り
        Context context = getBaseContext();
        if (context == null) {
            context = mContext;
        } else {
            mContext = context;
        }

        MySIPURI = chatCommon.getMySIPURI();
        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        String selection = ( "id_box=x'" + groupCommon.toHex(gname.getBytes()) + "'");
        gtList.clear();
        tList.clear();
        mList.clear();
        if (client != null) {
            try {  // グループに参加している端末のリストを作る。
                Cursor cursor = client.query(uri, null, selection, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            DbDefineShare.BoxMember rec = DbDefineShare.BoxMember.newInstance();
                            long nowDate = System.currentTimeMillis();
                            rec.setFromQuery(cursor);
                            try {
                                GroupBoxMember boxMember = GroupBoxMember.newInstance(rec);
                                String sipuri = rec.mUriBoat;
                                if (!MySIPURI.equals(sipuri)) {  // 自端末以外
                                    if (nowDate < boxMember.timeDiscard) {  // 有効期間内
                                        if (boxMember.flagInvalid != 1) {   // 有効レコード
                                            // 参加端末リストに追加する。
                                            TerminalItem items = new TerminalItem();
                                            items.name = boxMember.name;
                                            items.sipuri = sipuri;
                                            gtList.add(items);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("nassua", "GroupBoxMember fromDB record Exception");
                            }
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.release();
            }
            if (gtList.size() > 0) {  // 自端末以外に1台以上の端末が参加している。
                if (terminalList.getTerminalCount() > 0) {  // グループに未参加の端末がある。
                    boolean found;
                    int gtidx, tidx, aidx, gtcnt, tcnt;
                    String wkTsipuri, wkGtsipuri;
                    tcnt = terminalList.getTerminalCount();
                    gtcnt = gtList.size();
                    for(tidx=0, aidx=0; tidx < tcnt; tidx++) {
                        found = false;
                        wkTsipuri = terminalList.getTreminalSipuri(tidx);
                        for(gtidx=0; gtidx < gtcnt; gtidx++) {
                            wkGtsipuri = gtList.get(gtidx).sipuri;
                            if (wkTsipuri.equals(wkGtsipuri)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // 未参加の端末があった。
                            TerminalItem terminalItem = new TerminalItem();
                            terminalItem.sipuri = terminalList.getTreminalSipuri(tidx);
                            terminalItem.name = terminalList.getTreminalName(tidx);
                            tList.add(terminalItem);
                            mList.add(terminalList.getTreminalName(tidx));
                            bret = true;
                        }
                    }
                }
            }
        }
        return bret;
    }

    private void pushBoxMember(String idRecord) {

        byte[] data = idRecord.getBytes();

        Context context;
        context = getApplicationContext();
        if (context == null) {
            context = mContext;
        }
        Intent intent = new Intent(ConstantShare.ACT_PUBLISH);
        Bundle extras = new Bundle();
        extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
        extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxMember.PATH);
        extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, idRecord.getBytes());
        intent.putExtras(extras);
        context.sendBroadcast(intent);
    }

}
