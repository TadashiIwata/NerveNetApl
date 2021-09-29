package jp.co.nassua.nervenet.voicerecorder;

/**
 * Created by I.Tadshi on 2016/07/14.
 */
public class VoiceMessageNotify {
    public final static int STAT_SEND_EXEC = 0;      //メッセージ送信中
    public final static int STAT_SEND_COMPLETE = 1; //メッセージ送信完了

    // イベントリスナー
    private VoiceMessage.VoiceMessageEventListener listener = null;

    public void SendMessageNotify(int cmd) {
        switch (cmd) {
            case STAT_SEND_EXEC : {
                listener.SendMessageExec();
                break;
            }
            case STAT_SEND_COMPLETE: {
                listener.SendMessageComplete();
                break;
            }
        }
    }

    /**
     * 送信完了イベントリスナーを追加する。
     * @param listener
     */
    public void setSendMessageListener(VoiceMessage.VoiceMessageEventListener listener) {
        this.listener = listener;
    }

    /**
     * 送信完了イベントリスナーを削除する
     */
    public void removeSendMessageListener() {
        this.listener = null;
    }
}
