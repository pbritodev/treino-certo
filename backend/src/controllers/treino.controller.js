const { HttpError } = require('../middlewares/erros');
const { validarTreinoRealizado } = require('../validators/treino.validator');
const { montarProgresso } = require('../services/progresso.service');
const { dataValida } = require('../validators/nutricao.validator');
const { estimarKcal } = require('../services/gasto.service');

function criarTreinoController({
  treinos, usuarios, avaliacoes, pesagens,
}) {
  function usuarioOu404(id) {
    const usuario = usuarios.buscarPorId(id);
    if (!usuario) throw new HttpError(404, 'Usuário não encontrado');
    return usuario;
  }

  return {
    listar(req, res) {
      const usuarioId = Number(req.params.id);
      usuarioOu404(usuarioId);
      if (req.query.data && !dataValida(req.query.data)) throw new HttpError(400, 'data deve estar no formato AAAA-MM-DD');
      res.json(treinos.listarPorUsuario(usuarioId, req.query.data));
    },
    criar(req, res) {
      const erros = validarTreinoRealizado(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      usuarioOu404(req.body.usuarioId);
      if (req.body.avaliacaoId && !avaliacoes.buscarPorId(req.body.avaliacaoId)) {
        throw new HttpError(404, 'Avaliação não encontrada');
      }
      const treino = { tipo: 'PLANO', ...req.body };
      // Só os dias do plano são únicos por data; treinos avulsos podem se repetir (ex.: 2 corridas).
      if (treino.tipo === 'PLANO' && treinos.jaRegistrado(treino)) {
        throw new HttpError(409, 'Este treino já foi registrado nesta data');
      }
      const peso = pesagens.listarPorUsuario(treino.usuarioId).at(-1)?.peso
        ?? avaliacoes.buscarUltimaDoUsuario(treino.usuarioId)?.peso;
      res.status(201).json(treinos.criar({ ...treino, kcal: estimarKcal({ ...treino, peso }) }));
    },
    remover(req, res) {
      if (!treinos.remover(Number(req.params.id))) throw new HttpError(404, 'Treino não encontrado');
      res.status(204).end();
    },
    progresso(req, res) {
      const usuarioId = Number(req.params.id);
      res.json(montarProgresso({
        usuario: usuarioOu404(usuarioId),
        ultimaAvaliacao: avaliacoes.buscarUltimaDoUsuario(usuarioId),
        pesagens: pesagens.listarPorUsuario(usuarioId),
        treinos: treinos.listarPorUsuario(usuarioId),
      }));
    },
  };
}

module.exports = { criarTreinoController };
