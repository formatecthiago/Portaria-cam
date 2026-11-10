package com.control.door.portaria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    private static final String TAG = "WebAppInterface";
    private Context mContext;

    public WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void ativarServico(String phoneNumber) {
        Log.d(TAG, "JS solicitou ativação com número: " + phoneNumber);
        
        // Salva o número nas preferências compartilhadas para o CallService usar
        SharedPreferences prefs = mContext.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("numeroAlvo", phoneNumber).apply();

        // Inicia o serviço em segundo plano com suporte a foreground service
        Intent serviceIntent = new Intent(mContext, CallService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mContext.startForegroundService(serviceIntent);
        } else {
            mContext.startService(serviceIntent);
        }
    }

    @JavascriptInterface
    public void testarAlerta() {
        Log.d(TAG, "JS solicitou teste de alerta.");
        Intent intent = new Intent(mContext, CallService.class);
        intent.setAction("TEST_ALERT");
        mContext.startService(intent);
    }
}
