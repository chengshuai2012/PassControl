package com.link.cloud.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
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
import com.link.cloud.network.bean.BindUser;
import com.link.cloud.network.bean.CabnetDeviceInfoBean;
import com.link.cloud.network.bean.CheckInBean;
import com.link.cloud.network.bean.CodeInBean;
import com.link.cloud.network.bean.DeviceInfo;
import com.link.cloud.network.bean.PasswordBean;
import com.link.cloud.network.bean.UserFace;
import com.link.cloud.network.bean.YuanGuMessage;
import com.link.cloud.utils.DialogUtils;
import com.link.cloud.utils.HexUtil;
import com.link.cloud.utils.NettyClientBootstrap;
import com.link.cloud.utils.RxTimerUtil;
import com.link.cloud.utils.TTSUtils;
import com.link.cloud.utils.Venueutils;
import com.link.cloud.veune.MdDevice;
import com.link.cloud.veune.MdUsbService;
import com.link.cloud.widget.CameraFrameData;
import com.link.cloud.widget.CameraGLSurfaceView;
import com.link.cloud.widget.CameraSurfaceView;
import com.link.cloud.widget.ClipView;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
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
    String gpiotext = "1067";
    private String deviceType;
    int total, direction, deviceTypeId;
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
            FaceSDKManager.getInstance().setKey("BLUF-54JT-RB4A-ZKDR");
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
        dialogUtils = DialogUtils.getDialogUtils(this);
        manager.setOnClickListener(this);
        entranceContronller = new EntranceContronller(this);
        RegisteReciver();
        ExecutorService service = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                nettyClientBootstrap = new NettyClientBootstrap(EntanceActivity.this, Constants.TCP_PORT, Constants.TCP_URL, "{\"data\":{},\"msgType\":\"HEART_BEAT\",\"token\":\"" + HttpConfig.TOKEN + "\"}");
                nettyClientBootstrap.start();
            }
        };
        service.execute(runnable);

        RealmResults<UserFace> UserFace = realm.where(UserFace.class).findAll();
        if(UserFace.size()==0){
            final UserFace userFace = new UserFace();
            userFace.setFeature("DD980F6FAFB507949F471C18890ABE633A139AFF13C9BEFB6A162008C87ACE0CA3D7DD4F5FD938B4747D42386C8C243DD37423A0F3CDB5A4597A44A8A843CEAD6A87CDD011AA07D4E29685D936243D5C02E15FC1905C6C45C6BA4B48513DB2B398819DF03BBDC10BDFD55007FFF3127CE7EC7DE01876BF9B0E27D917B128DD6C38A8A391F6EABD1401AC1998221F9E9C88342F00AE8FA70527581888C8258BF336541C30498267B451190C4726669243B1CB7721E5B112A4D47CADD70B421DD2ED26BB50316CD2548F3768278CDCFEA3A640673FB27F3044A16A9248B299BA4CF81D41F0EA2583741C0DC278D2A24C839A34391F11BFF0E5013A1297E91D6B134A046211C7E008EB0EA91A6762587863C0127E82E3B837FB41C7248854E8A3F3ADCE06B24368E84B27F3BBC7C3F1033C980953207095032448E5AE57A1EBBE53F5BC492F1FFD7A540DD04ED8E5C883A3F285EBBF70824EC52457C9378AB566CCE1FAFF8F2DF1C3F51F1DEB07AD86FE036310041FC8C6929BEE65C9E840825B6F406E94903E24D895D70B1FE76291BA63585E6A7F26F18E84FBA96FF7FFFEF18EF56E3C4F18EE8636FEEE5D38EC22ACBD551CB121D5FBB45B74AE89A8030BC0ACFB4D44AF8D817AAB5D0217275F295F2348EBB33F0A93DBBBED552AC9061566CC3838C470F8EF788BFA0BC77846FB8F7C52A5651FAE1F9564F7B36117F5EB036CEC6AEE6FCDEF2F6B469E0819B759816345FFEE00E61D2504349E02771451AB73F18DE5CF8907BECBCA51BAC7ADFE54BCFE3E2A215C1ED6A49A879FD79D7D74AC4680525051BA36D4730F4D272E51DAA3FB2F6640C1214C3B3EF385B732C1094D4674FC0FAE3FE58B44045C7D961F8C7C05D81C60DE4C11E595A79B684A65E6ED0A642FEF8147ED9537CE301806B7BB1D948F0EFFB8886204160B3277B2D5E68CDAD72E304BD6E434295875B8501611C3003281DF14101A5B8E6CC028163F7153D240A65103F2F1D47CC80527EF1FB65D95A038BFB5A726444E78164820BC64494245CA0FA38DBCF4119E5087CCEE3C03188F7B6042DC381B8565FFE84EBC571377210F10C912941512720A99A8B2C91C78CF62806ADAA27BE63A04080A9E7D0D52B7CD4F2B6EB14B2407B2B8233B0D43FD5C9B5FDA337E25ABD0B3A8A0554DAD8D421AD00752C0AB18ECC05AE1ED45A3F16BE2C0A55F27C47AE17EC894CF684C35BC630F60B3DD8B075C4BF8182EE2FD41CF5EE082CA5EE4A42F94E9D1DA9D936D6B87906062C49414E493185496F71CA45CF800434374FB769B5A884EE7AD8CFF53BCB0F92063CBB00A01C7431302C38C32D1A0D67BAC5B322E0AD7F4ED652C4A193DD0E76BC1D67D1B07D81BD933A3D8BBEF3F73A75DBB9B0670C83F1AEDCEE4B2AA7174A6870B3E77C8075215F1832427089F1E174B9BAD351C17262B4093DC1EED6FFCF7371452D3FDE7A820551CCEDE0F808B198305FC7297F732BBBF8D65BCAE4FEB128ECB027C9B392269A743164A8EDFB1F950A439819E577294CAD305A3AE2F105419D51C5A74276305E35D1F0C6DBF8F6E22C5CC5567C8FB3BF64C70AA0671226ECE0B34EA26F971C0EF83965ED0E09FA443E5382697E884E2A9EC7956A76F296A9D6B45D40F18601FED1C9AAE01FFB039A004ED681EF75337657378EE05B08C0C344BF43423B9EC4B9F431D10885F96B210A4876FE0A811B91053024ED7509673FC541C2D2BD87E9445DDF468F23F3A8620BB80427CB7A6F466CDFAFDA9706B52EBF4FCE78587DDA5AA7CE4635660EA56F4654B32C01701124413CBD93DEFC0E929946500AC180130EAE32211ACFF5AE7F4FB99EFBB0953ECD10C245653B24A7C474BF59D0AB9026688BCAEE7DADF400437A4B888E857E877532C91CFE4507D8DB0D42FE354A7DA7CA55E5D1F6BC0D8A85DC599EEE448EC2FA7335F4BDBF07142418B63468187CA937D7E1537D09FFA89DCE48B2FF0971E64BFECA95312904C3EA414DABB41678B0ACF9C30C4B680B954C404BCD1FB081D25758D6A0A21B0FB9AA0CBEAC92D386A1481C30FC83E5FA6126CDB73D76357335B1E530E96ED50DE502A54AE0BA258D669F8A34E51D2402435753BC367CD481A5CFECE2727168F3824B1F44B2D6AF8FB782B03FA1BC46096C156641873421707085B13C8843590AB56B76B29CC7A98F58B1263668D96FFD32CCC0409456688F6280BF370E855B10C2970B4D104F1B8A2DF92BCA15FD620BB0072246D005F578200E92C467DEFAF104D702B3EF649D8EDA9165CFB0D4E40F56C1C46342054C8561830CCE82D6470CF507576A7AAC08753FD4A838C4F9561D4DEED9B0A87A768B1BC9F93F2AE286F22B2CB1517366E67865B6E9DDC627CFFE1DFD204FB2191775481918E4CFC04B069DAF1358FC901B84546F53C04F691DF8F01BBA444DE01D7117D852CC3182BD084A69456A0BBFCD835CF665DB05588407E3C99458BCE25488A1A5CCC7687E0F1D78DDD0B69B32A0780DE0183B1A58460C4B1409B16350F6860A8986FF57504100E752E6B3889D599AD77621CEFA75B002D81947B3F3F090854E6B58C8095E84F654CA54BF1A9D63840707CBC0720FADFE322A05BF41B8C57D6BF83535EA1315116B71154C50C51D87C17D3A3BE0E7AC09568BBBBF70DD6C8A965324DEF5565702C9202F406059BF8E9EFB6838C2760E05D404E9B66C0E697F04CBE6FA9E01BEFCE12611409F07E67DAD6E31C9C3184FFA6A93E04F52A45886A731E73822E6DCF58381C3492BB2038679944BC318C3E5FFA80D425734290A811C32CD3F564CED04E712C2B2191212733F8185DF46B1D3F1273F5C41A85B6C8E8433DCCD9F0460F79EE8A74E43DEC7A346A1A7C2A33E91F58C5D1625371939775E9D713");
            userFace.setUserId("cc7083a03e344a10945abfbd32491cbe");
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(userFace);
                }
            });

            final UserFace userFace1 = new UserFace();
            userFace1.setFeature("AD13666F7A7C0C6B3E21F267831C3F9C3582FEFFCC3D2204744E1E77167E6B0CD48A85CE38BAFAB4FA1B8E4771223EBC69769221A6E9BD5B75FCC2D720817A2CEF364CD144D6BD548A85D5A70C1AA823A0A1ED3FD4F069C42A8D75C9339B94B30D4D61F0835E2B8BA390CA87DCFDB3036FFA01607611A99BA2D65AE825FA711318D6F1100F7C3E1406A00E6749BB331C6B4D0B7FDD0AA504CE144108A8D1838E167A4D4F3D1B50B4E4AEC1B8F41AF73C4A5E925FC1DD8FDBFD4A0557CF071853F10C572F8B6408D51962CF272FADB0DC1D68F63F388795462066B8C8FA53784D8DC7740F5E9C6B8B680D49788E1D4483F12CE7604E457764DB4B2C1754F74593BF2B0A10EE51EE6BBAC355181EB2E063B090BD007F2631FBAA662577CACEB773274549B0E0695235F64A54B9687E1443B581D0DFB192A4DBFBC1F757C53512536379FC2FE70E6A54FE9E13D84BE04BDDE4107E3FCFC7D4BB7687C2C871274EB3E42053F01DC25F74BC2E3879EB440EFCF175476194B8E1E442B21097C87D181359B710103C6CB5143B8135E7AB6E35E3C4C642816EBE73FB75732B776B35C00C34A5393075E7F635109920C7E6A6AE3DAEF6A9DF9CA8435B2703B3D7BEBFADAC93A7B6D058787ED66365F059FB229E5CD57E433FEEB8103B6E2200C893F8D033CC1E458F86E5F3F48FF63F79CC2B100301145C62A40C8BE4C548CD97CDFA69ECFCC2A16F4F6FB86B527C6CE75FDB7A1C41FF3D80A2697C84FB6D82F78C2D630C566915CF1242A14B1C7FEFC795CADA43BA8213DF86828FDB5E2772A890157E29FD35D9D0E7863DD6F5BDB627266D4C2387D57E3F6975F43A7C9237C88EAC6633EB7111703EC65BF4531B05071417CDFC0680EF6026E86C65B18386E853E62D6DB2323E10A7BCB594FF1CE898D249001C6EDEA880D244F404E4350B08E46AAC736553FA4F53EA124B1EE94BC790F263C37C9F265F5C6B10DAF6EB102A9FE5E8AC2049FB2FE4438CD4351ED727B64FEBDCCCDDBC406962F8BBA88D2648A5E4A9CE5EA5508F20C4398BFA3E997805D1BAFC54F8C661ED55E31B30DD179740D87D1329C7FA10D71E87EBFEF6541976B70A9C4C03DC7F69685B84B7B24188D29A9BF35AA5A34FA43CD64BBB4C0AB88CB83AC34DFCE45FCD757624C9C4E22840A0A92C680DAD2FA8209F2BD69DDB27A5405BDCC8A45340E062613BB384B3B7A38A0C4C7CDAED0FA0E9F58B970B7EF84766E6037B5E14615C5EC99BA86849E91A966DEDCA863AEFC994E714DBA38719406FF81ED66D8F00D0790E8407118A081618B50EAA5863B1F598D2B4C94789B802750E43B4A5B4A18AD4765B29D4BD28A47CC853AA66322FE6A721AB180F4927EC59C7DC980E57BF238146C4DBC52137BD0F8FCCDDF37C0F482080F53F88647862484F8318B5711F7D28BAE4AEC45E6832E3A66FB2C3D61074AB6914C773C118641FEA9C38D43D7F10B35E04C7863BF7A8719CF3A3EA484F14A939B4D7126EB84C70ACBC7B9370A097564BA410DD8C2990CE27D36C3E952FCFB98855D2AD495AFD5732A3C27DC3411982A8BB7E81B33743FE934CF4849E0F5AA4DD8B72C2DD0705F9D4836FCF9B1F3DDB779BCCE340179C8CF113B2719E10ED9ED56B26814018123AA89CBEAF03FF32CECF848C1311886CB85A8C9C6FE6B0D7B95B4B982196C713CA8143964E80DFDE2CB6DBF6E790A84F62B42CA0A61FAF1CAE5D2BA94297D900C06BDC9ED06DBF2DF3673BBC39AEC99DACC3CC9FCED970231F350BE3C2E9871635B17C49209A1F847CD11B74BCFF6966AE436C177F7290F69578153A05BE1809C8B81C0D7CBAFFBD91997BD8B4AC7717D6DC73A3D1E3CF4D4092CBC658F447A431E3C34221402199703EA42E9F082834DB4BADA5696D5176E767ABDAF47BA7B5D68DA3E0B3A3C0300742C5EA86D8B7CE3DF6334B1C518F0EAF578B87A8698718DE938352D642601CE4BFE5F4047F178FCA1D136808A26F964BA16BEC9A2767589B999C0119B6FF3FF9EF844EC25C77809BB68C3FB28BB0F3ABBDB4B4AE864789F33BBCA5DE41DFBD371E247A3170AAE55D81536FC50A50B58A71AB7668D5589174CAA3DA3B80C02E603CBB979AEB37778DFAB3260A1B70C976DB0B18AC0387B4023183BC5895609E47CC9B2678A9681A174F6C9F3C9CEFD6D7C3EB0BA300991CC58D9D9A3EAA7F3538B77B083EEAF7570EA773E2BCCD4FA7293A347D37D6385039FF43DCF062A0357C09A4A3A7C457DD0468AC80FB4151E13EFE2B108227D999C93DDD41D473BF1E5EF9C69A4C0F48D01D6B331F9A498FA557F5F5D6408D077D457B7CC4B64F60F086C49BDDB83C69475CF5137DCF39EF3DEB1BEB6B433DE79B4EB1E3EF79AE8099E7457B3376B777F0F27C8CED0D82B0E26390CB3699F338DC810F4343215FDFC48638A4982E445783564A5341AAAAD092FF12AB655DC1A7514D27A392F767BFF94E793AB0DAD2481B46E84D3573EC8F747FFA74CB5BEBF8F9616B836E28591F0A55B16434AC5968FF4BB56C058B76103D18449498EC33678EB3469CDA54BB7F46D84904B66180882C9E38F38A052BB088D12DB4CCB89E473662EC3D5077A55F52763B5BD9F34F57D09D5ED31273B62F944EB9ABCE4094A74BFA92DC2358E43FC855664483A650C802F225CCF1BE7B71EA1441F44269DF07F05DD58350EF84624B66F99B282613E8AADFD86D6A6DB26FA642256B3679C418BE75FFE3462B240004071C7B2D743B880E4641736D2C0FCF97A2B54B1466163972B79E43D1477C5F43D73724822FCE2836B86753D17E65AF0CE201ABD973BF59D6B851A3F9D7383F2335CF3BD98558B798E6F833156C1D8F409534F4C04B9BF8D227C4835905311FD28C5A9B18A8FF685F76D66C");
            userFace1.setUserId("e74e0c893fe64f82833a246679103c0d");
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(userFace1);
                }
            });

        }
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
    private void saveFace(FaceInfo faceInfo, ImageFrame imageFrame) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
            // 注册来源保存到注册人脸目录
             File file= new File(Environment.getExternalStorageDirectory()+"/register.jpg");
                // 压缩人脸图片至300 * 300，减少网络传输时间
       resize(bitmap, file, 300, 300);


    }
    public  void resize(Bitmap bitmap, File outputFile, int maxWidth, int maxHeight) {
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 图片大于最大高宽，按大的值缩放
            if (bitmapWidth > maxHeight || bitmapHeight > maxWidth) {
                float widthScale = maxWidth * 1.0f / bitmapWidth;
                float heightScale = maxHeight * 1.0f / bitmapHeight;

                float scale = Math.min(widthScale, heightScale);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
            }

            // save image
            FileOutputStream out = new FileOutputStream(outputFile);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                entranceContronller.checkYuanguFace(first.getDeviceNo(),first.getDeviceId(),Environment.getExternalStorageDirectory()+"/register.jpg");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean startPreviewImmediately() {
        return true;
    }
    long start = 0;
    @Override
    public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {

        if(faceRecognize==null){
          //  Toast.makeText(EntanceActivity.this,"人脸识别初始化失败",Toast.LENGTH_SHORT).show();
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
            File file= new File(Environment.getExternalStorageDirectory()+"/register.jpg");
            if(file.exists()){
                file.delete();
            }
            saveFace(faces[0],frame);
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

            byte[] feature = HexUtil.hexStringToByte(next.getFeature());
            final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(
                    feature, imageFrameFeature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = next.getUserId();

            }
        }
        Log.e(TAG, "identity: "+System.currentTimeMillis());
        identityStatus = IDENTITY_IDLE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EntanceActivity.this,userIdOfMaxScore,Toast.LENGTH_SHORT).show();
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
                                openDoor();
                                entranceContronller.checkInLog(uid, null, direction, 1);
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
            entranceContronller.getUser(1);
            if (venueutils.img != null) {
                entranceContronller.checkInLog(uid, HexUtil.bytesToHexString(PassControlApplication.getVenueUtils().img), direction, 2);
            }
        } else {
            entranceContronller.checkInLog(uid, null, direction, 1);
        }

    }

    @Override
    public void passSuccess(PasswordBean data) {
        showActivity(SettingActivity.class);
    }

    @Override
    public void CodeInSuccess(CodeInBean data) {
        openDoor();
        entranceContronller.checkInLog(null, null, direction, 3);
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
    public void YuanGuSuccess(YuanGuMessage yuanGuMessage) {
        if(yuanGuMessage.getRel()==0){
            openDoor();
        }else {
            TTSUtils.getInstance().speak(yuanGuMessage.getMsg());
        }
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
