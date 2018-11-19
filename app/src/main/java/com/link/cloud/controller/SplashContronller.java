package com.link.cloud.controller;

import android.os.Environment;
import android.util.Log;

import com.link.cloud.base.Constants;
import com.link.cloud.network.BaseEntity;
import com.link.cloud.network.BaseObserver;
import com.link.cloud.network.BaseService;
import com.link.cloud.network.FileDownLoadSubscriber;
import com.link.cloud.network.IOMainThread;
import com.link.cloud.network.RetrofitFactory;
import com.link.cloud.network.bean.APPVersionBean;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.RequestBindFinger;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

import static com.link.cloud.network.IOMainThread.ioMainDownload;

/**
 * Created by 49488 on 2018/11/12.
 */

public class SplashContronller {
    SplashControllerListener listener;

    public interface SplashControllerListener {
        void onLoginSuccess(CabnetDeviceInfoBean cabnetDeviceInfoBean);

        void onMainErrorCode(String msg);

        void onMainFail(Throwable e, boolean isNetWork);

        void getUserSuccess(BindUser data);

        void getVersionSuccess(APPVersionBean data);

    }

    private BaseService api;

    public SplashContronller(SplashControllerListener listener) {
        api = RetrofitFactory.getInstence().API();
        this.listener = listener;
    }

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
                        listener.onMainErrorCode(codeErrorr);
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError)  {
                        listener.onMainFail(e, isNetWorkError);
                    }
                });
    }

    public void getUser( int Page) {
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
                listener.onMainErrorCode(codeErrorr);

            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });

    }
    public void getAppVersion(){
        api.getAppVersion(2).compose(IOMainThread.<BaseEntity<APPVersionBean>>composeIO2main()).subscribe(new BaseObserver<APPVersionBean>() {

            @Override
            protected void onSuccees(BaseEntity<APPVersionBean> t) {
                listener.getVersionSuccess(t.getData());
            }

            @Override
            protected void onCodeError(String msg, String codeErrorr) {
                listener.onMainErrorCode(codeErrorr);
            }

            @Override
            protected void onFailure(Throwable e, boolean isNetWorkError) {
                listener.onMainFail(e, isNetWorkError);
            }
        });
    }
    String TAG ="download";
    public void downloadFile() {
            File file = new File(Environment.getExternalStorageDirectory()+"/lingxi.apk");
            api.getApp(2).
                    compose(ioMainDownload()).
                    subscribeWith(new FileDownLoadSubscriber(file){
                        @Override
                        public void onError(Throwable t) {
                            super.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                        }

                    });

    }


}

