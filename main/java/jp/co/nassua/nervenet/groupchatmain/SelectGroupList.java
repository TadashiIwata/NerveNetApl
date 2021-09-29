package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectGroupList {
    public static List<SelectGroupItem> ITEMS = new ArrayList<SelectGroupItem>();
    public static Map<String, SelectGroupItem> ITEM_MAP = new HashMap<String, SelectGroupItem>();

    public static void addItem( SelectGroupItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.groupname, item );
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public String getGroupName() {
        String name = null;
        return name;
    }


    public int getSelectGroupCount() {
        return ITEMS.size();
    }
}
