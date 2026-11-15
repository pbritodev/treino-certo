const { SEXOS } = require('./dominio');

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const SENHA_MINIMA = 6;

function senhaValida(senha) {
  return typeof senha === 'string' && senha.length >= SENHA_MINIMA && senha.length <= 64;
}

/** Retorna a lista de erros de validação de um usuário (vazia se válido). */
function validarUsuario(body = {}, { exigirSenha = false } = {}) {
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
  if (exigirSenha && !senhaValida(body.senha)) {
    erros.push(`senha deve ter entre ${SENHA_MINIMA} e 64 caracteres`);
  }
  return erros;
}

function validarLogin(body = {}) {
  const erros = [];
  if (typeof body.email !== 'string' || !body.email.trim()) erros.push('email é obrigatório');
  if (typeof body.senha !== 'string' || !body.senha) erros.push('senha é obrigatória');
  return erros;
}

function validarTrocaSenha(body = {}) {
  const erros = [];
  if (typeof body.senhaAtual !== 'string' || !body.senhaAtual) erros.push('senhaAtual é obrigatória');
  if (!senhaValida(body.novaSenha)) erros.push(`novaSenha deve ter entre ${SENHA_MINIMA} e 64 caracteres`);
  if (body.novaSenha && body.novaSenha === body.senhaAtual) erros.push('a nova senha deve ser diferente da atual');
  return erros;
}

module.exports = { validarUsuario, validarLogin, validarTrocaSenha };
