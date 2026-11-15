const crypto = require('node:crypto');

/**
 * Hash de senha com scrypt (módulo nativo do Node) e salt aleatório.
 * Formato salvo no banco: "salt:hash" em hexadecimal. A senha nunca é guardada em texto puro.
 */
function gerarHash(senha) {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(senha, salt, 64).toString('hex');
  return `${salt}:${hash}`;
}

function conferirSenha(senha, salvo) {
  if (!salvo) return false;
  const [salt, hash] = salvo.split(':');
  const calculado = crypto.scryptSync(senha, salt, 64);
  return crypto.timingSafeEqual(calculado, Buffer.from(hash, 'hex'));
}

module.exports = { gerarHash, conferirSenha };
