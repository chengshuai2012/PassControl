package com.link.cloud.network;


/**
 * Created by qianlu on 2018/5/16.
 * 邮箱：zar.l@qq.com
 */
public class ApiConstants {

    //APP登录
    public static final String APPLOGIN = "/{deviceCode}/{password}";

    //获取验证码
    public static final String SENDVCODE = "sendVCode/{phone}";

    //退柜
    public static final String RETURNCABINET = "returnCabinet";

    //临时柜号
    public static final String TEMCABINET = "temporaryCabinet";

    //续开柜号
    public static final String USECABINET = "useCabinet";

    //分页获取指静脉
    public static final String GETUSERS = "users";

    //获取柜子配置信息
    public static final String GETCABINETINFO = "cabinetInfo";

    //VIP开柜
    public static final String VIPCABINET = "useCabinetVip";

    //验证指纹信息
    public static final String VALIDATEFINGERPRINTS = "/app/validateFingerprints";

    //获取单独用户
    public static final String GETSINGLEUSER = "user/{uuid}";

    //验证人脸
    public static final String IDENTIFYFACE = "user/{uuid}";

    //进场
    public static final String CHECKIN = "entranceGuard/{type}";

    //密码
    public static final String VALIDATEPASS = "validatePassword/{password}";

    //获取APP版本
    public static final String APPVERSION = "appVersion/{appType}";

    //下载App
    public static final String DOWNLOAD = "downloadApp/{appType}";

    //二维码开门
    public static final String QROPENDOOR = "entranceGuardByOther";

    //开门日志
    public static final String QROPENDOORLOG = "entranceGuardLog";

    //获取单个人脸
    public static final String GETSINGLEPERSONFACE = "userFaces/{uuid}";

    //分页获取人脸信息
    public static final String GETALLFACE = "faces";


}
