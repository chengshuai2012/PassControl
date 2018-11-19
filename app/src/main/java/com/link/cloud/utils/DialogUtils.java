package com.link.cloud.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

import android.view.Window;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.link.cloud.R;
import com.link.cloud.activity.EntanceActivity;
import com.link.cloud.activity.SettingActivity;

import com.link.cloud.base.BaseActivity;


/**
 * Created by OFX002 on 2018/9/21.
 */

public class DialogUtils implements View.OnClickListener {
    private AlertDialog dialog;
    StringBuilder builder = new StringBuilder();
    Activity context;
    TextView inputTel;
    private DialogUtils(Activity context) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private static DialogUtils dialogUtils;

    public static synchronized DialogUtils getDialogUtils(Activity context) {
        if (dialogUtils == null) {
            dialogUtils = new DialogUtils(context);
        } else {
            dialogUtils = null;
            System.gc();
            dialogUtils = new DialogUtils(context);
        }
        return dialogUtils;
    }

    public void showManagerDialog(View view) {
        dialog.show();
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(770, 500);
        TextView cancel = view.findViewById(R.id.cancel);
        TextView psw_login = view.findViewById(R.id.psw_login);
        ImageView close = view.findViewById(R.id.close);
        cancel.setOnClickListener(this);
        psw_login.setOnClickListener(this);
        close.setOnClickListener(this);
        dialog.setCancelable(false);
        params.leftMargin = 30;
        window.setContentView(view, params);
    }


    public void showPsdDialog(View view) {
        dialog.show();
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(770, 869);
        inputTel = view.findViewById(R.id.input_tel);
        TextView bind_keypad_0 = view.findViewById(R.id.bind_keypad_0);
        TextView bind_keypad_1 = view.findViewById(R.id.bind_keypad_1);
        TextView bind_keypad_2 = view.findViewById(R.id.bind_keypad_2);
        TextView bind_keypad_3 = view.findViewById(R.id.bind_keypad_3);
        TextView bind_keypad_4 = view.findViewById(R.id.bind_keypad_4);
        TextView bind_keypad_5 = view.findViewById(R.id.bind_keypad_5);
        TextView bind_keypad_6 = view.findViewById(R.id.bind_keypad_6);
        TextView bind_keypad_7 = view.findViewById(R.id.bind_keypad_7);
        TextView bind_keypad_8 = view.findViewById(R.id.bind_keypad_8);
        TextView bind_keypad_9 = view.findViewById(R.id.bind_keypad_9);
        TextView bind_keypad_ok = view.findViewById(R.id.bind_keypad_ok);
        TextView bind_keypad_delect = view.findViewById(R.id.bind_keypad_delect);
        TextView confirm = view.findViewById(R.id.confirm);
        TextView venue_login = view.findViewById(R.id.venue_login);
        ImageView close = view.findViewById(R.id.close);
        close.setOnClickListener(this);
        venue_login.setOnClickListener(this);
        confirm.setOnClickListener(this);
        bind_keypad_0.setOnClickListener(this);
        bind_keypad_1.setOnClickListener(this);
        bind_keypad_2.setOnClickListener(this);
        bind_keypad_3.setOnClickListener(this);
        bind_keypad_4.setOnClickListener(this);
        bind_keypad_5.setOnClickListener(this);
        bind_keypad_6.setOnClickListener(this);
        bind_keypad_7.setOnClickListener(this);
        bind_keypad_8.setOnClickListener(this);
        bind_keypad_9.setOnClickListener(this);
        bind_keypad_ok.setOnClickListener(this);
        bind_keypad_delect.setOnClickListener(this);
        confirm.setOnClickListener(this);
        inputTel.setText(context.getResources().getString(R.string.manager_pwd));
        params.leftMargin = 30;
        window.setContentView(view, params);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
            case R.id.close:
                dialog.dismiss();
                break;
            case R.id.psw_login:
                dialog.dismiss();
                builder.delete(0,builder.length());
                View psw_dialog = View.inflate(context, R.layout.psw_dialog, null);
                showPsdDialog(psw_dialog);
                break;
            case R.id.venue_login:
                dialog.dismiss();
                View veune_dialog = View.inflate(context, R.layout.veune_dialog, null);
                showManagerDialog(veune_dialog);
                break;
            case R.id.bind_keypad_0:
            case R.id.bind_keypad_1:
            case R.id.bind_keypad_2:
            case R.id.bind_keypad_3:
            case R.id.bind_keypad_4:
            case R.id.bind_keypad_5:
            case R.id.bind_keypad_6:
            case R.id.bind_keypad_7:
            case R.id.bind_keypad_8:
            case R.id.bind_keypad_9:
                builder.append(((TextView) view).getText());
                if (inputTel != null) {
                    inputTel.setText(builder.toString());
                }
                break;
            case R.id.bind_keypad_ok:
                builder.delete(0, builder.length());
                inputTel.setText(context.getResources().getString(R.string.manager_pwd));
                break;
            case R.id.bind_keypad_delect:
                if (builder.length() >= 1) {
                    builder.deleteCharAt(builder.length() - 1);
                    inputTel.setText(builder.toString());
                } else {
                    inputTel.setText(context.getResources().getString(R.string.manager_pwd));
                }

                break;

            case R.id.confirm:
                String fisrt = Utils.getMD5( builder.toString()).toUpperCase();
                String second = Utils.getMD5(fisrt).toUpperCase();
                ( (EntanceActivity)context).gotoSetting(second);
                dialog.dismiss();
                break;
        }
    }

    public void dissMiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public boolean isShowing() {
      return dialog.isShowing();
    }
}
