package com.control.door.portaria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.getcapacitor.Bridge;

public class CallService {
    private static final String TAG = "CallServiceDebug";
    private Context context;
    private Bridge bridge;
    private BroadcastReceiver callReceiver;

    public CallService(Context ctx, Bridge bridge) {
        this.context = ctx;
        this.bridge = bridge;
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
                            
                            Log.d(TAG, "Chamada real detectada de: " + incomingNumber);
                            
                            if (bridge != null && bridge.getWebView() != null) {
                                final String numeroFinal = incomingNumber;
                                bridge.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bridge.getWebView().evaluateJavascript(
                                            "onChamadaDetectadaDoNativo('" + numeroFinal + "');", 
                                            null
                                        );
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao capturar chamada", e);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(callReceiver, filter);
        Log.d(TAG, "BroadcastReceiver de chamadas nativas ativado com sucesso.");
    }
}
