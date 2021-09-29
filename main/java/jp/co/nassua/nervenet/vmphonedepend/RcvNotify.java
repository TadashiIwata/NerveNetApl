package jp.co.nassua.nervenet.vmphonedepend;

// NerveNet端末機能 通知受信用ブロードキャストレシーバー
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import jp.co.nassua.nervenet.boat.ConstantBoat;
import jp.co.nassua.nervenet.Dialing.dialing;
import jp.co.nassua.nervenet.phone.ConstantPhone;
import jp.co.nassua.nervenet.share.ConstantShare;
import jp.co.nassua.nervenet.song.ConstantSong;
import jp.co.nassua.nervenet.voicemessage.ActMain;
import jp.co.nassua.nervenet.voicerecorder.VoiceMessage;

//// ブロードキャスト受信 ////

public class RcvNotify extends BroadcastReceiver {
    private static String curphase = null;
    private String sipuri;
    // 受信処理
    //
    @Override
    public void onReceive( Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        //Log.i("nassua", "RcvNotify onReceive");

        //基地局監視通知
        if (action.equalsIgnoreCase(ConstantBoat.ACT_TSG_NOTIFY)) {
            if (dialing.actMyself != null) {
                dialing.actMyself.actTsg(extras);
            } else if (ActRinging.actMyself != null) {
                ActRinging.actMyself.actTsg(extras);
            }
        //通話通知
        } else if (action.equalsIgnoreCase( ConstantPhone.ACT_NOTIFY )) {
            // NerveNet通話画面が起こされていれば通知する
            if (dialing.actMyself != null) {
                dialing.actMyself.actPhone(extras);
            } else {
                Bundle wkfields = null;
                String phase = null;
                wkfields = intent.getExtras();
                if (wkfields != null) {
                    phase = wkfields.getString(ConstantPhone.EXTRA_PHASE);
                    Log.i("nassua", "Phase=" + phase + " / Current Phase=" + curphase);
                    if (!(phase.equalsIgnoreCase(curphase))) {
                        if (phase.equalsIgnoreCase(ConstantPhone.PHASE_IDLE)) {
                            if ((curphase != null)
                            && (curphase.equalsIgnoreCase(ConstantPhone.PHASE_INCOMMING))) {
                                // 着信中に切断された時の処理。着信画面の有無を確認する。
                                if (ActRinging.actMyself != null) {
                                    ActRinging.actMyself.cancelCalled();
                                } else {
                                    Log.i("nassua", "ActRinging not started.");
                                }
                                curphase = phase;
                                return;
                            }
                        }
                        sipuri = wkfields.getString(ConstantPhone.EXTRA_URI_THERE);
                        if (phase.equals(ConstantPhone.PHASE_INCOMMING)) {
                            // 録音中は着信を拒否する。
                            if (VoiceMessage.actMyself != null) {
                                VoiceMessage.actMyself.Recording_Called_cancel();
                                return;
                            }
                            // 着信処理

                            if (ActRinging.actMyself == null) {
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(context, ActRinging.class);
                                // あくてぃびでぃが存在しない場合に生成
                                context.startActivity(intent);
                            } else {
                                Log.i("nassua", "AcrRinging is already started.");
                            }
                        } else if (ActRinging.actMyself != null) {
                            ActRinging.actMyself.actPhone(extras);
                        }
                        curphase = phase;
                    }
                }

            }
            //}
        //生存確認ブロードキャストへの応答
        } else if (action.equalsIgnoreCase(ConstantShare.ACT_ALIVE)) {
            // 端末間情報共有を使用中であることを通知
            setResultCode(Activity.RESULT_OK);
        } else if (action.equalsIgnoreCase( ConstantSong.ACT_ALIVE )) {
            //基地局間同期データを使用中であることを通知
            setResultCode( Activity.RESULT_OK );
        }
    }
}
