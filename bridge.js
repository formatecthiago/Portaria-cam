// Ponte Nativa do Aplicativo de Portaria
document.addEventListener('deviceready', () => {
    console.log("Ambiente nativo Capacitor inicializado com sucesso.");
    
    // Opcional: Solicitar permissões de notificação ou estado se necessário
    if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.LocalNotifications) {
        window.Capacitor.Plugins.LocalNotifications.requestPermissions();
    }
}, false);
