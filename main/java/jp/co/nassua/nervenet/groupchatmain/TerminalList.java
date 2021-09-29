package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalList {
    public static List<TerminalItem> ITEMS = new ArrayList<TerminalItem>();
    public static Map<String, TerminalItem> ITEM_MAP = new HashMap<String, TerminalItem>();

    public static void addItem( TerminalItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.sipuri, item );
    }

    public static void removeItem( TerminalItem item ) {
        boolean flag = false;
        int cnt, idx;
        String name, sipuri;
        cnt = ITEMS.size();
        for(idx=0; idx < cnt; idx++) {
            name = ITEMS.get(idx).name;
            sipuri = ITEMS.get(idx).sipuri;
            if (name.equals(item.name) && sipuri.equals(item.sipuri)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            ITEMS.remove(idx);
        }
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public int getTerminalCount() {
        return ITEMS.size();
    }

    public String getTreminalName(int idx) {
        String name;
        name = ITEMS.get(idx).name;
        return name;
    }

    public String getTreminalSipuri(int idx) {
        String sipuri;
        sipuri = ITEMS.get(idx).sipuri;
        return sipuri;
    }
    // ニックネームが登録されているか検索する。
    public boolean findNickName(String name) {
        int listsz = ITEMS.size();
        String wkmame;
        boolean bret = false;
        for (int i = 0; i < listsz; i++) {
            wkmame = ITEMS.get(i).name;
            if (wkmame.equals(name)) {
                bret = true;
                break;
            }
        }
        return bret;
    }

    // 登録済みの端末か検索する。
    public boolean searchSipUri(String sipuri) {
        boolean bret = false;  // false：未登録端末  true：登録済み端末
        String wksipuri;
        int listsz = ITEMS.size();
        for (int i = 0; i < listsz; i++) {
            wksipuri = ITEMS.get(i).sipuri;
            if (sipuri.equals(wksipuri)) {
                bret = true;
                break;
            }
        }
        return bret;
    }
}
