package com.link.cloud.network;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by OFX002 on 2018/3/2.
 */

public class IOMainThread {
    public static <T>ObservableTransformer<T,T> composeIO2main(){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public static <T>ObservableTransformer<T,T> composeMain2IO(){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        })
                        .observeOn(Schedulers.io());
            }
        };
    }
    public static FlowableTransformer<ResponseBody, ResponseBody> ioMainDownload(){
        return new FlowableTransformer<ResponseBody, ResponseBody>() {
            @Override
            public Publisher<ResponseBody> apply(Flowable<ResponseBody> upstream) {
                return upstream.subscribeOn(Schedulers.io()).
                        observeOn(Schedulers.io()).
                        observeOn(Schedulers.computation()).
                        map(new Function<ResponseBody, ResponseBody>() {
                            @Override
                            public ResponseBody apply(ResponseBody responseBody) throws Exception {
                                return responseBody;
                            }
                        }).
                        observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

}
