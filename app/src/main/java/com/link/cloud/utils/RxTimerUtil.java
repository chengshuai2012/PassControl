package com.link.cloud.utils;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class RxTimerUtil {
    private static Disposable mDisposable;

    /**
     * milliseconds毫秒后执行next操作 * * @param milliseconds * @param next
     */
    public static void timer(long milliseconds, final IRxNext next) {
        Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //取消订阅
                        cancel();
                    }

                    @Override
                    public void onComplete() { //
                        // 取消订阅

                        cancel();
                    }
                });
    }

    // /** 每隔milliseconds毫秒后执行next操作 * * @param milliseconds * @param next */
    public static void interval(final long milliseconds, final IRxNext next) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                mDisposable = disposable;
            }

            @Override
            public void onNext(@NonNull Long number) {
                if (next != null) {
                    next.doNext(number);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                cancel();
                interval(milliseconds,next);
                Log.e("onError: ", e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * 取消订阅
     */

    public static void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();

        }
    }

    public interface IRxNext {
        void doNext(long number);
    }
}

