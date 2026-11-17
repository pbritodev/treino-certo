const { dataValida } = require('./nutricao.validator');
const { TIPOS_TREINO, INTENSIDADES_TREINO } = require('./dominio');

/**
 * Valida o registro de um treino. Pode ser um dia do plano (tipo PLANO, o padrão)
 * ou um treino avulso cadastrado pelo usuário (CORRIDA, NATACAO...).
 */
function validarTreinoRealizado(body = {}) {
  const erros = [];
  const {
    usuarioId, avaliacaoId, tipo, dia, foco, duracaoMin, intensidade, observacao, data,
  } = body;
  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (avaliacaoId !== undefined && avaliacaoId !== null && !Number.isInteger(avaliacaoId)) {
    erros.push('avaliacaoId deve ser inteiro');
  }
  if (tipo !== undefined && !TIPOS_TREINO.includes(tipo)) erros.push(`tipo deve ser um de: ${TIPOS_TREINO.join(', ')}`);
  if (typeof dia !== 'string' || !dia.trim() || dia.length > 40) erros.push('dia (ou título do treino) é obrigatório, até 40 caracteres');
  if (foco !== undefined && (typeof foco !== 'string' || foco.length > 100)) erros.push('foco deve ter até 100 caracteres');
  if (!Number.isInteger(duracaoMin) || duracaoMin < 5 || duracaoMin > 300) {
    erros.push('duracaoMin deve ser um inteiro entre 5 e 300');
  }
  if (intensidade !== undefined && intensidade !== null && !INTENSIDADES_TREINO.includes(intensidade)) {
    erros.push(`intensidade deve ser um de: ${INTENSIDADES_TREINO.join(', ')}`);
  }
  if (observacao !== undefined && (typeof observacao !== 'string' || observacao.length > 300)) {
    erros.push('observacao deve ter até 300 caracteres');
  }
  if (!dataValida(data)) erros.push('data deve estar no formato AAAA-MM-DD');
  return erros;
}

module.exports = { validarTreinoRealizado };
