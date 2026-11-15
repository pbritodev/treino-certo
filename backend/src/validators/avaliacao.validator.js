const {
  OBJETIVOS, NIVEIS, LOCAIS, RESTRICOES, MINUTOS, ATIVIDADES,
} = require('./dominio');

/** Valida as respostas do questionário enviadas pelo app. */
function validarAvaliacao(body = {}) {
  const erros = [];
  const {
    usuarioId, objetivo, nivel, diasSemana, minutosSessao,
    local, possuiEquipamento, restricoes, peso, altura,
    atividadeDiaria, horasSono, pesoMeta,
  } = body;

  if (!Number.isInteger(usuarioId)) erros.push('usuarioId é obrigatório');
  if (!OBJETIVOS.includes(objetivo)) erros.push(`objetivo deve ser um de: ${OBJETIVOS.join(', ')}`);
  if (!NIVEIS.includes(nivel)) erros.push(`nivel deve ser um de: ${NIVEIS.join(', ')}`);
  if (!Number.isInteger(diasSemana) || diasSemana < 2 || diasSemana > 6) {
    erros.push('diasSemana deve ser um inteiro entre 2 e 6');
  }
  if (!MINUTOS.includes(minutosSessao)) erros.push(`minutosSessao deve ser um de: ${MINUTOS.join(', ')}`);
  if (!LOCAIS.includes(local)) erros.push(`local deve ser um de: ${LOCAIS.join(', ')}`);
  if (possuiEquipamento !== undefined && typeof possuiEquipamento !== 'boolean') {
    erros.push('possuiEquipamento deve ser booleano');
  }
  if (restricoes !== undefined
      && (!Array.isArray(restricoes) || restricoes.some((r) => !RESTRICOES.includes(r)))) {
    erros.push(`restricoes deve ser uma lista com valores de: ${RESTRICOES.join(', ')}`);
  }
  if (typeof peso !== 'number' || peso < 30 || peso > 300) erros.push('peso deve estar entre 30 e 300 kg');
  if (typeof altura !== 'number' || altura < 1.0 || altura > 2.5) erros.push('altura deve estar entre 1.00 e 2.50 m');
  if (!ATIVIDADES.includes(atividadeDiaria)) erros.push(`atividadeDiaria deve ser um de: ${ATIVIDADES.join(', ')}`);
  if (!Number.isInteger(horasSono) || horasSono < 3 || horasSono > 12) {
    erros.push('horasSono deve ser um inteiro entre 3 e 12');
  }
  if (pesoMeta !== undefined && pesoMeta !== null
      && (typeof pesoMeta !== 'number' || pesoMeta < 30 || pesoMeta > 300)) {
    erros.push('pesoMeta deve estar entre 30 e 300 kg');
  }
  return erros;
}

/** Valida a atualização parcial (PATCH) de uma avaliação. */
function validarAtualizacaoAvaliacao(body = {}) {
  const erros = [];
  const { observacao, favorita } = body;
  if (observacao === undefined && favorita === undefined) {
    erros.push('informe observacao e/ou favorita');
  }
  if (observacao !== undefined && (typeof observacao !== 'string' || observacao.length > 500)) {
    erros.push('observacao deve ser texto com até 500 caracteres');
  }
  if (favorita !== undefined && typeof favorita !== 'boolean') erros.push('favorita deve ser booleano');
  return erros;
}

module.exports = { validarAvaliacao, validarAtualizacaoAvaliacao };
