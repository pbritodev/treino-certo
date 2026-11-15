const { REFEICOES } = require('../validators/dominio');

const META_PADRAO = 2000;
const arred = (valor) => Math.round(valor * 10) / 10;

function somar(itens) {
  return itens.reduce(
    (total, registro) => ({
      kcal: arred(total.kcal + registro.kcal),
      proteina: arred(total.proteina + registro.proteina),
      carboidrato: arred(total.carboidrato + registro.carboidrato),
      gordura: arred(total.gordura + registro.gordura),
    }),
    { kcal: 0, proteina: 0, carboidrato: 0, gordura: 0 },
  );
}

/**
 * Monta o diário de um dia: itens agrupados por refeição, totais consumidos
 * e comparação com a meta calórica da avaliação mais recente do usuário.
 */
function montarDiario({ data, itens, ultimaAvaliacao }) {
  const metaCalorica = ultimaAvaliacao?.metaCalorica ?? META_PADRAO;
  const metaProteina = ultimaAvaliacao?.metaProteina ?? null;
  const totais = somar(itens);

  return {
    data,
    metaCalorica,
    metaProteina,
    metaDefinidaPorAvaliacao: Boolean(ultimaAvaliacao),
    totais,
    restante: Math.round(metaCalorica - totais.kcal),
    refeicoes: REFEICOES.map((refeicao) => {
      const daRefeicao = itens.filter((i) => i.refeicao === refeicao);
      return { refeicao, kcal: somar(daRefeicao).kcal, itens: daRefeicao };
    }),
  };
}

module.exports = { montarDiario, META_PADRAO };
