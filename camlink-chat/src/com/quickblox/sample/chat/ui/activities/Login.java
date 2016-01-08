package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.ApplicationSingleton;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.sample.chat.pushnotifications.Consts;
import com.quickblox.sample.chat.utils.IViewInitializer;
import com.quickblox.users.model.QBUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ebad on 11/4/2015.
 */
public class Login extends Activity implements IViewInitializer {

    @Bind({

            (R.id.login_text_stay_login_for),
            (R.id.login_forgtpass),
            (R.id.login_signup),
            (R.id.login_reg_btn),
            (R.id.login_text_error)
    })
    List<TextView> nameViews;


    @Bind(R.id.login_email)
    FormEditText loginEmail;
    @Bind(R.id.login_pass)
    FormEditText password;
    @Bind(R.id.login_reg_btn)
    Button btn;
    private String TAG = "Login : ";
    long sixTimesAday = 7200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        SetupView();

//

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void SetupView() {
        ButterKnife.bind(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }

    @OnClick(R.id.login_forgtpass)
    public void ForgotPass() {
        startActivityForResult(new Intent(this, LoginForgot.class), 0);
    }

    @OnClick((R.id.login_signup))
    public void SignUp() {
        startActivityForResult(new Intent(this, Register.class), 1);
    }

    ProgressDialog progress;

    //    @OnClick (R.id.login_reg_btn)
    public void Login() {

        if (loginEmail.testValidity() && password.testValidity()) {

            progress = new ProgressDialog(this);
            progress.setMessage("Authenticating User");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            progress.show();


            ApplicationSingleton.USER_LOGIN = new String(loginEmail.getText().toString());
            ApplicationSingleton.USER_PASSWORD = new String(password.getText().toString());

            //if(view != null )view.setVisibility(View.GONE);
            final QBUser user = new QBUser();
            user.setLogin(ApplicationSingleton.USER_LOGIN);
            user.setPassword(ApplicationSingleton.USER_PASSWORD);


            ChatService.initIfNeed(this);

            ChatService.getInstance().login(user, new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    // Go to Dialogs screen
                    //
                    Log.i(TAG, "Login Succesfull");
                    progress.dismiss();

                    Intent intent = new Intent(Login.this, DialogsActivity.class);
                    startActivity(intent);
                    finish();


                    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(DialogsActivity.mPushReceiver,
                            new IntentFilter(Consts.NEW_PUSH_EVENT));

                }

                @Override
                public void onError(List errors) {
                    progress.dismiss();
                    if (errors.toString().contains("already")) {
                        Intent intent = new Intent(Login.this, DialogsActivity.class);
                        startActivity(intent);
                        finish();


                    } else {
                        Log.i(TAG, "Login Failed " + errors.toString());

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                        dialog.setMessage("chat login errors: " + errors).create().show();

                    }

                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            Log.i(TAG, "Result Code = " + resultCode);
            Log.i(TAG, "Request Code = " + requestCode);
            if (resultCode == RESULT_OK) {

                loginEmail.setText(data.getStringExtra("login"));
                password.setText(data.getStringExtra("password"));
                btn.callOnClick();
            }
        } catch (Exception ex) {
            Toast.makeText(Login.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
