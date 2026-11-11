package com.control.door.portaria;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Solicita todas as permissões críticas logo na inicialização
        verificarESolicitarPermissoes();
        
        // Inicia o serviço de escuta em segundo plano
        iniciarServicoSegundoPlano();
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
}
