package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupList {

    public static List<GroupItem> ITEMS = new ArrayList<GroupItem>();
    public static Map<String, GroupItem> ITEM_MAP = new HashMap<String, GroupItem>();

    public static void addItem( GroupItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.boxname, item );
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public int getGroupCount() {
        return ITEMS.size();
    }

    // グループ名が登録されているか検索する。
    public boolean findGroupName(String name) {
        int listsz = ITEMS.size();
        String wkmame;
        boolean bret = false;
        for (int i = 0; i < listsz; i++) {
            wkmame = ITEMS.get(i).boxname;
            if (wkmame.equals(name)) {
                bret = true;
                break;
            }
        }
        return bret;
    }

    public GroupItem getGroupItem(int idx) {
        GroupItem groupItem = new GroupItem();
        groupItem = ITEMS.get(idx);
        return groupItem;
    }

    public void removeItem(String gname) {
        boolean flag = false;
        int cnt, idx;
        String name;
        cnt = ITEMS.size();
        for(idx=0; idx < cnt; idx++) {
            name = ITEMS.get(idx).boxname;
            if (name.equals(gname)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            ITEMS.remove(idx);
        }
    }
}
