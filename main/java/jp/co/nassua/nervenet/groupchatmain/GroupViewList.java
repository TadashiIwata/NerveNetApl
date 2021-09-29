package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupViewList {
    public static List<GroupViewItem> ITEMS = new ArrayList<GroupViewItem>();
    public static Map<String, GroupViewItem> ITEM_MAP = new HashMap<String, GroupViewItem>();

    public static void addItem( GroupViewItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.groupname, item );
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public String getGroupName(int idx) {
        String name = null;
        name = ITEMS.get(idx).groupname;
        return name;
    }

    public int getGroupViewListCount() {
        return ITEMS.size();
    }


    public int getSelectGroupCount() {
        return ITEMS.size();
    }

}
