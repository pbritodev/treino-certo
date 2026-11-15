const express = require('express');

/** Registra todas as rotas da API sob /api. */
function criarRotas({
  usuarioController, avaliacaoController, nutricaoController, pesagemController,
}) {
  const router = express.Router();

  router.get('/health', (req, res) => res.json({ status: 'ok' }));

  // Usuários
  router.get('/usuarios', usuarioController.listar);
  router.post('/usuarios', usuarioController.criar);
  router.get('/usuarios/:id', usuarioController.buscar);
  router.put('/usuarios/:id', usuarioController.atualizar);
  router.delete('/usuarios/:id', usuarioController.remover);
  router.get('/usuarios/:id/avaliacoes', usuarioController.listarAvaliacoes);
  router.get('/usuarios/:id/diario', nutricaoController.diario);
  router.get('/usuarios/:id/pesagens', pesagemController.listar);

  // Avaliações (questionário + plano de treino)
  router.post('/avaliacoes', avaliacaoController.criar);
  router.get('/avaliacoes/:id', avaliacaoController.buscar);
  router.patch('/avaliacoes/:id', avaliacaoController.atualizar);
  router.delete('/avaliacoes/:id', avaliacaoController.remover);

  // Alimentos e diário alimentar
  router.get('/alimentos', nutricaoController.listarAlimentos);
  router.get('/alimentos/:id', nutricaoController.buscarAlimento);
  router.post('/registros-alimentares', nutricaoController.criarRegistro);
  router.put('/registros-alimentares/:id', nutricaoController.atualizarRegistro);
  router.delete('/registros-alimentares/:id', nutricaoController.removerRegistro);

  // Pesagens
  router.post('/pesagens', pesagemController.criar);
  router.delete('/pesagens/:id', pesagemController.remover);

  return router;
}

module.exports = { criarRotas };
