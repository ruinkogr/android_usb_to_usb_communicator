package com.example.usb2usbcomm;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import com.example.usb2usbcomm.databinding.ActivityChatBinding;

import java.util.HashMap;

public class ConnectActivity extends Activity {
    private static final String TAG = "ConnectActivity";
    public static final String DEVICE_EXTRA_KEY = "device";
    private UsbManager mUsbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityChatBinding binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.i(TAG, "onCreate: mUsbManager=" + mUsbManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.i(TAG, "onResume: deviceList=" + deviceList);
        if (deviceList == null || deviceList.size() == 0) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (searchForUsbAccessory(deviceList)) {
            return;
        }
        for (UsbDevice device : deviceList.values()) {
            initAccessory(device);
        }
        finish();
    }

    private boolean searchForUsbAccessory(final HashMap<String, UsbDevice> deviceList) {
        Log.i(TAG, "searchForUsbAccessory: deviceList=" + deviceList);
        for (UsbDevice device : deviceList.values()) {
            if (isUsbAccessory(device)) {
                Log.i(TAG, "searchForUsbAccessory(if (isUsbAccessory(device))): " + device);
                final Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(DEVICE_EXTRA_KEY, device);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return false;
    }

    private boolean isUsbAccessory(final UsbDevice device) {
        Log.i(TAG, "isUsbAccessory: device=[name=" + device.getDeviceName() +
                ", manufacturerName=" + device.getManufacturerName() +
                ", productName=" + device.getProductName() +
                ", deviceId=" + device.getDeviceId() +
                ", productId=" + device.getProductId() +
                ", deviceProtocol=" + device.getDeviceProtocol() + "]");
        return (device.getProductId() == 0x2d00) || (device.getProductId() == 0x2d01);
    }

    private boolean initAccessory(final UsbDevice device) {
        Log.i(TAG, "initAccessory: device=[name=" + device.getDeviceName() +
                ", manufacturerName=" + device.getManufacturerName() +
                ", productName=" + device.getProductName() +
                ", deviceId=" + device.getDeviceId() +
                ", productId=" + device.getProductId() +
                ", deviceProtocol=" + device.getDeviceProtocol() + "]");
        if (!mUsbManager.hasPermission(device)) {
            Log.i(TAG, "initAccessory: Do not have permission on device=" + device.getProductName());
            Intent intent = new Intent(this, this.getClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Log.i(TAG, "initAccessory: Trying to get permissions with pendingIntent=" + pendingIntent);
            mUsbManager.requestPermission(device, pendingIntent);
        }
        final UsbDeviceConnection connection = mUsbManager.openDevice(device);
        Log.i(TAG, "initAccessory: connection=" + connection);
        if (connection == null) {
            return false;
        }
        initStringControlTransfer(connection, 0, "example"); // MANUFACTURER
        initStringControlTransfer(connection, 1, "Usb2UsbAccessory"); // MODEL
        initStringControlTransfer(connection, 2, "demonstrating USB-to-USB communication for Android based devices"); // DESCRIPTION
        initStringControlTransfer(connection, 3, "0.1"); // VERSION
        initStringControlTransfer(connection, 4, "http://ping-test.net"); // URI
        initStringControlTransfer(connection, 5, "42"); // SERIAL
        connection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, Constants.USB_TIMEOUT_IN_MS);
        connection.close();
        return true;
    }

    private void initStringControlTransfer(final UsbDeviceConnection deviceConnection,
                                           final int index,
                                           final String string) {
        Log.i(TAG, "initStringControlTransfer: deviceConnection=" + deviceConnection +
                ", index=" + index + ", string=" + string);
        deviceConnection.controlTransfer(0x40, 52, 0, index,
                string.getBytes(), string.length(), Constants.USB_TIMEOUT_IN_MS);
    }
}
