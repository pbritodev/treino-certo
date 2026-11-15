const { HttpError } = require('../middlewares/erros');
const { validarLogin } = require('../validators/usuario.validator');
const { conferirSenha } = require('../utils/senha');

function criarAuthController(usuarios) {
  return {
    /** Confere e-mail e senha e devolve os dados do usuário (sem a senha). */
    login(req, res) {
      const erros = validarLogin(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);

      const usuario = usuarios.buscarPorEmail(req.body.email.trim().toLowerCase());
      // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem).
      if (!usuario || !conferirSenha(req.body.senha, usuarios.buscarHashSenha(usuario.id))) {
        throw new HttpError(401, 'E-mail ou senha incorretos');
      }
      res.json(usuario);
    },
  };
}

module.exports = { criarAuthController };
