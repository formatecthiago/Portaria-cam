private void triggerAlertNotification(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String alertChannelId = "portaria_alert_channel_v5";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            alertChannelId, 
            "Chamada Urgente da Portaria", 
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alerta prioritário de chamada");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
        manager.createNotificationChannel(channel);
    }

    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, alertChannelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("🚨 CHAMADA DA PORTARIA!")
            .setContentText("Toque para ver a câmera ao vivo.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true) // Força o Heads-up agressivo na tela
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

    manager.notify(9999, builder.build());
    Log.d(TAG, "Notificação de Chamada Maxima disparada com FullScreenIntent!");
}
