package com.control.door.portaria;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. Inicia solicitando APENAS as permissões comuns em lote na primeira abertura
        verificarESolicitarPermissoesComuns();
        
        // 2. Inicia o serviço de escuta em segundo plano
        iniciarServicoChamada();
    }

    private void verificarESolicitarPermissoesComuns() {
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

    // Método chamado após o usuário lidar com as permissões comuns ou acionado por botão específico
    private void verificarPermissaoSobreposicaoRestrita() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Assim que o usuário responde às permissões comuns, podemos dar o próximo passo
            // e solicitar a permissão restrita de sobreposição de forma limpa.
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    verificarPermissaoSobreposicaoRestrita();
                }
            }, 1000);
        }
    }

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

        @JavascriptInterface
        public void simularChamadaComAtraso(final String numero) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    CallService.dispararNotificacaoBanner(mContext, numero);
                }
            }, 10000);
        }
    }
}
