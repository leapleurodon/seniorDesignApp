package com.example.eric.seniordesign;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    Handler mHandlerBT = new Handler();
    Runnable mTimer;
    Runnable mBTsend;

    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int MY_PERMISSIONS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_COARSE_LOCATION);

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

                if (autoStatus == 0) {
                    statusColor.setBackgroundColor(Color.RED);
                    dispStatus.setText("Auto: Disabled");
                } else if (autoStatus == 1) {
                    statusColor.setBackgroundColor(Color.YELLOW);
                    dispStatus.setText("Auto: Pre-Flight Check");
                } else if (autoStatus == 2) {
                    statusColor.setBackgroundColor(Color.BLUE);
                    dispStatus.setText("Auto: Running");
                }

                /* (checkDoors()) {
                    message = "Doors:Open";
                }
                else {
                    message = "Doors:Closed";
                }*/


                mHandler.postDelayed(mTimer, 1000);

            }
        };

        /* mBTsend = new Runnable() {
            @Override
            public void run() {

                if (bluetoothLEControl != null && bluetoothLEControl.currentBluetoothConnection()) {
                    bluetoothLEControl.setMessage(message);
                }

                mHandlerBT.postDelayed(mBTsend, 1000);
            }
        }; */

        mHandler.postDelayed(mTimer, 100);
        //mHandlerBT.postDelayed(mBTsend, 1000);

        connect();
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

    private boolean checkDoors() {

        if(FD.isChecked() && FP.isChecked() && BD.isChecked() && BP.isChecked())
        {
            return false;
        }

        // One of the doors is opened
        return true;
    }


    public void startAuto(View v) {
       setMessage("Start");
    }

    public void brakePressed(View v) {

        setMessage("Brakes:On");
    }

    BluetoothLEControl bluetoothLEControl;
    private void connect() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,"Bluetooth is not supported on this device.",Toast.LENGTH_LONG).show();
        } else {
            // Turn on the bluetooth if it is off
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                connect();
                return;
            }
        }

        if (bluetoothLEControl == null || !bluetoothLEControl.currentBluetoothConnection()) {
            bluetoothLEControl = new BluetoothLEControl(this);
        } else {
            Toast.makeText(this, "The Bluetooth is already connected.", Toast.LENGTH_LONG).show();
        }

    }

    public void disconnectBT(){
        if(bluetoothLEControl != null) {
            bluetoothLEControl.disconnect();
            bluetoothLEControl = null;
        }
    }

    public void setMessage(String s)
    {
        if (bluetoothLEControl != null && bluetoothLEControl.currentBluetoothConnection()) {
            bluetoothLEControl.setMessage(s);
        }
    }
}
