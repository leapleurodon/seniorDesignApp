package com.example.eric.seniordesign;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageView wheel;
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;

    TextView angle;
    TextView statusColor;
    TextView dispStatus;

    Switch FD,FP,BD,BP;

    int autoStatus = 0;

    Handler mHandler = new Handler();
    Runnable mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wheel = (ImageView) findViewById(R.id.imageView);
        wheel.setOnTouchListener(this);

        angle = (TextView) findViewById(R.id.textView_WheelAngle);
        statusColor = (TextView) findViewById(R.id.textView_StatusColor);
        dispStatus = (TextView) findViewById(R.id.textView_DispStatus);


        FD = (Switch) findViewById(R.id.switch_FD);
        FP = (Switch) findViewById(R.id.switch_FP);
        BD = (Switch) findViewById(R.id.switch_BD);
        BP = (Switch) findViewById(R.id.switch_BP);


        mTimer = new Runnable() {
            @Override
            public void run() {
                if(autoStatus == 0) {
                    statusColor.setBackgroundColor(Color.RED);
                    dispStatus.setText("Auto: Disabled");
                }

                if(autoStatus == 1) {
                    statusColor.setBackgroundColor(Color.YELLOW);
                    autoStatus = 2;
                    dispStatus.setText("Auto: Pre-Flight Check");
                }

                else if(autoStatus == 2) {
                    statusColor.setBackgroundColor(Color.BLUE);
                    dispStatus.setText("Auto: Running");
                }

                if( checkDoors() ) {
                    autoStatus = 0;
                }

                if(autoStatus == 0 || autoStatus == 1) {
                    mHandler.postDelayed(mTimer, 100);
                }
                else {
                    mHandler.postDelayed(mTimer, 2000);
                }
            }
        };

        mHandler.postDelayed(mTimer, 100);
    }

    private boolean checkDoors() {

        if(FD.isChecked() && FP.isChecked() && BD.isChecked() && BP.isChecked())
        {
            return false;
        }

        // One of the doors is opened
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final float xc = wheel.getWidth() / 2;
        final float yc = wheel.getHeight() / 2;
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                {
                    wheel.clearAnimation();
                    mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                {
                    mPrevAngle = mCurrAngle;
                    mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                    animate(mPrevAngle, mCurrAngle, 0);
                    angle.setText(String.valueOf(mCurrAngle));
                    break;
                }
            case MotionEvent.ACTION_UP : {
                    mPrevAngle = mCurrAngle = 0;
                    break;
                }
            }
            return true;
    }

    private void animate(double fromDegrees, double toDegrees, long durationMillis) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        wheel.startAnimation(rotate);
        angle.setText(String.valueOf(mCurrAngle));
    }

    public void startAuto(View v) {
        autoStatus = 1;
    }

    public void brakePressed(View v) {
        autoStatus = 0;
    }
}
