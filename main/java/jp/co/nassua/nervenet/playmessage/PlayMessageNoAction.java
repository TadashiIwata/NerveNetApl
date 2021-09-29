package jp.co.nassua.nervenet.playmessage;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import jp.co.nassua.nervenet.voicemessage.R;

/**
 * Created by I.Tadshi on 2016/09/14.
 */
public class PlayMessageNoAction extends Activity {
    private static final int NOTIFICATION_ID = R.layout.activity_act_main;

    @Override
    protected void  onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
