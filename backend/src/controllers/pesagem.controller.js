const { HttpError } = require('../middlewares/erros');
const { validarPesagem } = require('../validators/nutricao.validator');

function criarPesagemController({ pesagens, usuarios }) {
  return {
    listar(req, res) {
      const usuarioId = Number(req.params.id);
      if (!usuarios.buscarPorId(usuarioId)) throw new HttpError(404, 'Usuário não encontrado');
      res.json(pesagens.listarPorUsuario(usuarioId));
    },
    criar(req, res) {
      const erros = validarPesagem(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      if (!usuarios.buscarPorId(req.body.usuarioId)) throw new HttpError(404, 'Usuário não encontrado');
      const { pesagem, criada } = pesagens.salvarDoDia(req.body);
      res.status(criada ? 201 : 200).json(pesagem);
    },
    remover(req, res) {
      if (!pesagens.remover(Number(req.params.id))) throw new HttpError(404, 'Pesagem não encontrada');
      res.status(204).end();
    },
  };
}

module.exports = { criarPesagemController };
