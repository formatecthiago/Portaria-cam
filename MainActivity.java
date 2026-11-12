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
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica e solicita permissões necessárias em tempo de execução
        verificarESolicitarPermissoes();

        // Inicia o serviço de escuta de chamadas em segundo plano
        iniciarServicoChamada();
    }

    private void verificarESolicitarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissoes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissoes = new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.CAMERA
                };
            } else {
                permissoes = new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CAMERA
                };
            }

            boolean needsRequest = false;
            for (String perm : permissoes) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    needsRequest = true;
                    break;
                }
            }

            if (needsRequest) {
                ActivityCompat.requestPermissions(this, permissoes, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void iniciarServicoChamada() {
        Intent serviceIntent = new Intent(this, CallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    // Configuração da Bridge para interagir com o JavaScript do index.html
    @Override
    public void onStart() {
        super.onStart();
        if (this.bridge != null && this.bridge.getWebView() != null) {
            WebView webView = this.bridge.getWebView();
            webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");
        }
    }

    public static class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void salvarNumeroNativo(String numero) {
            SharedPreferences prefs = mContext.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("numero_alvo", numero);
            editor.apply();
        }
    }
}
