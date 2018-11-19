package com.link.cloud.network.bean;

/**
 * Created by 49488 on 2018/11/15.
 */

public class APPVersionBean {

    /**
     * id : 9
     * version : 2
     * appType : 2
     * createTime : 2018-11-15 14:54:04
     */

    private int id;
    private String version;
    private int appType;
    private String createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
