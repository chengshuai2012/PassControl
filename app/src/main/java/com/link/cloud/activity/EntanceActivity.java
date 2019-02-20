package com.link.cloud.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Power;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.face.ArgbPool;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceRecognize;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.link.cloud.R;
import com.link.cloud.base.BaseActivity;
import com.link.cloud.base.Constants;
import com.link.cloud.base.PassControlApplication;
import com.link.cloud.controller.EntranceContronller;
import com.link.cloud.gpiotest.Gpio;
import com.link.cloud.network.HttpConfig;
import com.link.cloud.network.bean.AllUser;
import com.link.cloud.network.bean.AllUserFaceBean;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CodeInBean;
import com.link.cloud.network.bean.DeviceInfo;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.network.bean.UserFace;
import com.link.cloud.utils.DialogUtils;
import com.link.cloud.utils.HexUtil;
import com.link.cloud.utils.NettyClientBootstrap;
import com.link.cloud.utils.RxTimerUtil;
import com.link.cloud.utils.TTSUtils;
import com.link.cloud.utils.Utils;
import com.link.cloud.utils.Venueutils;
import com.link.cloud.veune.MdDevice;
import com.link.cloud.veune.MdUsbService;
import com.link.cloud.widget.CameraFrameData;
import com.link.cloud.widget.CameraGLSurfaceView;
import com.link.cloud.widget.CameraSurfaceView;
import com.link.cloud.widget.ClipView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import md.com.sdk.MicroFingerVein;

/**
 * Created by 49488 on 2018/10/16.
 */

public class EntanceActivity extends BaseActivity implements EntranceContronller.EntranceControllerListener, View.OnClickListener, View.OnTouchListener, CameraSurfaceView.OnCameraListener {
    @BindView(R.id.face_ll)
    LinearLayout faceLl;
    @BindView(R.id.qrcode_ll)
    LinearLayout qrcodeLl;
    @BindView(R.id.veune_ll)
    LinearLayout veuneLl;
    @BindView(R.id.code_number)
    EditText code_mumber;
    @BindView(R.id.manager)
    TextView manager;
    @BindView(R.id.surfaceView)
    CameraSurfaceView surfaceView;
    @BindView(R.id.sv_camera_surfaceview)
    CameraGLSurfaceView svCameraSurfaceview;
    @BindView(R.id.clipView)
    ClipView clipView;
    private RealmResults<AllUser> all;
    private RealmResults<AllUser> managersRealm;
    List<AllUser> peoples = new ArrayList<>();
    List<AllUser> managers = new ArrayList<>();
    private EntranceContronller entranceContronller;
    String uid;
    boolean IsNoPerson = false;
    boolean isDeleteAll = false;
    boolean isDeleteAllFace = false;
    String gpiotext = "1067";
    private String deviceType;
    int total, direction, deviceTypeId;
    int totaFace;
    private DialogUtils dialogUtils;
    private NettyClientBootstrap nettyClientBootstrap;
    Venueutils venueutils;
    public MdUsbService.MyBinder mdDeviceBinder;
    private DeviceInfo deviceInfo;
    private int mWidth;
    private int mHeight;
    private int mFormat;
    private Camera mCamera;
    private ArgbPool argbPool = new ArgbPool();
    FaceRecognize faceRecognize ;
    private int face;
    private int qcode;
    private int veune;
    private DeviceInfo first;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initViews() {
        entranceContronller = new EntranceContronller(this);
        first = realm.where(DeviceInfo.class).findFirst();
        if(first !=null){
            deviceType = first.getDeviceType();
            face = first.getFace();
            qcode = first.getQcode();
            veune = first.getVeune();
            deviceTypeId = first.getDeviceTypeId();
        }

        if (face == 1) {
            faceLl.setVisibility(View.GONE);
        }else {
            entranceContronller.getUserFace(1,first.getDeviceId());
            FaceSDKManager.getInstance().setKey(first.getBaiduKey());
            FaceSDKManager.getInstance().init(this);
            FaceEnvironment faceEnvironment = new FaceEnvironment();
            FaceSDKManager.getInstance().getFaceDetector().setFaceEnvironment(faceEnvironment);
            FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
                @Override
                public void initStart() {
                    Log.e(TAG, "initStart: ");
                }

                @Override
                public void initSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            faceRecognize = new FaceRecognize(EntanceActivity.this);
                            // RECOGNIZE_LIVE普通生活照、视频帧识别模型（包含特征抽取）
                            // RECOGNIZE_ID_PHOTO 身份证芯片模型（包含特征抽取）
                            // RECOGNIZE_NIR 近红外图片识别模型（包含特征抽取）
                            // 两张图片的识别需要使用相同的模型
                            faceRecognize.initModel(FaceSDK.RecognizeType.RECOGNIZE_LIVE);
                        }
                    });
                }

                @Override
                public void initFail(int errorCode, String msg) {
                    Log.e(TAG, "initFail: ");
                }
            });
        }
        if (qcode == 1) {
            qrcodeLl.setVisibility(View.GONE);
        }else {
            initData();
        }
        if (veune == 1) {
            veuneLl.setVisibility(View.GONE);
        }else {
            venueutils = PassControlApplication.getVenueUtils();
            Intent intent = new Intent(this, MdUsbService.class);
            bindService(intent, mdSrvConn, Service.BIND_AUTO_CREATE);
        }

        switch (deviceTypeId) {
            case 1006:
            case 1008:
                direction = 1;
                break;
            case 1007:
            case 1009:
                direction = 2;
                break;
        }
        all = realm.where(AllUser.class).findAll();
        managersRealm = realm.where(AllUser.class).equalTo("isadmin", 1).findAll();
        peoples.addAll(realm.copyFromRealm(all));
        managers.addAll(realm.copyFromRealm(managersRealm));
        all.addChangeListener(new RealmChangeListener<RealmResults<AllUser>>() {
            @Override
            public void onChange(RealmResults<AllUser> allUsers) {
                peoples.clear();
                peoples.addAll(realm.copyFromRealm(allUsers));
            }
        });
        managersRealm.addChangeListener(new RealmChangeListener<RealmResults<AllUser>>() {
            @Override
            public void onChange(RealmResults<AllUser> allUsers) {
                managers.clear();
                managers.addAll(realm.copyFromRealm(allUsers));
            }
        });
        dialogUtils = DialogUtils.getDialogUtils(this);
        manager.setOnClickListener(this);

        RegisteReciver();
        nettyClientBootstrap = new NettyClientBootstrap(EntanceActivity.this, Constants.TCP_PORT, Constants.TCP_URL, "{\"data\":{},\"msgType\":\"HEART_BEAT\",\"token\":\"" + HttpConfig.TOKEN + "\"}");
        ExecutorService service = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                nettyClientBootstrap.start();
            }
        };
        service.execute(runnable);
        setCameraView();
    }

    private void setCameraView() {
        int mCameraRotate = 0;
        boolean mCameraMirror = true;
        mWidth = 640;
        mHeight = 480;
        mFormat = ImageFormat.NV21;
        svCameraSurfaceview.setOnTouchListener(this);
        surfaceView.setOnCameraListener(this);
        surfaceView.setupGLSurafceView(svCameraSurfaceview, true, mCameraMirror, mCameraRotate);
        surfaceView.debug_print_fps(false, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initData() {
        code_mumber.setFocusable(true);
        code_mumber.setCursorVisible(true);
        code_mumber.setFocusableInTouchMode(true);
        code_mumber.requestFocus();
        code_mumber.setShowSoftInputOnFocus(false);
        /**
         * EditText编辑框内容发生变化时的监听回调
         */
        code_mumber.addTextChangedListener(new EditTextChangeListener());
    }

    @Override
    public void onClick(View view) {
        View dialog = View.inflate(getApplicationContext(), R.layout.veune_dialog, null);
        dialogUtils.showManagerDialog(dialog);
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public Camera setupCamera() {
        // TODO Auto-generated method stub
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mWidth, mHeight);
            parameters.setPreviewFormat(mFormat);
            mCamera.setDisplayOrientation(90);
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
            }
            for (Integer format : parameters.getSupportedPreviewFormats()) {
                Log.d(TAG, "FORMAT:" + format);
            }

            List<int[]> fps = parameters.getSupportedPreviewFpsRange();
            for (int[] count : fps) {
                Log.d(TAG, "T:");
                for (int data : count) {
                    Log.d(TAG, "V=" + data);
                }
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            mWidth = mCamera.getParameters().getPreviewSize().width;
            mHeight = mCamera.getParameters().getPreviewSize().height;
        }
        return mCamera;
    }

    @Override
    public void setupChanged(int format, int width, int height) {

    }

    @Override
    public boolean startPreviewImmediately() {
        return true;
    }
    long start = 0;
    @Override
    public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {

        if(faceRecognize==null){
            Toast.makeText(EntanceActivity.this,"人脸识别初始化失败",Toast.LENGTH_SHORT).show();
            return null;
        }
        int[] argb = argbPool.acquire(width, height);

        if (argb == null || argb.length != width * height) {
            argb = new int[width * height];
        }
        FaceDetector.yuvToARGB(data, width, height, argb, 0, 0);

        ImageFrame frame = new ImageFrame();
        frame.setArgb(argb);
        frame.setWidth(width);
        frame.setHeight(height);
        frame.setPool(argbPool);
        argbPool.release(argb);
        int value = FaceSDKManager.getInstance().getFaceDetector().detect(frame);
        // FaceSDKManager.getInstance().getFaceDetector().detectMultiFace(frame,5);
        FaceInfo[] faces = FaceSDKManager.getInstance().getFaceDetector().getTrackedFaces();
        if (faces != null) {
            Log.e("faceMulti", faces.length + "");
        }
        if (value == FaceTracker.ErrCode.OK.ordinal() && faces != null) {
            if(System.currentTimeMillis()-start<2000){
                return null;
            }
            start =System.currentTimeMillis();
            asyncIdentity(frame, faces);
        }
        return null;
    }
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private volatile int identityStatus = IDENTITY_IDLE;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {
        if (identityStatus != IDENTITY_IDLE) {
            return ;
        }

        es.submit(new Runnable() {

            @Override
            public void run() {
                if (faceInfos == null || faceInfos.length == 0) {
                    return;
                }
                identity(imageFrame, faceInfos[0]);


            }
        });
    }
    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        identityStatus = IDENTITYING;

        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            identityStatus = IDENTITY_IDLE;
            return;
        }


        long starttime = System.currentTimeMillis();
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;

        IdentifyRet identifyRet = null;
        identifyRet = identity(argb,rows,cols,landmarks);

        if (identifyRet != null) {
            entranceContronller.checkIn(identifyRet.getUserId(),"",direction);
            Log.e( "identity: ", identifyRet.getUserId()+">>>>>>>>"+ identifyRet.getScore());
        }
    }
    String userIdOfMaxScore = "";
    public IdentifyRet identity(int[] argbData, int rows, int cols, int[] landmarks) {
        if (argbData == null ) {
            identityStatus = IDENTITY_IDLE;
            return null;
        }
        byte[] imageFrameFeature = new byte[2048];
        int ret = faceRecognize.extractFeature(argbData, rows, cols, FaceSDK.ImgType.ARGB, imageFrameFeature, landmarks,
                FaceSDK.RecognizeType.RECOGNIZE_LIVE);
        userIdOfMaxScore="";
        float identifyScore = 0;
        Realm realm = Realm.getDefaultInstance();
        Log.e(TAG, "identity: "+System.currentTimeMillis());
        RealmResults<UserFace> all = realm.where(UserFace.class).findAll();

        Iterator<UserFace> iterator = all.iterator();

        while (iterator.hasNext()) {
            UserFace next = iterator.next();

            byte[] feature = HexUtil.hexStringToByte(next.getFace());
            final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(
                    feature, imageFrameFeature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = next.getUuid();

            }
        }
        Log.e(TAG, "identity: "+System.currentTimeMillis());
        identityStatus = IDENTITY_IDLE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(EntanceActivity.this,userIdOfMaxScore,Toast.LENGTH_SHORT).show();
            }
        });
        return new IdentifyRet(userIdOfMaxScore, identifyScore);
    }

    @Override
    public void onBeforeRender(CameraFrameData data) {

    }

    @Override
    public void onAfterRender(CameraFrameData data) {

    }


    public class EditTextChangeListener implements TextWatcher {
        long lastTime;

        /**
         * 编辑框的内容发生改变之前的回调方法
         */
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        /**
         * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
         * 我们可以在这里实时地 通过搜索匹配用户的输入
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        /**
         * 编辑框的内容改变以后,用户没有继续输入时 的回调方法
         */
        @Override
        public void afterTextChanged(Editable editable) {
            String str = code_mumber.getText().toString();
            if (str.contains("\n")) {
                if (System.currentTimeMillis() - lastTime < 1500) {
                    code_mumber.setText("");
                    return;
                }
                lastTime = System.currentTimeMillis();
                entranceContronller.openDoorQr(code_mumber.getText().toString(), direction);
                code_mumber.setText("");
            }
        }
    }


    private void startVerify() {
        RxTimerUtil.interval(1000, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                Log.e(TAG, "doNext:+" + number);
                int state = venueutils.getState();
                if (state == 3) {
                    if (dialogUtils.isShowing()) {
                        uid = null;
                        uid = venueutils.identifyNewImg(managers);
                        if (uid != null) {
                            showActivity(SettingActivity.class);
                        } else {
                            TTSUtils.getInstance().speak(getString(R.string.no_manager));

                        }
                    } else {
                        uid = null;
                        uid = venueutils.identifyNewImg(peoples);
                        if (uid != null) {
                            final RealmResults<AllUser> personIn = realm.where(AllUser.class).equalTo("uuid", uid).equalTo("isIn", 1).findAll();
                            if (personIn.size() > 0) {
                                int ifNetwork = Utils.getNetWorkState(EntanceActivity.this);
                                if(ifNetwork==Utils.NETWORK_NONE){
                                    openDoor();
                                }else {

                                    entranceContronller.checkIn(uid, null, direction);
                                }
                               // entranceContronller.checkInLog(uid, null, direction, 1);
                            } else {

                                entranceContronller.checkIn(uid, null, direction);
                            }
                            IsNoPerson = false;
                        } else {
                            if (PassControlApplication.getVenueUtils().img != null) {
                                entranceContronller.checkIn(null, HexUtil.bytesToHexString(PassControlApplication.getVenueUtils().img), direction);
                                IsNoPerson = true;
                                isDeleteAll = false;
                            }

                        }
                    }


                }


            }
        });
    }

    @Override
    protected int getLayoutId() {

        return R.layout.activity_entance;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(veune==0){
            startVerify();
        }
        if (dialogUtils.isShowing()) {
            dialogUtils.dissMiss();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        RxTimerUtil.cancel();
        Log.e(TAG, "onStop: ");
    }

    @Override
    public void onMainErrorCode(String msg, String codeError) {
        if (codeError.equals("400000100000")) {
            skipActivity(SettingActivity.class);
            TTSUtils.getInstance().speak(getString(R.string.login_fail));
        } else if (codeError.equals("400000999102")) {
            HttpConfig.TOKEN = "";
            getToken();

        } else if (codeError.equals("400000500028")) {

        } else {

            TTSUtils.getInstance().speak(msg);
        }
    }

    private void getToken() {
        final RealmResults<DeviceInfo> all = realm.where(DeviceInfo.class).findAll();
        if (!all.isEmpty()) {
            deviceInfo = all.get(0);
            entranceContronller.login(deviceInfo.getDeviceId().trim(), deviceInfo.getPsw());
        } else {
            skipActivity(SettingActivity.class);
        }

    }

    @Override
    public void onMainFail(Throwable e, boolean isNetWork) {
        if (isNetWork) {
            TTSUtils.getInstance().speak(getString(R.string.error_net));
        } else {
            TTSUtils.getInstance().speak(getString(R.string.parse_error));
            if (TextUtils.isEmpty(HttpConfig.TOKEN)) {
                getToken();
            }
        }

    }

    @Override
    public void getUserSuccess(final BindUser data) {
        final RealmResults<AllUser> all = realm.where(AllUser.class).findAll();
        total = data.getTotal();
        if (!isDeleteAll) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    all.deleteAllFromRealm();
                    isDeleteAll = true;

                }
            });
            int totalPage = total / Constants.PAGE_NUM + 1;
            ExecutorService executorService = Executors.newFixedThreadPool(totalPage);
            List<Future<Boolean>> futures = new ArrayList();
            if (totalPage >= 2) {
                for (int i = 2; i <= totalPage; i++) {
                    final int finalI = i;
                    Callable<Boolean> task = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            entranceContronller.getUser(finalI,first.getDeviceId());
                            return true;
                        }
                    };

                    futures.add(executorService.submit(task));
                }
                for (Future<Boolean> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(data.getData());
            }
        });

    }

    @Override
    public void CheckInSuccess(CheckInBean data) {
        openDoor();
        if (IsNoPerson) {
            entranceContronller.getUser(1,first.getDeviceId());
            if (venueutils.img != null) {
             //   entranceContronller.checkInLog(uid, HexUtil.bytesToHexString(PassControlApplication.getVenueUtils().img), direction, 2);
            }
        } else {
            //entranceContronller.checkInLog(uid, null, direction, 1);
        }

    }

    @Override
    public void passSuccess(PasswordBean data) {
        showActivity(SettingActivity.class);
    }

    @Override
    public void CodeInSuccess(CodeInBean data) {
        openDoor();
      //  entranceContronller.checkInLog(null, null, direction, 3);
    }

    @Override
    public void onLoginSuccess(final CabnetDeviceInfoBean cabnetDeviceInfoBean) {
        final RealmResults<DeviceInfo> all = realm.where(DeviceInfo.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DeviceInfo device = all.get(0);
                device.setToken(cabnetDeviceInfoBean.getToken());
                device.setDeviceTypeId(cabnetDeviceInfoBean.getDeviceInfo().getDeviceTypeId());
                deviceInfo = device;
                realm.copyToRealm(device);
            }
        });
        HttpConfig.TOKEN = cabnetDeviceInfoBean.getToken();
    }

    @Override
    public void CheckInLogSuccess(CheckInBean data) {

    }

    @Override
    public void getUserFaceSuccess(final AllUserFaceBean data) {
        final RealmResults<UserFace> all = realm.where(UserFace.class).findAll();
        totaFace = data.getTotal();
        if (!isDeleteAllFace) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    all.deleteAllFromRealm();
                    isDeleteAllFace = true;

                }
            });
            int totalPage = totaFace / Constants.PAGE_NUM + 1;
            ExecutorService executorService = Executors.newFixedThreadPool(totalPage);
            List<Future<Boolean>> futures = new ArrayList();
            if (totalPage >= 2) {
                for (int i = 2; i <= totalPage; i++) {
                    final int finalI = i;
                    Callable<Boolean> task = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            entranceContronller.getUserFace(finalI,first.getDeviceId());
                            return true;
                        }
                    };

                    futures.add(executorService.submit(task));
                }
                for (Future<Boolean> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(data.getData());
            }
        });

    }

    private void openDoor() {
        if ("rk3399-mid".equals(deviceType)) {
            try {
                Gpio.gpioInt(gpiotext);
                Thread.sleep(400);
                Gpio.set(gpiotext, 48);
                TTSUtils.getInstance().speak(getString(R.string.door_open));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Gpio.set(gpiotext, 49);
        } else if ("rk3288".equals(deviceType)) {
            try {
                Power.set_zysj_gpio_value(4, 0);
                Thread.sleep(400);
                TTSUtils.getInstance().speak(getString(R.string.door_open));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Power.set_zysj_gpio_value(4, 1);
        }
    }

    public void initVenue() {
        venueutils.initVenue(this, mdDeviceBinder, false);
    }

    public void gotoSetting(String pass) {
        entranceContronller.password(pass);
    }

    String TAG = "EntranceActivity";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
        if(veune==0){
            unBindService();
        }
        if(mCamera!=null){
            mCamera.release();
        }
    }

    private List<MdDevice> mdDevicesList = new ArrayList<MdDevice>();
    public static MdDevice mdDevice;
    private final int MSG_REFRESH_LIST = 0;
    private Handler listManageH = new Handler(new Handler.Callback() {

        @Override

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST: {
                    mdDevicesList.clear();
                    mdDevicesList = getDevList();
                    if (mdDevicesList.size() > 0) {
                        mdDevice = mdDevicesList.get(0);
                    } else {
                        listManageH.sendEmptyMessageDelayed(MSG_REFRESH_LIST, 1500L);

                    }
                    break;
                }

            }
            return false;

        }

    });

    private List<MdDevice> getDevList() {
        List<MdDevice> mdDevList = new ArrayList<MdDevice>();
        if (mdDeviceBinder != null) {
            int deviceCount = MicroFingerVein.fvdev_get_count();
            for (int i = 0; i < deviceCount; i++) {
                MdDevice mdDevice = new MdDevice();
                mdDevice.setNo(i);
                mdDevice.setIndex(mdDeviceBinder.getDeviceNo(i));
                mdDevList.add(mdDevice);
                initVenue();
            }
        } else {
            Logger.e("microFingerVein not initialized by MdUsbService yet,wait a moment...");
        }
        return mdDevList;

    }

    private ServiceConnection mdSrvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mdDeviceBinder = (MdUsbService.MyBinder) service;
            if (mdDeviceBinder != null) {
                mdDeviceBinder.setOnUsbMsgCallback(mdUsbMsgCallback);
                listManageH.sendEmptyMessage(MSG_REFRESH_LIST);
                Logger.e("bind MdUsbService su.");
            } else {
                Logger.e("bind MdUsbService failed.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.e("disconnect MdUsbService.");
        }
    };

    private MdUsbService.UsbMsgCallback mdUsbMsgCallback = new MdUsbService.UsbMsgCallback() {
        @Override
        public void onUsbConnSuccess(String usbManufacturerName, String usbDeviceName) {
            String newUsbInfo = "USB厂商：" + usbManufacturerName + "  \nUSB节点：" + usbDeviceName;
            Logger.e(newUsbInfo);
        }

        @Override
        public void onUsbDisconnect() {
            Logger.e("USB连接已断开");
        }

    };

    public void unBindService() {
        unbindService(mdSrvConn);
    }

    public static <E extends RealmObject> List<E> getLimitList(
            RealmResults<E> data, int offset, int limit) {
        List<E> obtainList = new ArrayList();
        Realm realm = Realm.getDefaultInstance();
        if (data.size() == 0) {
            return obtainList;
        }
        for (int i = offset; i < offset + limit; i++) {
            if (i >= data.size()) {
                break;
            }
            E temp = realm.copyFromRealm(data.get(i));
            obtainList.add(temp);
        }
        realm.close();
        return obtainList;
    }
}
