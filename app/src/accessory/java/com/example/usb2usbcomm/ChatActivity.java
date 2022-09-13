package com.example.usb2usbcomm;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class ChatActivity extends BaseChatActivity {
    private AccessoryCommunicator communicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        communicator = new AccessoryCommunicator(this) {
            @Override
            public void onReceive(byte[] payload, int length) {
                printLineToUI("host> " + new String(payload, 0, length));
            }

            @Override
            public void onError(String msg) {
                printLineToUI("notify" + msg);
            }

            @Override
            public void onConnected() {
                printLineToUI("connected");
            }

            @Override
            public void onDisconnected() {
                printLineToUI("disconnected");
            }
        };
    }

    @Override
    protected void sendString(String string) {
        communicator.send(string.getBytes());
    }
}
