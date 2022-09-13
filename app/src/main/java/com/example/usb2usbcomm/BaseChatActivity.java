package com.example.usb2usbcomm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.usb2usbcomm.databinding.ActivityChatBinding;

public abstract class BaseChatActivity extends Activity {
    private ActivityChatBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputString = binding.inputEdittext.getText().toString();
                if (inputString.length() == 0) {
                    return;
                }

                sendString(inputString);
                printLineToUI(getString(R.string.local_prompt) + inputString);
                binding.inputEdittext.setText("");
            }
        });
    }

    protected abstract void sendString(final String string);

    protected void printLineToUI(final String line) {
        runOnUiThread(() -> {
            binding.contentText.append("\n");
            binding.contentText.append(line);
        });
    }
}
