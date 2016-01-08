package com.quickblox.sample.chat;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;

import java.util.Date;
import java.util.List;

import vc908.stickerfactory.StickersManager;

public class ApplicationSingleton extends Application {
    private static final String TAG = ApplicationSingleton.class.getSimpleName();

    public static final String APP_ID = "30955";
    public static final String AUTH_KEY = "VwyumZjEX2PDLEz";
    public static final String AUTH_SECRET = "2J6PuhAdXMwfrvz";
    public static final String STICKER_API_KEY = "847b82c49db21ecec88c510e377b452c";

    public static String USER_LOGIN = "camlinkuser2";
    public static String USER_PASSWORD = "camlinkuser2";
    public static Date OLD_DATE = new Date();

    public static String APP_SESSION = "7279c0c233681fe0e51b1df6689e554376dd7003";


    private static ApplicationSingleton instance;
    public static ApplicationSingleton getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        instance = this;
        // Initialise QuickBlox SDK
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        try {

            QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle params) {
                    APP_SESSION = session.getToken();
                    OLD_DATE = new Date(session.getTimestamp());
                    // success
                }

                @Override
                public void onError(List<String> errors) {
                    Date date = new Date();
                    date.setYear(2017);
                    try {
                        BaseService.createFromExistentToken(APP_SESSION ,date);
                    } catch (BaseServiceException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        StickersManager.initialize(STICKER_API_KEY, this);
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void CreateSession()
    {

    }
}
