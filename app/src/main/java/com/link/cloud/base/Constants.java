package com.link.cloud.base;

/**
 * Created by lu on 2016/6/16.
 */
public class Constants {

    public static final String TCP_URL = "120.79.244.162";
    public static final String MSG = "MSG_RECIEVE";
    public static final int TCP_PORT = 12125;
//   1:n入场
    public static final int MIX_IN_N = 1009;
//    1:n出场
    public static final int MIX_OUT_N = 1008;
//    1:1入场
    public static final int MIX_IN_1 = 1007;
    //    1:1出场
    public static final int MIX_OUT_1 = 1006;

    public static  int CABINET_TYPE =0;

    public static  int PAGE_NUM =50;



    public interface ActivityExtra {

        String TYPE="TYPE";
        String ENTITY="ENTITY";
        String UUID="UUID";
        String FINGER="FINGER";
        String XIAOCHENGXU="XIAOCHENGXU";
        String PASSWORD="PASSWORD";
    }

    public interface FragmentExtra {
        String BEAN="BEAN";
        String PRICELEVELBEAN="PRICELEVELBEAN";

    }


}
