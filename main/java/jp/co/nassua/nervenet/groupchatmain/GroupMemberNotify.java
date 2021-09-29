package jp.co.nassua.nervenet.groupchatmain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class GroupMemberNotify  extends BroadcastReceiver {

    private static GroupCommon groupCommon;
    private static GroupChatMainFragment groupChatMainFragment;
    private static TerminalListFragment terminalListFragment;
    private static GroupChatMain groupChatMain;
    private static UnjoinedTerminalLists unjoinedTerminalLists;
    private static UpdateTerminalInfomation updateTerminalInfomation;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        String cmd;
        Cursor cursor;

        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (groupChatMainFragment == null) {
            groupChatMainFragment = new GroupChatMainFragment();
        }
        if (terminalListFragment == null) {
            terminalListFragment = new TerminalListFragment();
        }
        if (groupChatMain == null) {
            groupChatMain = new GroupChatMain();
        }
        if (unjoinedTerminalLists == null) {
            unjoinedTerminalLists = new UnjoinedTerminalLists();
        }
        if (updateTerminalInfomation == null) {
            updateTerminalInfomation = new UpdateTerminalInfomation();
        }

        cursor = groupCommon.getBoxMembercursor();
        if (action.equalsIgnoreCase(ConstantGroup.ACT_NOTIFY)) {
            if (extras != null) {
                cmd = extras.getString(ConstantGroup.EXTRA_EVENT);
                // Load完了通知
                if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_LOAD_COMPLETE)) {
                    // チャット名が登録されていない時は無視する。
                    if (groupCommon.readConf()) {
                        groupCommon.addGroupFromDB(cursor);
                    }
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_GROUP_JOINED)) {
                    // 参加済みグループ名
                    groupChatMain.addSuccessMessage(ConstantGroup.EVENT_GROUP_JOINED);
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_GROUPNAME_ALREADY)) {
                    // 登録済みのグループ名
                    groupChatMain.addSuccessMessage(ConstantGroup.EVENT_GROUPNAME_ALREADY);
                }
            }
        } else if (action.equalsIgnoreCase(ConstantGroup.ACT_REQUEST)) {
            if (extras != null) {
                cmd = extras.getString(ConstantGroup.EXTRA_EVENT);
                if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST)) {
                    // グループリスト画面更新要求 + 通知
                    groupChatMain.addSuccessMessage(ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST);
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST2)) {
                    // グループリスト画面更新要求
                    groupChatMain.refreshGrouplist();
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_LOAD_REFLESH_TERMINALLIST)) {
                    // 端末リスト画面更新要求
                    terminalListFragment.refleshTerminalList();
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_LOAD_REFLESH_TERMINALLIST2)) {
                    // 端末リスト画面更新要求
                    terminalListFragment.refleshTerminalList();
                    // グループ未参加端末リスト画面更新要求
                    unjoinedTerminalLists.addNreTerminal();
                } else if (cmd.equalsIgnoreCase(ConstantGroup.EVENT_UPDATE_TERMINAL)) {
                    // XML更新
                    updateTerminalInfomation.UpdateXml();
                    // ターミナルリストから削除する
                    String name = updateTerminalInfomation.get().user;
                    String sipuri = updateTerminalInfomation.get().sipuri;
                    groupCommon.removeUserForTerminalList(name, sipuri);
                    // 端末リスト画面更新要求
                    terminalListFragment.refleshTerminalList();
                }
            }
        }
    }
}
