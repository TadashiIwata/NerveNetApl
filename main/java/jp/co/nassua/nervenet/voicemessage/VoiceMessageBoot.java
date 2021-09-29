package jp.co.nassua.nervenet.voicemessage;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import jp.co.nassua.nervenet.boat.DbDefineBoat;
import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.phone.DbDefinePhone;
import jp.co.nassua.nervenet.service.BoxVoice;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.vmphonedepend.BoatApi;
import jp.co.nassua.nervenet.vmphonedepend.UserProfilePermission;
import jp.co.nassua.nervenet.voicerecorder.VoiceMessageSubFunctions;

//import jp.co.nassua.nervenet.phonedepend.PrefApp;
//import jp.co.nassua.nervenet.playmessage.PlayMessageService;

/**
 * Created by I.Tadshi on 2016/07/21.
 */
public class VoiceMessageBoot extends BroadcastReceiver {
    private static ActMain actMain;

    // 受信処理
    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.i("nassua", "VoiceMessageBoot onReceive");

        String action = intent.getAction();
        //OSブート完了通知?
        if (action.equalsIgnoreCase( Intent.ACTION_BOOT_COMPLETED )) {
            android.util.Log.i("nassua", "VoiceMessage boot process complete.");
            actMain = new ActMain();
            actMain.bootStart(context);
        } else {
            android.util.Log.i("nassua", "Others boot action received.");
        }
    }
}
