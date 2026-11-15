const { HttpError } = require('../middlewares/erros');
const { montarDiario } = require('../services/diario.service');
const { dataLocal } = require('../utils/datas');
const {
  validarRegistroAlimentar, validarAtualizacaoRegistro, validarAlimento, dataValida,
} = require('../validators/nutricao.validator');

function criarNutricaoController({ alimentos, registros, usuarios, avaliacoes }) {
  return {
    listarAlimentos(req, res) {
      const usuarioId = req.query.usuarioId ? Number(req.query.usuarioId) : undefined;
      res.json(alimentos.listar({ busca: req.query.busca, categoria: req.query.categoria, usuarioId }));
    },
    /** Cria um alimento personalizado (fica visível apenas para quem o criou). */
    criarAlimento(req, res) {
      const erros = validarAlimento(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      if (!usuarios.buscarPorId(req.body.usuarioId)) throw new HttpError(404, 'Usuário não encontrado');
      if (alimentos.buscarPorNome(req.body.nome)) throw new HttpError(409, 'Já existe um alimento com esse nome');
      res.status(201).json(alimentos.criar(req.body));
    },
    buscarAlimento(req, res) {
      const alimento = alimentos.buscarPorId(Number(req.params.id));
      if (!alimento) throw new HttpError(404, 'Alimento não encontrado');
      res.json(alimento);
    },
    diario(req, res) {
      const usuarioId = Number(req.params.id);
      if (!usuarios.buscarPorId(usuarioId)) throw new HttpError(404, 'Usuário não encontrado');
      const data = req.query.data ?? dataLocal();
      if (!dataValida(data)) throw new HttpError(400, 'data deve estar no formato AAAA-MM-DD');

      res.json(montarDiario({
        data,
        itens: registros.listarPorData(usuarioId, data),
        ultimaAvaliacao: avaliacoes.buscarUltimaDoUsuario(usuarioId),
      }));
    },
    criarRegistro(req, res) {
      const erros = validarRegistroAlimentar(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      if (!usuarios.buscarPorId(req.body.usuarioId)) throw new HttpError(404, 'Usuário não encontrado');
      if (!alimentos.buscarPorId(req.body.alimentoId)) throw new HttpError(404, 'Alimento não encontrado');
      res.status(201).json(registros.criar(req.body));
    },
    atualizarRegistro(req, res) {
      const erros = validarAtualizacaoRegistro(req.body);
      if (erros.length) throw new HttpError(400, 'Dados inválidos', erros);
      const registro = registros.atualizar(Number(req.params.id), req.body);
      if (!registro) throw new HttpError(404, 'Registro não encontrado');
      res.json(registro);
    },
    removerRegistro(req, res) {
      if (!registros.remover(Number(req.params.id))) throw new HttpError(404, 'Registro não encontrado');
      res.status(204).end();
    },
  };
}

module.exports = { criarNutricaoController };
