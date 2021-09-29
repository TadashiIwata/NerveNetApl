package jp.co.nassua.nervenet.groupchatmain;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;
import jp.co.nassua.nervenet.voicemessage.R;

public class GrouplistAdapter extends FragmentPagerAdapter {

    static ActMain actMain = ActMain.actMyself;
    static VoiceMessageCommon voiceMessageCommon;

    String tab1 = actMain.getResources().getString(R.string.group_chat_list1);
    String tab2 = actMain.getResources().getString(R.string.group_chat_list2);
    private CharSequence[] tabTitles = {tab1, tab2};

    public GrouplistAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        switch (position) {
            case VoiceMessageCommon.MAKE_JOIN_LIST:
                // グループチャット
                return new GroupChatMainFragment();
            case VoiceMessageCommon.MAKE_TERMINAL_LIST:
                // 端末管理
                return new TerminalListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

}
