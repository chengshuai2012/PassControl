package com.link.cloud.network.bean;

import io.realm.RealmObject;

/**
 * Created by 49488 on 2018/12/6.
 */

public class UserFace extends RealmObject {
    private String feature;

    private String userId;

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
