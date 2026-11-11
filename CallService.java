package com.control.door.portaria;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import androidx.core.app.NotificationCompat;

public class CallService extends Service {

    private static final String CHANNEL_ID = "portaria_listen_channel";
    private static final String ALERT_CHANNEL_ID = "portaria_aggressive_alert_channel";
    private static final int NOTIF_ID = 1001;
    private static final int ALERT_NOTIF_ID = 2002;
    private BroadcastReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        startForeground(NOTIF_ID, buildForegroundNotification());
        registerCallReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // Canal de serviço de fundo (silencioso)
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Portaria Monitorando",
                        NotificationManager.IMPORTANCE_LOW
                );
                
                // Canal ultra-agressivo para chamadas (Prioridade Máxima + LED Vermelho/Azul + Vibração pesada)
                NotificationChannel alertChannel = new NotificationChannel(
                        ALERT_CHANNEL_ID,
                        "🚨 PORTARIA - ALERTA VERMELHO/AZUL",
                        NotificationManager.IMPORTANCE_HIGH
                );
                alertChannel.setDescription("Alerta máximo de chamada detectada para portaria");
                alertChannel.enableVibration(true);
                // Padrão de vibração agressivo (vibra, pausa, vibra forte)
                alertChannel.setVibrationPattern(new long[]{0, 800, 200, 800, 200, 800});
                alertChannel.enableLights(true);
                alertChannel.setLightColor(Color.RED); // Luz principal vermelha/azul de alerta policial/emergência
                alertChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(alertChannel);
            }
        }
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🛡️ Portaria Ativa (Modo Vigia)")
                .setContentText("Escutando chamadas do sistema...")
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void registerCallReceiver() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                
                if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && incomingNumber != null) {
                        dispararAlertaAgressivo(context, incomingNumber);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);
    }

    private void dispararAlertaAgressivo(Context context, String numeroEntrando) {
        // Intent para abrir o app ao clicar na notificação ou no botão
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openAppIntent.putExtra("numeroDetectado", numeroEntrando);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Constrói uma notificação altamente chamativa, pulsante, com cores de destaque e botões diretos
        NotificationCompat.Builder alertBuilder = new NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning) // Ícone chamativo na barra de status
                .setContentTitle("🔴🔵 ALERTA DE PORTARIA: CHAMADA DETECTADA!")
                .setContentText("Número Alvo Identificado: " + numeroEntrando)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("🚨 ATENÇÃO! Chamada recebida do número alvo: " + numeroEntrando + "\nToque em 'VER PORTARIA ONLINE' para abrir o painel de controle imediatamente!"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false) // Mantém fixa até que o usuário interaja
                .setOngoing(true)
                .setColor(Color.RED) // Cor de destaque vermelha na barra
                .addAction(android.R.drawable.ic_menu_view, "👁️ VER PORTARIA ONLINE", pendingIntent)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(ALERT_NOTIF_ID, alertBuilder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
