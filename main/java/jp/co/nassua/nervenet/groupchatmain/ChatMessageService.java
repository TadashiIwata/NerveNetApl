package jp.co.nassua.nervenet.groupchatmain;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.validation.Validator;

import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class ChatMessageService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    public static ChatMessageService actMyself = null;  //プロセス上の単一インスタンス
    private static ActMain actMain;
    private static ChatCommon chatCommon;
    private static GroupCommon groupCommon;
    private static GroupList groupList;
    private static GroupItem groupItem;
    public static ChatBoxShare chatBoxShare;
    private static VoiceMessageCommon voiceMessageCommon;
    private static CursorLoader cursorLoader;
    private static Context loader_context;
    private static Long currentTime = Long.valueOf(0);
    private static boolean startForeground = false;

    // XML関連
    private XmlPullParser xmlPullParser;

    // Vibrator関連
    private static Vibrator vibrator;

    public ChatMessageService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!startForeground) {
                Notification notification = new Notification();
                Intent intent1 = new Intent(getApplicationContext(), ActMain.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                notification = builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setTicker("")
                        .setAutoCancel(true).setContentTitle("VoiceMessage")
                        .setContentText("ChatMessageService").build();
                startForeground(2, notification);
                startForeground = true;
            }
        }
        actMyself = this;
        if (actMain == null) {
            actMain = new ActMain();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        if (chatBoxShare == null) {
            chatBoxShare = new ChatBoxShare();
        }
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupItem == null) {
            groupItem = new GroupItem();
        }
        // Chat Message用
        if (cursorLoader == null) {
            cursorLoader = createLoader();
        }
        if (currentTime < 1) {
            /*
            currentTime = System.currentTimeMillis() - chatCommon.getLimitTime();
            chatCommon.setChatCurrentTime(currentTime);
            */
            currentTime = System.currentTimeMillis();
            chatCommon.setChatCurrentTime(currentTime);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        long pattern[] = {0,1000, 400, 200, 400, 200 };
        long pattern2[] = {300, 300};
        int vibrate[] = {0, DEFAULT_AMPLITUDE};
        String boxList = null;
        boxList = makeBoxListFromDB();  // BoxMemberから自局が参加しているグループのリストを作る。
        if (boxList == null) {
            makeBoxListFromXML();  // BoxMemberからグループのリストが作成できない時は、XMLから作成する。
        }

        Context context = loader_context;
        String boxName;
        //if (gname != null) {
        if (boxList != null) {
            Uri uri = DbDefineShare.BoxShare.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentProviderClient client = resolver.acquireContentProviderClient(uri);
            //String selection = ( "( id_box = x'" + toHex(gname.getBytes()) + "') and ( flag_invalid = 0 )");
            String selection = ( boxList + " and ( flag_invalid = 0 )");
            String body = null;
            String uri_attached = null;
            if (client != null) {
                try {
                    Log.i("nassua", "ChatMessageService: Select = " + selection);
                    Cursor cursor = client.query(uri, null, selection, null, null);
                    //Cursor cursor = client.query(uri, null, null, null, null);
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            // 振動させる。
                            if (vibrator == null) {
                                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern2, vibrate, -1);
                                vibrator.vibrate(vibrationEffect);
                            } else {
                                vibrator.vibrate(pattern, -1);
                            }
                            cursor.moveToFirst();
                            do {
                                DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
                                rec.setFromQuery(cursor);
                                try {
                                    ChatBoxShare chatBoxShare = ChatBoxShare.newInstance(rec);
                                    byte[] wkrecordId = chatBoxShare.idBox;
                                    int idlen = wkrecordId.length - 8;
                                    byte[] wkboatId = new byte[32];
                                    System.arraycopy(wkrecordId, 0, wkboatId, 0, idlen);
                                    String wkdata = new String(wkboatId).substring(0, idlen);
                                    boxName = chatBoxShare.boxName;
                                    //if (boxName.equalsIgnoreCase(groupCommon.getCurrentGroupName())) {
                                    //if (boxName.equalsIgnoreCase(groupCommon.getDefaultGroupName())) {
                                    {
                                        ChatItem chatItem = new ChatItem();  // この外で定義してはいけない。　外に出すと最終のデータで全て上書きされる。
                                        chatItem.id = wkdata;
                                        chatItem.time = chatBoxShare.timeCalibrate;
                                        String wkMassage = chatBoxShare.MessageInfo;
                                        String decBase64 = new String(Base64.decode(wkMassage, Base64.DEFAULT));
                                        int idx = decBase64.indexOf("@@");
                                        chatItem.name = decBase64.substring(0, idx);
                                        idx = idx + 2;
                                        chatItem.comment = decBase64.substring(idx);
                                        if (chatBoxShare.uriAttached != null) {
                                            // 画像ファイル有り。
                                            chatItem.fileuri = Uri.parse(chatBoxShare.uriAttached);
                                        }
                                        // ここで廃棄時刻を書き換える？
                                        chatCommon.updateTimeDiscard(context, chatBoxShare);
                                        // 自分が送ったメッセージは表示しない様に修正する。
                                        String MyUriBoat = chatCommon.getMySIPURI();
                                        String sendUriBoat = rec.mUriBoat;
                                        if (MyUriBoat == null) {
                                            showChatMessage(chatItem, boxName);
                                        } else if (!(MyUriBoat.equals(sendUriBoat))) {
                                            showChatMessage(chatItem, boxName);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.i("nassua", "ChatBoxSAhare fromDB record Exception");
                                }
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.release();
                }
            }
        }
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!startForeground) {
                Notification notification = new Notification();
                Intent intent1 = new Intent(getApplicationContext(), ActMain.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                notification = builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setTicker("")
                        .setAutoCancel(true).setContentTitle("VoiceMessage")
                        .setContentText("ChatMessageService").build();
                startForeground(2, notification);
                startForeground = true;
            }
        }
        actMyself = this;
        //CursorLoader初期化
        if (cursorLoader == null) {
            cursorLoader = createLoader();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (!groupCommon.getChatServiceStatus()) {
            // サービス開始の状態を設定する。
            groupCommon.setChatServiceStatus(true);
        }
        if (currentTime < 1) {
            currentTime = System.currentTimeMillis() - chatCommon.getLimitTime();  // 読み出すメッセージの有効範囲
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyLoader();
    }

    // CursorLoader作成
    private CursorLoader createLoader() {
        Context context = getApplicationContext();
        // 取りあえずデフォルトのグループ名にする。
        // BoxMasterなどから取ってくるのが本当。
        String gruupname = groupCommon.getDefaultGroupName();

        CursorLoader loader = new CursorLoader(context);
        loader.setUri(DbDefineShare.BoxShare.CONTENT_URI);

        byte[] boxName;
        StringWriter sw = new StringWriter();

        boxName = gruupname.getBytes();
        sw.write("(");
        //無効フラグ
        sw.write(DbDefineShare.Common.whereValid(System.currentTimeMillis(), 0));
        //論理積
        //sw.write(")AND(");
        //ボックスID
        //sw.write("id_box=x'");
        //sw.write(toHex(boxName));
        //sw.write("')");
        sw.write(")");
        loader.setSelection(sw.toString());

        String[] projection = {};
        loader.setProjection(projection);
        loader.registerListener(0, this);
        loader.startLoading();

        loader_context = context;
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }

        return loader;
    }

    private String toHex(byte[] data) {
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        for (byte d : data) {
            stringBuilder.append(String.format("%02X", d));
        }
        str = stringBuilder.toString();
        return str;
    }

    // CursorLoader破棄
    public void destroyLoader() {
        if (cursorLoader != null) {
            cursorLoader.stopLoading();
            cursorLoader.unregisterListener(this);
            cursorLoader = null;
        }
    }

    public void showChatMessage(ChatItem chatItem, String boxName) {
        String date;
        String displayMessage;

        // 過去メッセージ、画像のみのメッセージは表示しない。
        long wktime = chatItem.time;
        int wklen = chatItem.comment.length();
        currentTime = chatCommon.getChatCurrentTime();
        if ((currentTime < chatItem.time) && (chatItem.comment.length() > 0)) {
            displayMessage = chatCommon.getTimeStr(chatItem.time) + "\n";
            // 取りあえずデフォルトのグループ名を出す。
            //displayMessage = displayMessage + groupCommon.getDefaultGroupName() + "\n";
            displayMessage = displayMessage + boxName + "\n";
            displayMessage = displayMessage + chatItem.name + "\n";
            displayMessage = displayMessage + chatItem.comment;
            // 画面中央に表示する
            final Toast toast = Toast.makeText(this, displayMessage, Toast.LENGTH_LONG);
            new FrameLayout(this) {
                {
                    addView(toast.getView());
                    toast.setView(this);
                }
                @Override
                public void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    if (vibrator != null) {
                        vibrator.cancel();
                    }
                }

            };
            //Toast toast = Toast.makeText(this, "Test", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            chatCommon.setChatCurrentTime(chatItem.time);
        }

    }

    private String makeBoxListFromXML() {
        // XMLをパースしてグループリストを作成する。
        String boxList = null;
        boolean notfound = true;
        boolean groupListFlag = false;
        boolean groupFlag = false;
        String groupName = null;
        // XMLを読み込んでグループリストを作成する。
        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + groupCommon.VM_Group_Terminal_List);
        try (InputStream inputs = new FileInputStream(vmfile)) {
            xmlPullParser = Xml.newPullParser();
            try {
                xmlPullParser.setInput(inputs, "UTF-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != xmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xmlPullParser.getName().equalsIgnoreCase("grouplist")) {
                                // タグが見つかったら初期化して作り直す。
                                groupListFlag = true;
                                eventType = xmlPullParser.next();
                                continue;
                            }
                            if (groupListFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    groupFlag = true;
                                }
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (groupFlag) {
                                groupName = xmlPullParser.getText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (groupFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    if (boxList == null) {
                                        boxList = "( id_box in (x'" + toHex(groupName.getBytes()) + "'";
                                        notfound = false;
                                    } else {
                                        boxList = boxList + ", ";
                                        boxList = boxList + "x'" + toHex(groupName.getBytes()) + "'";
                                    }
                                    groupFlag = false;
                                }
                            }
                    }
                    eventType = xmlPullParser.next();
                }
                boxList = boxList + "))";
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            xmlPullParser = null;
        }
        if (notfound) {
            // "NerveNetChat"のみ
            boxList = "( id_box = '" + toHex(groupCommon.getDefaultGroupName().getBytes()) + "')";
        }
        return boxList;
    }

    private String makeBoxListFromDB() {
        // BoxMemberから自局が参加しているグループを抽出してグループリストを作る。
        int columnIdBox;
        int columsDiscardTime;
        String boxList = null;
        boolean notfound = true;
        String groupName = null;
        String sipuri;

        groupCommon.readBoatId(getApplicationContext());
        sipuri = chatCommon.getMySIPURI();
        if (sipuri != null) {
            Uri uri = DbDefineShare.BoxMember.CONTENT_URI;
            ContentResolver resolver = getContentResolver();
            ContentProviderClient client = resolver.acquireContentProviderClient(uri);
            String selection = ( "(uri_boat='" + sipuri + "') AND (flag_invalid=0)" );

            if (client != null) {
                try {  // 参加中のグループを抽出する。
                    Cursor cursor = client.query(uri, null, selection, null, null);
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            long nowDate = System.currentTimeMillis();
                            long discatdTime;
                            columnIdBox = cursor.getColumnIndex("id_box");
                            columsDiscardTime = cursor.getColumnIndex("time_discard");
                            cursor.moveToFirst();
                            do {
                                byte[] idBox;
                                idBox = cursor.getBlob(columnIdBox);
                                groupName = new String(idBox);
                                discatdTime = cursor.getLong(columsDiscardTime);
                                if (nowDate < discatdTime) {
                                    if (boxList == null) {
                                        boxList = "( id_box in (x'" + toHex(groupCommon.getDefaultGroupName().getBytes()) + "', " + "x'" + toHex(groupName.getBytes()) + "'";
                                        notfound = false;
                                    } else {
                                        boxList = boxList + ", ";
                                        boxList = boxList + "x'" + toHex(groupName.getBytes()) + "'";
                                    }
                                }
                            } while (cursor.moveToNext());
                        }
                        if (boxList != null) {
                            boxList = boxList + "))";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.release();
                }
            }
        }
        return boxList;
    }

}
