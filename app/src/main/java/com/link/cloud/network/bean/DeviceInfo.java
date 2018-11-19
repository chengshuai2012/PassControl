package com.link.cloud.network.bean;

import io.realm.RealmObject;

/**
 * Created by 49488 on 2018/10/28.
 */

public class DeviceInfo extends RealmObject {
    String deviceId;
    String psw;
    String token;
    int deviceTypeId;
    int face;

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getQcode() {
        return qcode;
    }

    public void setQcode(int qcode) {
        this.qcode = qcode;
    }

    public int getVeune() {
        return veune;
    }

    public void setVeune(int veune) {
        this.veune = veune;
    }

    int qcode;
    int veune;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    String deviceType;
    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
