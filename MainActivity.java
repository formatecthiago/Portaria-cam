package com.control.door.portaria;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarBridgeNativa();
    }

    private void configurarBridgeNativa() {
        if (this.bridge != null && this.bridge.getWebView() != null) {
            this.bridge.getWebView().getSettings().setJavaScriptEnabled(true);
            this.bridge.getWebView().addJavascriptInterface(new WebAppInterface(this, this.bridge.getWebView()), "AndroidInterface");
        }
    }
}

class WebAppInterface {
    Context mContext;
    WebView mWebView;

    WebAppInterface(Context c, WebView w) {
        mContext = c;
        mWebView = w;
    }

    @JavascriptInterface
    public void salvarNumero(String numero) {
        SharedPreferences prefs = mContext.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("numeroAlvo", numero).apply();
    }

    @JavascriptInterface
    public void ativarServico() {
        // Exige as permissões nativas se o Android for M ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissoes = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.POST_NOTIFICATIONS
            };
            
            boolean temPermissoes = true;
            for (String perm : permissoes) {
                if (ContextCompat.checkSelfPermission(mContext, perm) != PackageManager.PERMISSION_GRANTED) {
                    temPermissoes = false;
                    break;
                }
            }

            if (!temPermissoes && mContext instanceof android.app.Activity) {
                ActivityCompat.requestPermissions((android.app.Activity) mContext, permissoes, 505);
            }
        }

        Intent serviceIntent = new Intent(mContext, CallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(serviceIntent);
        } else {
            mContext.startService(serviceIntent);
        }

        final String msgLog = "--- SISTEMA ---\\nServiço ativado e permissões solicitadas.";
        String script = "if(typeof logMsg === 'function'){ logMsg('" + msgLog + "'); }";
        mWebView.post(() -> mWebView.evaluateJavascript(script, null));
    }

    @JavascriptInterface
    public void testarAlerta() {
        Intent intent = new Intent(mContext, CallService.class);
        intent.setAction("TEST_ALERT");
        mContext.startService(intent);

        finalString msgLog = "--- AUDITORIA NATIVA ---\\nComando de teste despachado.";
        String script = "if(typeof logMsg === 'function'){ logMsg('" + msgLog + "'); }";
        mWebView.post(() -> mWebView.evaluateJavascript(script, null));
    }
}
