const { HttpError } = require('../middlewares/erros');
const { validarAvaliacao, validarAtualizacaoAvaliacao } = require('../validators/avaliacao.validator');
const { gerarRecomendacao } = require('../services/recomendacao.service');

const { dataLocal } = require('../utils/datas');

function criarAvaliacaoController(avaliacoes, usuarios, pesagens) {
  return {
    criar(req, res) {
      const erros = validarAvaliacao(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      const usuario = usuarios.buscarPorId(req.body.usuarioId);
      if (!usuario) throw new HttpError(404, 'Usuário não encontrado');

      const respostas = { possuiEquipamento: false, restricoes: [], pesoMeta: null, ...req.body };
      const resultado = gerarRecomendacao(respostas, usuario);
      const avaliacao = avaliacoes.criar({ ...respostas, ...resultado });
      // O peso informado no questionário passa a ser o peso atual do usuário.
      pesagens.salvarDoDia({ usuarioId: usuario.id, peso: respostas.peso, data: dataLocal() });
      res.status(201).json(avaliacao);
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
