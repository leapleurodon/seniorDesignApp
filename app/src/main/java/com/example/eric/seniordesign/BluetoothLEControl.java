package com.example.eric.seniordesign;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
class BluetoothLEControl {

    private BluetoothGatt mGatt;

    private int numberConnections = 0;

    private boolean readyToSend = false;

    // UUIDs for UART service and associated characteristics.
    private static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    private static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    private String message;

    String recMsg;

    private BTControl BTControl;

    private Thread control;

    BluetoothLEControl(Context context) {
        Log.i("ServicePath", "Service Started...");

        message = "";

        BTControl = new BTControl();
        control = new Thread(BTControl);

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    // Get the bluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    Log.i("ServicePath", "Found the device: " + device.getName());

                    if(device.getName().compareTo("nRF8001") == 0)
                    {
                        mBluetoothAdapter.cancelDiscovery();

                        mGatt = device.connectGatt(context,false,gattCallback);
                    }

                }

            }

        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);

    }

    boolean currentBluetoothConnection() {
        // The method that is called from the activity to get the current BT connection
        return readyToSend;
    }

    void setMessage(String msg) {
        message = msg;
    }

    void disconnect() {
        Log.i("ThreadPath", "Thread Stopped...");

        readyToSend = false;

        if (mGatt != null) {
            mGatt.disconnect();
        }

        mGatt = null;

        tx = null;
        rx = null;

        BTControl.isStopped.set(true);

    }

    private class BTControl implements Runnable {

        AtomicBoolean isStopped = new AtomicBoolean(false);

        @Override
        public void run() {
            Log.i("ThreadPath", "Thread Started...");

            while(!this.isStopped.get()) {
                // Send the check command to the BT
                if(message.length() > 0) {
                    waitToSend(100);
                    sendMessage(message);
                    message = "";
                }
            }
        }

        // Method to wait x amount of milliseconds
        private void waitToSend(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Log.i(TAG, "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (numberConnections == 0) {
                        numberConnections = 1;
                        Log.i("ThreadPath", "STATE_CONNECTED = " + numberConnections);
                        gatt.discoverServices();
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED: {
                    numberConnections = 0;
                    Log.i("ThreadPath", "STATE_DISCONNECTED");
                    disconnect();
                    //gatt.disconnect();
                    break;
                }
                default:
                    Log.e("ThreadPath", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //List<BluetoothGattService> services = gatt.getServices();

            // Get the UART - which is how it sends messages
            Log.i("ThreadPath" + "UART_SER: ", "" + mGatt.getService(UART_UUID));
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));

            // Setup sending and receiving
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            // Setup a listener so it knows when a message is received
            mGatt.setCharacteristicNotification(rx, true);
            BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(desc);

            Log.i("ThreadPath", "TX: " + tx + " RX: " + rx);

            readyToSend = true;
            control.start();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("ThreadPath", characteristic.toString());
        }

        // Called when a message is sent
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

        }

        // Called when a message is received
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic chara) {
            onReceive();
        }

    }; // End of BluetoothGattCallback

    // Send the data to the UART
    private void send(byte[] data) {

        tx.setValue(data);

        mGatt.writeCharacteristic(tx);

        //while (writeInProgress) { }
    }

    // Convert the data into the format for the UART
    private void send(String data) {
        if (data != null && !data.isEmpty()) {
            send(data.getBytes(Charset.forName("UTF-8")));
        }
    }

    // Send a message with whatever string is passes in
    private void sendMessage(String message) {

        if (readyToSend) {
            // Make sure the message is 20 or less chars
            if (message.length() > 20) {
                message = message.substring(0, 20);
            }

            Log.i("ServicePath", message);
            send(message);
        }
    }

    // Update the screen when a message is received
    private void onReceive() {

        mGatt.readCharacteristic(rx);
        recMsg = rx.getStringValue(0);
        //Log.i("ThreadPath", "Rec: " + rx.getStringValue(0) );

    }

}
