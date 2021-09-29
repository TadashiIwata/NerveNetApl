package jp.co.nassua.nervenet.voicemessage;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;

import jp.co.nassua.nervenet.boat.ConstantBoat;
import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.groupchatmain.ChatMessageService;
import jp.co.nassua.nervenet.groupchatmain.GroupCommon;
import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.song.ConstantSong;
import jp.co.nassua.nervenet.vmphonedepend.RcvNotify;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by I.Tadshi on 2016/09/28.
 */
public class VoiceMessageCommon {

    static ActMain actMain = ActMain.actMyself;
    static GroupCommon groupCommon;

    public final static int AUDIO_FORMAT_PCM = 0;
    public final static int AUDIO_FORMAT_3GP = AUDIO_FORMAT_PCM + 1;
    public final static int AUDIO_ENCODER_DEFAULT = 0;
    public final static int AUDIO_ENCODER_AAC = AUDIO_ENCODER_DEFAULT + 1;
    public final static int AUDIO_ENCODER_HE_AAC = AUDIO_ENCODER_AAC + 1;
    public final static int AUDIO_ENCODER_AAC_ELD = AUDIO_ENCODER_HE_AAC + 1;
    public final static int AUDIO_ENCODER_AMR_NB = AUDIO_ENCODER_AAC_ELD + 1;
    public final static int AUDIO_ENCODER_AMR_WB = AUDIO_ENCODER_AMR_NB + 1;
    public final static int AUDIO_ENCODER_VORBIS = AUDIO_ENCODER_AMR_WB + 1;

    public final static int ALERT_MESSAGE_TYPE1 = 1;
    public final static int ALERT_MESSAGE_TYPE2 = ALERT_MESSAGE_TYPE1 + 1;
    public final static int ALERT_MESSAGE_TYPE3 = ALERT_MESSAGE_TYPE2 + 1;
    public final static int ALERT_MESSAGE_TYPE4 = ALERT_MESSAGE_TYPE3 + 1;

    AlertDialog.Builder alertDialog;
    private final Handler alertHandle = new Handler();
    private static RcvNotify shareRcvNotify;
    private static RcvNotify phoneRcvNotify;
    private static RcvNotify songRcvNotify;
    private static int AudioFormat;
    private static int AudioEncoder;
    private static boolean alertFlag = false;
    private static int GroupTab = 0;
    public static final int MAKE_JOIN_LIST = 0;
    public static final int MAKE_TERMINAL_LIST = MAKE_JOIN_LIST + 1;


    private static ArrayList<byte[]> SendWaitRecordIds;

    // 端末間情報共有(音声メッセージ受信)開始
    public void startShareAlive(Context context) {
        if (shareRcvNotify == null) {
            // 端末間情報共有開始
            Intent intent = new Intent(ConstantShare.ACT_START);
            context.sendBroadcast(intent);
            //ブロードキャストレシーバー登録
            shareRcvNotify = new RcvNotify();
            IntentFilter filter = new IntentFilter();
            filter.addAction( ConstantShare.ACT_ALIVE );
            context.getApplicationContext().registerReceiver( shareRcvNotify, filter );
        } else {
            // 端末間情報共有開始
            Intent intent = new Intent(ConstantShare.ACT_START);
            context.sendBroadcast(intent);
        }
    }

    // 端末間情報共有(音声メッセージ受信)停止
    public void stopShareAlive(Context context) {
        if (shareRcvNotify != null) {
            // 端末間情報共有停止
            Intent intent = new Intent(ConstantShare.ACT_STOP);
            context.sendBroadcast(intent);
        }
    }

    // 通話機能開始
    public void startPhoneNotify(Context context) {
        if (phoneRcvNotify == null) {
            //通話サービス起動
            Intent intent_bcast = new Intent( ConstantPhone.ACT_REQUEST);
            intent_bcast.putExtra( ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_START );
            context.sendBroadcast( intent_bcast );
            //ブロードキャストレシーバー登録
            phoneRcvNotify = new RcvNotify();
            IntentFilter filter = new IntentFilter();
            filter.addAction( ConstantBoat.ACT_TSG_NOTIFY );
            filter.addAction( ConstantPhone.ACT_NOTIFY );
            context.getApplicationContext().registerReceiver( phoneRcvNotify, filter );
        }
    }

    // 通話機能停止
    public void stopPhoneNotify(Context context) {
        if (phoneRcvNotify != null) {
            //通話サービス停止
            Intent intent_bcast = new Intent( ConstantPhone.ACT_REQUEST);
            intent_bcast.putExtra( ConstantPhone.EXTRA_EVENT, ConstantPhone.EVENT_STOP );
            context.sendBroadcast( intent_bcast );
        }
    }

    // NerveNet基地局間同期データの生存確認 開始
    //
    public void startSongAlive(Context context) {
        if (songRcvNotify == null) {
            songRcvNotify = new RcvNotify();
            //ブロードキャストレシーバー登録
            IntentFilter filter = new IntentFilter();
            filter.addAction( ConstantSong.ACT_ALIVE );
            context.getApplicationContext().registerReceiver( songRcvNotify, filter );
        }
    }
    // NerveNet基地局間同期データの生存確認 停止
    //
    public void stopSongAlive(Context context) {
        if (songRcvNotify != null) {
            //ブロードキャストレシーバー登録解除
            context.getApplicationContext().unregisterReceiver( songRcvNotify );
            songRcvNotify = null;
        }
    }

    // オーディオフォーマット保存
    public void setAudioFormat(int audioformat) {
        AudioFormat = audioformat;
    }

    // オーディオフォーマット読み出し
    public int getAudioFormat() {
        return AudioFormat;
    }

    // オーディオエンコーダ保存
    public void setAudioEncoder(int audioencoder) {
        AudioEncoder = audioencoder;
    }

    // オーディオフォーマット読み出し
    public int getAudioEncoder() {
        return AudioEncoder;
    }

    public boolean  checkNodeStatus(Context context) {
        boolean bret = false;
        alertFlag = false;

        //端末状態 取得
        DbDefineBoat.StatNode stat_node = new DbDefineBoat.StatNode();
        Uri uri_tbl = DbDefineBoat.StatNode.CONTENT_URI;
        ContentProviderClient resolver = context.getContentResolver().acquireContentProviderClient(uri_tbl);
        if (resolver != null) {
            try {
                Cursor cursor = resolver.query(uri_tbl, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        stat_node.setFromQuery(cursor);
                        if ((stat_node != null) && (stat_node.mIdTsg != null)) {
                            bret = true;
                            alertFlag = true;
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
        return bret;
    }

    public void alertBaseStation(Context context, int alerttype) {
        String alertMessage;

        if (!alertFlag) {
            alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
            alertDialog.setTitle(context.getResources().getString(R.string.alert_dialog_title));
            if (alerttype == ALERT_MESSAGE_TYPE1) {
                alertMessage = context.getResources().getString(R.string.alert_dialog_message1);
            } else if (alerttype == ALERT_MESSAGE_TYPE2) {
                alertMessage = context.getResources().getString(R.string.alert_dialog_message2);
            } else if (alerttype == ALERT_MESSAGE_TYPE3) {
                alertMessage = context.getResources().getString(R.string.alert_dialog_message3_1) + getSendWaitRecordIdsCount() + context.getResources().getString(R.string.alert_dialog_message3_2);
                alertDialog.setMessage(alertMessage);
            } else if (alerttype == ALERT_MESSAGE_TYPE4) {
                alertMessage = context.getResources().getString(R.string.alert_dialog_message4_1) + getSendWaitRecordIdsCount() + context.getResources().getString(R.string.alert_dialog_message4_2);
            } else {
                alertMessage = context.getResources().getString(R.string.alert_dialog_message_other);
            }
            alertDialog.setMessage(alertMessage);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // プログラム終了
                }
            });
            alertDialog.create();
            alertHandle.post(new Runnable() {
                @Override
                public void run() {
                    alertDialog.show();
                }
            });
            alertFlag = true;
        }
    }

    public void addSendWaitRecordIds(byte[] recordid) {
        if (SendWaitRecordIds == null) {
            SendWaitRecordIds = new ArrayList<byte[]>();
        }
        SendWaitRecordIds.add(recordid);
    }

    public int getSendWaitRecordIdsCount() {
        if (SendWaitRecordIds != null) {
            return SendWaitRecordIds.size();
        } else {
            return 0;
        }
    }

    public byte[] getSendWaitRecordId(int idx) {
        byte[] wkId;
        wkId = SendWaitRecordIds.get(idx);
        SendWaitRecordIds.remove(idx);
        return wkId;
    }

    public void setGroupTab(int Tabnum) {
        GroupTab = Tabnum;
    }

    public int getGroupTab() {
        return GroupTab;
    }

    public void startChatMessageService(Context context) {
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (!groupCommon.getChatServiceStatus()) {
            //context = actMain
            Intent intent;
            intent = new Intent(context, ChatMessageService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
            groupCommon.setChatServiceStatus(true);
        }
    }

    public void stopChatMessageService(Context context) {
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }
        if (groupCommon.getChatServiceStatus()) {
            //Context context = actMain;
            Intent intent;
            intent = new Intent(context, ChatMessageService.class);
            context.stopService(intent);
            groupCommon.setChatServiceStatus(false);
        }
    }

}
