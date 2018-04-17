package com.example.eric.seniordesign;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
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
    TextView dispStatus,btStatus;

    Switch FD,FP,BD,BP,seatBelt;

    Button park,reverse,drive,reconnect;

    Handler mHandler = new Handler();
    Runnable mTimer;

    String recMsg = "";

    int autoStatus = 0;

    int wasConnected = 0;

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
        btStatus = (TextView) findViewById(R.id.textView_btStatus);

        park = (Button) findViewById(R.id.button_Park);
        reverse = (Button) findViewById(R.id.button_Reverse);
        drive = (Button) findViewById(R.id.button_Drive);

        reconnect = (Button) findViewById(R.id.button_Reconnect);

        FD = (Switch) findViewById(R.id.switch_FD);
        FP = (Switch) findViewById(R.id.switch_FP);
        BD = (Switch) findViewById(R.id.switch_BD);
        BP = (Switch) findViewById(R.id.switch_BP);
        seatBelt = (Switch) findViewById(R.id.switch_SeatBelt);

        // Switch code here
        //region
        FD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)
                {
                    if(checkDoors())
                    {
                        setMessage("Doors:Open");
                    }
                }
                else
                {
                    setMessage("Doors:Closed");
                }
            }
        });


        FP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)
                {
                    if(checkDoors())
                    {
                        setMessage("Doors:Open");
                    }
                }
                else
                {
                    setMessage("Doors:Closed");
                }
            }
        });

        BD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)
                {
                    if(checkDoors())
                    {
                        setMessage("Doors:Open");
                    }
                }
                else
                {
                    setMessage("Doors:Closed");
                }
            }
        });

        BP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)
                {
                    if(checkDoors())
                    {
                        setMessage("Doors:Open");
                    }
                }
                else
                {
                    setMessage("Doors:Closed");
                }
            }
        });

        seatBelt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)
                {
                    setMessage("SeatBelt:On");
                }
                else
                {
                    setMessage("SeatBelt:Off");
                }
            }
        });
        //endregion
        // End of switch code

        mTimer = new Runnable() {
            @Override
            public void run() {

                if(bluetoothLEControl != null && bluetoothLEControl.currentBluetoothConnection())
                {
                    btStatus.setBackgroundResource(R.drawable.btcon);
                    wasConnected = 1;
                    reconnect.setEnabled(false);

                    recMsg = getMessage();
                    if(recMsg.compareTo("AutoStatus:0") == 0)
                    {
                        statusColor.setBackgroundColor(Color.RED);
                        dispStatus.setText(R.string.disabled);
                    }
                    else if(recMsg.compareTo("AutoStatus:1") == 0)
                    {
                        statusColor.setBackgroundColor(Color.YELLOW);
                        dispStatus.setText(R.string.preFlight);
                    }
                    else if(recMsg.compareTo("AutoStatus:2") == 0)
                    {
                        autoStatus = 1;
                        statusColor.setBackgroundColor(Color.BLUE);
                        dispStatus.setText(R.string.running);
                    }
                }
                else
                {
                    btStatus.setBackgroundResource(R.drawable.btdis);

                    if(wasConnected == 1)
                    {
                        reconnect.setEnabled(true);
                        wasConnected = 0;
                    }
                }

                mHandler.postDelayed(mTimer, 250);

            }
        };


        mHandler.postDelayed(mTimer, 100);

        connect();
    }

    @SuppressLint("ClickableViewAccessibility")
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
                animate(mPrevAngle, mCurrAngle);
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

    private void animate(double fromDegrees, double toDegrees) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration((long) 0);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        wheel.startAnimation(rotate);
        angle.setText(String.valueOf(mCurrAngle));
    }

    private boolean checkDoors() {

        if(FD.isChecked() && FP.isChecked() && BD.isChecked() && BP.isChecked())
        {
            return true;
        }

        // One of the doors is opened
        return false;
    }


    public void startAuto(View v) {
        setMessage("Start");
    }

    public void exitAuto(View v) {
        setMessage("Exit");
    }

    public void brakePressed(View v) {
        setMessage("Brakes:On");
    }

    public void accelPressed(View v) {
        setMessage("Accelerator:On");
    }

    public void stabilityPressed(View v) {
        setMessage("Stability:On");
    }

    public void park(View v) {
        setMessage("Park:On");

        park.setBackgroundColor(Color.DKGRAY);
        reverse.setBackgroundColor(Color.LTGRAY);
        drive.setBackgroundColor(Color.LTGRAY);
    }

    public void reverse(View v) {
        setMessage("Park:Off");

        park.setBackgroundColor(Color.LTGRAY);
        reverse.setBackgroundColor(Color.DKGRAY);
        drive.setBackgroundColor(Color.LTGRAY);
    }

    public void drive(View v) {
        setMessage("Park:Off");

        park.setBackgroundColor(Color.LTGRAY);
        reverse.setBackgroundColor(Color.LTGRAY);
        drive.setBackgroundColor(Color.DKGRAY);
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

    public void reconnect(View v){
        if(bluetoothLEControl != null && !bluetoothLEControl.currentBluetoothConnection()) {
            connect();
        }
    }

    public void setMessage(String s)
    {
        if (bluetoothLEControl != null && bluetoothLEControl.currentBluetoothConnection()) {
            bluetoothLEControl.setMessage(s);
        }
    }

    public String getMessage()
    {
        if (bluetoothLEControl != null && bluetoothLEControl.currentBluetoothConnection()) {
            return bluetoothLEControl.getMessage();
        }
        else
            return "";
    }
}
