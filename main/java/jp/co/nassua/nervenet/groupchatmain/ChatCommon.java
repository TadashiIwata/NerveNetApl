package jp.co.nassua.nervenet.groupchatmain;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.utlcontent.resolver.FileResolveHelper;

public class ChatCommon extends AppCompatActivity  {

    private static ActMain actMain = null;
    public static ImageView selectedImage = null;
    protected static Uri ImageUri = null;
    protected static Uri workImageUri = null;
    private  static  ContentResolver resolver = null;
    private static DbDefineBoat.ConfNode confNode;
    private static ChatBoxShare chatBoxShare;
    private static GroupCommon groupCommon;
    private static GroupChatFragment groupChatFragment;

    private static GroupChatActivity groupChatContext = null;

    private  static  Uri uri_tbl;
    private static ContentResolver contentResolver;
    private static ContentProviderClient contentProviderClient;

    // メッセージ管理用
    public static final long LimitTime3years = (long) 24 * (long) 3600 * (long) 1000 * (long) 365 * (long) 3;  // 有効期限 3年(24時間 * 3600秒 * 1000ミリ秒 * 365日 * 3年)
    public static final long LimitTime60days = (long) 24 * (long) 3600 * (long) 1000 * (long) 60;  // 有効期限 60日(24時間 * 3600秒 * 1000ミリ秒 * 60日)
    public static final long LimitTime30days = (long) 24 * (long) 3600 * (long) 1000 * (long) 30;  // 有効期限 30日(24時間 * 3600秒 * 1000ミリ秒 * 30日)
    public static final long LimitTime2days  = (long) 24 * (long) 3600 * (long) 1000 * (long) 2;  // 有効期限 2日(24時間 * 3600秒 * 1000ミリ秒 * 2日)
    public static final long LimitTime60 = 60 * 60 * 1000;  // 有効期限 60分
    public static final long LimitTime45 = 45 * 60 * 1000;  // 有効期限 45分
    public static final long LimitTime30 = 30 * 60 * 1000;  // 有効期限 30分
    public static final long LimitTime15 = 15 * 60 * 1000;  // 有効期限 15分
    public static final long LimitTime10 = 10 * 60 * 1000;  // 有効期限 10分
    public static final long LimitTime5  = 5 * 60 * 1000;   // 有効期限  5分

    public static final long LimitTime  = LimitTime15;  // 過去メッセージ有効範囲
    public static final long minimumSize = 600;  // 画像最小サイズ
    private static int MaxMessageCount;
    private static String MyChatName;
    private static byte[] MyBoatId;
    private static boolean AutoReceive;
    private static boolean TestMode;
    private static long chatCurrentTime;
    private static String mySIPURI;

    // 画像処理用
    private Bitmap bitmapFile;
    private Bitmap bitmapResize;
    private static int ImageQuality;
    private static long ImageMaxSize;
    private static float ImageSendRatio;
    private static float ImageDisplayRatio;

    public static final float IMAGE_RATIO_100 = 1.00f;
    public static final float IMAGE_RATIO_75 = 0.75f;
    public static final float IMAGE_RATIO_50 = 0.50f;
    public static final float IMAGE_RATIO_25 = 0.25f;

    // チャット画面更新の方法 Debug用
    public static final boolean DOWNLOAD_REQUEST = false;
    public static final boolean RELOAD_REQUEST = true;

    // 画面更新用
    private static Context chatContext;

    public void setChatContext(Context context) {
        chatContext = context;
    }

    public Context getChatContext() {
        return chatContext;
    }

    public void refreshview() {
        if (groupChatFragment == null) {
            groupChatFragment = new GroupChatFragment();
        }
        groupChatFragment.requestrefreshView();
    }

    public void SendButton_performClick() {
        if (groupChatFragment == null) {
            groupChatFragment = new GroupChatFragment();
        }
        groupChatFragment.dummyButtonClick();
    }

    public boolean saveWorkImageUrl(Uri uri, Context context) {
        boolean execFlag = false;
        byte[] picData;
        float widthScale;
        float heightScale;
        String filename = null;

        // 画像ファイルパス取得
        String filePath = getPath(uri, context);
        if (filePath != null) {
            // ファイル名のみ取り出し。
            // こうしないと BitMapFactoryで使えない。
            filename = new File(filePath).getAbsolutePath();
        } else {
            return false;
        }
        long fileSize;
        File picfile = new File(filePath);
        fileSize = picfile.length();

        if (fileSize > getImageMaxSize()) {
            // 画像ファイルサイズ変更
            bitmapFile = BitmapFactory.decodeFile(filename);
            if (bitmapFile != null) {
                int wkWidthSz = bitmapFile.getWidth();
                int wkHeightSz = bitmapFile.getHeight();
                widthScale = getImageSendRatio();
                heightScale = getImageSendRatio();
                Matrix matrix = new Matrix();
                matrix.postScale(widthScale, heightScale, 0, 0);
                bitmapResize = Bitmap.createBitmap(bitmapFile, 0, 0, wkWidthSz, wkHeightSz, matrix, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapResize.compress(Bitmap.CompressFormat.JPEG, getImageQuality(), byteArrayOutputStream);
                picData = byteArrayOutputStream.toByteArray();
                int datasz;
                datasz = byteArrayOutputStream.size();
                if (datasz <= getImageMaxSize()) {
                    execFlag = true;
                } else {
                    // ファイルサイズ警告(縮小しても大きい)
                    execFlag = false;
                }
            } else {
                picData = null;
            }
        } else if (fileSize >= minimumSize) {
            picData = new byte[(int) fileSize];
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(picfile);
                fileInputStream.read(picData);
                fileInputStream.close();
                execFlag = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            picData = null;
        }

        if (execFlag) {
            FileResolveHelper fileResolveHelper = FileResolveHelper.newInstance(context);
            try {
                Uri fileUri = fileResolveHelper.newFileUri(DbDefineShare.File.CONTENT_URI);
                ParcelFileDescriptor fd = fileResolveHelper.openForWrite(fileUri, false);
                ParcelFileDescriptor.AutoCloseOutputStream stream = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                stream.write(picData);
                workImageUri = fileUri;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return execFlag;
    }

    private String getPath(Uri uri, Context context) {
        String[] columns = null;
        String path = uri.toString();
        ContentResolver contentResolver;
        Cursor cursor;

        Pattern patternFile = Pattern.compile("^file://[0-9a-zA-Z\\.:/%]");
        Pattern patternGoogle = Pattern.compile("^content://com.google[0-9a-zA-Z\\.:/%]"); // OK
        Pattern patternMediaExternal = Pattern.compile("^content://media/external/images/media/[0-9a-zA-Z\\.:/%]");  // OK
        Pattern patternAndroid = Pattern.compile("^content://com.android.providers.media.documents/document/[0-9a-zA-Z\\.:/%]");  // OK

        if (patternFile.matcher(path).find()) {
            return path.replaceFirst("file://", "");
        } else if (patternGoogle.matcher(path).find()) {
            columns = new String[]{MediaStore.MediaColumns.DATA};
            contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, columns, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    path = cursor.getString(0);
                }
                cursor.close();
            }
        } else if (patternMediaExternal.matcher(path).find()) {
            columns = new String[] {MediaStore.MediaColumns.DATA};
            contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, columns, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    path = cursor.getString(0);
                }
                cursor.close();
            }
        } else if (patternAndroid.matcher(path).find()) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            Uri contentUri = null;
            contentUri = MediaStore.Files.getContentUri("external");
            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
                    split[1]
            };
            path = getDataColumn(context, contentUri, selection, selectionArgs);
        } else {
            path = null;
        }
        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };
        String path = null;
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int cindex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return path;
    }

    public Uri getWorkImageUri() {
        return workImageUri;
    }

    public static void clearWorkImageUri() {
        workImageUri = null;
    }

    // メッセージを端末間情報共有テーブルに書き込む
    public void writeChatMessage(Context context, Uri uri, ChatItem chatItem) {

        String wkMessage;
        String encBase64;

        Uri result;
        if (actMain == null) {
            actMain = new ActMain();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (resolver == null) {
            resolver = context.getContentResolver();
        }

        readConfNode();
        String strBortId = bin2hex(confNode.mIdBoat);
        String recordId = strBortId + getNowDayTimeStr();
        ChatBoxShare.idMyself = confNode.mIdBoat;
        ChatBoxShare.uriMyself = confNode.mUriBoat;
        ChatBoxShare.idBox = recordId.getBytes();
        chatBoxShare = ChatBoxShare.newInstance(confNode.mUriBoat);

        chatBoxShare.idRecord = recordId.getBytes();
        chatBoxShare.boxName = groupCommon.getCurrentGroupName();
        if (uri != null) {
            chatBoxShare.uriAttached = uri.toString();
        }
        wkMessage = chatItem.name + "@@" + chatItem.comment;
        encBase64 = Base64.encodeToString(wkMessage.getBytes(), Base64.DEFAULT);
        chatBoxShare.MessageInfo = encBase64;
        chatBoxShare.timeCalibrate = chatItem.time;
        DbDefineShare.BoxShare rec = chatBoxShare.toRecord();
        rec.mCommon.mNodeUpdate = confNode.mIdBoat;
        rec.mCommon.mTimeUpdate = System.currentTimeMillis();
        rec.mCommon.mTimeDiscard = System.currentTimeMillis() + getCacheLimitTime();  // サーバ上でのメッセージ有効期間

        if (resolver == null) {
            resolver = context.getContentResolver();
        }
        uri_tbl = DbDefineShare.BoxShare.CONTENT_URI;
        contentResolver = resolver;
        contentProviderClient = contentResolver.acquireContentProviderClient(uri_tbl);

        int ret = 0;
        if (contentProviderClient != null) {
            try {
                result = contentProviderClient.insert(uri_tbl, rec.getForInsert());
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
                // 能動配信要求
                if (ret == 1) {
                    if (groupChatContext == null) {
                        groupChatContext = GroupChatActivity.groupChatActivity;
                    }
                    Intent intent = new Intent(ConstantShare.ACT_PUBLISH);
                    Bundle extras = new Bundle();
                    extras.putString(ConstantShare.EXTRA_TRANSPORT, "tcp");
                    extras.putString(ConstantShare.EXTRA_TABLE, DbDefineShare.BoxShare.PATH);
                    extras.putByteArray(ConstantShare.EXTRA_ID_RECORD, recordId.getBytes());
                    intent.putExtras(extras);
                    groupChatContext.sendBroadcast(intent);
                } else {
                    // ログ取る？
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                contentProviderClient.release();
            }
        }
    }

    public static String getTimeStr(Long time) {
        String strTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.JAPANESE);
        try {
            String strDateTime = sdf.format(time);
            java.util.Date dt = sdf.parse(strDateTime);
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss",Locale.JAPANESE);
            strTime = sdfTime.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strTime;
    }

    // 端末諸元 読み取り
    //
    public void readConfNode() {
        String debugMsg = "DEBUG";

        confNode = new DbDefineBoat.ConfNode();
        if (confNode == null) {
            debugMsg = "confNode is NULL.";
        }

        /*
        if (resolver == null) {
            if (actMain == null) {
                actMain = new ActMain();
            }
            resolver = actMain.actMyself.getContentResolver();
        }
        */
        resolver = actMain.actMyself.getContentResolver();
        Uri uri_tbl = DbDefineBoat.ConfNode.CONTENT_URI;
        ContentResolver cont = resolver;
        ContentProviderClient providerClient = cont.acquireContentProviderClient( uri_tbl );
        if (providerClient != null ) try {
            Cursor cursor = providerClient.query( uri_tbl, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    confNode.setFromQuery(cursor);
                } else {
                    debugMsg = "moveToFirst Failed.";
                }
                cursor.close();
            } else {
                debugMsg = "cursor is NULL.";
            }
        } catch (RemoteException e) {
            //if (prefLog.logCamera >= PrefLog.LV_WARN) {
            //    Log.w(logLabel, "no conf-node err=" + e.getMessage());
            //}
            e.printStackTrace();
        } finally {
            providerClient.release();
        }
    }

    public void updateTimeDiscard(Context context, ChatBoxShare rec) {

        String hexIdRecord;
        int result;
        Long newTimeDiscard = rec.timeUpdate + getMessageLimitTime(); // 新しいレコード廃棄日時を設定する。
        if (newTimeDiscard > rec.timeDiscard) {
            hexIdRecord = bin2hex(rec.idBox);
            ContentResolver contentResolver1 = context.getContentResolver();
            // レコードを更新する
            ContentValues contentValues = new ContentValues();
            contentValues.put("time_discard", newTimeDiscard);
            String selection = "id_msg=x'" + hexIdRecord + "'";
            Uri uri = DbDefineShare.BoxShare.CONTENT_URI;
            contentProviderClient = contentResolver1.acquireContentProviderClient(uri);
            int ret = 0;
            if (contentProviderClient != null) {
                try {
                    result = contentProviderClient.update(uri, contentValues, selection, null);
                    if (0 < result) {
                        // 更新成功
                        ret = 1;
                    } else {
                        // エラー
                        ret = -1;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    contentProviderClient.release();
                }
            }
        }
    }

    public void setMyChatName(String name) {
        MyChatName = name;
    }

    public String getMyChatName() {
        return MyChatName;
    }

    public static String bin2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static byte[] hex2bin(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
        }
        return bytes;
    }

    public void setMyBoatId(byte[] boatId) {
        MyBoatId = boatId;
    }

    public byte[] getMyBoatId() {
        return MyBoatId;
    }

    public void setMySIPURI(String uri) {
        mySIPURI = uri;
    }

    public String getMySIPURI() {
        return mySIPURI;
    }

    public String getNowDayTimeStr() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyDDDHHmmss",Locale.JAPANESE);
        return sdf.format(time);
    }

    public void setMaxMessageCount(int cnt) {
        MaxMessageCount = cnt;
    }

    public int getMaxMessageCount() {
        return MaxMessageCount;
    }

    public void setImageQuality(int quality) {
        ImageQuality = quality;
    }

    public int getImageQuality() {
        return ImageQuality;
    }

    public void setImageMaxSize(long size) {
        ImageMaxSize = size;
    }

    public long getImageMaxSize() {
        return ImageMaxSize;
    }

    public void setImageSendRatio(int iRatio) {
        switch (iRatio) {
            case 100:
                ImageSendRatio = IMAGE_RATIO_100;
                break;
            case 75:
                ImageSendRatio = IMAGE_RATIO_75;
                break;
            case 50:
                ImageSendRatio = IMAGE_RATIO_50;
                break;
            case 25:
                ImageSendRatio = IMAGE_RATIO_25;
                break;
            default:
                ImageSendRatio = IMAGE_RATIO_25;
        }
    }

    public float getImageSendRatio() {
        return ImageSendRatio;
    }

    public void setImageDisplayRatio(int iRatio) {
        switch (iRatio) {
            case 100:
                ImageDisplayRatio = IMAGE_RATIO_100;
                break;
            case 75:
                ImageDisplayRatio = IMAGE_RATIO_75;
                break;
            case 50:
                ImageDisplayRatio = IMAGE_RATIO_50;
                break;
            case 25:
                ImageDisplayRatio = IMAGE_RATIO_25;
                break;
            default:
                ImageDisplayRatio =IMAGE_RATIO_50;
        }
    }

    public float getImageDisplayRatio() {
        return ImageDisplayRatio;
    }

    public void setAutoReceive(boolean flag) {
        AutoReceive = flag;
    }

    public boolean getAutoReceive() {
        return  AutoReceive;
    }

    public void setTestModee(boolean flag) {
        TestMode = flag;
    }

    public boolean getTestMode() {
        return  TestMode;
    }

    // 端末起動時に読み出すメッセージの有効範囲(過去分)を返す。
    public long getLimitTime() {
        return LimitTime;
    }

    // 端末上でのメッセージ有効期間を返す。
    public long getMessageLimitTime() {
        if (getTestMode()) {
            return LimitTime60;
        } else {
            return LimitTime3years;
        }
    }

    // 基地局上でのメッセージ有効期間を返す。
    public long getCacheLimitTime() {
        if (getTestMode()) {
            return LimitTime15;
        } else {
            return LimitTime2days;
        }
    }

    // グループの有効期間
    public long getGroupLimitTime() {
        if (getTestMode()) {
            return LimitTime60;
        } else {
            //GroupLimitTime = LimitTime3years;
            return LimitTime3years;
        }
    }

    public void setChatCurrentTime (long date) {
        chatCurrentTime = date;
    }

    public long getChatCurrentTime() {
        return chatCurrentTime;
    }


}
