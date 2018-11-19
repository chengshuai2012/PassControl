package com.link.cloud.network;

/**
 * Created by OFX002 on 2018/10/28.
 */

public class BaseEntity<T> {
    private static String SUCCESS_CODE="200000";
    private String code;
    private String message;
    private String secondMessage;
    private T data;

    public String getSecondMessage() {
        return secondMessage;
    }

    public void setSecondMessage(String secondMessage) {
        this.secondMessage = secondMessage;
    }

    public boolean isSuccess(){
        return getCode().equals(SUCCESS_CODE);
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }





}
