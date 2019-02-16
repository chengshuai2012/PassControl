package com.link.cloud.utils;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.link.cloud.R;
import com.link.cloud.network.bean.AllUser;
import com.link.cloud.veune.MdDevice;
import com.link.cloud.veune.MdUsbService;
import com.link.cloud.veune.ModelImgMng;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import md.com.sdk.MicroFingerVein;

/**
 * Created by 49488 on 2018/10/15.
 */

public class Venueutils {
    public MdUsbService.MyBinder mdDeviceBinder;
    public byte[] img;
    Context context;
    private boolean bOpen = false;//设备是否打开
    private boolean ret;
    public ModelImgMng modelImgMng = new ModelImgMng();
    private int[] tipTimes = {0, 0};//后两次次建模时用了不同手指或提取特征识别时，最多重复提醒限制3次
    private int lastTouchState = 0;//记录上一次的触摸状态
    private int modOkProgress = 0;
    private final static float IDENTIFY_SCORE_THRESHOLD = 0.53f;
    private final static float MODEL_SCORE_THRESHOLD = 0.4f;
    long first;
    public  void initVenue(Context context, MdUsbService.MyBinder mdDeviceBinder,  Boolean bOpen){
        this.bOpen=bOpen;
        this.context=context;
        this.mdDeviceBinder =mdDeviceBinder;
    }
    public int getState() {
        if (!bOpen) {
            modOkProgress = 0;
            modelImgMng.reset();
            bOpen = mdDeviceBinder.openDevice(0);//开启指定索引的设备
            if (bOpen) {

            } else {


            }
        }
        int state = mdDeviceBinder.getDeviceTouchState(0);
        Log.e("getState: ",state+"" );
        if (state != 3) {
            if (lastTouchState != 0) {
                mdDeviceBinder.setDeviceLed(0, MdUsbService.getFvColorRED(), true);
            }
            lastTouchState = 0;
        }
        if (state == 3) {
            //返回值state=3表检测到了双Touch触摸,返回1表示仅指腹触碰，返回2表示仅指尖触碰，返回0表示未检测到触碰
            if (lastTouchState == 3) {
                return 4;
            }
            lastTouchState = 3;
            mdDeviceBinder.setDeviceLed(0, MdUsbService.getFvColorGREEN(), false);
            img = mdDeviceBinder.tryGetBestImg(5);
            if (img == null) {
                Logger.e("get img failed,please try again");
                TTSUtils.getInstance().speak(context.getString(R.string.again_finger));
            }
        }
        return state;
    }

    public String identifyNewImg(final List<AllUser> peoples) {
        final int nThreads = peoples.size() / 1000 + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList();
        for (int i = 0; i < nThreads; i++) {
            List<AllUser> subListPeople = new ArrayList<>();
            if (i == nThreads - 1) {
                subListPeople = peoples.subList(1000 * i, peoples.size());
            } else {
                subListPeople = peoples.subList(1000 * i, 1000 * (i + 1));
            }

            final List<AllUser> finalSubListPeople = subListPeople;
            Callable<String> task = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int[] pos = new int[1];
                    float[] score = new float[1];
                    StringBuffer sb = new StringBuffer();
                    String[] uids = new String[1000];
                    int position = 0;
                    for (AllUser userBean : finalSubListPeople) {
                        sb.append(userBean.getFingerprint());
                        uids[position] = userBean.getUuid();
                        position++;

                    }
                    byte[] allFeaturesBytes = HexUtil.hexStringToByte(sb.toString());
                    boolean identifyResult = MicroFingerVein.fv_index(allFeaturesBytes, allFeaturesBytes.length / 3352, img, pos, score);
                    identifyResult = identifyResult && score[0] > IDENTIFY_SCORE_THRESHOLD;//得分是否达标
                    if (identifyResult) {//比对通过且得分达标时打印此手指绑定的用户名
                        String uid = uids[pos[0]];
                        return uid;
                    } else {
                        return null;

                    }
                }
            };

            futures.add(executorService.submit(task));

        }
        for (Future<String> future : futures) {
            try {
                Log.d("future=", future.get() + "");
                if (!TextUtils.isEmpty(future.get())) {
                    return future.get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return null;
    }


}
