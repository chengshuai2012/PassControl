package com.link.cloud.base;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.link.cloud.network.BaseEntity;
import com.link.cloud.network.BaseObserver;
import com.link.cloud.network.IOMainThread;
import com.link.cloud.network.RetrofitFactory;
import com.link.cloud.network.bean.AllUser;
import com.link.cloud.network.bean.SingleUser;
import com.link.cloud.utils.Venueutils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by OFX002 on 2018/9/20.
 */

public abstract class BaseActivity extends AppCompatActivity implements Venueutils.VenueCallBack {

    private Unbinder bind;
    public Realm realm;
    MesReceiver mesReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        realm=Realm.getDefaultInstance();
        bind = ButterKnife.bind(this);
        initViews();
        setTranslucentStatus(true);

    }

    protected abstract void initViews();
    public void initVenue(){
        PassControlApplication.getVenueUtils().initVenue(this, this, false);
    }
    protected abstract int getLayoutId();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();
        realm.close();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("AVChatRestart", this.getClass() + " onStop");
    }
    public void RegisteReciver(){
        mesReceiver=new MesReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.MSG);
        registerReceiver(mesReceiver, intentFilter);
    }
    public class MesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            String type  =null;
            JSONObject object=null;
            Log.e( "onReceive: ",msg );
            Toast.makeText(BaseActivity.this,msg,Toast.LENGTH_LONG).show();
            try {
                object = new JSONObject(msg);
                type = object.getString("msgType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if("GET_USERS_FINGERPRINTS".equals(type)){
                try {
                    final String uuid = object.getJSONObject("data").getString("uuid");
                    RetrofitFactory.getInstence().API().findOneUserFinger(uuid).compose(IOMainThread.<BaseEntity<SingleUser>>composeIO2main())
                            .subscribe(new BaseObserver<SingleUser>() {
                                @Override
                                protected void onSuccees(final BaseEntity<SingleUser> t) {
                                    Log.e("onNext: ",uuid );
                                    final RealmResults<AllUser> all = realm.where(AllUser.class).equalTo("uuid",uuid).findAll();
                                    Log.e("onNext: ",all.size()+"" );
                                    if(all.size()>0){
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                all.deleteAllFromRealm();
                                                realm.copyToRealm(t.getData().getFingerprints());
                                            }
                                        });
                                    }else {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.copyToRealm(t.getData().getFingerprints());
                                            }
                                        });
                                    }
                                }

                                @Override
                                protected void onCodeError(String msg, String codeErrorr) {

                                }

                                @Override
                                protected void onFailure(Throwable e, boolean isNetWorkError) {

                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    protected void onResume() {
        super.onResume();

    }

    protected void onPause() {
        super.onPause();

    }

    /**
     * @param cls 目标activity
     *            跳转并finish当前activity
     * @throws ActivityNotFoundException
     */
    public void skipActivity(Class<?> cls) {
        showActivity(cls);

        finish();
    }

    /**
     * @param cls
     * @param extras
     */
    public void skipActivity(Class<?> cls, Bundle extras) {
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(this, cls);
        startActivity(intent);
        finish();
    }
    public void unRegisterReceiver() {
        unregisterReceiver(mesReceiver);
    }
    public void showActivityForResult(Class<?> cls, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        super.startActivityForResult(intent, requestCode);
    }

    public void showActivityForResult(Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        intent.putExtras(bundle);
        super.startActivityForResult(intent, requestCode);
    }

    public void showActivity(Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        super.startActivity(intent);
    }

    public void showActivity(Class<?> cls, Bundle extras) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        intent.putExtras(extras);
        super.startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();

    }

    protected void onPermissionGranted(String... permissions) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    public Context getContext() {
        return this;
    }

    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)) return;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void modelMsg(int state, String msg) {
        Log.e("modelMsg: ", state+"");
    }
}
