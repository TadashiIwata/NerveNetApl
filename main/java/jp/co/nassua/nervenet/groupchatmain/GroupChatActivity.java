package jp.co.nassua.nervenet.groupchatmain;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jp.co.nassua.nervenet.voicemessage.R;
import jp.co.nassua.nervenet.voicemessage.VoiceMessageCommon;

public class GroupChatActivity extends AppCompatActivity {

    private static GroupList groupList;
    private static GroupItem groupItem;
    private static GroupCommon groupCommon;
    private static ChatCommon chatCommon;
    private static VoiceMessageCommon voiceMessageCommon;
    private static ContentChat contentChat;
    private String ChatTitle;
    public static GroupChatActivity groupChatActivity;
    public static BroadcastReceiver chatNotify;
    private static Uri ImageUri;

    public final static int REQUEST_GALLERY_CODE = 101;
    public final static int REQUEST_CAMERA_CODE = REQUEST_GALLERY_CODE + 1;
    public static final List<String> types = Collections.unmodifiableList(new LinkedList<String>() {
        {
            add("image/jpeg");
            add("image/jpg");
            add("image/png");
        }
    });

    public GroupChatActivity() {
        if (groupList == null) {
            groupList = new GroupList();
        }
        if (groupItem == null) {
            groupItem = new GroupItem();
        }
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (voiceMessageCommon == null) {
            voiceMessageCommon = new VoiceMessageCommon();
        }
        if (chatCommon == null) {
            chatCommon = new ChatCommon();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        // Fragment再生成防止
        if ( savedInstanceState == null ) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            GroupChatFragment groupChatFragment = new GroupChatFragment();
            //GroupChatFragment groupChatFragment = (GroupChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_frameLayout_fragment);
            Intent intent = getIntent();
            if (intent != null) {
                Bundle bundle = new Bundle();
                if (intent.hasExtra("GROUP")) {
                    GroupItem group = intent.getParcelableExtra("GROUP");
                    bundle.putParcelable("GROUP", group);
                    if (group != null) {
                        ChatTitle = group.boxname;
                    }
                }
                groupChatFragment.setArguments(bundle);
            }
            transaction.add(R.id.chat_frameLayout_fragment, groupChatFragment);
            transaction.commit();
        }
        if (ChatTitle == null) {
            // 取りあえずデフォルトグループを表示する。
            ChatTitle = groupCommon.getDefaultGroupName();
        }
        groupCommon.setChatGroupName(ChatTitle);

        // ブロードキャストレシーバー登録 (BoxShare用)
        chatNotify = new ChatNotify();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ContentChat.ACT_NOTIFY);
        filter.addAction(ContentChat.ACT_REQUEST);
        //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(chatNotify, filter);
        getApplicationContext().registerReceiver(chatNotify, filter);

        groupChatActivity = this;
        ImageUri = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 画面を縦に固定する。
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // タイトル設定
        setTitle(ChatTitle);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch (item.getItemId()) {
             case R.id.action_OpenFiles:
                startSelectPictureFiles();
                break;
            case R.id.action_Camera:
                try {
                    /* 高知工科大向けは カメラ有効。 */
                    /* 当面は未サポートにしておく */
                    boolean cameraFlag = true;
                    if (cameraFlag) {
                        if (!startCamera()) {
                        }
                    } else {
                        // カメラ未サポートメッセージ
                        Context context =this;
                        groupCommon.alertMessage(context, contentChat.ALERT_NOT_SUPPORTED_CAMERA);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    private static String filePath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean sendFlag = false;

        switch (requestCode) {
            case REQUEST_GALLERY_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, getString(R.string.unselected_image), Toast.LENGTH_LONG).show();
                    return;
                }
                // 画像ファイルの Uriを退避する。
                ImageUri = data.getData();
                if (!(chatCommon.saveWorkImageUrl(ImageUri, this))) {
                    Toast.makeText(this, getString(R.string.illegal_image), Toast.LENGTH_LONG).show();
                } else {
                    sendFlag = true;
                }
                break;
            case REQUEST_CAMERA_CODE:
                // 撮影したファイルを登録する。
                if (resultCode == RESULT_OK) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    contentValues.put("_data", filePath);
                    ContentResolver contentResolver = this.getContentResolver();
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    // ここでイメージファイルの URIを設定する。
                    File file = new File(filePath);
                    ImageUri = Uri.fromFile(file);
                    chatCommon.saveWorkImageUrl(ImageUri, this);
                    sendFlag = true;
                }
                break;
            default:
                break;
        }
        // 画像ファイル送信
        if (sendFlag) {
            // 送信ボタンを擬似的に発生させる。
            chatCommon.SendButton_performClick();
        }
    }
    private void startSelectPictureFiles() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  // Android 4以前？
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");  // Android 4以前？
        //intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, types.toArray());
        startActivityForResult(Intent.createChooser(intent, null), GroupChatActivity.REQUEST_GALLERY_CODE);

        return;
    }

    private boolean startCamera() throws IOException {
        Uri imageUri = createFileUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, REQUEST_CAMERA_CODE);
        return true;
    }

    private Uri createFileUri() throws IOException {
        // 撮影日時
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // 画像の保存先
        File cameraStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
        if (!cameraStorageDir.exists()) {
            cameraStorageDir.mkdirs();
        }
        //
        File file = File.createTempFile("IMG_" + timeStamp, ".jpg", cameraStorageDir );
        //String filePath = cameraStorageDir.getPath() + File.separator + timeStamp + ".jpg";
        filePath = file.getAbsolutePath();
        //File imageFile = new File(filePath);
        Uri imageUri  = FileProvider.getUriForFile(this, "jp.co.nassua.nervenet.groupchatmain", file);
        return imageUri;
    }

}
