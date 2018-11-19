package com.link.cloud.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by ludaiqian on 16/7/7.
 */
public class Utils{
    public static String getMD5(String message) {
        String md5str = "";
        try {
            // 1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2 将消息变成byte数组
            byte[] input = message.getBytes();

            // 3 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            // 4 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            md5str = HexUtil.bytesToHexString(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }
    public static String getMac() {
        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig", "HWaddr");
        //如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "网络出错，请检查网络";
        }
        //对该行数据进行解析
        //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
            Log.i("test", "Mac:" + Mac + " Mac.length: " + Mac.length());
            result = Mac;
            Log.i("test", result + " result.length: " + result.length());
        }
        return result;
    }
    private static String callCmd(String cmd, String filter) {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            //执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine()) != null && line.contains(filter) == false) {
                //result += line;
                Log.i("test", "line: " + line);
            }
            result = line;
            Log.i("test", "result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }




    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        Date begDate = new Date(System.currentTimeMillis());
        String date = simpleDateFormat.format(begDate);
        return date;
    }


    /**
     * 截取scrollview的屏幕
     **/
    public static Bitmap getScrollViewBitmap(View view, Context context, boolean isSave) {
        if (view == null) {
            return null;
        }
        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);

        if (isSave)
            saveImageToGallery(context, bitmap);
        return bitmap;
    }

    public static Bitmap loadBitmapFromViewBySystem(View v) {
        if (v == null) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();
        return bitmap;
    }


    //保存文件到指定路径
    public static boolean imageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片  dearxy是可以改的
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + appDir)));
    }

    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    public static int Dp2Px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * px转dp
     *
     * @param px
     * @return
     */
    public static int Px2Dp(Context context, float px) {
        return (int) (px / context.getResources().getDisplayMetrics().density + 0.5f);

    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        // 获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        // 用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        // 从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }



    public static String readFromAssets(Context context, String name) {
        try {
            InputStream e = context.getResources().getAssets().open(name);
            byte[] buffer = new byte[1];
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            int len = -1;
            while (-1 != (len = e.read(buffer))) {
//                bIn.append(buffer, 0, buffer.length);
                bout.write(buffer, 0, len);
            }
            e.close();
            String res = new String(bout.toByteArray());
            return res.trim();
        } catch (IOException var6) {
            var6.printStackTrace();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return null;
    }




    public static boolean isPhoneNum(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        try {
            Pattern p = Pattern.compile("^((13[0-9])|(14[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9])|(92[0-9])|(98[0-9])|(16[0-9])|(19[0-9]))\\d{8}$");
            Matcher m = p.matcher(phoneNumber);
            return m.matches();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 密码大于等于6位
     *
     * @param password
     * @return
     */
    public static boolean isPasswordValid(String password) {
        return password.length() >= 6 && password.length() <= 18;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<?> wrap(Object value) {
        ArrayList list = new ArrayList();
        list.add(value);
        return list;
    }


    public static final int INCOME_LIST = 1;
    public static final int FEMALE_OCCUPATION_LIST = 2;
    public static final int MALE_OCCUPATION_LIST = 3;
    public static final int EMOTIONAL_STATE_LIST = 4;
    public static final int DATE_OF_DEPARTURE_LIST = 5;
    public static final int TAKE_TIME_LIST = 6;
    public static final int GIRL_LABELS = 7;//男标签
    public static final int BOY_LABELS = 8;//女标签
    public static final int WHETHER_DRIVING_RO_NOT = 9;//是否开车
    public static final int WHETHER_SMOKING_RO_NOT = 10;//是否抽烟
    public static final int WHETHER_DRINKING_RO_NOT = 11;//是否喝酒
    //
    public static final int VIEWPOINT_FOR_SEX = 12;//对性的看法
    public static final int GOOD_AT_SKILLS = 13;//擅长技能

    public static final int REQUIREMENT_FOR_BOY = 14;//对男伴的要求
    public static final int REQUIREMENT_FOR_GIRL = 15;//对女伴的要求
    public static final int TARGET_PERSON_GIRL_LIST = 16;//邀请类型
    public static final int TARGET_PERSON_BOY_LIST = 17;//邀请类型
    public static final int TARGET_PERSON_LIST = 18;//邀请类型
    public static final int DATE_PLAN_MESSAGE_LIST = 19;//邀请类型
    public static final int PAYMENT_BY = 20;//由谁支付
    public static final int TRAVEL_MODES = 21;//旅行方式


    public static boolean hasEmpty(EditText tv1, EditText... tv2s) {
        if (TextUtils.isEmpty(tv1.getText().toString().trim())) {
            return true;
        }
        for (EditText tv2 : tv2s) {
            if (TextUtils.isEmpty(tv2.getText().toString().trim())) {
                return true;
            }
        }

        return false;

    }

    public static boolean hasEmpty(TextView tv1, TextView... tv2s) {
        if (TextUtils.isEmpty(tv1.getText().toString())) {
            return true;
        }
        for (TextView tv2 : tv2s) {
            if (TextUtils.isEmpty(tv2.getText().toString())) {
                return true;
            }
        }

        return false;

    }

    @SuppressLint("SimpleDateFormat")
    public static Age caculateAge(String birthday) {
        if (birthday == null) {
            return new Age();
        }
        java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar from = Calendar.getInstance();
        try {
            from.setTime(df.parse(birthday));
        } catch (ParseException e) {
            birthday = "";
        }
        if ("".equals(birthday)) {
            Age brs = new Age();
            brs.setDays(0);
            brs.setMonth(0);
            return brs;
        } else {

            int yearbirthday = from.get(Calendar.YEAR);
            int monthbirthday = from.get(Calendar.MONTH);
            int daysbirthday = from.get(Calendar.DAY_OF_MONTH);

            GregorianCalendar to = (GregorianCalendar) Calendar.getInstance();

            int yearTo = to.get(Calendar.YEAR);
            int monthTo = to.get(Calendar.MONTH);
            int daysTo = to.get(Calendar.DAY_OF_MONTH);
            Age age = new Age();
            if (from.getTimeInMillis() > to.getTimeInMillis()) {
                age.setDays(0);
                age.setMonth(0);
            } else {
                int monthResult = 12 * (yearTo - yearbirthday) + (monthTo - monthbirthday);
                int daysResult = daysTo - daysbirthday;
                if (daysResult < 0) {
                    monthResult = monthResult - 1;
                    int daysCount = getDaysOfMonth(monthTo - 1, to.isLeapYear(yearTo));

                    daysResult = daysCount + daysResult;
                }
                age.setDays(daysResult);
                age.setYear(monthResult / 12);
                age.setMonth(monthResult % 12);
            }
            return age;
        }
    }


    private static int getDaysOfMonth(int month, boolean isLeepYear) {
        switch (month) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return isLeepYear ? 29 : 28;
            default:
                return 30;
        }
    }


    /**
     * 实现文本复制功能
     *
     * @param content
     */
    public static void copy(Context context, String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }


    public static boolean containEmpty(String... values) {
        for (String value : values) {
            if (TextUtils.isEmpty(value)) {
                return true;
            }
        }
        return false;
    }


    public static int parseInt(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);

        } catch (Exception e) {
            return 0;
        }
    }

    public static void removeFromParent(View child) {
        if (child.getParent() != null) {
            ViewGroup parent = (ViewGroup) child.getParent();
            parent.removeView(child);
        }
    }

    public static String parseTitle(String text) {
        String split = "\\n";
        int pos = text.indexOf(split);
        if (pos != -1) {
            String title = text.substring(0, pos);
            return title;
        }
        return text;
    }

    public static String splitLines(String text) {
        String split = "\\n";
        int pos = text.indexOf(split);
        if (pos != -1) {
            String title = text.substring(0, pos);
            String content = text.substring(pos + split.length(), text.length());
            return title + '\n' + content;
        } else {
            return text;
        }
    }

    public static int countNotEmpty(TextView t1, TextView... t2s) {
        int count = 0;
        if (!TextUtils.isEmpty(t1.getText().toString())) {
            count++;
        }
        for (TextView t : t2s) {
            if (!TextUtils.isEmpty(t.getText().toString())) {
                count++;
            }
        }
        return count;
    }



    public static String pack(List<String> picUrls) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < picUrls.size(); i++) {
            buffer.append(picUrls.get(i));
            if (i != picUrls.size() - 1) {
                buffer.append("@X@");
            }
        }
        return buffer.toString();
    }

    public static String getBlurUrl(String url) {
        if (!TextUtils.isEmpty(url)) {

            int position = url.indexOf("?");
            if (position != -1) {
                //?x-oss-process=image/resize,w_200/blur,r_3,s_2
                String result = url.substring(0, position) + "?x-oss-process=image/resize,w_300/blur,r_8,s_8";
                return result;
            } else {
                String result = url + "?x-oss-process=image/resize,w_300/blur,r_8,s_8";
                return result;
            }
        }
        return url;
    }

    private static final String COMPRESSED_PHOTO_NAME = "_saved.jpg";


    public static String parseDistance(String distance) {
        float distanceFloat = 10000f;
        try {
            distanceFloat = Float.parseFloat(!TextUtils.isEmpty(distance) ? distance : "0");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (distanceFloat >= 100) {
            return String.valueOf(Math.round((float) distanceFloat / 1000f)) + "km";

//            contentHolder.distanceTv.setText(String.valueOf(Math.round((float) distanceInt / 1000f)) + "km");
        } else {
            DecimalFormat formatter = new DecimalFormat("0.##");
            return formatter.format(distanceFloat / 1000f) + "km";
//            contentHolder.distanceTv.setText(String.format(Locale.CHINESE, "%.2f", + "km");

        }
    }


    /**
     * 获取当前进程名
     *
     * @param context
     * @return 进程名
     */
    public static final String getProcessName(Context context) {
        String processName = null;

        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;

                    break;
                }
            }

            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }

            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static String getPercentString(float percent) {
        return String.format(Locale.US, "%d%%", (int) (percent * 100));
    }


    /**
     * 坐标转换，百度地图坐标转换成腾讯地图坐标
     *
     * @param lat 百度坐标纬度
     * @param lon 百度坐标经度
     * @return 返回结果：纬度,经度
     */
    public static double[] map_bd2hx(double lat, double lon) {
        double tx_lat;
        double tx_lon;
        double x_pi = 3.14159265358979324;
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        tx_lon = z * Math.cos(theta);
        tx_lat = z * Math.sin(theta);

        double[] doubles = new double[]{tx_lat, tx_lon};
        return doubles;
    }


    public static String getCurrentActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
        ComponentName component = cinfo.topActivity;
        return component.getClassName();
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static boolean contain(String[] valueArray, String value) {
        for (String v : valueArray) {
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }


    public static class Age {
        int days;
        int month;
        int year;

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

    }

    public static String hidePhoneNum(String phoneNum) {
        String hideNum = "";
        if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() == 11) {
            String first = phoneNum.substring(0, 3);
            String sec = phoneNum.substring(7, 11);
            hideNum = first + "****" + sec;
        }
        return hideNum;
    }


    /**
     * Fragment跳转， 将一个layout替换为新的fragment。
     *
     * @param fm
     * @param fragmentClass
     * @param replaceLayoutId
     * @param args
     */
    public static Fragment replace(FragmentManager fm, Class<? extends Fragment> fragmentClass, int replaceLayoutId, Bundle args) {
        return replace(fm, fragmentClass, replaceLayoutId, fragmentClass.getSimpleName(), args);
    }

    /**
     * Fragment跳转， 将一个layout替换为新的fragment。
     *
     * @param fm
     * @param replaceLayoutId
     * @param fragmentClass
     * @return
     */
    public static Fragment replace(FragmentManager fm, int replaceLayoutId, Class<? extends Fragment> fragmentClass) {
        return replace(fm, fragmentClass, replaceLayoutId, fragmentClass.getSimpleName(), null);
    }

    /**
     * Fragment跳转， 将一个layout替换为新的fragment。
     *
     * @param fm
     * @param fragmentClass
     * @param tag
     * @param args
     * @return
     */
    public static Fragment replace(FragmentManager fm, Class<? extends Fragment> fragmentClass, int replaceLayoutId, String tag,
                                   Bundle args) {
        // mIsCanEixt = false;
        Fragment fragment = fm.findFragmentByTag(tag);
        boolean isFragmentExist = true;
        if (fragment == null) {
            try {
                isFragmentExist = false;
                fragment = fragmentClass.newInstance();
                if (args != null)
                    fragment.setArguments(args);
                else {
                    fragment.setArguments(new Bundle());
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            if (args != null) {
                if (fragment.getArguments() != null)
                    fragment.getArguments().putAll(args);
                else
                    fragment.setArguments(args);
            }
        }
        if (fragment == null)
            return null;
        if (fragment.isAdded()) {
            // fragment.onResume();
            return fragment;
        }
        FragmentTransaction ft = fm.beginTransaction();
        if (isFragmentExist) {
            ft.replace(replaceLayoutId, fragment);
        } else {
            ft.replace(replaceLayoutId, fragment, tag);
        }

        ft.addToBackStack(tag);
        ft.commitAllowingStateLoss();
        return fragment;
    }

    public static String formatMoney(float amounts) {
        double doubleAmounts = Double.valueOf(String.valueOf(amounts));
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(0);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return formater.format(doubleAmounts);
    }

    public static float valueOfMoney(float value) {
        return Float.valueOf(formatMoney(value));
    }

    /**
     * 浮点数加法，计算金额时使用 a+b
     *
     * @param a
     * @param b
     * @return 返回String类型
     */
    public static String floatAddString(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).add(new BigDecimal(Float.toString(b)));
        return result.toString();
    }

    /**
     * 浮点数加法，计算金额时使用 a+b
     *
     * @param a
     * @param b
     * @return 返回float型
     */
    public static float floatAddFloat(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).add(new BigDecimal(Float.toString(b)));
        return result.floatValue();
    }

    /**
     * 浮点数减法，计算金额时使用 a-b
     *
     * @param a
     * @param b
     * @return 返回String类型
     */
    public static String floatSubString(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).subtract(new BigDecimal(Float.toString(b)));
        return result.toString();
    }

    /**
     * 浮点数减法，计算金额时使用 a-b
     *
     * @param a
     * @param b
     * @return 返回float型
     */
    public static float floatSubFloat(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).subtract(new BigDecimal(Float.toString(b)));
        return result.floatValue();
    }


    /**
     * 浮点数减法，计算金额时使用 a-b
     *
     * @param a
     * @param b
     * @return 返回float型
     */
    public static String floatSubFloat(String a, String b) {
        BigDecimal result = new BigDecimal(a).subtract(new BigDecimal(b));
        return result.toString();
    }

    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    /**
     * 浮点数除法，计算金额使用 a/b
     *
     * @param a
     * @param b
     * @return 返回String类型
     */
    public static String floatDivideString(String a, String b) {
        BigDecimal result = new BigDecimal(a).divide(new BigDecimal(b));
        return result.toString();
    }

    /**
     * 浮点数除法，计算金额使用 a/b
     *
     * @param a
     * @param b
     * @return 返回float类型
     */
    public static float floatDivideFloat(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).divide(new BigDecimal(Float.toString(b)));
        return result.floatValue();
    }

    /**
     * 浮点数乘法，计算金额时使用 a*b
     *
     * @param a
     * @param b
     * @return 返回String类型
     */
    public static String floatMultiplyString(String a, String b) {
        BigDecimal result = new BigDecimal(a).multiply(new BigDecimal(b));
        return result.toString();
    }

    /**
     * 浮点数乘法，计算金额时使用 a*b
     *
     * @param a
     * @param b
     * @return 返回float类型
     */
    public static float floatMultiplyFloat(float a, float b) {
        BigDecimal result = new BigDecimal(Float.toString(a)).multiply(new BigDecimal(Float.toString(b)));
        return result.floatValue();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }


    public interface OnVisitorAccountCreatedListener {
        public void onVistorAccountCreated(String id, String password);

        public void onCreateFailed(String reason);
    }


    public static void createVisitorAccount(final OnVisitorAccountCreatedListener l) {
        // TODO Auto-generated method stub
//        RequestHeader header = new RequestHeader(Api.VISITOR_REGISTER, null);
//        Request request = new Request(header, null);
//        HttpTaskExecutor.getInstance().execute(request, new ResponseListener<ApiResponse<?>>() {
//
//            @Override
//            public void onResponse(ApiResponse<?> result) {
//                @SuppressWarnings("unchecked")
//                Response<RegisterResultResponse> resp = (Response<RegisterResultResponse>) result;
//                RegisterResultResponse data = resp.getData();
//                if (l != null)
//                    l.onVistorAccountCreated(data.getUser_id(), data.getPwd());
//            }
//
//            @Override
//            public void onError(HttpError paramHttpError) {
//                if (l != null)
//                    l.onCreateFailed(paramHttpError.getCauseMessage());
//            }
//        });

    }


    public static String hidePartCode(String code) {
        if (TextUtils.isEmpty(code)) return "******";
        if (code.length() == 1 || code.length() == 2) return code + "******";
        return code.substring(0, 3) + "******";
    }


    public interface OnVideoCoverCallBack {
        void onCoverResult(String coverPath);
    }

    private static String setVideoCachePath(String var0) {
        File var1;
        if (!(var1 = new File(var0)).exists()) {
            var1.mkdirs();
        }

        File var3;
        if (!(var3 = new File(var0, +System.currentTimeMillis() + "_cover.jpg")).exists()) {
            try {
                var3.createNewFile();
                return var3.getPath();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }
        return var0 + "/" + System.currentTimeMillis() + "_cover.jpg";
    }

    public static String parseObjectId(String data) {
//        data.replaceAll(" ","").replaceAll("\\s","");
        Pattern pattern = Pattern.compile("\"{0,1}objectId\"{0,1}\\s*[:=]\\s*\"{0,1}([^\"}]+)\"{0,1}");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find() && matcher.groupCount() > 0) {
            String objectId = matcher.group(1);
            //Config.get().storeObjectIdIfNull(objectId);
            return objectId;
        }
        return null;
    }


    public static String getAreaName(String province, String city) {
        if (province != null) {
            if (province.endsWith("省") || province.equals("市")) {
                province = province.substring(0, province.length() - 1);
            } else if (province.endsWith("新疆维吾尔自治区")) {
                province = "新疆";
            } else if (province.endsWith("内蒙古自治区")) {
                province = "内蒙古";
            } else if (province.endsWith("广西壮族自治区")) {
                province = "广西";
            } else if (province.endsWith("宁夏回族自治区")) {
                province = "宁夏";
            } else if (province.endsWith("西藏自治区")) {
                province = "西藏";
            } else if (province.equals("香港特别行政区")) {
                province = "香港";
            } else if (province.equals("澳门特别行政区")) {
                province = "澳门";
            }
        }

        if (city != null) {
            if (city.endsWith("市") || city.equals("县")) {
                city = city.substring(0, city.length() - 1);
            } else if (city.equals("延边朝鲜族自治州")) {
                city = "延边";
            } else if (city.equals("恩施土家族苗族自治州")) {
                city = "恩施";
            } else if (city.equals("湘西土家族苗族自治州")) {
                city = "湘西";
            } else if (city.equals("阿坝藏族羌族自治州")) {
                city = "阿坝";
            } else if (city.equals("甘孜藏族自治州")) {
                city = "甘孜";
            } else if (city.equals("凉山彝族自治州")) {
                city = "凉山";
            } else if (city.equals("黔东南苗族侗族自治州")) {
                city = "黔东南";
            } else if (city.equals("黔南布依族苗族自治州")) {
                city = "黔南";
            } else if (city.equals("黔西南布依族苗族自治州")) {
                city = "黔西南";
            } else if (city.equals("楚雄彝族自治州")) {
                city = "楚雄";
            } else if (city.equals("红河哈尼族彝族自治州")) {
                city = "红河";
            } else if (city.equals("文山壮族苗族自治州")) {
                city = "文山";
            } else if (city.equals("西双版纳傣族自治州")) {
                city = "西双版纳";
            } else if (city.equals("大理白族自治州")) {
                city = "大理";
            } else if (city.equals("德宏傣族景颇族自治州")) {
                city = "德宏";
            } else if (city.equals("怒江傈僳族自治州")) {
                city = "怒江";
            } else if (city.equals("迪庆藏族自治州")) {
                city = "迪庆";
            } else if (city.equals("临夏回族自治州")) {
                city = "临夏";
            } else if (city.equals("甘南藏族自治州")) {
                city = "甘南";
            } else if (city.equals("海南藏族自治州")) {
                city = "海南";
            } else if (city.equals("海北藏族自治州")) {
                city = "海北";
            } else if (city.equals("海西蒙古族藏族自治州")) {
                city = "海西";
            } else if (city.equals("黄南藏族自治州")) {
                city = "黄南";
            } else if (city.equals("果洛藏族自治州")) {
                city = "果洛";
            } else if (city.equals("玉树藏族自治州")) {
                city = "玉树";
            } else if (city.equals("伊犁哈萨克自治州")) {
                city = "伊犁";
            } else if (city.equals("博尔塔拉蒙古自治州")) {
                city = "博尔塔拉";
            } else if (city.equals("昌吉回族自治州")) {
                city = "昌吉";
            } else if (city.equals("巴音郭楞蒙古自治州")) {
                city = "巴音郭楞";
            } else if (city.equals("克孜勒苏柯尔克孜自治州")) {
                city = "克孜勒苏";
            }

        }
        if (TextUtils.equals(province, city)) {
            return province;
        }
        return province + " " + city;
    }

}

