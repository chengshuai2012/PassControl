package com.link.cloud.network.bean;

/**
 * Created by 49488 on 2018/10/28.
 */

public class CheckInByOther {
    private String gateTime;
    private String fingerprint;
    private String other;
    private int type;

    public int getValidType() {
        return validType;
    }

    public void setValidType(int validType) {
        this.validType = validType;
    }

    private int validType;
    private String uuid;

    public String getGateTime() {
        return gateTime;
    }

    public void setGateTime(String gateTime) {
        this.gateTime = gateTime;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
