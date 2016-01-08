package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.andreabaccega.widget.FormEditText;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.ApplicationSingleton;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.IViewInitializer;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ebad on 11/3/2015.
 */
public class Register extends Activity implements IViewInitializer {


    private String TAG = "Register Activity : ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register);
        SetupView();
    }

    @Bind({
            (R.id.fname),
            (R.id.email),
            (R.id.password),
            (R.id.cnfpassword)
    })
    List<FormEditText> inputValues;


    @OnClick(R.id.reg_btn)
    public void SignUP() {

        boolean allValid  = true;
        for (FormEditText ed : inputValues )
        {
            allValid = ed.testValidity() && allValid;
        }

        if(!allValid)
            return;

        final String login = new String(inputValues.get(0).getText().toString());
        final String password = new String(inputValues.get(2).getText().toString());
        final QBUser user = new QBUser(login, password);
        user.setEmail(inputValues.get(1).getText().toString());


        QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {
                Toast.makeText(Register.this, "Signup Sucessful: " , Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Sign up succesfull");
                Intent intent = getIntent();

                intent.putExtra("login", login);
                intent.putExtra("password", password);
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onError(List<String> errors) {
                Toast.makeText(Register.this, "Error: " +errors.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Sign up errors : " + errors.toString());
            }
        });
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
}
