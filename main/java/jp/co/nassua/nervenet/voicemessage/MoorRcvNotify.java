package jp.co.nassua.nervenet.voicemessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import jp.co.nassua.nervenet.boat.ConstantBoat;

public class MoorRcvNotify extends BroadcastReceiver {

    private static ActMain mainActivity;
    public static final long pastLimit   = 60 * 10 * 1000;  // 時刻遅れ 10分以内
    public static final long futureLimit = 60 * 10 * 1000;  // 時刻進み 10分以内

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Context context1;
        Intent intent1;
        String cmd;
        String result;
        Long bsDate;
        long nowDate, pastDate, futureDate;

        if (mainActivity == null) {
            mainActivity = new ActMain();
        }

        // 基地局時刻取得(moor)
        if (action.equalsIgnoreCase(ConstantBoat.ACT_MOOR_NOTIFY)) {
            if (extras != null) {
                cmd = extras.getString(ConstantBoat.EXTRA_EVENT);
                if ((cmd.equalsIgnoreCase(ConstantBoat.EVENT_GETTIME) && mainActivity.getBsGetTimeFlag())) { // 基地局の時刻取得
                    Log.i("nassua", "GETTIME RESULT");
                    result = extras.getString(ConstantBoat.EXTRA_RESULT);
                    if (result.equalsIgnoreCase(ConstantBoat.RESULT_OK)) {
                        bsDate = extras.getLong(ConstantBoat.EXTRA_TIME_TSG);
                        // 時刻チェック
                        nowDate = System.currentTimeMillis();  // 端末の時刻取得
                        pastDate = nowDate - pastLimit;     // 時刻遅れ
                        futureDate = nowDate + futureLimit; // 時刻進み
                        //if ( bsDate >= nowDate ) { // 時刻誤差チェック
                        if (( bsDate >= pastDate ) && ( bsDate <= futureDate )) { // 時刻誤差チェック 10分以内
                            // 基地局の日時の方が進んでいれば、基地局の時刻を正とする。
                            // Androidには自分の日時を補正するための APIはないので端末の日時補正はできない。
                        } else { // 時刻不一致
                            // 基地局の日時の方が遅れていれば基地局の時刻補正を要求する。
                            mainActivity.saveBaseStationDate(bsDate);
                            // 基地局時刻補正要求
                            mainActivity.alertNowDate();
                        }
                    }
                    mainActivity.resetBsGetTimeFlag();  // 基地局時刻要求中解除
                } else if ((cmd.equalsIgnoreCase(ConstantBoat.EVENT_SETTIME)) && mainActivity.getBsSetTimeFlag()) { // 基地局の時刻設定
                    Log.i("nassua", "SETTIME RESULT");
                    result = extras.getString(ConstantBoat.EXTRA_RESULT);
                    if (result.equalsIgnoreCase(ConstantBoat.RESULT_OK)) { // 時刻設定成功
                        // 時刻取得要求(正しく設定されているか確認するため)
                        mainActivity.RequestBaseStationDate();
                    } else {
                    }
                    mainActivity.resetBsSetTimeFlag();  // 基地局時刻設定中解除
                } else {
                    Log.i("nassua", "RcvNotify onReceive : Moor Others.");
                }
            }
        }
    }
}
