package com.link.cloud.network.bean;

/**
 * Created by 49488 on 2018/10/28.
 */

public class CheckInRequest {

    /**
     * createTime : 2018-10-28T05:56:03.950Z
     * fingerprint : string
     * id : 0
     * isadmin : 0
     * merchantId : 0
     * userType : 0
     * uuid : string
     */

    private String createTime;
    private String fingerprint;
    private int id;
    private int isadmin;
    private int merchantId;
    private int userType;
    private String uuid;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsadmin() {
        return isadmin;
    }

    public void setIsadmin(int isadmin) {
        this.isadmin = isadmin;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
