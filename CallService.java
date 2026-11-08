private void triggerAlertNotification(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String alertChannelId = "portaria_alert_channel_v2"; // Mudamos o ID para forçar recriação do canal

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            alertChannelId, 
            "Alerta Crítico da Portaria", 
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notificação urgente de chamada da portaria");
        channel.enableVibration(true);
        manager.createNotificationChannel(channel);
    }

    // Intent para abrir o MainActivity quando tocar na notificação
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, alertChannelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("🚨 CHAMADA DA PORTARIA!")
            .setContentText("Toque para ver a câmera ao vivo.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Força som e vibração padrão do sistema
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

    manager.notify(9999, builder.build());
    Log.d(TAG, "Notificação forçada disparada com sucesso!");
}
