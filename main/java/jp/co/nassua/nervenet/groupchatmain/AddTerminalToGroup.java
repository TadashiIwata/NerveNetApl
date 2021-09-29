package jp.co.nassua.nervenet.groupchatmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;


import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;

public class AddTerminalToGroup  extends Activity {


    static ActMain actMain;
    static GroupViewItem groupViewItem;
    static GroupViewList groupViewList;
    static GroupList groupList;
    static GroupCommon groupCommon;
    static ChatCommon chatCommon;
    static SelectGroupList selectGroupList;
    static SelectGroupItem selectGroupItem;
    static SelectTerminalItem selectTerminalItem;
    static SelectTerminalList selectTerminalList;
    static GroupBoxMember groupBoxMember;
    private static ContentChat contentChat;

    static final ArrayList<String> mList = new ArrayList<>();
    static private ArrayAdapter arrayAdapter;
    static ListView listView;

    static final int BOXMEMBER_STATUS_RECORD_QUERY_FAILED = 0;  // クエリー失敗
    static final int BOXMEMBER_STATUS_RECORD_FOUND = BOXMEMBER_STATUS_RECORD_QUERY_FAILED + 1;  // レコードが有った
    static final int BOXMEMBER_STATUS_RECORD_NOT_FOUND = BOXMEMBER_STATUS_RECORD_FOUND + 1; //レコードが無かった
    static final int BOXMEMBER_STATUS_RECORD_EXPIRED = BOXMEMBER_STATUS_RECORD_NOT_FOUND + 1; // 期限切れのレコード

    public AddTerminalToGroup() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_terminal_to_group);

        if (groupViewItem == null) {
            groupViewItem = new GroupViewItem();
        }
        if (groupViewList == null) {
            groupViewList = new GroupViewList();
        }
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (selectGroupItem == null) {
            selectGroupItem = new SelectGroupItem();
        }
        if (selectGroupList == null) {
            selectGroupList = new SelectGroupList();
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
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (actMain == null) {
            actMain = new ActMain();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // タイトル設定
        setTitle(getString(R.string.group_chat_title) + " " + ActMain.appversion);

        // 登録ボタン
        Button addButton = (Button) findViewById(R.id.addterminaltogroup);
        addButton.setClickable(true);

        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // グループリスト作成
        int groupcnt = groupList.getGroupCount();
        String defname = groupCommon.getDefaultGroupName(), wkgname;
        int groupViewcnt = 0, idx, idx2;
        String[] viewGroupName;
        GroupItem groupItem;
        // Default Chat Group以外のグループの件数を算出する。
        for(idx=0; idx < groupcnt; idx++) {
            groupItem = groupList.getGroupItem(idx);
            if (!(defname.equals(groupItem.boxname))) {
                groupViewcnt++;
            }
        }
        // Default Chat Group以外の参加可能グループのリストを作る。
        if (groupViewcnt > 0) {
            viewGroupName = new String[groupViewcnt];
            for(idx=0, idx2=0; idx < groupcnt; idx++) {
                groupItem = groupList.getGroupItem(idx);
                if (!(defname.equals(groupItem.boxname))) {
                    viewGroupName[idx2] = groupItem.boxname;
                    idx2++;
                }
            }
            mList.clear();
            groupViewList.clearAll();
            for(idx=0; idx < groupViewcnt; idx++) {
                String name = viewGroupName[idx];
                //item.setGroupname(name);
                mList.add(name);
                groupViewItem = new GroupViewItem();
                groupViewItem.groupname = name;
                groupViewList.addItem(groupViewItem);
            }
            arrayAdapter = new ArrayAdapter(
                    this.getBaseContext(),
                    android.R.layout.simple_list_item_checked,
                    mList
            );
            listView = findViewById(R.id.add_to_group);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(arrayAdapter);

        } else {
            // 参加可能なグループが無いメッセージを出してメインに戻る。
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                makeSelectGroupList(checked);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void makeSelectGroupList(SparseBooleanArray checked) {
        int gcnt = groupViewList.getGroupViewListCount();
        int tcnt = selectTerminalList.getSelectTerminalCount();
        int idx, idx2;
        String groupname, nickname, sipuri;
        SelectTerminalItem terminalItem;
        selectGroupList.clearAll();
        for(idx=0; idx < gcnt; idx++) {
            if (checked.get(idx) == true) {
                groupname = groupViewList.getGroupName(idx);
                for(idx2=0; idx2 < tcnt; idx2++) {
                    terminalItem = selectTerminalList.getItem(idx2);
                    nickname = terminalItem.name;
                    sipuri   = terminalItem.sipuri;
                    addGroupMember(groupname, nickname, sipuri); // デバッグ中　一時的にコメントアウト
                    addSuccessMessage();
                }
            }
        }
        selectTerminalList.clearAll();
    }

    // グループチャットのメイン画面に戻る。
    private void returnChatMainTop() {
        Intent intent = new Intent(this, GroupChatMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void addSuccessMessage() {
        String alertMessage = null;
        Context context = AddTerminalToGroup.this;

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle(R.string.alert_dialog_append_group);
        alertMessage = context.getResources().getString(R.string.alert_dialog_append_success);
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // チャットメイン画面位戻る。
                returnChatMainTop();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }


    // 端末をグループに追加する。
    public void addGroupMember(String groupname, String terminalname, String terminalsipuri) {
        /* 1. boxmamberへの書き込み
         *    端末を追加するグループに端末が登録されているか確認する。
         *    端末が登録されていければ端末を登録する。
         *    端末が登録済みの場合はレコードの有効期間を延長する。
         *    検索条件
         *      1. グループ名(id_box)と SIPURI(uri_boat)を指定する。
         *      2. flag_invalidは無視する。
         *    登録項目
         *      1. グループ名 (id_box)
         *      2. 名前 (name)
         *      3. SIPURI (uri_boat)
         */
        // グループに登録済みか確認する。
        int stat = BOXMEMBER_STATUS_RECORD_QUERY_FAILED;  // 0: 実行エラー / 1: 登録済み / 2: 未登録(insert) / 3:有効期間超過(update) 2:
        // query parameter
        //Context context = actMain.actMyself;
        Context context = getApplicationContext();
        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        String selection = ( "( id_box=x'" + str2strhex(groupname) + "') and ( uri_boat='" + terminalsipuri + "')");
        // query data
        String hexIdRecord = null; // レコードID (更新に使用する)
        try {
            Cursor cursor = client.query(uri, null, selection, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        DbDefineShare.BoxMember rec = DbDefineShare.BoxMember.newInstance();
                        rec.setFromQuery(cursor);
                        try {
                            GroupBoxMember boxMember = GroupBoxMember.newInstance(rec);
                            // レコードが見つかった
                            stat = BOXMEMBER_STATUS_RECORD_FOUND;
                            // レコード ID取得
                            hexIdRecord = bin2hex(rec.mIdRecord);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //Log.i("nassua", "ChatBoxSAhare fromDB record Exception");
                        }
                    } while (cursor.moveToNext());
                } else {
                    // グループに未登録の端末。
                    stat = BOXMEMBER_STATUS_RECORD_NOT_FOUND;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        // insert, update parameter
        ContentResolver  contentResolver1 = context.getContentResolver();
        ContentProviderClient contentProviderClient1 = contentResolver1.acquireContentProviderClient(uri);

        int ret = 0;
        long timeUpdate = System.currentTimeMillis();  // レコード更新日時
        long timeDiscard = System.currentTimeMillis() + chatCommon.getGroupLimitTime();  // レコード破棄日時
        if (BOXMEMBER_STATUS_RECORD_NOT_FOUND == stat) {
            // レコードを追加する。
            String strBortId = new String(chatCommon.getMyBoatId());
            String recordId = strBortId + groupCommon.getNowDayTimeStr();
            hexIdRecord = recordId;
            // GroupBoxMember
            GroupBoxMember.idMyself = chatCommon.getMyBoatId();
            GroupBoxMember.uriMember = terminalsipuri;
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
            groupBoxMember.name = terminalname;
            DbDefineShare.BoxMember rec = groupBoxMember.toRecord();
            rec.mCommon.mNodeUpdate = chatCommon.getMyBoatId();
            rec.mCommon.mTimeDiscard = timeDiscard;
            rec.mCommon.mTimeUpdate = timeUpdate;
            Uri result;
            if (contentProviderClient1 != null) {
                try {
                    result = contentProviderClient1.insert(uri, rec.getForInsert());
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
                    contentProviderClient1.close();
                }
            }
        } else if (BOXMEMBER_STATUS_RECORD_FOUND == stat) {
            // レコードの有効期限を更新する。
            ContentValues contentValues = new ContentValues();
            contentValues.put("time_update", timeUpdate);
            contentValues.put("time_discard", timeDiscard);
            contentValues.put("flag_invalid", 0);
            selection = "id_record=x'" + hexIdRecord + "'";
            int result;
            try {
                result = contentProviderClient1.update(uri, contentValues, selection, null);
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
                contentProviderClient1.release();
            }
        }
        if (ret == 1) {
            Intent intent = new Intent(ConstantShare.ACT_PUBLISH);
            Bundle extras = new Bundle();
            extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
            extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxMember.PATH);
            extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, hexIdRecord.getBytes());
            intent.putExtras(extras);
            context.sendBroadcast(intent);
        } else {
            ret = 5;
        }
    }

    private static String bin2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private String str2strhex(String data) {
        String strdata;
        byte[] hexdata = new String(data).getBytes();
        strdata = chatCommon.bin2hex(hexdata);
        return strdata;
    }

}
