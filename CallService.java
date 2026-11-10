package com.control.door.portaria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallService {
    private static final String TAG = "CallServiceDebug";
    private Context context;
    private BroadcastReceiver callReceiver;

    public CallService(Context ctx) {
        this.context = ctx;
        registerReceiver();
    }

    private void registerReceiver() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            if (incomingNumber == null) incomingNumber = "Desconhecido";
                            
                            Log.d(TAG, "Chamada recebida de: " + incomingNumber);
                            
                            // Aqui podemos disparar o log direto para o WebView se necessário
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro no BroadcastReceiver", e);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(callReceiver, filter);
        Log.d(TAG, "BroadcastReceiver de chamadas registrado com sucesso.");
    }
}
