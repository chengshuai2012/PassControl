package com.link.cloud.network.bean;

/**
 * Created by chengshuai on 2019/4/11.
 */

public class VerifyFaceBean {
    String faceBase64;
    String deviceId;

    public String getFaceBase64() {
        return faceBase64;
    }

    public void setFaceBase64(String faceBase64) {
        this.faceBase64 = faceBase64;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
