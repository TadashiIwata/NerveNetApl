package jp.co.nassua.nervenet.vmphonedepend;

// Activity間発信先 URI設定、取得API
//
// Copyright (C) 2016 Nassua Solutions Corp.
// Iwata Tadashi <iwata@nassua.co.jp>
//
public class CallUri {
    private static String callSipUri;
    private static String displaydialingnumber;
    private static String displaycallnumber;


    public void setCallSipUri(String calluri) {
        callSipUri = calluri;
    }

    public String getCallSipUri() {
        return callSipUri;
    }

    public void setDisplayDialingNumber(String dialingnum) {
        displaydialingnumber = dialingnum;
    }

    public String getDisplayDialingNumber() {
        return displaydialingnumber;
    }

    public void setDisplayCallNumber(String callnum) {
        displaycallnumber = callnum;
    }

    public String getDisplayCallNumber() {
        return displaycallnumber;
    }


}
