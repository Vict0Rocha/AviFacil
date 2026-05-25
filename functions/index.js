const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

// Inicializa o Admin SDK para acessar o Firestore
admin.initializeApp();

// Configurações globais (limite de instâncias para economizar custo)
setGlobalOptions({ maxInstances: 10, region: "us-central1" });

/**
 * Função para obter o resumo de um lote.
 * O Front-end apenas passa o ID do lote, e o Back-end faz os cálculos.
 */
exports.getLotePerformance = onCall(async (request) => {

    // 1. Verificação de Autenticação (DESATIVADA TEMPORARIAMENTE PARA TESTE)
    /*
    if (!request.auth) {
        throw new HttpsError("unauthenticated", "Você precisa estar logado.");
    }
    */

    const { loteId } = request.data;
    if (!loteId) {
        throw new HttpsError("invalid-argument", "O ID do lote é obrigatório.");
    }

    try {
        // 2. Busca os dados brutos no Firestore
        const loteDoc = await admin.firestore().collection("lotes").doc(loteId).get();

        if (!loteDoc.exists) {
            throw new HttpsError("not-found", `O lote com ID '${loteId}' não existe na coleção 'lotes'.`);
        }

        const data = loteDoc.data();

        // 3. REGRA DE NEGÓCIO (Calculada aqui no Back-end)
        const qtdInicial = data.quantidadeInicial || 0;
        const mortes = data.mortalidadeAcumulada || 0;

        // Cálculo da taxa de mortalidade
        const taxaMortalidade = qtdInicial > 0 ? (mortes / qtdInicial) * 100 : 0;

        // Status do lote (Lógica de decisão centralizada)
        let statusDesempenho = "Bom";
        if (taxaMortalidade > 5) statusDesempenho = "Crítico";
        else if (taxaMortalidade > 3) statusDesempenho = "Alerta";

        // 4. Retorna apenas o que o site precisa exibir
        return {
            identificacao: data.identificacao || "Lote sem nome",
            totalAves: qtdInicial,
            mortalidadePercentual: taxaMortalidade.toFixed(2) + "%",
            alertaStatus: statusDesempenho,
            dataInicio: data.dataInicio
        };

    } catch (error) {
        console.error("Erro ao processar performance:", error);
        // Retorna o erro real para sabermos o que aconteceu
        throw new HttpsError("internal", error.message || "Erro desconhecido");
    }
});
