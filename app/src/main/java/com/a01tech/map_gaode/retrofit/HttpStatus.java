package com.a01tech.map_gaode.retrofit;

import com.google.gson.annotations.SerializedName;

//HttpStatus.java
public class HttpStatus {
    @SerializedName("code")
    private int mCode;
    @SerializedName("msg")
    private String mMessage;

    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    /**
     * API是否请求失败
     *
     * @return 失败返回true, 成功返回false
     */
//    public boolean isCodeInvalid() {
//        return mCode != Constants.WEB_RESP_CODE_SUCCESS;
//    }
}

