package com.link.cloud.network;

import android.accounts.NetworkErrorException;
import android.content.Context;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by OFX002 on 2018/10/28.
 */

public abstract class BaseObserver<T> implements Observer<BaseEntity<T>> {
    protected Context mContext;

    public BaseObserver(Context cxt) {
        this.mContext = cxt;
    }

    public BaseObserver() {

    }

    @Override
    public void onSubscribe(Disposable d) {
        onRequestStart();

    }

    @Override
    public void onNext(BaseEntity<T> tBaseEntity) {
        if (tBaseEntity.isSuccess()) {
            onSuccees(tBaseEntity);
        } else {
            if (tBaseEntity.getCode().equals("400000502001")) {
                onCodeError(tBaseEntity.getSecondMessage(), tBaseEntity.getCode());
            } else {
                onCodeError(tBaseEntity.getSecondMessage() + tBaseEntity.getMsg(), tBaseEntity.getCode());
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        try {
            if (e instanceof ConnectException || e instanceof TimeoutException || e instanceof NetworkErrorException || e instanceof UnknownHostException) {
                onFailure(e, true);
            } else {
                onFailure(e, false);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onComplete() {

    }

    /**
     * 返回成功
     *
     * @param t
     * @throws Exception
     */
    protected abstract void onSuccees(BaseEntity<T> t);

    /**
     * 返回成功了,但是code错误
     *
     * @param
     * @throws Exception
     */
    protected abstract void onCodeError(String msg, String codeErrorr);

    /**
     * 返回失败
     *
     * @param e
     * @param isNetWorkError 是否是网络错误
     * @throws Exception
     */
    protected abstract void onFailure(Throwable e, boolean isNetWorkError);

    protected void onRequestStart() {

    }


}
