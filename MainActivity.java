package com.control.door.portaria;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injeta a ponte JavaScript no WebView do Capacitor de forma segura e completa
        if (this.bridge != null && this.bridge.getWebView() != null) {
            this.bridge.getWebView().addJavascriptInterface(new WebAppInterface(this), "AndroidInterface");
        }
    }
}
