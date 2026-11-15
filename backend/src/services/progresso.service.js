const { dataLocal } = require('../utils/datas');

function somarDias(dataTexto, dias) {
  const [ano, mes, dia] = dataTexto.split('-').map(Number);
  return dataLocal(new Date(ano, mes - 1, dia + dias));
}

/** Segunda-feira da semana da data informada (AAAA-MM-DD). */
function inicioDaSemana(hoje) {
  const [ano, mes, dia] = hoje.split('-').map(Number);
  const diaSemana = new Date(ano, mes - 1, dia).getDay(); // 0 = domingo
  return somarDias(hoje, -((diaSemana + 6) % 7));
}

/** Dias seguidos com treino, terminando hoje (ou ontem, se ainda não treinou hoje). */
function calcularSequencia(datasComTreino, hoje) {
  const datas = new Set(datasComTreino);
  let atual = datas.has(hoje) ? hoje : somarDias(hoje, -1);
  let sequencia = 0;
  while (datas.has(atual)) {
    sequencia += 1;
    atual = somarDias(atual, -1);
  }
  return sequencia;
}

/**
 * Monta o resumo de progresso: perfil, último plano, peso e estatísticas
 * dos treinos realizados (total, minutos, semana atual, sequência e últimos 7 dias).
 */
function montarProgresso({
  usuario, ultimaAvaliacao, pesagens, treinos, hoje = dataLocal(),
}) {
  const datas = treinos.map((t) => t.data);
  const segunda = inicioDaSemana(hoje);
  const pesoInicial = pesagens[0]?.peso ?? ultimaAvaliacao?.peso ?? null;
  const pesoAtual = pesagens.at(-1)?.peso ?? ultimaAvaliacao?.peso ?? null;

  return {
    usuario,
    planoAtual: ultimaAvaliacao && {
      id: ultimaAvaliacao.id,
      perfil: ultimaAvaliacao.perfil,
      objetivo: ultimaAvaliacao.objetivo,
      classificacao: ultimaAvaliacao.classificacao,
      diasSemana: ultimaAvaliacao.diasSemana,
      metaCalorica: ultimaAvaliacao.metaCalorica,
      imc: ultimaAvaliacao.imc,
      pesoMeta: ultimaAvaliacao.pesoMeta,
    },
    pesoInicial,
    pesoAtual,
    totalTreinos: treinos.length,
    minutosTotais: treinos.reduce((soma, t) => soma + t.duracaoMin, 0),
    treinosSemana: datas.filter((d) => d >= segunda && d <= hoje).length,
    metaSemanal: ultimaAvaliacao?.diasSemana ?? 3,
    sequenciaDias: calcularSequencia(datas, hoje),
    ultimos7Dias: Array.from({ length: 7 }, (_, i) => {
      const data = somarDias(hoje, i - 6);
      return { data, treinos: datas.filter((d) => d === data).length };
    }),
    treinosRecentes: treinos.slice(0, 20),
  };
}

module.exports = { montarProgresso, calcularSequencia, inicioDaSemana };
