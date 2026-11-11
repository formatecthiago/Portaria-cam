package com.control.door.portaria;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        verificarESolicitarPermissoes();
        iniciarServicoSegundoPlano();
        
        // Injeta a ponte de comunicação JavaScript para o HTML salvar o número no APK nativo
        if (this.bridge != null && this.bridge.getWebView() != null) {
            this.bridge.getWebView().addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");
        }
    }

    private void verificarESolicitarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissoes = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.POST_NOTIFICATIONS
            };

            boolean todasPermitidas = true;
            for (String permissao : permissoes) {
                if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED) {
                    todasPermitidas = false;
                    break;
                }
            }

            if (!todasPermitidas) {
                ActivityCompat.requestPermissions(this, permissoes, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void iniciarServicoSegundoPlano() {
        Intent serviceIntent = new Intent(this, CallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    // Classe de ponte nativa para receber o número salvo pelo index.html
    public static class WebAppInterface {
        private Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void salvarNumeroNativo(String numero) {
            if (numero != null) {
                SharedPreferences prefs = mContext.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("numero_alvo", numero.trim());
                editor.apply();
            }
        }
    }
}
