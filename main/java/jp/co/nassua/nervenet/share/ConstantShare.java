
// 端末間情報共有 外部定数一覧
//
// Copyright (C) 2016 Nassua Solutions Corp.
// NAKAMURA Takumi <takumi@nassua.co.jp>
//

package jp.co.nassua.nervenet.share;

/**
 * 端末間情報共有 外部定数一覧
 * @author takumi
 * @version 1.2.1
 */
public final class ConstantShare {
    public static final String PKG_SHARE = "jp.co.nassua.nervenet.share";
    /// アクション ///
    //要求
    public static final String ACT_START = PKG_SHARE +".START";
    public static final String ACT_STOP = PKG_SHARE +".STOP";
    public static final String ACT_PUBLISH =PKG_SHARE +".PUBLISH";
    //生存確認
    public static final String ACT_ALIVE = PKG_SHARE +".ALIVE";

    /// 要求諸元 ///
    public static final String EXTRA_TRANSPORT  = "TRANSPORT";  //伝送路
    public static final String EXTRA_TABLE      = "TABLE";      //テーブル名
    public static final String EXTRA_ID         = "ID";         //IDフィールドの内容
    public static final String EXTRA_ID_RECORD  = "ID_RECORD";  //レコードID

    // コンストラクタ
    //
    private ConstantShare() {
    }
    /**
     * このクラスのバージョンを返します。
     * @return このクラスのバージョン
     */
    public static String getVersionName() {
        final String v_name = "1.2.1";
        return v_name;
    }
    public static int getVersionCode() {
        final int v_code = 2;
        return v_code;
    }
}
