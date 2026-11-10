package com.control.door.portaria;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private CallService callService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callService = new CallService(this, this.bridge);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
