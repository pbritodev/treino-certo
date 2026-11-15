const express = require('express');

/** Registra todas as rotas da API sob /api. */
function criarRotas({ usuarioController, avaliacaoController }) {
  const router = express.Router();

  router.get('/health', (req, res) => res.json({ status: 'ok' }));

  router.get('/usuarios', usuarioController.listar);
  router.post('/usuarios', usuarioController.criar);
  router.get('/usuarios/:id', usuarioController.buscar);
  router.put('/usuarios/:id', usuarioController.atualizar);
  router.delete('/usuarios/:id', usuarioController.remover);
  router.get('/usuarios/:id/avaliacoes', usuarioController.listarAvaliacoes);

  router.post('/avaliacoes', avaliacaoController.criar);
  router.get('/avaliacoes/:id', avaliacaoController.buscar);
  router.patch('/avaliacoes/:id', avaliacaoController.atualizar);
  router.delete('/avaliacoes/:id', avaliacaoController.remover);

  return router;
}

module.exports = { criarRotas };
