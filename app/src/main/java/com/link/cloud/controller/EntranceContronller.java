package com.link.cloud.controller;

import android.util.Log;

import com.link.cloud.base.Constants;
import com.link.cloud.network.BaseEntity;
import com.link.cloud.network.BaseObserver;
import com.link.cloud.network.BaseService;
import com.link.cloud.network.IOMainThread;
import com.link.cloud.network.RetrofitFactory;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CheckInRequest;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.network.bean.RequestBindFinger;

/**
 * Created by 49488 on 2018/11/12.
 */

public class EntranceContronller {
    EntranceControllerListener listener;

    public interface EntranceControllerListener {

        void onMainErrorCode(String msg);

        void onMainFail(Throwable e, boolean isNetWork);

        void getUserSuccess(BindUser data);

        void CheckInSuccess(CheckInBean data);
        void passSuccess(PasswordBean data);



    }

    private BaseService api;

    public EntranceContronller(EntranceControllerListener listener) {
        api = RetrofitFactory.getInstence().API();
        this.listener = listener;
    }


    public void getUser(int Page) {
        RequestBindFinger requestBindFinger = new RequestBindFinger();
        requestBindFinger.setContent("CHINA00001");
        requestBindFinger.setPageNo(Page);
        requestBindFinger.setPageSize(Constants.PAGE_NUM);
        api.getUser(requestBindFinger).compose(IOMainThread.<BaseEntity<BindUser>>composeIO2main()).subscribe(new BaseObserver<BindUser>() {
            @Override
            protected void onSuccees(BaseEntity<BindUser> t)  {
                listener.getUserSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg,String codeErrorr)  {
                listener.onMainErrorCode(msg);

            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });

    }

    public void checkIn(String uid,String finger,int type){
        CheckInRequest request = new CheckInRequest();
        request.setUuid(uid);
        request.setFingerprint(finger);
        api.checkIn(type,request).compose(IOMainThread.<BaseEntity<CheckInBean>>composeIO2main()).subscribe(new BaseObserver<CheckInBean>() {

            @Override
            protected void onSuccees(BaseEntity<CheckInBean> t) {
                listener.CheckInSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(msg);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });
    }
    public void password(String pass){
        api.validatePass(pass).compose(IOMainThread.<BaseEntity<PasswordBean>>composeIO2main()).subscribe(new BaseObserver<PasswordBean>() {


            @Override
            protected void onSuccees(BaseEntity<PasswordBean> t) {
                listener.passSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(msg);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });
    }
    public void openDoorQr(String qrCode){
        api.openDoorByQr(qrCode).compose(IOMainThread.<BaseEntity>composeIO2main()).subscribe(new BaseObserver() {


            @Override
            public void onNext(Object o) {

            }

            @Override
            protected void onSuccees(BaseEntity t) {

            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {

            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {

            }
        });}
}

