const { HttpError } = require('../middlewares/erros');
const { validarUsuario } = require('../validators/usuario.validator');

function criarUsuarioController(usuarios, avaliacoes) {
  function obterOu404(id) {
    const usuario = usuarios.buscarPorId(id);
    if (!usuario) throw new HttpError(404, 'Usuário não encontrado');
    return usuario;
  }

  function garantirEmailLivre(email, idIgnorado) {
    const existente = usuarios.buscarPorEmail(String(email).trim().toLowerCase());
    if (existente && existente.id !== idIgnorado) throw new HttpError(409, 'E-mail já cadastrado');
  }

  return {
    listar(req, res) {
      res.json(usuarios.listar());
    },
    buscar(req, res) {
      res.json(obterOu404(Number(req.params.id)));
    },
    criar(req, res) {
      const erros = validarUsuario(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      garantirEmailLivre(req.body.email);
      res.status(201).json(usuarios.criar(req.body));
    },
    atualizar(req, res) {
      const id = Number(req.params.id);
      obterOu404(id);
      const erros = validarUsuario(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      garantirEmailLivre(req.body.email, id);
      res.json(usuarios.atualizar(id, req.body));
    },
    remover(req, res) {
      if (!usuarios.remover(Number(req.params.id))) throw new HttpError(404, 'Usuário não encontrado');
      res.status(204).end();
    },
    listarAvaliacoes(req, res) {
      const id = Number(req.params.id);
      obterOu404(id);
      res.json(avaliacoes.listarPorUsuario(id));
    },
  };
}

module.exports = { criarUsuarioController };
