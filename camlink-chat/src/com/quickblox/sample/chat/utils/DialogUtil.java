package com.quickblox.sample.chat.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.quickblox.sample.chat.R;

import java.util.zip.Inflater;

/**
 * Created by Ebad on 11/30/2015.
 */
public class DialogUtil {

    public static ProgressDialog GetLoadingDialog(Context cntx) {
        ProgressDialog progressDoalog = new ProgressDialog(cntx);
        if (!progressDoalog.isShowing()) {
            progressDoalog.show();
            progressDoalog.hide();
        }
        //progressDoalog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        progressDoalog.setCancelable(true);
        progressDoalog.getWindow().setGravity(Gravity.CENTER);
        progressDoalog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDoalog.setContentView(R.layout.progressdialog);


        return progressDoalog;
    }



    public static ProgressDialog GetChatActions(Context cntx, View.OnClickListener download, View.OnClickListener delete) {

        ProgressDialog progressDoalog = new ProgressDialog(cntx);
        if (!progressDoalog.isShowing()) {
            progressDoalog.show();
            progressDoalog.hide();
        }
        progressDoalog.setCancelable(true);

        progressDoalog.getWindow().setGravity(Gravity.CENTER);
        progressDoalog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        View view = progressDoalog.getLayoutInflater().inflate(R.layout.popup_keepmelogin, null);

        view.findViewById(R.id.download).setOnClickListener(download);
        view.findViewById(R.id.delete).setOnClickListener(delete);

        progressDoalog.setContentView(view);
        if (!progressDoalog.isShowing()) {
            progressDoalog.show();
            progressDoalog.hide();
        }
        return progressDoalog;


    }


}
