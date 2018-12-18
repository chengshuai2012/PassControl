package com.link.cloud.network.bean;

import io.realm.RealmObject;

/**
 * Created by 49488 on 2018/12/6.
 */

public class UserFace extends RealmObject {
    private String face;

    private String uuid;

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
