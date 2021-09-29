package jp.co.nassua.nervenet.groupchatmain;

import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QrcodeCaptureActivity  extends CaptureActivity {

    /*
    static GroupCommon groupCommon;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String code;

        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && (code = result.getContents()) != null) {
            String QRCode = result.getContents();
            groupCommon.setQrcodeSipUri(QRCode);
        } else {
            groupCommon.setQrcodeSipUri(null);
        }
    }
    */
}

