const { dataValida } = require('./nutricao.validator');

/** Valida o registro de um treino concluído. */
function validarTreinoRealizado(body = {}) {
  const erros = [];
  const {
    usuarioId, avaliacaoId, dia, foco, duracaoMin, data,
  } = body;
  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (avaliacaoId !== undefined && avaliacaoId !== null && !Number.isInteger(avaliacaoId)) {
    erros.push('avaliacaoId deve ser inteiro');
  }
  if (typeof dia !== 'string' || !dia.trim() || dia.length > 30) erros.push('dia é obrigatório');
  if (typeof foco !== 'string' || !foco.trim() || foco.length > 100) erros.push('foco é obrigatório');
  if (!Number.isInteger(duracaoMin) || duracaoMin < 5 || duracaoMin > 300) {
    erros.push('duracaoMin deve ser um inteiro entre 5 e 300');
  }
  if (!dataValida(data)) erros.push('data deve estar no formato AAAA-MM-DD');
  return erros;
}

module.exports = { validarTreinoRealizado };
