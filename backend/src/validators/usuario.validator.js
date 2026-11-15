const { SEXOS } = require('./dominio');

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Retorna a lista de erros de validação de um usuário (vazia se válido). */
function validarUsuario(body = {}) {
  const erros = [];
  const { nome, email, idade, sexo } = body;

  if (typeof nome !== 'string' || nome.trim().length < 2) {
    erros.push('nome deve ter pelo menos 2 caracteres');
  }
  if (typeof email !== 'string' || !EMAIL_REGEX.test(email)) {
    erros.push('email inválido');
  }
  if (!Number.isInteger(idade) || idade < 12 || idade > 100) {
    erros.push('idade deve ser um inteiro entre 12 e 100');
  }
  if (!SEXOS.includes(sexo)) {
    erros.push(`sexo deve ser um de: ${SEXOS.join(', ')}`);
  }
  return erros;
}

module.exports = { validarUsuario };
