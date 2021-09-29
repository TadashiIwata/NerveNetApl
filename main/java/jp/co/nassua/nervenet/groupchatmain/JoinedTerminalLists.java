package jp.co.nassua.nervenet.groupchatmain;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;

public class JoinedTerminalLists extends FragmentActivity {

    static ChatCommon chatCommon;
    static GroupCommon groupCommon;
    static GroupBoxMember groupBoxMember;

    static String groupname;
    private static Context mContext;
    static final ArrayList<String> mList = new ArrayList<>();        // 表示用の参加端末リスト

    public JoinedTerminalLists() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_terminal_list);

        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (groupBoxMember == null) {
            groupBoxMember = new GroupBoxMember();
        }
        if (mContext == null) {
            mContext = this.getBaseContext();
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

        TextView textView = (TextView) findViewById(R.id.joinedterminallist_title);
        textView.setText(groupname);
        // BoxMemberを検索してグループに未参加の端末リストを作る。
        createJoinedTerminalList(groupname);
        ArrayAdapter arrayAdapter = new ArrayAdapter(
                mContext,
                android.R.layout.simple_list_item_1,
                mList
        );
        ListView listView = findViewById(R.id.joined_terminal_list);
        listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        listView.setAdapter(arrayAdapter);
    }

    // グループに参加している端末のリストを作成する。
    private void createJoinedTerminalList(String gname) {

        String MySIPURI = chatCommon.getMySIPURI();
        Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
        ContentResolver resolver = mContext.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        String selection = ( "id_box=x'" + groupCommon.toHex(gname.getBytes()) + "'");
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
                                            String terminalInfo = getString(R.string.alert_dialog_add_terminal_name) + " : " + boxMember.name + "\n";
                                            terminalInfo = terminalInfo + getString(R.string.alert_dialog_add_terminal_sipuri) + " : " + sipuri;
                                            mList.add(terminalInfo);
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
        }
    }
}
