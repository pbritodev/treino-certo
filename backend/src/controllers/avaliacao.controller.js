const { HttpError } = require('../middlewares/erros');
const { validarAvaliacao, validarAtualizacaoAvaliacao } = require('../validators/avaliacao.validator');
const { gerarRecomendacao } = require('../services/recomendacao.service');

function criarAvaliacaoController(avaliacoes, usuarios) {
  return {
    criar(req, res) {
      const erros = validarAvaliacao(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      const usuario = usuarios.buscarPorId(req.body.usuarioId);
      if (!usuario) throw new HttpError(404, 'Usuário não encontrado');

      const respostas = { possuiEquipamento: false, restricoes: [], pesoMeta: null, ...req.body };
      const resultado = gerarRecomendacao(respostas, usuario);
      res.status(201).json(avaliacoes.criar({ ...respostas, ...resultado }));
    },
    buscar(req, res) {
      const avaliacao = avaliacoes.buscarPorId(Number(req.params.id));
      if (!avaliacao) throw new HttpError(404, 'Avaliação não encontrada');
      res.json(avaliacao);
    },
    atualizar(req, res) {
      const erros = validarAtualizacaoAvaliacao(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      const avaliacao = avaliacoes.atualizar(Number(req.params.id), req.body);
      if (!avaliacao) throw new HttpError(404, 'Avaliação não encontrada');
      res.json(avaliacao);
    },
    remover(req, res) {
      if (!avaliacoes.remover(Number(req.params.id))) throw new HttpError(404, 'Avaliação não encontrada');
      res.status(204).end();
    },
  };
}

module.exports = { criarAvaliacaoController };
