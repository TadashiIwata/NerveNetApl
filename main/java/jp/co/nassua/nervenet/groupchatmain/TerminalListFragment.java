package jp.co.nassua.nervenet.groupchatmain;

import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.android.listview.EnhancedListView;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class TerminalListFragment extends Fragment {
    static ActMain actMain;
    static VoiceMessageCommon voiceMessageCommon;
    static GroupCommon groupCommon;
    static ChatCommon chatCommon;
    static GroupItem groupItem;
    static GroupList groupList;
    static TerminalItem terminalItem;
    static TerminalList terminalList;
    static SelectTerminalItem selectTerminalItem;
    static SelectTerminalList selectTerminalList;
    static SelectGroupItem selectGroupItem;
    static SelectGroupList selectGroupList;
    static GroupViewItem groupViewItem;
    static GroupViewList groupViewList;
    static ChatMessageService chatMessageService;
    static Context tfContext;
    static ContentChat contentChat;
    static private ArrayAdapter terminalAdapter;
    static private View view;
    static EnhancedListView listView;

    static List<Map<String, byte[]>> mIdList;
    static final ArrayList<String> mList = new ArrayList<>();
    static final ArrayList<TerminalItem> tList = new ArrayList<>();
    public static Map<Integer, Boolean> checkList = new HashMap<>();
    public static boolean createGroupSuccessFlag;


    public TerminalListFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.terminaltab_fragment, container, false);
        //fm = getFragmentManager();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 端末リスト作成
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupItem == null) {
            groupItem = new GroupItem();
        }
        if (terminalList == null) {
            terminalList = new TerminalList();
        }
        if (terminalItem == null) {
            terminalItem = new TerminalItem();
        }
        if (selectTerminalList == null) {
            selectTerminalList = new SelectTerminalList();
        }
        if (selectTerminalItem == null) {
            selectTerminalItem = new SelectTerminalItem();
        }
        if (selectGroupList == null) {
            selectGroupList = new SelectGroupList();
        }
        if (selectGroupItem == null) {
            selectGroupItem = new SelectGroupItem();
        }
        if (groupViewList == null) {
            groupViewList = new GroupViewList();
        }
        if (groupViewItem == null) {
            groupViewItem = new GroupViewItem();
        }
        if (actMain == null) {
            actMain = ActMain.actMyself;
        }
        createGroupSuccessFlag = false;
        tfContext = this.getContext();

        // イメージボタン配置
        final ImageButton imageButton = (ImageButton) view.findViewById(R.id.plus_button);
        //imageButton.setImageResource(R.mipmap.plus);
        imageButton.setImageResource(R.mipmap.user_plus);
        //imageButton.setImageResource(R.drawable.plus);

        // 端末リスト作成
        String MySIPURI;
        String[] terminalname;
        String[] terminalsipuri;
        MySIPURI = chatCommon.getMySIPURI();
        int listcnt = terminalList.getTerminalCount();
        int terminalcnt = 0;
        if (listcnt > 0) {
            terminalname = new String[listcnt];
            terminalsipuri = new String[listcnt];
            for(int idx=0, idx2=0; idx < listcnt; idx++) {
                if (!MySIPURI.equals(terminalList.ITEMS.get(idx).sipuri)) {
                    terminalname[idx2] = terminalList.ITEMS.get(idx).name;
                    terminalsipuri[idx2] = terminalList.ITEMS.get(idx).sipuri;
                    terminalcnt++;
                    idx2++;
                }
            }
        } else {
            terminalname = new String[0];
            terminalsipuri = new String[0];
        }
        if (terminalcnt > 0) {
            mList.clear();
            tList.clear();
            for (int midx = 0; midx < terminalcnt; midx++) {
                TerminalItem terminalItem = new TerminalItem();
                terminalItem.sipuri = terminalsipuri[midx];
                terminalItem.name = terminalname[midx];
                tList.add(terminalItem);
                mList.add(terminalname[midx]);
            }
        }
        // ListViewを表示する
        terminalAdapter = new ArrayAdapter(
                this.getContext(),
                android.R.layout.simple_list_item_checked,
                mList
        );
        listView = (EnhancedListView) view.findViewById(R.id.terminal_list);
        if (terminalcnt > 0) {
            for(int idx=0; idx < terminalcnt; idx++) {
                listView.setItemChecked(idx, false);
            }
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(terminalAdapter);

        // ユーザ名長押し処理
        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!groupCommon.readConf()) {
                    // チャット名を登録を促すダイアログを表示する。
                    Context context = tfContext;
                    groupCommon.alertMessage(context, contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
                } else {
                    // 長押しされたら修正用のダイアログを表示する。
                    UpdateTerminalInfomation wkinfo = new UpdateTerminalInfomation();
                    wkinfo.user = tList.get(position).name;
                    wkinfo.sipuri = tList.get(position).sipuri;
                    // 確認ダイアログを表示する。
                    // Layout作成
                    LinearLayout linearLayout = new LinearLayout(getContext());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    // ユーザ名
                    TextView name_label = new TextView(getActivity());
                    name_label.setText(R.string.alert_dialog_add_terminal_name);
                    TextView textView_name = new TextView(getActivity());
                    textView_name.setText(wkinfo.user);
                    linearLayout.addView(name_label);
                    linearLayout.addView(textView_name);
                    // SIP URI設定
                    TextView sipuri_label = new TextView(getActivity());
                    final TextView textView_sipuri = new TextView(getActivity());
                    sipuri_label.setText(R.string.alert_dialog_add_terminal_sipuri);
                    textView_sipuri.setText(wkinfo.sipuri);
                    linearLayout.addView(sipuri_label);
                    linearLayout.addView(textView_sipuri);
                    // ダイアログ作成
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setView(linearLayout);
                    alertDialog.setTitle(R.string.alert_dialog_confirm_terminal);
                    alertDialog.setPositiveButton(R.string.alert_dialog_add_terminal_ok4, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 何もしない。
                            dialog.dismiss();
                        }
                    });
                    alertDialog.create();
                    alertDialog.show();
                }
                return true;
            }
        });

        // Imageボタン処理
        // 短押し
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectcnt = 0;
                int tcnt = terminalList.getTerminalCount();
                MakeGroupDialogFragment makeGroupDialogFragment;
                SparseBooleanArray checkedTerminal = listView.getCheckedItemPositions();

                if (!groupCommon.readConf()) {
                    // チャット名を登録を促すダイアログを表示する。
                    Context context = tfContext;
                    groupCommon.alertMessage(context, contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
                } else {
                    if (tcnt > 0) {
                        selectcnt = makeSelectTerminalList(checkedTerminal);
                        if (selectcnt > 0) {
                            // グループ作成のダイアログを出す。
                            createGroupSuccessFlag = false;
                            makeGroupDialogFragment = new MakeGroupDialogFragment();
                            makeGroupDialogFragment.show(getFragmentManager(), ConstantGroup.DIALOG_TAG_ADD_GROUP);
                        } else {
                            // 登録ユーザ有り、ユーザ未選択
                            // QRコード読み取り画面
                            readQRCode();
                        }
                    } else {
                        // 登録ユーザ無し。
                        // QRコード読み取り画面
                        readQRCode();
                    }
                }
            }
        });

        // 長押しはメンバー追加
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int selectcnt = 0;
                SparseBooleanArray checkedTerminal = listView.getCheckedItemPositions();
                selectcnt = makeSelectTerminalList(checkedTerminal);
                if (!groupCommon.readConf()) {
                    // チャット名を登録を促すダイアログを表示する。
                    Context context = tfContext;
                    groupCommon.alertMessage(context, contentChat.ALERT_NICKNAME_NOT_REGISTRATION);
                } else {
                    if (selectcnt > 0) {
                        // グループ選択画面へ遷移
                        Intent intent = new Intent(getContext(), AddTerminalToGroup.class);
                        startActivity(intent);
                    }
                }
                return true;  // onClickイベントを発生させない。
            }
        });

        // スワイプされたらユーザリストから消す。
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int position) {
                Context context = getActivity().getApplicationContext();
                UpdateTerminalInfomation info = new UpdateTerminalInfomation();
                info.user   = tList.get(position).name;
                info.sipuri = tList.get(position).sipuri;
                info.setUpdateInfo(info);
                Intent intent = new Intent();
                intent.setAction(ConstantGroup.ACT_REQUEST);
                intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_UPDATE_TERMINAL);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                return null;
            }
        });

        // ユーザにチェックが入っていたらボタンを入れ替える。
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int cnt = listView.getCheckedItemCount();
                if (cnt == 0) {
                    imageButton.setImageResource(R.mipmap.user_plus);
                } else {
                    imageButton.setImageResource(R.mipmap.group_plus);
                };
            }
        });

        listView.enableSwipeToDismiss();

    }

    private void readQRCode() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
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
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // ユーザ名
        TextView textView_name = new TextView(getActivity());
        textView_name.setText(R.string.alert_dialog_add_terminal_name);
        final EditText editText_name = new EditText(getActivity());
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
        TextView sipuri_label = new TextView(getActivity());
        final TextView textView_sipuri = new TextView(getActivity());
        // SIP URI設定：表示内容設定
        sipuri_label.setText(R.string.alert_dialog_add_terminal_sipuri);
        String qrsipuri = groupCommon.getQrcodeSipUri();
        if (qrsipuri != null) {
            textView_sipuri.setText(qrsipuri); }
        textView_sipuri.setTextColor(Color.parseColor("black"));
        linearLayout.addView(sipuri_label);
        linearLayout.addView(textView_sipuri);

        // ダイアログ作成
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    Context context = getActivity().getApplicationContext();
                    Intent intent = new Intent();
                    intent.setAction(ConstantGroup.ACT_REQUEST);
                    intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_TERMINALLIST);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    alertDialog.dismiss();
                } else {
                    editText_name.setError(getResources().getString(R.string.error_message_invlied_name));
                }
            }
        });
        // 取消ボタン
        Button NgButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        NgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private int makeSelectTerminalList(SparseBooleanArray checked) {
        int selectcnt = 0;
        int tcnt = terminalList.getTerminalCount();
        String selectNickname = null;
        String selectSiputi;
        String wknickname = null;
        String wksipuri = null;
        boolean addFlag = false;

        if (tcnt > 0) {
            SelectTerminalList.clearAll();
            for (int position = 0; position < tcnt; position++) {
                if (checked.get(position) == true) {
                    addFlag = false;
                    selectNickname = tList.get(position).name;
                    selectSiputi = tList.get(position).sipuri;
                    for (int idx = 0; idx < tcnt; idx++) {
                        wknickname = terminalList.ITEMS.get(idx).name;
                        wksipuri = terminalList.ITEMS.get(idx).sipuri;
                        if ((selectNickname.equals(wknickname) && selectSiputi.equals(wksipuri))) {
                            addFlag = true;
                            break;
                        }
                    }
                    if (addFlag) {
                        SelectTerminalItem selectTerminalItem1 = new SelectTerminalItem();
                        selectTerminalItem1.name = selectNickname;
                        selectTerminalItem1.sipuri =selectSiputi;
                        selectTerminalList.addItem(selectTerminalItem1);
                        selectcnt++;
                    }
                }
            }
        }
        return selectcnt;
    }

    public void refleshTerminalList() {
        refleshTerminalListView();
    }

    // 端末リスト更新
    protected void refleshTerminalListView() {
        // 端末リスト更新
        String MySIPURI;
        String[] terminalname;
        String[] terminalsipuri;
        MySIPURI = chatCommon.getMySIPURI();
        int listcnt = terminalList.getTerminalCount();
        int terminalcnt = 0;
        if (listcnt > 0) {
            terminalname = new String[listcnt];
            terminalsipuri = new String[listcnt];
            for(int idx=0, idx2=0; idx < listcnt; idx++) {
                if (!MySIPURI.equals(terminalList.ITEMS.get(idx).sipuri)) {
                    terminalname[idx2] = terminalList.ITEMS.get(idx).name;
                    terminalsipuri[idx2] = terminalList.ITEMS.get(idx).sipuri;
                    terminalcnt++;
                    idx2++;
                }
            }
        } else {
            terminalname = new String[0];
            terminalsipuri = new String[0];
        }
        if (terminalcnt > 0) {
            mList.clear();
            tList.clear();
            for (int midx = 0; midx < terminalcnt; midx++) {
                TerminalItem terminalItem = new TerminalItem();
                terminalItem.sipuri = terminalsipuri[midx];
                terminalItem.name = terminalname[midx];
                tList.add(terminalItem);
                mList.add(terminalname[midx]);
            }
        } else {
            mList.clear();
            tList.clear();
        }
        // ListViewを表示する
        Context context = actMain.getBaseContext();
        terminalAdapter = new ArrayAdapter(
                context,
                android.R.layout.simple_list_item_checked,
                mList
        );
        //listView = (EnhancedListView) view.findViewById(R.id.terminal_list);
        if (terminalcnt > 0) {
            for(int idx=0; idx < terminalcnt; idx++) {
                listView.setItemChecked(idx, false);
            }
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(terminalAdapter);
        terminalAdapter.notifyDataSetChanged();
        listView.invalidateViews();
    }

    // 新規グループ作成ダイアログクラス
    public static class MakeGroupDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            TextView textView_name = new TextView(getActivity());
            final EditText editText_name = new EditText(getActivity());
            // 文字数制限設定
            InputFilter[] filters_name = new InputFilter[1];
            filters_name[0] = new InputFilter.LengthFilter(32);
            // 表示内容設定
            textView_name.setText(R.string.alert_dialog_add_group_name);
            editText_name.setFilters(filters_name);
            editText_name.setInputType(InputType.TYPE_CLASS_TEXT);
            // Layout作成
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(textView_name);
            linearLayout.addView(editText_name);

            // ダイアログ作成
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setView(linearLayout);
            alertDialog.setTitle(R.string.alert_dialog_add_group);
            // 登録ボタン
            alertDialog.setPositiveButton(R.string.alert_dialog_add_terminal_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 新規グループ作成処理
                    Context context = getActivity().getApplicationContext();
                    Intent intent = new Intent();
                    String name = editText_name.getText().toString();
                    if (name.equals("")) {
                        name = null;
                    }
                    if (name != null) {
                        if (!(groupCommon.searchGroupName(name))) {
                            int ret = queryGroupName(name);
                            if (ret > ConstantGroup.QUERY_STATUS_INVALID) {
                                if (ret == ConstantGroup.QUERY_STATUS_ALREADY) {
                                    intent.setAction(ConstantGroup.ACT_NOTIFY);
                                    intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_GROUPNAME_ALREADY);
                                } else {
                                    if (ret == ConstantGroup.QUERY_STATUS_NOT_FOUND) {
                                        groupCommon.createGroup(name);
                                    } else {
                                        updateGroup(name);
                                        if (!(groupList.findGroupName(name))) {
                                            GroupItem groupItem = new GroupItem();
                                            groupItem.boxname = name;
                                            groupList.addItem(groupItem);
                                        }
                                    }
                                    intent.setAction(ConstantGroup.ACT_REQUEST);
                                    intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST);
                                }
                                groupCommon.saveNewGroupname(name);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        } else {
                            groupCommon.saveNewGroupname(name);
                            intent.setAction(ConstantGroup.ACT_NOTIFY);
                            intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_GROUP_JOINED);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                    dialog.dismiss();
                }
            });
            // 取消ボタン
            alertDialog.setNegativeButton(R.string.alert_dialog_add_terminal_ng, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 何もしない
                    dialog.cancel();
                }
            });
            return alertDialog.create();
        }

        private int queryGroupName(String groupname) {
            int columnUriBoat;
            String uriboat;
            String myuri = chatCommon.getMySIPURI();
            int bret = ConstantGroup.QUERY_STATUS_INVALID;  // Query失敗
            String sipuri = chatCommon.getMySIPURI();
            String selection = "id_box=x'" + groupCommon.toHex(groupname.getBytes()) + "'";
            Context context = getContext();
            Uri uri_tbl = DbDefineShare.BoxMember.CONTENT_URI;
            ContentResolver contentResolver = context.getContentResolver();
            ContentProviderClient resolver = contentResolver.acquireContentProviderClient(uri_tbl);
            if (resolver != null) try {
                Cursor cursor = resolver.query(uri_tbl, null, selection, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        bret = ConstantGroup.QUERY_STATUS_ALREADY;  // 同じグループ名が見つかった。
                        columnUriBoat = cursor.getColumnIndex("uri_boat");
                        cursor.moveToFirst();
                        do {
                            uriboat = cursor.getString(columnUriBoat);
                            if (uriboat.equalsIgnoreCase(myuri)) {
                                bret = ConstantGroup.QUERY_STATUS_JOINED;  // グループに参加済み
                                break;
                            }
                        } while (cursor.moveToNext());
                    } else {
                        bret = ConstantGroup.QUERY_STATUS_NOT_FOUND;  // 同じグループ名が見つからなかった。
                    }
                    cursor.close();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                resolver.release();
            }
            return bret;
        }

        private void updateGroup(String groupname) {
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
            contentValues.put("flag_invalid", 0);
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

}
