package com.link.cloud.network;

import com.link.cloud.network.bean.APPVersionBean;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CheckInRequest;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.network.bean.RequestBindFinger;
import com.link.cloud.network.bean.SingleUser;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Created by OFX002 on 2018/10/28.
 */

public interface BaseService {
    /**
     * 登陆接口
     */

    @POST(ApiConstants.APPLOGIN)
    Observable<BaseEntity<CabnetDeviceInfoBean>> appLogin(@Path("deviceCode") String deviceCode, @Path("password") String password);


    /**
     * 获取用户
     */
    @POST(ApiConstants.GETUSERS)
    Observable<BaseEntity<BindUser>> getUser(@Body RequestBindFinger requestBindFinger);



    /**
     * 获取单个用户指静脉
     */
    @GET(ApiConstants.GETSINGLEUSER)
    @Headers("Content-Type:application/json;charset=utf-8")
    Observable<BaseEntity<SingleUser>> findOneUserFinger(@Path("uuid") String uuid);


    /**
     * 开门
     */
    @POST(ApiConstants.CHECKIN)
    Observable<BaseEntity<CheckInBean>> checkIn(@Path("type") int type, @Body CheckInRequest checkInRequest);
  /**
     * 密码
     */
    @POST(ApiConstants.VALIDATEPASS)
    Observable<BaseEntity<PasswordBean>> validatePass(@Path("password") String password);

    /**
     * 二维码开门
     */
    @POST(ApiConstants.QROPENDOOR)
    Observable<BaseEntity> openDoorByQr(@Path("qr") String qr);
    /**
     * APP版本
     */
    @GET(ApiConstants.APPVERSION)
    Observable<BaseEntity<APPVersionBean>> getAppVersion(@Path("appType") int type);
    /**
     * 获取最新App
     */
    @Streaming
    @Headers("Content-Type:application/force-download")
    @GET(ApiConstants.DOWNLOAD)
    Flowable<ResponseBody> getApp(@Path("appType") int type);



}
