package jp.co.nassua.nervenet.playmessage;

import android.os.CountDownTimer;

/**
 * Created by I.Tadshi on 2016/12/07.
 */

public class PlaybackTimer extends CountDownTimer {
    private static PlayMessageCommon pmc = new PlayMessageCommon();

    public PlaybackTimer(long playMaxtime, long playInterval) {
        super(playMaxtime, playInterval);
    }

    @Override
    public void onFinish() {
        if (pmc.getPlayStatus().equalsIgnoreCase(ConstantPlayMessage.STAT_PLAY)) {
            // 再生中ならば再生を停止させる。
            pmc.VoiceMessagePlaybackCancel();
        }
    }

    @Override
    public void onTick(long playUntilFinished) {
        // 1秒ごとに再生が終了しているかチェックする。
        pmc.checkPlaybackStatus();
    }

}
