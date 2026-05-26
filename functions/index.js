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

    let geralAlojadas = 0;
    let geralMortas = 0;
    let geralVivas = 0;
    let somaGMP = 0;
    let somaCA = 0;
    let lotesAtivosCount = 0;

    const lotesPromessas = lotesSnapshot.docs.map(async (loteDoc) => {
      const data = loteDoc.data();
      const registrosSnapshot = await loteDoc.ref.collection("registros").get();

      let totalMortes = 0;
      let totalConsumoRacao = 0;
      let pesoAtualKg = data.pesoInicial || 0.045;
      const pesoInicialKg = data.pesoInicial || 0.045;

      registrosSnapshot.forEach(regDoc => {
        const reg = regDoc.data();
        totalMortes += (reg.avesMortasPeriodo || 0);
        totalConsumoRacao += (reg.consumoRacaoPeriodo || 0);
        if (reg.pesoAtualMedio > 0) pesoAtualKg = reg.pesoAtualMedio;
      });

      const qtdAlojada = data.quantidadeAvesInicial || 0;
      const avesVivas = Math.max(0, qtdAlojada - totalMortes);

      geralAlojadas += qtdAlojada;
      geralMortas += totalMortes;
      geralVivas += avesVivas;

      const taxaMortalidade = qtdAlojada > 0 ? (totalMortes / qtdAlojada) * 100 : 0;
      const viabilidade = 100 - taxaMortalidade;

      const dataInicioRaw = data.dataInicio;
      const dataInicio = (dataInicioRaw && dataInicioRaw.toDate) ? dataInicioRaw.toDate() : new Date(dataInicioRaw);
      const hoje = new Date();
      const idadeDias = Math.max(1, Math.ceil((hoje - dataInicio) / (1000 * 60 * 60 * 24)));

      const gmp = ((pesoAtualKg - pesoInicialKg) * 1000) / idadeDias;
      const ganhoPesoTotalDasAves = (pesoAtualKg - pesoInicialKg) * avesVivas;
      const ca = ganhoPesoTotalDasAves > 0 ? (totalConsumoRacao / ganhoPesoTotalDasAves) : 0;

      if (data.status === "ATIVO") {
        somaGMP += gmp;
        somaCA += ca;
        lotesAtivosCount++;
      }

      return {
        id: loteDoc.id,
        identificacao: data.numeroLote || "Lote",
        galpao: data.galpao,
        avesAtuais: avesVivas,
        mortalidade: taxaMortalidade.toFixed(2) + "%",
        viabilidade: viabilidade.toFixed(2) + "%",
        gmp: gmp.toFixed(2),
        ca: ca.toFixed(2),
        pesoAtual: pesoAtualKg.toFixed(3),
        status: data.status || "ATIVO"
      };
    });

    const lotesResult = await Promise.all(lotesPromessas);

    const mortalidadeGeral = geralAlojadas > 0 ? (geralMortas / geralAlojadas) * 100 : 0;
    const viabilidadeGeral = 100 - mortalidadeGeral;
    const gmpMedioGeral = lotesAtivosCount > 0 ? (somaGMP / lotesAtivosCount) : 0;
    const caMediaGeral = lotesAtivosCount > 0 ? (somaCA / lotesAtivosCount) : 0;

    return {
      nomeProdutor: avicultorData ? avicultorData.nome : "Produtor",
      propriedade: avicultorData ? avicultorData.nomePropriedade : "Fazenda",
      estatisticasGerais: {
        totalLotes: lotesResult.length,
        avesVivas: geralVivas,
        mortalidadeMedia: mortalidadeGeral.toFixed(2) + "%",
        viabilidadeMedia: viabilidadeGeral.toFixed(2) + "%",
        gmpMedio: gmpMedioGeral.toFixed(2),
        caMedia: caMediaGeral.toFixed(2)
      },
      lotes: lotesResult
    };
  } catch (error) {
    console.error("Erro no Dashboard:", error);
    throw new HttpsError("internal", error.message);
  }
});
