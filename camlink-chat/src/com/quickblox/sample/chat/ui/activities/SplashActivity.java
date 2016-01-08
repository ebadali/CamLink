package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.quickblox.sample.chat.R;

/**
 * Created by Ebad on 11/3/2015.
 */

public class SplashActivity extends Activity {

    private Thread mSplashThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StartAnimations();

        final SplashActivity sPlashScreen = this;

        // The thread to wait for splash screen events
//        mSplashThread =  new Thread(){
//            @Override
//            public void run(){
//                try {
//                    synchronized(this){
//                        // Wait given period of time or exit on touch
//                        wait(8000);
//                    }
//                }
//                catch(InterruptedException ex){
//                }
//
////                finish();
////
////                // Run next activity
////                Intent intent = new Intent();
////                intent.setClass(sPlashScreen, Login.class);
////                startActivity(intent);
//
//            }
//        };

        //mSplashThread.start();

    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);
        iv.clearAnimation();
        iv.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();

                // Run next activity
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, Login.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });




    }
}