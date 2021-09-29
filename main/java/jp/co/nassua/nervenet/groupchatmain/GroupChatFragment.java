package jp.co.nassua.nervenet.groupchatmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ViewUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.share.DbDefineShare;
import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.utlcontent.resolver.FileResolveHelper;

import android.media.ThumbnailUtils;

public class GroupChatFragment extends Fragment implements View.OnClickListener {

    protected String TAG = getClass().getSimpleName();
    private static ListView listViewChat;
    private EditText editTextComment;
    private Button buttonSend;

    protected static ChatList chatList;
    private static ChatCommon chatCommon;
    private static GroupCommon groupCommon;
    private static CursorLoaderChatBoxShare cursorLoaderChatBoxShare;
    protected SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");;
    private static ChatAdapter mAdapter;

    private static final int DATELENGTH = 11;
    private static View dummuyView;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_button_send:
                onSendChatMessage();
                break;
            default:
                break;
        }
    }

    // ChatAdapter Class
    private class ChatAdapter extends ArrayAdapter {
        private static final int resourceId = R.layout.fragment_chat_item;

        public ChatAdapter() {
            super(getActivity(), resourceId);
        }

        @Override
        public int getCount() {
            return chatList.ITEMS.size();
        }

        @Override
        public Object getItem(int position) {
            return chatList.ITEMS.get(position);
        }

        @Override
        public boolean isEnabled(int position) {
            //return super.isEnabled(position);
            return false;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override

        public View getView(int position, View convertView, ViewGroup parent) {
            // Viewの再利用時など
            View v = convertView;
            final Context context = getContext();
            long wkDate;
            // イメージ縮小設定
            final Bitmap bitmap, bitmapThumbnail;
            boolean kakudai = false;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(resourceId, parent, false);
            }

            final ChatItem item = (ChatItem) getItem(position);
            if (item != null) {
                // 発言メッセージ用TextView
                int len = item.comment.length();
                TextView msg =  (TextView) v.findViewById(R.id.chatItem_textView_message);;
                if (len > 0) {
                    msg.setText(item.comment);
                }
                // 画像
                ImageView chatImage = (ImageView) v.findViewById(R.id.chatItem_imageView);
                // 発言時刻と発言者名用TextView
                TextView dateAndName = (TextView) v.findViewById(R.id.chatItem_textView_date_and_name);
                // IDチェック
                byte[] myidbytes = chatCommon.getMyBoatId();
                String myidstr = toHex(myidbytes);
                if (item.id.equals(myidstr)) {
                    // 発信者は自分
                    // メッセージ部分
                    if (len > 0) {
                        // メッセージ有りの時に吹き出しを付ける。
                        msg.setBackgroundResource(R.drawable.chatright);
                        msg.setVisibility(View.VISIBLE);
                    } else {
                        msg.setVisibility(View.GONE);
                    }
                    // 時刻部分（自分の場合は時間のみ）
                    if (item.time != null) {
                        dateAndName.setText(chatCommon.getTimeStr(item.time));
                    }
                    // 画像貼り付け
                    if (item.fileuri != null) {
                        bitmap = makeBitmapData(context, item.fileuri);
                        bitmapThumbnail = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
                        chatImage.setImageBitmap(bitmapThumbnail);
                        kakudai = true;
                    } else {
                        // ダミー画像を表示してずれを抑制する。
                        bitmap = null;
                        chatImage.setImageResource(R.drawable.dummy);
                    }
                    // 全体的に右寄せ
                    LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.chatItem_linearLayout);
                    linearLayout.setGravity(Gravity.RIGHT | Gravity.TOP);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
                    layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
                    if (msg != null) {
                        msg.setGravity(Gravity.RIGHT);
                    }
                    dateAndName.setGravity(Gravity.RIGHT);
                } else {
                    // 発信者は他の人
                    // メッセージ部分
                    if (len > 0) {
                        // メッセージ有りの時に吹き出しを付ける。
                        msg.setBackgroundResource(R.drawable.chatleft);
                        msg.setVisibility(View.VISIBLE);
                    } else {
                        msg.setVisibility(View.GONE);
                    }
                    // 時刻部分（他人の場合は名前と時間）
                    if (item.time != null) {
                        dateAndName.setText("by " + item.name + " " + chatCommon.getTimeStr(item.time));
                    }
                    // 画像貼り付け
                    if (item.fileuri != null) {
                        bitmap = makeBitmapData(context, item.fileuri);
                        bitmapThumbnail = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
                        chatImage.setImageBitmap(bitmapThumbnail);
                        kakudai = true;
                        boolean saveFlag = true;
                        if (saveFlag) {
                            chatImage.setOnLongClickListener(
                                    new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            // 長押しされたら保存確認のメッセージを出す。
                                            ImageSaveMessage(getContext(), item.fileuri);
                                            return true;
                                        }
                                    }
                            );
                        }

                    } else {
                        // ダミー画像を表示してずれを抑制する。
                        chatImage.setImageResource(R.drawable.dummy);
                        bitmap = null;
                    }
                    // 全体的に左寄せ
                    LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.chatItem_linearLayout);
                    linearLayout.setGravity(Gravity.LEFT | Gravity.TOP);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                    if (msg != null) {
                        msg.setGravity(Gravity.LEFT);
                    }
                    dateAndName.setGravity(Gravity.LEFT);
                    wkDate = chatCommon.getChatCurrentTime();
                    if (item.time > wkDate) {
                        chatCommon.setChatCurrentTime(item.time);
                    }
                }
                if (kakudai) {
                    chatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 画像を拡大表示する。
                            ImageView imageView = new ImageView(GroupChatActivity.groupChatActivity);
                            imageView.setImageBitmap(bitmap);
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            Dialog dialog = new Dialog(GroupChatActivity.groupChatActivity);
                            dialog.setContentView(imageView);
                            dialog.show();
                        }
                    });
                }
            }
            return v;
        }
    }

    public static GroupChatFragment newInstance(String param1, String param2) {
        GroupChatFragment fragment = new GroupChatFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Bitmap makeBitmapData(Context context, Uri uri) {
        Bitmap retBitmap = null;
        byte[] picData = null;
        boolean nextFlag = false;
        Bitmap bitmap;
        Bitmap bitmapResize;
        Matrix matrix;
        float widthScale;
        float heightScale;

        try {
            FileResolveHelper file_resolver = FileResolveHelper.newInstance(context);
            ParcelFileDescriptor fd = file_resolver.openForRead(uri);
            ParcelFileDescriptor.AutoCloseInputStream stm = new ParcelFileDescriptor.AutoCloseInputStream(fd);
            long msgsize = fd.getStatSize();
            picData = new byte[(int)msgsize];
            stm.read(picData);
            stm.close();
            nextFlag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nextFlag) {
            nextFlag = false;
            int picDataSz = picData.length;
            if (picDataSz >= chatCommon.minimumSize) {
                bitmap = BitmapFactory.decodeByteArray(picData, 0, picDataSz);
                int wkWidthSz = bitmap.getWidth();
                int wkHeightSz = bitmap.getHeight();
                widthScale = chatCommon.getImageDisplayRatio();  // 縮小倍率を取得する。
                heightScale = chatCommon.getImageDisplayRatio(); // 縮小倍率を取得する。
                matrix = new Matrix();
                matrix.postScale(widthScale, heightScale, 0, 0);
                bitmapResize = Bitmap.createBitmap(bitmap, 0, 0, wkWidthSz, wkHeightSz, matrix, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                // 表示処理なので Qualityは 100固定。
                bitmapResize.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                picData = byteArrayOutputStream.toByteArray();
                picDataSz = picData.length;
                retBitmap = BitmapFactory.decodeByteArray(picData, 0, picDataSz);
            }
        }
        return retBitmap;
    }

    AlertDialog.Builder alertDialog;
    private final Handler alertHandle = new Handler();
    private void ImageSaveMessage(Context context, final Uri uri) {
        String alertMessage;

        alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.alert_image_save_title);
        alertMessage = context.getResources().getString(R.string.alert_image_save_message);
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // プログラム終了
                try {
                    saveImageData(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 何もしない。
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

    private boolean saveImageData(Uri uri) throws IOException {
        Context context;
        boolean bret = false;
        byte[] picData = null;
        boolean nextFlag = false;
        Bitmap bitmap;
        Bitmap bitmapData = null;
        Matrix matrix;
        float widthScale;
        float heightScale;
        String filename;
        String AttachName = null;

        File file = null;

        context = this.getContext();
        try {
            FileResolveHelper file_resolver = FileResolveHelper.newInstance(context);
            ParcelFileDescriptor fd = file_resolver.openForRead(uri);
            ParcelFileDescriptor.AutoCloseInputStream stm = new ParcelFileDescriptor.AutoCloseInputStream(fd);
            long msgsize = fd.getStatSize();
            picData = new byte[(int)msgsize];
            stm.read(picData);
            stm.close();
            nextFlag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // jpg形式に変換
        if (nextFlag) {
            int picDataSz = picData.length;
            bitmap = BitmapFactory.decodeByteArray(picData, 0, picDataSz);
            int wkWidthSz = bitmap.getWidth();
            int wkHeightSz = bitmap.getHeight();
            widthScale = 1.0f;
            heightScale = 1.0f;
            matrix = new Matrix();
            matrix.postScale(widthScale, heightScale, 0, 0);
            bitmapData = Bitmap.createBitmap(bitmap, 0, 0, wkWidthSz, wkHeightSz, matrix, true);
        }
        // ファイルに保存する。
        if (nextFlag) {
            // 撮影日時
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            // 画像の保存先
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
            try {
                if (!file.exists()) {
                    file.mkdirs();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                nextFlag = false;
            }
            if (nextFlag) {
                filename = "IMG_" + timeStamp + ".jpg";
                AttachName = file.getAbsolutePath() + "/" + filename;
                try {
                    FileOutputStream fos = new FileOutputStream(AttachName);
                    bitmapData.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    nextFlag = false;
                }
            }
        }
        if (nextFlag) {
            // 保存したファイルを登録する。
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put("_data", AttachName);
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri1 = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            bret = true;
        }
        return bret;
    }

    public GroupChatFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (chatList == null) {
            chatList = new ChatList();
        }
        chatCommon.setChatContext(this.getContext());

        // Fragment再生成防止
        setRetainInstance(true);
        mAdapter = new ChatAdapter();

        // Chat Message用
        if (cursorLoaderChatBoxShare == null) {
            cursorLoaderChatBoxShare = new CursorLoaderChatBoxShare();
            cursorLoaderChatBoxShare.createLoader(getContext(), groupCommon.getCurrentGroupName());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.fragment_chat_item_list, container, false);

        editTextComment = (EditText) view.findViewById(R.id.chat_editText_message);
        // メッセージ送信用Button
        buttonSend = (Button) view.findViewById(R.id.chat_button_send);
        buttonSend.setOnClickListener(this);
        // メッセージ表示用ListView
        listViewChat = (ListView) view.findViewById(R.id.chat_listView);
        listViewChat.setAdapter(mAdapter);
        // 表示を更新
        refreshView();
        // ソフトキーボードを隠す
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        dummuyView = view;
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursorLoaderChatBoxShare != null) {
            cursorLoaderChatBoxShare.destroyLoader();
            cursorLoaderChatBoxShare = null;
        }
    }

    public void requestrefreshView() {
        refreshView();
    }

    public void dummyButtonClick() {
        Button button = (Button) dummuyView.findViewById(R.id.chat_button_send);
        button.performClick();
    }

    protected void refreshView() {
        Log.d(TAG, "refreshView()");

        if (chatList == null) {
            chatList = new ChatList();
        }
        // 一覧を一旦削除してみる
        chatList.clearAll();

        // 所属グループ一覧を取得
        Context context = chatCommon.getChatContext();
        chatList = getChatMessage(context, groupCommon.getCurrentGroupName());

        // adapterへグループ一覧を設定
        mAdapter.notifyDataSetChanged();

        if (listViewChat != null) {
            listViewChat.setSelectionFromTop(chatList.ITEMS.size() - 1, chatList.ITEMS.size());
        }
    }

    protected void onSendChatMessage() {
        String Myname = chatCommon.getMyChatName();
        int ChatCount = chatCommon.getMaxMessageCount();
        ChatItem chatItem = new ChatItem();

        chatItem.name = Myname;
        chatItem.comment = editTextComment.getText().toString().trim();
        chatItem.time = System.currentTimeMillis();
        chatItem.id = chatCommon.bin2hex(chatCommon.getMyBoatId());
        if (chatList.ITEMS.size() >= ChatCount) {
            chatList.ITEMS.remove(0);
        }
        chatList.addItem(chatItem);
        mAdapter.notifyDataSetChanged();
        listViewChat.setSelectionFromTop(chatList.ITEMS.size() - 1, chatList.ITEMS.size());
        editTextComment.setText("");
        saveChatMessage(chatItem);

    }

    protected boolean saveChatMessage(ChatItem chatItem) {
        boolean ret = false;

        if (chatItem != null) {
            if (chatCommon.getWorkImageUri() == null) {
                // 画像なし
                chatCommon.writeChatMessage(getContext(), null, chatItem);
            } else {
                // 画像有
                chatCommon.writeChatMessage(getContext(), chatCommon.getWorkImageUri(), chatItem);
                // 登録したら
                chatCommon.clearWorkImageUri();
            }

        }
        return ret;
    }

    public static ChatList getChatMessage(Context context, String gname) {
        ChatList ret = new ChatList();
        String boxName;

        if (gname != null) {
            Uri uri = DbDefineShare.BoxShare.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentProviderClient client = resolver.acquireContentProviderClient(uri);
            String selection = ( "( id_box = x'" + toHex(gname.getBytes()) + "') and ( flag_invalid = 0 )");
            try {
                Cursor cursor = client.query(uri, null, selection, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            DbDefineShare.BoxShare rec = DbDefineShare.BoxShare.newInstance();
                            rec.setFromQuery(cursor);
                            try {
                                ChatBoxShare chatBoxShare = ChatBoxShare.newInstance(rec);
                                byte[] wkrecordId = chatBoxShare.idBox;
                                int idlen = wkrecordId.length - DATELENGTH;
                                byte[] wkboatId = new byte[32];
                                System.arraycopy(wkrecordId, 0, wkboatId, 0, idlen);
                                String wkdata = new String(wkboatId).substring(0, idlen);
                                boxName = chatBoxShare.boxName;
                                if (boxName.equalsIgnoreCase(groupCommon.getCurrentGroupName())) {
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
                                        // ToDo:サムネイル作成

                                    }
                                    ret.addItem(chatItem);
                                }
                                // ここで廃棄時刻を書き換える？
                                chatCommon.updateTimeDiscard(context, chatBoxShare);
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
        return ret;
    }

    private static String toHex(byte[] data) {
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        for (byte d : data) {
            stringBuilder.append(String.format("%02x", d));
        }
        str = stringBuilder.toString();
        return str;
    }

}
