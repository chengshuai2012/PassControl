package com.link.cloud.network.bean;

/**
 * 作者：qianlu on 2018/11/5 17:22
 * 邮箱：zar.l@qq.com
 */
public class CabnetDeviceInfoBean {
    private DeviceInfoBean deviceInfo;
    private String token;

    public DeviceInfoBean getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfoBean deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
