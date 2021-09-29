package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectTerminalList {
    public static List<SelectTerminalItem> ITEMS = new ArrayList<SelectTerminalItem>();
    public static Map<String, SelectTerminalItem> ITEM_MAP = new HashMap<String, SelectTerminalItem>();

    public static void addItem( SelectTerminalItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.sipuri, item );
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public int getSelectTerminalCount() {
        return ITEMS.size();
    }

    public SelectTerminalItem getItem(int idx) {
        SelectTerminalItem item;
        item = ITEMS.get(idx);
        return item;
    }

}
