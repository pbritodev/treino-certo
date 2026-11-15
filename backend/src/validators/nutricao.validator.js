const { REFEICOES, CATEGORIAS_ALIMENTO } = require('./dominio');

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

const numeroEntre = (valor, minimo, maximo) => typeof valor === 'number' && valor >= minimo && valor <= maximo;

/** Valida um alimento criado pelo usuário (valores por 100 g ou 100 ml). */
function validarAlimento(body = {}) {
  const erros = [];
  const {
    usuarioId, nome, categoria, kcal, proteina, carboidrato, gordura, porcaoG, porcaoDescricao,
  } = body;
  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (typeof nome !== 'string' || nome.trim().length < 2 || nome.length > 60) erros.push('nome deve ter entre 2 e 60 caracteres');
  if (!CATEGORIAS_ALIMENTO.includes(categoria)) erros.push(`categoria deve ser um de: ${CATEGORIAS_ALIMENTO.join(', ')}`);
  if (!numeroEntre(kcal, 0, 900)) erros.push('kcal (por 100 g) deve estar entre 0 e 900');
  [['proteina', proteina], ['carboidrato', carboidrato], ['gordura', gordura]].forEach(([campo, valor]) => {
    if (!numeroEntre(valor, 0, 100)) erros.push(`${campo} (por 100 g) deve estar entre 0 e 100`);
  });
  if (numeroEntre(proteina, 0, 100) && numeroEntre(carboidrato, 0, 100) && numeroEntre(gordura, 0, 100)
      && proteina + carboidrato + gordura > 100) {
    erros.push('a soma de proteína, carboidrato e gordura não pode passar de 100 g a cada 100 g');
  }
  if (!numeroEntre(porcaoG, 1, 2000)) erros.push('porcaoG deve estar entre 1 e 2000');
  if (typeof porcaoDescricao !== 'string' || !porcaoDescricao.trim() || porcaoDescricao.length > 40) {
    erros.push('porcaoDescricao deve ter entre 1 e 40 caracteres');
  }
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
  validarRegistroAlimentar, validarAtualizacaoRegistro, validarPesagem, validarAlimento, dataValida,
};
