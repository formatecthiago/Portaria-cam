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
    private static final String CHANNEL_ID = "portaria_service_channel";
    private BroadcastReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundServiceCustom();
        registerCallReceiver();
    }

    private void startForegroundServiceCustom() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Serviço Ativo da Portaria", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Portaria Protegida")
                .setContentText("Monitorando chamadas em segundo plano...")
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
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        if (incomingNumber == null) incomingNumber = "";

                        // Limpa o número que está chamando (deixa só os dígitos)
                        String cleanIncoming = incomingNumber.replaceAll("\\D", "");

                        // Lê o número alvo salvo no cofre nativo do Android
                        SharedPreferences prefs = context.getSharedPreferences("PortariaPrefs", Context.MODE_PRIVATE);
                        String targetNumber = prefs.getString("numeroAlvo", "");
                        String cleanTarget = targetNumber.replaceAll("\\D", "");

                        Log.d(TAG, "Chamada de: " + cleanIncoming + " | Alvo esperado: " + cleanTarget);

                        // Compara os números de forma segura
                        if (!cleanTarget.isEmpty() && (cleanIncoming.contains(cleanTarget) || cleanTarget.contains(cleanIncoming))) {
                            Log.d(TAG, "SUCESSO! O número da portaria coincidiu. Disparando alerta...");
                            triggerAlertNotification(context);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao interceptar chamada", e);
                }
            }
        };

        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);
    }

    private void triggerAlertNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String alertChannelId = "portaria_alert_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(alertChannelId, "Alerta Urgente de Chamada", NotificationManager.IMPORTANCE_HIGH);
            channel.setBypassDnd(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, alertChannelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("🚨 CHAMADA DA PORTARIA DETECTADA!")
                .setContentText("Toque imediatamente para abrir a câmera ao vivo.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent);

        manager.notify(9999, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public onDestroy() {
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
        super.onDestroy();
    }
}
