package com.baidu.aip.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.aip.ui.Activation;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.NetRequest;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceConfig;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.license.AndroidLicenser;

import org.json.JSONException;
import org.json.JSONObject;

public class FaceSDKManager {

    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_FAIL = 5;

    public static final String LICENSE_NAME = "com-link-cloud-face-android";
    private FaceDetector faceDetector;
    private FaceFeature faceFeature;
    private Context context;
    private SdkInitListener sdkInitListener;
    public static volatile int initStatus = SDK_UNACTIVATION;
    private Handler handler = new Handler(Looper.getMainLooper());

    private FaceSDKManager() {
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeature();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {

        return HolderClass.instance;
    }
    private  String key;
    public void setKey (String key){

        this.key = key;
    }
    public int initStatus() {
        return initStatus;
    }

    public void setSdkInitListener(SdkInitListener sdkInitListener) {
        this.sdkInitListener = sdkInitListener;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }


    /**
     * FaceSDK 初始化，用户可以根据自己的需求实例化FaceTracker 和 FaceRecognize
     *
     * @param context
     */
    public void init(final Context context) {
        this.context = context;
        PreferencesUtil.initPrefs(context);
        if (!check()) {
            initStatus = SDK_UNACTIVATION;
            return;
        }

        if (TextUtils.isEmpty(key)) {
            Toast.makeText(context, "激活序列号为空, 请先激活", Toast.LENGTH_SHORT).show();
            return;
        }

        initStatus = SDK_INITING;
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                if (sdkInitListener != null) {
                    sdkInitListener.initStart();
                }
                Log.e("FaceSDK", "初始化授权");
                FaceSDK.initLicense(context, key, LICENSE_NAME, false);
                if (!sdkInitStatus()) {
                    return;
                }
                Log.e("FaceSDK", "初始化sdk");
                faceDetector.init(context);
                faceFeature.init(context);
                initLiveness(context);
                if (sdkInitListener != null) {
                    sdkInitListener.initSuccess();
                }
            }
        });
    }

    /**
     * 初始化 活体检测
     *
     * @param context
     */
    private void initLiveness(Context context) {
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_VIS, 2);
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_IR);
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_DEPTH);
    }

    private boolean sdkInitStatus() {
        boolean success = false;
        int status = FaceSDK.getAuthorityStatus();
        if (status == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
            initStatus = SDK_INITED;
            success = true;
            faceDetector.setInitStatus(initStatus);
            Log.e("FaceSDK", "授权成功");


        } else if (status == AndroidLicenser.ErrorCode.LICENSE_EXPIRED.ordinal()) {
            initStatus = SDK_FAIL;
            // FileUitls.deleteLicense(context, LICENSE_NAME);
            Log.e("FaceSDK", "授权过期");
            if (sdkInitListener != null) {
                sdkInitListener.initFail(status, "授权过期");
            }
        } else {
            initStatus = SDK_FAIL;
            // FileUitls.deleteLicense(context, LICENSE_NAME);
            Log.e("FaceSDK", "授权失败" + status);
            if (sdkInitListener != null) {
                sdkInitListener.initFail(status, "授权失败");
            }
        }
        return success;
    }

    private void request(final String key) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                netRequest(key);
            }
        });
    }
    private void netRequest(final String key) {
        if (NetRequest.isConnected(context)) {
            boolean success = NetRequest.request(new NetRequest.RequestAdapter() {

//                public String getURL() {
//                    return "http://10.94.234.54:8087/activation/key/activate";
//                }

                public String getURL() {
                    return "https://ai.baidu.com/activation/key/activate";
                }
                String device = AndroidLicenser.get_device_id(context.getApplicationContext());
                public String getRequestString() {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("deviceId", device);
                        jsonObject.put("key", key);
                        jsonObject.put("platformType", 2);
                        jsonObject.put("version", "3.4.2");

                        return jsonObject.toString();
                    } catch (JSONException var10) {
                        var10.printStackTrace();
                        return null;
                    }
                }

                public void parseResponse(InputStream in) throws IOException, JSONException {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    try {
                        int e;
                        while ((e = in.read(buffer)) > 0) {
                            out.write(buffer, 0, e);
                        }
                        out.flush();
                        JSONObject json = new JSONObject(new String(out.toByteArray(), "UTF-8"));
                        Log.i("wtf", "netRequest->" + json.toString());
                        int errorCode = json.optInt("error_code");
                        if (errorCode != 0) {
                            String errorMsg = json.optString("error_msg");
                            toast(errorMsg);
                        } else {
                            parse(json, key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("激活失败");
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException var12) {
                                var12.printStackTrace();
                            }
                        }
                    }
                }
            });

//            if (!success) {
//                toast("激活失败");
//            }
        } else {
            toast("没有连接网络");
        }
    }
    private void parse(JSONObject json, String key) {
        boolean success = false;
        JSONObject result = json.optJSONObject("result");
        if (result != null) {
            String license = result.optString("license");
            if (!TextUtils.isEmpty(license)) {
                String[] licenses = license.split(",");
                if (licenses != null && licenses.length == 2) {
                    PreferencesUtil.putString("activate_key", key);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(licenses[0]);
                    list.add(licenses[1]);
                    success = FileUitls.c(context, FaceSDKManager.LICENSE_NAME, list);
                }
            }
        }

        if (success) {
            initStatus = SDK_UNINIT;
            Log.i("wtf", "activation callback");
            init(context);
            toast("激活成功");
        } else {
            toast("激活失败");
        }
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean check() {
        if (!FileUitls.checklicense(context, LICENSE_NAME)) {
            request(key);
            return false;
        } else {
            return true;
        }
    }



    public interface SdkInitListener {

        public void initStart();

        public void initSuccess();

        public void initFail(int errorCode, String msg);
    }


}