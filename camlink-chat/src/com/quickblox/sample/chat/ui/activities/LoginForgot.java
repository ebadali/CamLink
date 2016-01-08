package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.IViewInitializer;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ebad on 11/4/2015.
 */
public class LoginForgot extends Activity implements IViewInitializer {

    @Bind({
            (R.id.loginforgot_backtologin),
            (R.id.loginforgot_email),
            (R.id.loginforgot_ihavepin),
            (R.id.loginforgot_resetbtn)
    })
    List<TextView> nameViews;


    @Bind(R.id.loginforgot_forgtpass_layout)
    ViewGroup pinRequestView;

    @Bind(R.id.loginforgot_ihavepin_layout)
    ViewGroup insertPinView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loginforgot);
        SetupView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void SetupView() {
        ButterKnife.bind(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.loginforgot_ihavepin)
    public void IHaveAPinEvent(TextView view) {

        pinRequestView.setVisibility(View.GONE);
        insertPinView.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.loginforgot_ineedpin)
    public void INeedPinEvent(TextView view) {

        insertPinView.setVisibility(View.GONE);
        pinRequestView.setVisibility(View.VISIBLE);

    }

}
