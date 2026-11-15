const { REFEICOES } = require('./dominio');

const DATA_REGEX = /^\d{4}-\d{2}-\d{2}$/;

function dataValida(data) {
  return typeof data === 'string' && DATA_REGEX.test(data) && !Number.isNaN(Date.parse(data));
}

function validarQuantidade(quantidade, erros) {
  if (typeof quantidade !== 'number' || quantidade <= 0 || quantidade > 5000) {
    erros.push('quantidade deve ser um número entre 1 e 5000 (gramas ou ml)');
  }
}

/** Valida um item adicionado ao diário alimentar. */
function validarRegistroAlimentar(body = {}) {
  const erros = [];
  const { usuarioId, alimentoId, data, refeicao, quantidade } = body;
  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (!Number.isInteger(alimentoId)) erros.push('alimentoId é obrigatório');
  if (!dataValida(data)) erros.push('data deve estar no formato AAAA-MM-DD');
  if (!REFEICOES.includes(refeicao)) erros.push(`refeicao deve ser um de: ${REFEICOES.join(', ')}`);
  validarQuantidade(quantidade, erros);
  return erros;
}

/** Valida a edição (PUT) de um item do diário: quantidade e refeição. */
function validarAtualizacaoRegistro(body = {}) {
  const erros = [];
  validarQuantidade(body.quantidade, erros);
  if (!REFEICOES.includes(body.refeicao)) erros.push(`refeicao deve ser um de: ${REFEICOES.join(', ')}`);
  return erros;
}

/** Valida um registro de peso corporal. */
function validarPesagem(body = {}) {
  const erros = [];
  const { usuarioId, peso, data, observacao } = body;
  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (typeof peso !== 'number' || peso < 30 || peso > 300) erros.push('peso deve estar entre 30 e 300 kg');
  if (!dataValida(data)) erros.push('data deve estar no formato AAAA-MM-DD');
  if (observacao !== undefined && (typeof observacao !== 'string' || observacao.length > 200)) {
    erros.push('observacao deve ter até 200 caracteres');
  }
  return erros;
}

module.exports = {
  validarRegistroAlimentar, validarAtualizacaoRegistro, validarPesagem, dataValida,
};
