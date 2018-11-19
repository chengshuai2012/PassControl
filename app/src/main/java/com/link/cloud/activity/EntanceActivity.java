package com.link.cloud.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Power;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.link.cloud.R;
import com.link.cloud.base.BaseActivity;
import com.link.cloud.base.Constants;
import com.link.cloud.base.PassControlApplication;
import com.link.cloud.controller.EntranceContronller;
import com.link.cloud.gpiotest.Gpio;
import com.link.cloud.network.HttpConfig;
import com.link.cloud.network.bean.AllUser;
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CodeInBean;
import com.link.cloud.network.bean.DeviceInfo;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.utils.DialogUtils;
import com.link.cloud.utils.HexUtil;
import com.link.cloud.utils.NettyClientBootstrap;
import com.link.cloud.utils.RxTimerUtil;
import com.link.cloud.utils.TTSUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by 49488 on 2018/10/16.
 */

public class EntanceActivity extends BaseActivity implements EntranceContronller.EntranceControllerListener {
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
    private RealmResults<AllUser> all;
    private RealmResults<AllUser> managersRealm;
    List<AllUser> peoples = new ArrayList<>();
    List<AllUser> managers = new ArrayList<>();
    private EntranceContronller entranceContronller;
    String uid;
    boolean IsNoPerson =false;
    boolean isDeleteAll =false;
    String gpiotext = "1067";
    private String deviceType;
    int total,direction,deviceTypeId;
    private DialogUtils dialogUtils;
    private NettyClientBootstrap nettyClientBootstrap;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initViews() {
        DeviceInfo first = realm.where(DeviceInfo.class).findFirst();
        deviceType = first.getDeviceType();
        int face = first.getFace();
        int qcode = first.getQcode();
        int veune = first.getVeune();
        if(face==1){
            faceLl.setVisibility(View.GONE);
        }
        if(qcode==1){
            qrcodeLl.setVisibility(View.GONE);
        }
        if(veune==1){
            veuneLl.setVisibility(View.GONE);
        }
        deviceTypeId = first.getDeviceTypeId();
        switch (deviceTypeId){
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
                peoples.addAll(realm.copyFromRealm(all));
            }
        });
        managersRealm.addChangeListener(new RealmChangeListener<RealmResults<AllUser>>() {
            @Override
            public void onChange(RealmResults<AllUser> allUsers) {
                managers.clear();
                managers.addAll(realm.copyFromRealm(managersRealm));
            }
        });
        initVenue();
        dialogUtils = DialogUtils.getDialogUtils(this);
        manager.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View dialog = View.inflate(EntanceActivity.this,R.layout.veune_dialog,null);
                dialogUtils.showManagerDialog(dialog);
                return true;
            }
        });
        entranceContronller = new EntranceContronller(this);
        RegisteReciver();
        initData();
        ExecutorService service = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                nettyClientBootstrap = new NettyClientBootstrap(EntanceActivity.this, Constants.TCP_PORT, Constants.TCP_URL, "{\"data\":{},\"msgType\":\"HEART_BEAT\",\"token\":\"" + HttpConfig.TOKEN + "\"}");
                nettyClientBootstrap.start();
            }
        };
        service.execute(runnable);


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
            String str=code_mumber.getText().toString();
            if (str.contains("\n")) {
                if(System.currentTimeMillis()-lastTime<1500){
                    code_mumber.setText("");
                    return;
                }
                lastTime=System.currentTimeMillis();
                entranceContronller.openDoorQr(code_mumber.getText().toString());
                code_mumber.setText("");
            }
        }
    }
    private void startVerify() {
        RxTimerUtil.interval(1000, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                int state = PassControlApplication.getVenueUtils().getState();
                if (state == 3) {
                    if(dialogUtils.isShowing()){
                        uid = null;
                        uid = PassControlApplication.getVenueUtils().identifyNewImg(managers);
                        if (uid != null) {
                         showActivity(SettingActivity.class);

                        } else {
                            TTSUtils.getInstance().speak(getString(R.string.no_manager));

                        }
                    }else {
                        IsNoPerson =false;
                        uid = null;
                        uid = PassControlApplication.getVenueUtils().identifyNewImg(peoples);
                        if (uid != null) {
                            entranceContronller.checkIn(uid,null,direction);

                        } else {

                            if(PassControlApplication.getVenueUtils().img!=null){
                                entranceContronller.checkIn(null,HexUtil.bytesToHexString(PassControlApplication.getVenueUtils().img ),direction);
                                IsNoPerson = true;
                                isDeleteAll =false;
                            }



                        }
                    }

                }
                if (state == 4) {
                    //TTSUtils.getInstance().speak(getResources().getString(R.string.again_finger));
                }
                if (state != 4 && state != 3) {

                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_entance;
    }

    @Override
    public void onMainErrorCode(String msg) {
        if (msg.equals("400000100000") ) {
            skipActivity(SettingActivity.class);
            TTSUtils.getInstance().speak(getString(R.string.login_fail));
        }else if(msg.equals("400000999102")){
            HttpConfig.TOKEN = "";
            skipActivity(SettingActivity.class);
            TTSUtils.getInstance().speak(getString(R.string.login_fail));

        }
        TTSUtils.getInstance().speak(msg);
    }

    @Override
    public void onMainFail(Throwable e, boolean isNetWork) {
        if(isNetWork){
            TTSUtils.getInstance().speak(getString(R.string.error_net));
        }else {
            TTSUtils.getInstance().speak(getString(R.string.parse_error));
        }

    }

    @Override
    public void getUserSuccess(final BindUser data) {
        final RealmResults<AllUser> all = realm.where(AllUser.class).findAll();
        total =data.getTotal();
        if(!isDeleteAll){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    all.deleteAllFromRealm();
                    isDeleteAll=true;

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
                            entranceContronller.getUser(finalI);
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
                executorService.shutdown();}
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
        if(IsNoPerson){
            entranceContronller.getUser(1);
        }
    }

    @Override
    public void passSuccess(PasswordBean data) {
        showActivity(SettingActivity.class);
    }

    @Override
    public void CodeInSuccess(CodeInBean data) {
        openDoor();
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
                Power.set_zysj_gpio_value(4,0);
                Thread.sleep(400);
                TTSUtils.getInstance().speak(getString(R.string.door_open));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Power.set_zysj_gpio_value(4,1);
        }
    }
    @Override
    public void modelMsg(int state, String msg) {
        super.modelMsg(state, msg);
        if(state==10){
            startVerify();
        }
    }
    public void gotoSetting(String pass){
            entranceContronller.password(pass);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }
}
