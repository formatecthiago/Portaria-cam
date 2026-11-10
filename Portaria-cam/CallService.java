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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class CallService extends Service {
    private static final String TAG = "CallServiceDebug";
    private static final String SERVICE_CHANNEL_ID = "portaria_service_channel_clean";
    public static final String ALERT_CHANNEL_ID = "portaria_alert_channel_clean";
    private BroadcastReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanais(this);
        startForegroundServiceCustom();
        registerCallReceiver();
    }

    public static void criarCanais(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Canal de Serviço
            NotificationChannel serviceChannel = new NotificationChannel(
                SERVICE_CHANNEL_ID, 
                "Serviço Ativo da Portaria", 
                NotificationManager.IMPORTANCE_LOW
            );
            manager.createNotificationChannel(serviceChannel);

            // Canal de Alerta
            NotificationChannel alertChannel = new NotificationChannel(
                ALERT_CHANNEL_ID, 
                "Alerta de Chamadas da Portaria", 
                NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Notificações prioritárias de chamadas");
            alertChannel.enableVibration(true);
            manager.createNotificationChannel(alertChannel);
        }
    }

    private void startForegroundServiceCustom() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentTitle("Portaria Ativa")
                .setContentText("Monitorando chamadas...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1337, notification);
    }

    private void registerCallReceiver() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            if (incomingNumber == null) incomingNumber = "";
                            String cleanIncoming = incomingNumber.replaceAll("\\D", "");

                            SharedPreferences prefs = context.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
                            String targetNumber = prefs.getString("numeroAlvo", "");
                            String cleanTarget = targetNumber.replaceAll("\\D", "");

                            if (!cleanTarget.isEmpty() && !cleanIncoming.isEmpty()) {
                                boolean matchExato = cleanIncoming.contains(cleanTarget) || cleanTarget.contains(cleanIncoming);
                                if (matchExato) {
                                    triggerAlertNotification(context);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro no BroadcastReceiver", e);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        criarCanais(this);
        if (intent != null && "TEST_ALERT".equals(intent.getAction())) {
            triggerAlertNotification(this);
        }
        return START_STICKY;
    }

    private void triggerAlertNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        criarCanais(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("🚨 CHAMADA DA PORTARIA!")
                .setContentText("Toque para abrir o app e ver a câmera.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        manager.notify(7777, builder.build());
        Log.d(TAG, "Notificação de alerta disparada com sucesso.");
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (callReceiver != null) { unregisterReceiver(callReceiver); }
        super.onDestroy();
    }
}
