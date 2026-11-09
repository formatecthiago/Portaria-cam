// Ponte Nativa do Aplicativo de Portaria
window.PortariaBridge = {
    salvarNumero: function(numero) {
        try {
            if (window.AndroidInterface && typeof window.AndroidInterface.salvarNumero === 'function') {
                window.AndroidInterface.salvarNumero(numero);
            }
        } catch (e) { console.error(e); }
    },
    ativarServico: function() {
        try {
            if (window.AndroidInterface && typeof window.AndroidInterface.ativarServico === 'function') {
                window.AndroidInterface.ativarServico();
            }
        } catch (e) { console.error(e); }
    },
    testarAlerta: function() {
        try {
            if (window.AndroidInterface && typeof window.AndroidInterface.testarAlerta === 'function') {
                window.AndroidInterface.testarAlerta();
            }
        } catch (e) { console.error(e); }
    }
};
