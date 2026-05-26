const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
setGlobalOptions({maxInstances: 10, region: "us-central1"});

exports.getProdutorDashboard = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Acesso negado.");
  }

  const uid = request.auth.uid;

  try {
    const avicultorDoc = await admin.firestore().collection("avicultores").doc(uid).get();
    const avicultorData = avicultorDoc.exists ? avicultorDoc.data() : null;

    const lotesSnapshot = await admin.firestore()
      .collection("avicultores")
      .doc(uid)
      .collection("lotes")
      .get();

    const lotesPromessas = lotesSnapshot.docs.map(async (loteDoc) => {
      const data = loteDoc.data();

      // Busca os registros deste lote para calcular os totais
      const registrosSnapshot = await loteDoc.ref.collection("registros").get();

      let totalMortes = 0;
      let totalConsumo = 0;
      let ultimoPeso = data.pesoInicial || 0;

      registrosSnapshot.forEach(regDoc => {
        const reg = regDoc.data();
        totalMortes += (reg.avesMortasPeriodo || 0);
        totalConsumo += (reg.consumoRacaoPeriodo || 0);
        if (reg.pesoAtualMedio > 0) ultimoPeso = reg.pesoAtualMedio;
      });

      const qtdInicial = data.quantidadeAvesInicial || 0;
      const avesVivas = qtdInicial - totalMortes;
      const taxaMortalidade = qtdInicial > 0 ? (totalMortes / qtdInicial) * 100 : 0;

      return {
        id: loteDoc.id,
        identificacao: data.numeroLote || "Sem número",
        galpao: data.galpao,
        linhagem: data.linhagem,
        avesIniciais: qtdInicial,
        avesAtuais: avesVivas,
        mortalidadeAcumulada: totalMortes,
        mortalidadePercentual: taxaMortalidade.toFixed(2) + "%",
        consumoTotal: totalConsumo.toFixed(2),
        pesoAtual: ultimoPeso.toFixed(3),
        status: data.status || "ATIVO",
        alerta: taxaMortalidade > 3 ? "Alerta" : "Normal",
        dataInicio: data.dataInicio
      };
    });

    const lotesProcessados = await Promise.all(lotesPromessas);

    return {
      nomeProdutor: avicultorData ? avicultorData.nome : "Produtor",
      propriedade: avicultorData ? avicultorData.nomePropriedade : "Fazenda",
      estatisticasGerais: {
        totalLotes: lotesProcessados.length,
        avesEmCampo: lotesProcessados.reduce((acc, l) => acc + (l.status === "ATIVO" ? l.avesAtuais : 0), 0),
        mortalidadeMedia: (lotesProcessados.reduce((acc, l) => acc + parseFloat(l.mortalidadePercentual), 0) / (lotesProcessados.length || 1)).toFixed(2) + "%"
      },
      lotes: lotesProcessados
    };
  } catch (error) {
    console.error("Erro no Dashboard:", error);
    throw new HttpsError("internal", error.message);
  }
});
