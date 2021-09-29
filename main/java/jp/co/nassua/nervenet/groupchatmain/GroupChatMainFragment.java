package jp.co.nassua.nervenet.groupchatmain;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import de.timroes.android.listview.EnhancedListView;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class GroupChatMainFragment extends Fragment {
    static ActMain actMain;
    static VoiceMessageCommon voiceMessageCommon;
    static GroupCommon groupCommon;
    static GroupItem groupItem;
    static GroupList groupList;
    static ChatMessageService chatMessageService;
    static Context gfContext;
    static ContentChat contentChat;
    static int tabnum;
    static final ArrayList<String> mList = new ArrayList<>();

    //static private ArrayAdapter groupAdapter;
    static private View view;
    static GroupListAdapter groupListAdapter;
    static EnhancedListView listView;

    public GroupChatMainFragment() {
        tabnum = 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.grouptab_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] groupname;
        // グループリスト作成
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupItem == null) {
            groupItem = new GroupItem();
        }
        if (actMain == null) {
            actMain = ActMain.actMyself;
        }
        gfContext = this.getContext();
        int listcnt = groupList.getGroupCount();
        int groupcnt = 0;
        // グループ数を取得する
        if (listcnt > 0) {
            groupname = new String[listcnt];
            groupItem = groupList.getGroupItem(0);
            groupname[0] = groupItem.boxname;
            groupcnt = 1;
            for(int idx=0, idx2=1; idx2 < listcnt; idx2++) {
                groupItem = groupList.getGroupItem(idx2);
                String wkgroupname = groupItem.boxname;
                if (!(groupname[idx].equals(wkgroupname))) {
                    idx++;
                    groupcnt++;
                    groupname[idx] = wkgroupname;
                }
            }
        } else {
            // 参加グループ無し
            groupname = new String[1];
            groupname[0] = groupCommon.getDefaultGroupName();
            groupcnt = 1;
        }
        mList.clear();
        for (int midx = 0; midx < groupcnt; midx++) {
            mList.add(groupname[midx]);
        }
        // ListViewを表示する
        groupListAdapter = new GroupListAdapter(this.getContext(), R.layout.group_name, mList);
        listView = (EnhancedListView) view.findViewById(R.id.group_list);
        listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        listView.setAdapter(groupListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (groupCommon.readConf()) {
                    // グループ名を取得する。
                    ListView GrouplistView = (ListView)parent;
                    String clcikBoxName = (String )GrouplistView.getItemAtPosition(position);
                    GroupItem groupItem = new GroupItem();
                    groupItem.boxname = clcikBoxName;
                    // ChatMessageServiceを停止する。
                    voiceMessageCommon.stopChatMessageService(getContext());
                    // チャット画面へ遷移する
                    Context context = gfContext;
                    Intent intent = new Intent(context, GroupChatActivity.class);
                    intent.putExtra("GROUP", groupItem);
                    startActivity(intent);
                } else {
                    // チャット名が登録されていないことを表示する。
                    groupCommon.alertMessage(getContext(), contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
                }
                return;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!groupCommon.readConf()) {
                    // チャット名を登録を促すダイアログを表示する。
                    Context context = gfContext;
                    groupCommon.alertMessage(context, contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
                } else {
                    // グループ名を取得する。
                    ListView GrouplistView = (ListView) parent;
                    String clcikBoxName = (String) GrouplistView.getItemAtPosition(position);
                    GroupItem groupItem = new GroupItem();
                    groupItem.boxname = clcikBoxName;
                    // グループ名を取得する。
                    Intent intent;
                    Context context;
                    if (isAdded()) {
                        context = gfContext;
                        intent = new Intent(context, JoinedTerminalLists.class);
                        intent.putExtra("GROUP", clcikBoxName);
                        startActivity(intent);
                    } else {
                        // 画面遷移をさせない。
                    }
                }
                return true;
            }
        });

        // スワイプされたら表示から消してグループから退会する
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int position) {
                if (position > 0) {  // 2行目以降のグループのみ退会できる。
                    String groupname = mList.get(position);
                    UnsubscribeGroup(groupname);
                }
                return null;
            }
        });
        listView.enableSwipeToDismiss();
    }

    public void RefreshView() {

        String[] groupname;

        int listcnt = groupList.getGroupCount();
        int groupcnt = 0;
        // グループ数を取得する
        if (listcnt > 0) {
            groupname = new String[listcnt];
            groupItem = groupList.getGroupItem(0);
            groupname[0] = groupItem.boxname;
            groupcnt = 1;
            for(int idx=0, idx2=1; idx2 < listcnt; idx2++) {
                groupItem = groupList.getGroupItem(idx2);
                String wkgroupname = groupItem.boxname;
                if (!(groupname[idx].equals(wkgroupname))) {
                    idx++;
                    groupcnt++;
                    groupname[idx] = wkgroupname;
                }
            }
        } else {
            // 参加グループ無し
            groupname = new String[1];
            groupname[0] = groupCommon.getDefaultGroupName();
            groupcnt = 1;
        }
        mList.clear();
        for (int midx = 0; midx < groupcnt; midx++) {
            mList.add(groupname[midx]);
        }
        // ListViewを表示する
        listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        listView.setAdapter(groupListAdapter);
        groupListAdapter.notifyDataSetChanged();
        listView.invalidateViews();
    }

    public class GroupListAdapter extends ArrayAdapter {

        public GroupListAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            // Viewの再利用時など
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.group_name, parent, false);
            }

            final String groupname = getItem(position).toString();
            if (groupname != null) {
                TextView textView = (TextView) v.findViewById(R.id.group_list_name);
                textView.setText(groupname);
                textView.setTextSize(20);
            }
            ImageButton imageButton = (ImageButton) v.findViewById(R.id.group_list_button);
            if (position > 0) {
                imageButton.setClickable(true);
                imageButton.setVisibility(View.VISIBLE);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // グループ名を取得する。
                        Intent intent;
                        EnhancedListView GrouplistView = (EnhancedListView) parent;
                        String clcikBoxName = (String) GrouplistView.getItemAtPosition(position);
                        Context context;
                        if (isAdded()) {
                            context = gfContext;
                            intent = new Intent(context, UnjoinedTerminalLists.class);
                            intent.putExtra("GROUP", clcikBoxName);
                            startActivity(intent);
                        } else {
                            // 画面遷移をさせない。
                        }
                    }
                });
            } else {
                imageButton.setClickable(false);
                imageButton.setVisibility(View.INVISIBLE);
            }
            return v;
        }
    }

    private void UnsubscribeGroup(String groupname) {
        // XMLから削除する。
        String wkGroupname = "<group>" + groupname + "</group>";
        int len = wkGroupname.length();
        int len1, len2, idx1, idx2;

        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + groupCommon.VM_Group_Terminal_List);

        StringBuffer stringBuffer = new StringBuffer("");
        try {
            FileReader fileReader = new FileReader(vmfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String data = null;
            idx1 = -1;
            while ((data = bufferedReader.readLine()) != null) {
                data = data.trim();
                idx1 = data.indexOf(wkGroupname);
                if (idx1 != -1) {
                    String data1 = null;
                    String data2 = null;
                    if (idx1 == 0) {
                        // 先頭
                        len1 = data.length();
                        if (len > len1) {
                            idx1 = len1 + 1;
                            len2 = len - len1;
                            data1 = data.substring(idx1);
                            stringBuffer.append(data1);
                        }
                    } else {
                        // 先頭以外
                        idx2 = idx1 + len;
                        data1 = data.substring(0, idx1);
                        data2 = data.substring(idx2);
                        stringBuffer.append(data1);
                        stringBuffer.append(data2);
                    }
                } else {
                    stringBuffer.append(data);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String newRecord = stringBuffer.toString();
        try {
            FileWriter fileWriter = new FileWriter(vmfile);
            fileWriter.write(newRecord);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // BoxMemberのレコードを無効化する。
        UnsubscribeBoxMember(groupname);
        // グループリストテーブルからグループを消す
        groupList.removeItem(groupname);
        // グループリストの表示を更新する。
        Context context = actMain.actMyself.getApplicationContext();
        Intent intent = new Intent();
        intent.setAction(ConstantGroup.ACT_REQUEST);
        intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST2);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void UnsubscribeBoxMember(String groupname) {
        ChatCommon chatCommon = new ChatCommon();
        Context context = getContext();

        String sipuri = chatCommon.getMySIPURI();
        int result;

        Long newTImeUpdate = System.currentTimeMillis();  // 新しいレコード更新日時を設定する
        Long newTimeDiscard = System.currentTimeMillis() + chatCommon.getGroupLimitTime(); // 新しいレコード廃棄日時を設定する。
        ContentResolver contentResolver1 = context.getContentResolver();
        // レコードを更新する
        ContentValues contentValues = new ContentValues();
        contentValues.put("time_update", newTImeUpdate);
        contentValues.put("time_discard", newTimeDiscard);
        contentValues.put("flag_invalid", 1);
        String selection = ( "(id_box=x'" + groupCommon.toHex(groupname.getBytes()) + "') AND (uri_boat='" + sipuri + "')" );
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
    }
}