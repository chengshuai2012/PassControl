package com.link.cloud.controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.link.cloud.base.Constants;
import com.link.cloud.network.BaseEntity;
import com.link.cloud.network.BaseObserver;
import com.link.cloud.network.BaseService;
import com.link.cloud.network.IOMainThread;
import com.link.cloud.network.RetrofitFactory;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CheckInByOther;
import com.link.cloud.network.bean.CheckInLogRequest;
import com.link.cloud.network.bean.CheckInRequest;
import com.link.cloud.network.bean.CodeBean;
import com.link.cloud.network.bean.CodeInBean;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.network.bean.RequestBindFinger;
import com.link.cloud.network.bean.YuanGuMessage;


import java.io.File;
import java.text.SimpleDateFormat;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by 49488 on 2018/11/12.
 */

public class EntranceContronller {
    EntranceControllerListener listener;

    public interface EntranceControllerListener {

        void onMainErrorCode(String msg,String errorCode);

        void onMainFail(Throwable e, boolean isNetWork);

        void getUserSuccess(BindUser data);

        void CheckInSuccess(CheckInBean data);
        void passSuccess(PasswordBean data);
        void CodeInSuccess(CodeInBean data);
        void onLoginSuccess(CabnetDeviceInfoBean cabnetDeviceInfoBean);
        void CheckInLogSuccess(CheckInBean data);
        void YuanGuSuccess(YuanGuMessage yuanGuMessage);

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
                listener.onMainErrorCode(msg,codeErrorr);

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
        long createTime = System.currentTimeMillis();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time1=new Long(createTime);
        String d = format.format(time1);
        request.setCreateTime(d);
        api.checkIn(type,request).compose(IOMainThread.<BaseEntity<CheckInBean>>composeIO2main()).subscribe(new BaseObserver<CheckInBean>() {

            @Override
            protected void onSuccees(BaseEntity<CheckInBean> t) {
                listener.CheckInSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(msg,codeErrorr);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });
    }
    public void checkInLog(String uid,String finger,int type,int validType){
        CheckInLogRequest request = new CheckInLogRequest();
        request.setUuid(uid);
        request.setFingerprint(finger);
        long createTime = System.currentTimeMillis();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time1=new Long(createTime);
        String d = format.format(time1);
        request.setGateTime(d);
        request.setType(type);
        request.setValidType(validType);
        api.checkInLog(request).compose(IOMainThread.<BaseEntity<CheckInBean>>composeIO2main()).subscribe(new BaseObserver<CheckInBean>() {

            @Override
            protected void onSuccees(BaseEntity<CheckInBean> t) {
                listener.CheckInLogSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(msg,codeErrorr);
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
                listener.onMainErrorCode(msg,codeErrorr);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });
    }
    public void openDoorQr(String qrCode,int InOrOut){
        int type =0;
        try {
             Long.parseLong(qrCode);
             type=4;
        } catch (Exception e) {
            type=3;
            e.printStackTrace();
        }
        long createTime = System.currentTimeMillis();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time1=new Long(createTime);
        String d = format.format(time1);
        CheckInByOther checkInByOther = new CheckInByOther();
        checkInByOther.setOther(qrCode);
        checkInByOther.setValidType(type);
        checkInByOther.setType(InOrOut);
        checkInByOther.setGateTime(d);
        api.openDoorByQr(checkInByOther).compose(IOMainThread.<BaseEntity<CodeInBean>>composeIO2main()).subscribe(new BaseObserver<CodeInBean>() {


            @Override
            protected void onSuccees(BaseEntity<CodeInBean> t) {
                listener.CodeInSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(msg,codeErrorr);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });}
    public void login(String userNmae, String password) {
        api.appLogin(userNmae, password)
                .compose(IOMainThread.<BaseEntity<CabnetDeviceInfoBean>>composeIO2main())
                .subscribe(new BaseObserver<CabnetDeviceInfoBean>() {
                    @Override
                    protected void onSuccees(BaseEntity<CabnetDeviceInfoBean> t)  {
                        listener.onLoginSuccess(t.getData());
                    }

                    @Override
                    protected void onCodeError(String msg,String codeErrorr) {
                        listener.onMainErrorCode(msg,codeErrorr);
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError)  {
                        listener.onMainFail(e, isNetWorkError);
                    }
                });
    }
    public void checkYuanguFace(String deviceNo,String Mac,String filePath){

        RequestBody requestDeviceNo = RequestBody.create(MediaType.parse("multipart/form-data"), deviceNo);
        RequestBody requestMac = RequestBody.create(MediaType.parse("multipart/form-data"), Mac);
        File file =new File(filePath);
        RequestBody requestImgFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part requestImgPart = MultipartBody.Part.createFormData("files", file.getName(), requestImgFile);
        api.CheckInYuangu(requestDeviceNo,requestMac,requestImgPart).compose(IOMainThread.<YuanGuMessage>composeIO2main()).subscribe(new Observer<YuanGuMessage>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e( "onSubscribe: ", "start");
            }

            @Override
            public void onNext(YuanGuMessage yuanGuMessage) {
                listener.YuanGuSuccess(yuanGuMessage);
            }

            @Override
            public void onError(Throwable e) {
                Log.e( "onError: ",e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e( "onComplete: ","");
            }
        });
    };

}

