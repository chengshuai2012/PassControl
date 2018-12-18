package com.link.cloud.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.link.cloud.R;
import com.link.cloud.base.BaseActivity;
import com.link.cloud.network.bean.DeviceInfo;
import com.link.cloud.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by OFX002 on 2018/9/28.
 */

public class SettingActivity extends BaseActivity {
    @BindView(R.id.device_id)
    TextView deviceId;
    @BindView(R.id.psw_ll)
    LinearLayout pswLl;
    @BindView(R.id.open)
    TextView open;
    @BindView(R.id.close)
    TextView close;
    @BindView(R.id.open_or_close)
    LinearLayout openOrClose;
    @BindView(R.id.back_system_setting)
    TextView backSystemSetting;
    @BindView(R.id.back_app)
    TextView backApp;
    @BindView(R.id.back_system_main)
    TextView backSystemMain;
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.edit_psw)
    TextView edit_psw;
    @BindView(R.id.device_No_edit)
    EditText editDeviceNo;
    @BindView(R.id.qcode_have)
    TextView qcodeHave;
    @BindView(R.id.qcode_no)
    TextView qcodeNo;
    @BindView(R.id.qcode_ll)
    LinearLayout qcodeLl;
    @BindView(R.id.venue_have)
    TextView venueHave;
    @BindView(R.id.venue_no)
    TextView venueNo;
    @BindView(R.id.venue_ll)
    LinearLayout venueLl;
    @BindView(R.id.face_have)
    TextView faceHave;
    @BindView(R.id.face_no)
    TextView faceNo;
    @BindView(R.id.face_ll)
    LinearLayout faceLl;
    @BindView(R.id.restart_app)
    TextView restartApp;
    private String mac;
    int face, veune, qcode;

    @Override
    protected void initViews() {
        mac = Utils.getMac();
        deviceId.setText(getResources().getString(R.string.device_id) + mac);
        DeviceInfo first = realm.where(DeviceInfo.class).findFirst();
        if(first!=null){
            face = first.getFace();
            veune=first.getVeune();
            qcode=first.getQcode();
        }
        if(face==1){
            faceNo.setTextColor(getResources().getColor(R.color.almost_white));
            faceNo.setBackgroundResource(R.drawable.border_red_gradient);
            faceHave.setBackgroundResource(R.drawable.border_gray_gradient);
            faceHave.setTextColor(getResources().getColor(R.color.dark_black));
        }
        if(veune==1){
            venueNo.setTextColor(getResources().getColor(R.color.almost_white));
            venueNo.setBackgroundResource(R.drawable.border_red_gradient);
            venueHave.setBackgroundResource(R.drawable.border_gray_gradient);
            venueHave.setTextColor(getResources().getColor(R.color.dark_black));
        }
        if(qcode==1){
            qcodeNo.setTextColor(getResources().getColor(R.color.almost_white));
            qcodeNo.setBackgroundResource(R.drawable.border_red_gradient);
            qcodeHave.setBackgroundResource(R.drawable.border_gray_gradient);
            qcodeHave.setTextColor(getResources().getColor(R.color.dark_black));
        }
        String [] pemission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        checkReadPermission(pemission,REQUEST_SD_PERMISSION);

    }
    public static final int REQUEST_SD_PERMISSION = 10111;



    /**

     * 判断是否有某项权限

     * @param string_permission 权限

     * @param request_code 请求码

     * @return

     */

    public boolean checkReadPermission(String[] string_permission,int request_code) {

        boolean flag = false;

        if (ContextCompat.checkSelfPermission(this, string_permission[1]) == PackageManager.PERMISSION_GRANTED) {//已有权限

            flag = true;

        } else {//申请权限

            ActivityCompat.requestPermissions(this, string_permission, request_code);

        }

        return flag;

    }



    /**

     * 检查权限后的回调

     * @param requestCode 请求码

     * @param permissions  权限

     * @param grantResults 结果

     */

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_SD_PERMISSION:

                if (permissions.length != 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {//失败

                    Toast.makeText(this, getString(R.string.sd_permession),Toast.LENGTH_SHORT).show();

                }

                break;

        }

    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @OnClick({R.id.back_app, R.id.save, R.id.back_system_main,
            R.id.back_system_setting, R.id.restart_app, R.id.close, R.id.open, R.id.face_have, R.id.face_no, R.id.venue_no, R.id.venue_have
            , R.id.qcode_have, R.id.qcode_no
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_app:
                finish();
                break;
            case R.id.save:
                final String edit_pswText = edit_psw.getText().toString();
                final String deviceNo = editDeviceNo.getText().toString();
                String fisrt = Utils.getMD5(edit_pswText).toUpperCase();
                final String second = Utils.getMD5(fisrt).toUpperCase();
                if(face+veune+qcode==3){
                    Toast.makeText(this, getResources().getString(R.string.one_last), Toast.LENGTH_LONG).show();
                    return;
                }
                final RealmResults<DeviceInfo> all = realm.where(DeviceInfo.class).findAll();
                if (all.size() != 0) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DeviceInfo deviceInfo = all.get(0);
                            deviceInfo.setDeviceId(mac);
                            if(!TextUtils.isEmpty(edit_pswText)){
                                deviceInfo.setPsw(second);
                            }
                            if(!TextUtils.isEmpty(deviceNo)){
                                deviceInfo.setDeviceNo(deviceNo);
                            }
                            deviceInfo.setFace(face);
                            deviceInfo.setQcode(qcode);
                            deviceInfo.setVeune(veune);
                        }
                    });
                } else {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DeviceInfo deviceBean = new DeviceInfo();
                            deviceBean.setDeviceId(mac);
                            deviceBean.setPsw(second);
                            deviceBean.setDeviceType(Build.MODEL);
                            deviceBean.setFace(face);
                            deviceBean.setQcode(qcode);
                            deviceBean.setVeune(veune);
                            deviceBean.setDeviceNo(deviceNo);
                            realm.copyToRealm(deviceBean);
                        }
                    });
                }
                Toast.makeText(this, getResources().getString(R.string.save_success), Toast.LENGTH_LONG).show();
                break;
            case R.id.back_system_main:
                Intent intent1 = new Intent(Intent.ACTION_MAIN, null);
                intent1.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent1);

                break;
            case R.id.back_system_setting:
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                break;
            case R.id.restart_app:
                Intent intent2 = new Intent(this, SplashActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                Process.killProcess(Process.myPid());
                break;
            case R.id.close:
                close.setTextColor(getResources().getColor(R.color.almost_white));
                close.setBackgroundResource(R.drawable.border_red_gradient);
                open.setBackgroundResource(R.drawable.border_gray_gradient);
                open.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.open:
                open.setTextColor(getResources().getColor(R.color.almost_white));
                open.setBackgroundResource(R.drawable.border_red_gradient);
                close.setBackgroundResource(R.drawable.border_gray_gradient);
                close.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.face_have:
                face=0;
                faceHave.setTextColor(getResources().getColor(R.color.almost_white));
                faceHave.setBackgroundResource(R.drawable.border_red_gradient);
                faceNo.setBackgroundResource(R.drawable.border_gray_gradient);
                faceNo.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.face_no:
                face=1;
                faceNo.setTextColor(getResources().getColor(R.color.almost_white));
                faceNo.setBackgroundResource(R.drawable.border_red_gradient);
                faceHave.setBackgroundResource(R.drawable.border_gray_gradient);
                faceHave.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.qcode_have:
                qcode=0;
                qcodeHave.setTextColor(getResources().getColor(R.color.almost_white));
                qcodeHave.setBackgroundResource(R.drawable.border_red_gradient);
                qcodeNo.setBackgroundResource(R.drawable.border_gray_gradient);
                qcodeNo.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.qcode_no:
                qcode=1;
                qcodeNo.setTextColor(getResources().getColor(R.color.almost_white));
                qcodeNo.setBackgroundResource(R.drawable.border_red_gradient);
                qcodeHave.setBackgroundResource(R.drawable.border_gray_gradient);
                qcodeHave.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.venue_no:
                veune=1;
                venueNo.setTextColor(getResources().getColor(R.color.almost_white));
                venueNo.setBackgroundResource(R.drawable.border_red_gradient);
                venueHave.setBackgroundResource(R.drawable.border_gray_gradient);
                venueHave.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.venue_have:
                veune=0;
                venueHave.setTextColor(getResources().getColor(R.color.almost_white));
                venueHave.setBackgroundResource(R.drawable.border_red_gradient);
                venueNo.setBackgroundResource(R.drawable.border_gray_gradient);
                venueNo.setTextColor(getResources().getColor(R.color.dark_black));
                break;
        }
    }


}
