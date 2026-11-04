// Ponte Nativa do Aplicativo de Portaria
window.PortariaBridge = {
    salvarNumero: function(numero) {
        try {
            if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.PortariaPlugin) {
                window.Capacitor.Plugins.PortariaPlugin.salvarNumero({ numero: numero });
                console.log("Número enviado para o nativo via Capacitor:", numero);
            } else {
                console.log("Modo Web/Fallback: Número salvo na sessão.");
            }
        } catch (e) {
            console.error("Erro ao salvar número nativamente:", e);
        }
    }
};

document.addEventListener('deviceready', () => {
    console.log("Ambiente nativo Capacitor inicializado com sucesso.");
}, false);
