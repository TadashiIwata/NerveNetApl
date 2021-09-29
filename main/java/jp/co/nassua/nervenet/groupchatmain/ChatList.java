package jp.co.nassua.nervenet.groupchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatList {

    public static List<ChatItem> ITEMS = new ArrayList<ChatItem>();
    public static Map<String, ChatItem> ITEM_MAP = new HashMap<String, ChatItem>();

    public static void addItem( ChatItem item ) {
        ITEMS.add( item );
        ITEM_MAP.put( item.id, item );
    }

    public static void clearAll() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public int getGroupCount() {
        return ITEMS.size();
    }
}
