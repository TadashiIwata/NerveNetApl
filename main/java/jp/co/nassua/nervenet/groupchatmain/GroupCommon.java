package jp.co.nassua.nervenet.groupchatmain;


import android.app.NotificationChannelGroup;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.RangeValueIterator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CpuUsageInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.DTDHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicemessage.R;

public class GroupCommon {

    static ActMain actMain = ActMain.actMyself;
    private static ChatCommon chatCommon;
    private static ContentChat contentChat;
    private static GroupList groupList;
    //private static GroupItem groupItem;
    private static TerminalList terminalList;
    private static SelectTerminalList selectTerminalList;
    //private static SelectTerminalItem selectTerminalItem;
    private static AddTerminalToGroup addTerminalToGroup;
    private static UpdateTerminalInfomation updateTerminalInfomation;

    private static String currentGroupName;
    private static String newGroupName;
    private static String selectedGroupname;
    private static boolean execChatService = false;
    AlertDialog.Builder alertDialog;
    private final Handler alertHandle = new Handler();
    // 環境ファイル
    private File envFile;
    public static final String envFilename = "voicemessage.conf";
    public String envfilename;
    public static final String VM_Group_Terminal_List = "VM_Group_Terminal_List.xml";

    // 既定値
    private static final String sMsgCount = "MessageCount=20";
    private static final String sQuality = "ImageQuality=50";
    private static final String sMaxSize = "ImageSize=1000000";
    private static final String sSendRatio = "ImageSendRatio=50";
    private static final String sDisPlayRatio = "ImageDisplayRatio=50";
    private static final String sAutoReceive = "AutoReceive=Yes";
    private static final String DEFALUT_GROUP_NAME = "NerveNetChat";

    private XmlPullParser xmlPullParser;
    private static final int ADD_ELEMENT_TYPE_TERMINAL = 0;
    private static final int ADD_ELEMENT_TYPE_GROUP = ADD_ELEMENT_TYPE_TERMINAL + 1;

    private GroupBoxMember groupBoxMember;
    private static ContentProviderClient contentProviderClient;
    //公式名
    private static final String AUTHORITY = "jp.co.nassua.nervenet.share";
    private static final String PATH = "boxmember";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    private static Cursor BoxMembercursor;

    private static String qrcodeSipUri;

    public GroupCommon() {
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (terminalList == null) {
            terminalList = new TerminalList();
        }
        if (selectTerminalList == null) {
            selectTerminalList = new SelectTerminalList();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (addTerminalToGroup == null) {
            addTerminalToGroup = new AddTerminalToGroup();
        }
        if (updateTerminalInfomation == null) {
            updateTerminalInfomation = new UpdateTerminalInfomation();
        }

    }

    public void addGroupList(String groupName, String sipuri, String nickname) {

        GroupItem groupItem = new GroupItem();
        //groupItem.id = groupId;
        if (!(searchGroupName(DEFALUT_GROUP_NAME))) {
            // NERVENET CHAT が無ければ登録しておく。
            GroupItem groupItem1 = new GroupItem();
            groupItem1.boxname = DEFALUT_GROUP_NAME;
            groupList.addItem(groupItem1);
        }
        if (groupName != null) {
            if (!(groupName.equals(DEFALUT_GROUP_NAME))) {
                groupItem.boxname = groupName;
                groupList.addItem(groupItem);
            }
        }
    }

    public boolean searchGroupName(String groupName) {
        if (groupList != null) {
            return groupList.findGroupName(groupName);
        }
        return false;
    }

    public void setChatGroupName(String name) {
        currentGroupName = name;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }

    public boolean readConf() {
        boolean bret = false;
        boolean rflag = false;
        boolean nameFlag = false;
        boolean wkflag = false;
        int paramCheck = 0;
        String SaveBuff = null;
        String MyName = null;

        String wkMsgCount = null;
        String wkQuality = null;
        String wkMaxSize = null;
        String wkSendRatio = null;
        String wkDisPlayRatio = null;
        String wkAutoRecv = null;

        envfilename = Environment.getExternalStorageDirectory() + "/" + envFilename;
        envFile = new File(envfilename);
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (envFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(envFile);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufread = new BufferedReader(isr);
                String Param, Values;
                int sidx, eidx;
                while ((Param = bufread.readLine()) != null) {
                    if (!rflag) {
                        if (Param.toLowerCase().indexOf("[group]") != -1) {
                            rflag = true;
                            paramCheck = 1;
                            continue;
                        }
                    } else {
                        if (Param.toLowerCase().indexOf("chatname") != -1) {
                            nameFlag = true;
                            sidx = Param.toLowerCase().indexOf("chatname=") + 9;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setMyChatName(Values);
                            MyName = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("messagecount") != -1) {
                            sidx = Param.toLowerCase().indexOf("messagecount=") + 13;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setMaxMessageCount(Integer.parseInt(Values));
                            paramCheck = paramCheck + 2;
                            wkMsgCount = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("imagequality") != -1) {
                            sidx = Param.toLowerCase().indexOf("imagequality=") + 13;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setImageQuality(Integer.parseInt(Values));
                            paramCheck = paramCheck + 4;
                            wkQuality = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("imagesize") != -1) {
                            sidx = Param.toLowerCase().indexOf("imagesize=") + 10;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setImageMaxSize(Long.parseLong(Values));
                            paramCheck = paramCheck + 8;
                            wkMaxSize = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("imagesendratio") != -1) {
                            sidx = Param.toLowerCase().indexOf("imagesendratio=") + 15;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setImageSendRatio(Integer.parseInt(Values));
                            paramCheck = paramCheck + 16;
                            wkSendRatio = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("imagedisplayratio") != -1) {
                            sidx = Param.toLowerCase().indexOf("imagedisplayratio=") + 18;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            chatCommon.setImageDisplayRatio(Integer.parseInt(Values));
                            paramCheck = paramCheck + 32;
                            wkDisPlayRatio = Param;
                            continue;
                        }
                        if (Param.toLowerCase().indexOf("autoreceive") != -1) {
                            sidx = Param.toLowerCase().indexOf("autoreceive=") + 12;
                            eidx = Param.length();
                            Values = Param.substring(sidx, eidx);
                            if (Values.equalsIgnoreCase("yes")) {
                                chatCommon.setAutoReceive(true);
                            } else {
                                chatCommon.setAutoReceive(false);
                            }
                            paramCheck = paramCheck + 64;
                            wkAutoRecv = Param;
                            continue;
                        }
                        /* この下にオプションチェックを追加したら
                           OPTION_CHECK_VALUE の値も修正すること。
                           次の paramCheckの値は 128。
                           OPTION_CHECK_VALUE は 256 - 1 にする。
                           また、下の既定値の書き込みも追加する。
                         */
                    }
                    if (!wkflag) {
                        SaveBuff = Param + "\n";
                        wkflag = true;
                    } else {
                        SaveBuff = SaveBuff + Param + "\n";
                    }                }
                bufread.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if ((rflag) && (nameFlag)) {
                bret = true;
            }

            final int OPTION_CHECK_VALUE = 128 - 1;
            if (paramCheck != OPTION_CHECK_VALUE) {
                try {
                    envFile.delete();
                    envFile.createNewFile();
                } catch (IOException e) {
                    Log.i("nassua", "Configuration file not created.");
                }
                // ファイルに既定値を書き込む
                try {
                    FileOutputStream fos = new FileOutputStream(envFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    PrintWriter bufwrite = new PrintWriter(osw);
                    // VoiceMessage設定書き込み。
                    bufwrite.println(SaveBuff);
                    // Chat設定書き込み
                    bufwrite.println("[GROUP]");
                    if (MyName != null) {
                        bufwrite.println(MyName);
                    }
                    if ((paramCheck & 2) == 2) {
                        bufwrite.println(wkMsgCount);
                    } else {
                        bufwrite.println(sMsgCount);
                    }
                    if ((paramCheck & 4) == 4) {
                        bufwrite.println(wkQuality);
                    } else {
                        bufwrite.println(sQuality);
                    }
                    if ((paramCheck & 8) == 8) {
                        bufwrite.println(wkMaxSize);
                    } else {
                        bufwrite.println(sMaxSize);
                    }
                    if ((paramCheck & 16) == 16) {
                        bufwrite.println(wkSendRatio);
                    } else {
                        bufwrite.println(sSendRatio);
                    }
                    if ((paramCheck & 32) == 32) {
                        bufwrite.println(wkDisPlayRatio);
                    } else {
                        bufwrite.println(sDisPlayRatio);
                    }
                    if ((paramCheck & 64) == 64) {
                        bufwrite.println(wkAutoRecv);
                    } else {
                        bufwrite.println(sAutoReceive);
                        chatCommon.setAutoReceive(true);
                    }
                    bufwrite.close();
                } catch (Exception e) {
                    Log.i("nassua", "Configration file write error.");
                }
            }
        }
        return bret;
    }

    public void alertMessage(Context context, final int alerttype) {
        String alertMessage = null;

        alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
        if (alerttype == contentChat.ALERT_NICKNAME_NOT_REGISTRATION) {
            alertDialog.setTitle(R.string.setting_chat_name);
            alertMessage = context.getResources().getString(R.string.undefine_chat_name);
        } else if (alerttype == contentChat.ALERT_NOT_SUPPORTED_CAMERA) {
            alertDialog.setTitle(R.string.alert_camera_title);
            alertMessage = context.getResources().getString(R.string.alert_camera_not_supported_message);
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alerttype == contentChat.ALERT_ADD_TERMINAL_SUCCESS) {
                    Context context1 = addTerminalToGroup.getApplicationContext();
                    // チャットメイン画面位戻る。
                    Intent intent = new Intent(context1, AddTerminalToGroup.class);
                    context1.startActivity(intent);
                }
            }
        });
        alertDialog.create();
        alertHandle.post(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    public void setChatServiceStatus(boolean flag) {
        execChatService = flag;
    }

    public boolean getChatServiceStatus() {
        return execChatService;
    }

    public String getDefaultGroupName() {
        return DEFALUT_GROUP_NAME;
    }

    public void readBoatId(Context context) {
        //端末状態 取得
        DbDefineBoat.ConfNode conf_node = new DbDefineBoat.ConfNode();
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        ContentProviderClient resolver = context.getContentResolver().acquireContentProviderClient(uri_tbl);
        if (resolver != null) try {
            Cursor cursor = resolver.query(uri_tbl, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    conf_node.setFromQuery(cursor);
                    if ((conf_node != null) && (conf_node.mIdBoat != null))  {
                        chatCommon.setMyBoatId(conf_node.mIdBoat);
                        chatCommon.setMySIPURI(conf_node.mUriBoat);
                    }
                }
                cursor.close();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            resolver.release();
        }
    }

    public int ParseTerminalList() {
        boolean notfound = false;
        boolean terminalListFlag = false;
        boolean terminalFlag = false;
        int listcnt = 0;
        String terminalSipUri = null;
        String terminalName = null;
        // XMLを読み込んで端末リストを作成する。
        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + VM_Group_Terminal_List);
        try (InputStream inputs = new FileInputStream(vmfile)) {
            xmlPullParser = Xml.newPullParser();
            try {
                xmlPullParser.setInput(inputs, "UTF-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != xmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xmlPullParser.getName().equalsIgnoreCase("terminallist")) {
                                // タグが見つかったら初期化して作り直す。
                                terminalList.clearAll();
                                terminalListFlag = true;
                                eventType = xmlPullParser.next();
                                continue;
                            }
                            if (terminalListFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("name")) {
                                    terminalFlag = true;
                                    terminalSipUri = xmlPullParser.getAttributeValue(0);
                                }
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (terminalFlag) {
                                terminalName = xmlPullParser.getText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (terminalFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("name")) {
                                    terminalFlag = false;
                                    TerminalItem terminalItem = new TerminalItem();
                                    terminalItem.sipuri = terminalSipUri;
                                    terminalItem.name = terminalName;
                                    terminalList.addItem(terminalItem);
                                    listcnt = terminalList.getTerminalCount();
                                }
                            }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // ファイルを作成するして自端末を登録する。
            notfound = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            xmlPullParser = null;
            vmfile = null;
        }
        if (notfound) {
            // XMLファイルを作成する。
            makeXML(chatCommon.getMySIPURI());
        }
        return listcnt;
    }

    private void makeXML(String mySipuri) {

        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        DocumentBuilderFactory vmxml = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder vmxmlbuilder = vmxml.newDocumentBuilder();
            Document document = vmxmlbuilder.newDocument();
            // ルートノードを作成する。
            Element root = document.createElement("terminalinfornation");
            // 親ノード <myterminal> を作成する。
            Element myterminal = document.createElement("myterminal");
            // 要素 <name> を作成する。
            Element name = null;
            Text text = null;
            String  nickname = chatCommon.getMyChatName();
            if (nickname != null) {
                name = document.createElement("name");
                text = document.createTextNode(nickname);
                name.appendChild(text);
            }
            // 要素 <sipuri> を作成する。
            Element sipuri = document.createElement("sipuri");
            text = document.createTextNode(chatCommon.getMySIPURI());
            sipuri.appendChild(text);
            // 親ノードへ追加する。
            if (nickname != null) {
                myterminal.appendChild(name);
            }
            myterminal.appendChild(sipuri);
            // ルートに親ノード <myterminal> を追加する。
            root.appendChild(myterminal);

            // boxmemberをサーチして自端末が登録されているグループリストを作成する。



            // 出来あがった xml を出力する。
            document.appendChild(root);
            TransformerFactory tffactory = TransformerFactory.newInstance();
            Transformer transformer = tffactory.newTransformer();
            File vmdir = new File(path);
            if (!vmdir.exists()) {
                vmdir.mkdirs();
            }
            File vmfile = new File(path + VM_Group_Terminal_List);
            if (!vmfile.exists()) {
                vmfile.createNewFile();
            }
            transformer.transform(new DOMSource(document), new StreamResult(vmfile));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int ParseGroupList() {
        boolean GroupListFlag = false;
        boolean GroupFlag = false;
        boolean MemberFlag = false;
        boolean addFlag = false;
        int listcnt = 0;
        String GroupBoxName = null;
        // XMLを読んでグループリストを作成する。
        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + VM_Group_Terminal_List);
        try (InputStream inputs = new FileInputStream(vmfile)) {
            xmlPullParser = Xml.newPullParser();
            try {
                xmlPullParser.setInput(inputs, "UTF-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != xmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xmlPullParser.getName().equalsIgnoreCase("grouplist")) {
                                // grouplistが見つかったら初期化して作り直す。
                                GroupList.clearAll();
                                // デフォルトグループを追加する。
                                addGroupList(getDefaultGroupName(), null, null);
                                GroupListFlag = true;
                                eventType = xmlPullParser.next();
                                continue;
                            }
                            if (GroupListFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    GroupFlag = true;
                                }
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (GroupFlag) {
                                // グループ名取得
                                GroupBoxName = xmlPullParser.getText();
                                listcnt++;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (GroupFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    GroupFlag = false;
                                    GroupItem groupItem = new GroupItem();
                                    groupItem.boxname = GroupBoxName;
                                    GroupList.addItem(groupItem);
                                }
                            }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            xmlPullParser = null;
            vmfile = null;
        }
        return listcnt;
    }

    public void addTerminal(String terminalsipuri, String terminalname) {
        if (!terminalList.searchSipUri(terminalsipuri)) {  // 未登録の端末かチェックする。
            addItemToXml(ADD_ELEMENT_TYPE_TERMINAL, terminalsipuri, terminalname);
            TerminalItem terminalItem = new TerminalItem();
            terminalItem.sipuri = terminalsipuri;
            terminalItem.name = terminalname;
            terminalList.addItem(terminalItem);
        }
    }

    public String createGroup(String gname) {
        String wkgname = null;

        Context context = actMain.getBaseContext();
        int listcnt = selectTerminalList.getSelectTerminalCount();

        String nickname = null;
        String sipuri = null;
        Uri result;
        Uri uri = null;
        ContentResolver contentResolver1 = null;

        if (uri == null) {
            uri = DbDefineShare.BoxMember.CONTENT_URI;
        }
        if ( contentResolver1 == null) {
            contentResolver1 = context.getContentResolver();
        }
        if (contentProviderClient == null) {
            contentProviderClient = contentResolver1.acquireContentProviderClient(uri);
        }

        for(int idx=-1; idx < listcnt; idx++) {
            if (idx < 0 ) {
                // 自局の情報を設定する。
                nickname = chatCommon.getMyChatName();
                sipuri = chatCommon.getMySIPURI();
            } else {
                // 選択された端末の情報を取り出す。
                nickname = selectTerminalList.ITEMS.get(idx).name;
                sipuri = selectTerminalList.ITEMS.get(idx).sipuri;
            }
            // boxmemberにレコードを追加する。
            // 値を設定していく。
            //String strBortId = bin2hex(chatCommon.getMyBoatId());
            // 注意事項：レコードIDは自局のSIPURIではなくメンバーの URIを使用する。
            // 自局の SIPURIを使うと同じレコードIDになり insert出来ないことがある。
            String strBortId = new String(chatCommon.getMyBoatId());
            String recordId = strBortId + getNowDayTimeStr();
            long timeUpdate  = System.currentTimeMillis();
            long timeDiscard = System.currentTimeMillis() + chatCommon.getGroupLimitTime();
            if (gname == null) {
                // グループ自動生成
                gname = makeGroupName();
            }
            // GroupBoxMember
            GroupBoxMember.idMyself = chatCommon.getMyBoatId();
            GroupBoxMember.uriMember = sipuri;
            GroupBoxMember.idBox = recordId.getBytes();
            // Common
            groupBoxMember = GroupBoxMember.newInstance(chatCommon.getMySIPURI());
            groupBoxMember.timeUpdate = timeUpdate;
            groupBoxMember.timeDiscard = timeDiscard;
            groupBoxMember.flagInvalid = 0;
            // Boxmember
            groupBoxMember.authority = groupBoxMember.AUTHORITY_SETTING_ALL;
            groupBoxMember.idRecord = recordId.getBytes();
            groupBoxMember.boxName = gname;
            groupBoxMember.name = nickname;
            DbDefineShare.BoxMember rec = groupBoxMember.toRecord();
            rec.mCommon.mNodeUpdate = chatCommon.getMyBoatId();
            rec.mCommon.mTimeDiscard = timeDiscard;
            rec.mCommon.mTimeUpdate = timeUpdate;

            int ret = 0;
            if (contentProviderClient != null) {
                try {
                    result = contentProviderClient.insert(uri, rec.getForInsert());
                    String strResult = result.getLastPathSegment();
                    if (strResult != null) {
                        switch (Integer.parseInt(strResult)) {
                            case -1:
                                // エラー
                                ret = -1;
                                break;
                            default:
                                // 追加成功
                                ret = 1;
                                break;
                        }
                    } else {
                        ret = 2;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    //contentProviderClient.release();
                }
                if (ret == 1) {
                    wkgname = gname;
                    Intent intent = new Intent(ConstantShare.ACT_PUBLISH);
                    Bundle extras = new Bundle();
                    extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
                    extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxMember.PATH);
                    extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, recordId.getBytes());
                    intent.putExtras(extras);
                    context.sendBroadcast(intent);
                } else {
                    ret = 5;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (contentProviderClient != null) {
            contentProviderClient.release();
            contentProviderClient = null;
        }
        addItemToXml(ADD_ELEMENT_TYPE_GROUP, gname, null);
        int groupcnt = groupList.getGroupCount();
        if (groupcnt < 1) {
            addGroupList(DEFALUT_GROUP_NAME, null, null);
        }
        addGroupList(gname, null, null);
        // グループを追加したら選択リストを初期化する。
        selectTerminalList.clearAll();
        return wkgname;
    }

    public void addGroupFromDB(Cursor cursor) {
        int columnIdBox;
        int columnUriBoat;
        String myuri = chatCommon.getMySIPURI();
        boolean refreshFlag = false;

        int groupcnt = cursor.getCount();
        if (groupcnt > 0) {
            // XMLに記載されていないグループがあれば XMLに追加する。
            columnIdBox = cursor.getColumnIndex("id_box");
            columnUriBoat = cursor.getColumnIndex("uri_boat");
            cursor.moveToFirst();
            do {
                String uriboat;
                String boxname;
                byte[] idBox;
                uriboat = cursor.getString(columnUriBoat);
                if (uriboat.equalsIgnoreCase(myuri)) {
                    idBox = cursor.getBlob(columnIdBox);
                    boxname = new String(idBox);
                    if (!(searchGroupnameFromXml(boxname))) {
                        // XMLにグループを追加する。
                        addItemToXml(ADD_ELEMENT_TYPE_GROUP, boxname, null);
                        addGroupList(boxname, null, null);
                        refreshFlag = true;
                    }
                }
            } while (cursor.moveToNext());
        }
        if (refreshFlag) {
            // 画面更新
            Context context = actMain.actMyself.getApplicationContext();
            Intent intent = new Intent();
            intent.setAction(ConstantGroup.ACT_REQUEST);
            intent.putExtra(ConstantGroup.EXTRA_EVENT, ConstantGroup.EVENT_LOAD_REFLESH_GROUPLIST2);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public void setBoxMembercursor(Cursor cursor) {
        BoxMembercursor = cursor;
    }

    public Cursor getBoxMembercursor() {
        return BoxMembercursor;
    }

    public String getNowDayTimeStr() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyDDDHHmmssSSS",Locale.JAPANESE);
        return sdf.format(time);
    }

    private String makeGroupName() {
        String gname;
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyDDD",Locale.JAPANESE);
        gname = sdf.format(time);
        sdf = new SimpleDateFormat("HH",Locale.JAPANESE);
        int hhsec = Integer.parseInt(sdf.format(time)) * 3600;
        sdf = new SimpleDateFormat("mm",Locale.JAPANESE);
        int mmsec = Integer.parseInt(sdf.format(time)) * 60;
        sdf = new SimpleDateFormat("ss",Locale.JAPANESE);
        int sec = Integer.parseInt(sdf.format(time));
        int totalsec = hhsec + mmsec + sec;
        gname = gname + String.valueOf(totalsec);

        return gname;
    }

    // XMLを読んでグループ名が有るか検索する。
    private boolean searchGroupnameFromXml(String groupname) {

        boolean GroupListFlag = false;
        boolean GroupFlag = false;
        String GroupBoxName;
        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + VM_Group_Terminal_List);
        try (InputStream inputs = new FileInputStream(vmfile)) {
            xmlPullParser = Xml.newPullParser();
            try {
                xmlPullParser.setInput(inputs, "UTF-8");
                int eventType = xmlPullParser.getEventType();

                String wkname;

                while (eventType != xmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            wkname = xmlPullParser.getName();
                            if (xmlPullParser.getName().equalsIgnoreCase("grouplist")) {
                                GroupListFlag = true;
                                eventType = xmlPullParser.next();
                                continue;
                            }
                            if (GroupListFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    GroupFlag = true;
                                }
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (GroupFlag) {
                                // グループ名取得
                                GroupBoxName = xmlPullParser.getText();
                                // グループ名は一致するか？
                                if (groupname.equals(GroupBoxName)) {
                                    // グループが見つかった。
                                    return true;
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (GroupFlag) {
                                if (xmlPullParser.getName().equalsIgnoreCase("group")) {
                                    GroupFlag = false;
                                }
                            }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            xmlPullParser = null;
            vmfile = null;
        }
        // グループは見つからなかった。
        return false;
    }

    private void addItemToXml(int addtype, String item1, String item2) {

        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + VM_Group_Terminal_List);
        DocumentBuilderFactory vmxml = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder vmxmlbuilder = vmxml.newDocumentBuilder();
            Document document = vmxmlbuilder.newDocument();
            Element rootElement, subElement1, subElement2;
            String myname = chatCommon.getMyChatName();
            String nickname = chatCommon.getMyChatName();
            String mysipuri = chatCommon.getMySIPURI();
            String sipuri = chatCommon.getMySIPURI();
            Text text;
            int listcnt1, listcnt2;

            // ルートを作成する <terminalinfornation> を作成する。
            rootElement = document.createElement("terminalinfornation");

            // 自機登録
            // 親ノード <myterminal> を作成する。
            subElement1 = document.createElement("myterminal");
            // 要素 <name> を作成する。
            subElement2 = document.createElement("name");
            text = document.createTextNode(nickname);
            subElement2.appendChild(text);
            // 親ノード <myterminal> に追加する。
            subElement1.appendChild(subElement2);
            // 要素 <sipuri> を作成する。
            subElement2 = document.createElement("sipuri");
            text = document.createTextNode(mysipuri);
            subElement2.appendChild(text);
            // 親ノード <myterminal> に追加する。
            subElement1.appendChild(subElement2);
            // ルートに親ノード <myterminal> を追加
            rootElement.appendChild(subElement1);

            // 端末リスト作成
            // 親ノード <terminallist> を作成する。
            listcnt1 = terminalList.getTerminalCount();
            if (listcnt1 > 0) {
                subElement1 = document.createElement("terminallist");
                for (int idx = 0; idx < listcnt1; idx++) {
                    nickname = terminalList.getTreminalName(idx);
                    sipuri = terminalList.getTreminalSipuri(idx);
                    if (!mysipuri.equals(sipuri)) {
                        // 要素 <name> を作成する。
                        subElement2 = document.createElement("name");
                        subElement2.setAttribute("terminalid", sipuri);
                        text = document.createTextNode(nickname);
                        subElement2.appendChild(text);
                        // 親ノードへ追加
                        subElement1.appendChild(subElement2);
                    }
                }
                if (addtype == ADD_ELEMENT_TYPE_TERMINAL) {
                    // 新規端末を追加
                    // 要素 <name> を作成する。
                    subElement2 = document.createElement("name");
                    subElement2.setAttribute("terminalid", item1);
                    text = document.createTextNode(item2);
                    subElement2.appendChild(text);
                    // 親ノードへ追加
                    subElement1.appendChild(subElement2);
                }
                // ルートに親ノード <terminallist> を追加
                rootElement.appendChild(subElement1);
            } else if (addtype == ADD_ELEMENT_TYPE_TERMINAL) {
                subElement1 = document.createElement("terminallist");
                // 新規端末を追加
                // 要素 <name> を作成する。
                subElement2 = document.createElement("name");
                subElement2.setAttribute("terminalid", item1);
                text = document.createTextNode(item2);
                subElement2.appendChild(text);
                // 親ノードへ追加
                subElement1.appendChild(subElement2);
                // ルートに親ノード <terminallist> を追加
                rootElement.appendChild(subElement1);
            }

            // グループリストを作成する。
            // 端末リスト作成
            listcnt1 = groupList.getGroupCount();
            if (listcnt1 > 1) {
                // 二件目の情報を取得する。 注意事項：一件目(index:0) はデフォルトのグループなので読み飛ばす。
                // 親ノード１ <grouplist> を作成する。
                subElement1 = document.createElement("grouplist");
                for (int idx = 1; idx < listcnt1; idx++) {
                    String groupName = groupList.getGroupItem(idx).boxname;
                    // 要素 <group> を作成する。
                    subElement2 = document.createElement("group");
                    text = document.createTextNode(groupName);  // グループ名を設定
                    subElement2.appendChild(text);
                    // グループを追加する。
                    subElement1.appendChild(subElement2);
                }
                if (addtype == ADD_ELEMENT_TYPE_GROUP) {
                    // 新規グループを追加する。
                    // 要素 <group> を作成する。
                    subElement2 = document.createElement("group");
                    text = document.createTextNode(item1);
                    subElement2.appendChild(text);
                    // 親ノードへ追加
                    subElement1.appendChild(subElement2);
                }
                // ルートに親ノード <grouplist> を追加
                rootElement.appendChild(subElement1);
            } else if (addtype == ADD_ELEMENT_TYPE_GROUP) {
                // 親ノード１ <grouplist> を作成する。
                subElement1 = document.createElement("grouplist");
                // デフォルトグループを追加する。
                subElement2 = document.createElement("group");
                text = document.createTextNode(DEFALUT_GROUP_NAME);
                subElement2.appendChild(text);
                // <grouplist>に追加する。
                subElement1.appendChild(subElement2);
                // 新規グループを追加
                // 要素 <group> を作成する。
                subElement2 = document.createElement("group");
                text = document.createTextNode(item1);
                subElement2.appendChild(text);
                // <grouplist>に追加する。
                subElement1.appendChild(subElement2);
                // ルートに親ノード <grouplist> を追加
                rootElement.appendChild(subElement1);
            }
            // 作成したドキュメントを登録する。
            document.appendChild(rootElement);
            TransformerFactory tffactory = TransformerFactory.newInstance();
            Transformer transformer = tffactory.newTransformer();
            File vmdir = new File(path);
            if (!vmdir.exists()) {
                vmdir.mkdirs();
            }
            if (!vmfile.exists()) {
                vmfile.createNewFile();
            }
            transformer.transform(new DOMSource(document), new StreamResult(vmfile));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNewGroupname(String name) {
        newGroupName = name;
    }

    public String getNewGroupName() {
        return  newGroupName;
    }

    public String toHex(byte[] data) {
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        for (byte d : data) {
            stringBuilder.append(String.format("%02X", d));
        }
        str = stringBuilder.toString();
        return str;
    }

    public void removeUserForTerminalList(String name, String sipuri) {
        TerminalItem item = new TerminalItem();
        item.name = name;
        item.sipuri = sipuri;
        terminalList.removeItem(item);
    }

    public void setQrcodeSipUri(String  sipUri) {
        qrcodeSipUri = sipUri;
    }

    public String getQrcodeSipUri() {
        return qrcodeSipUri;
    }
}
