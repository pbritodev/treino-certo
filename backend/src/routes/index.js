const express = require('express');

/** Registra todas as rotas da API sob /api. */
function criarRotas({
  authController, usuarioController, avaliacaoController, nutricaoController, pesagemController, treinoController,
}) {
  const router = express.Router();

  router.get('/health', (req, res) => res.json({ status: 'ok' }));

  // Autenticação
  router.post('/auth/login', authController.login);

  // Usuários (POST = cadastre-se)
  router.get('/usuarios', usuarioController.listar);
  router.post('/usuarios', usuarioController.criar);
  router.get('/usuarios/:id', usuarioController.buscar);
  router.put('/usuarios/:id', usuarioController.atualizar);
  router.delete('/usuarios/:id', usuarioController.remover);
  router.put('/usuarios/:id/senha', usuarioController.trocarSenha);
  router.get('/usuarios/:id/avaliacoes', usuarioController.listarAvaliacoes);
  router.get('/usuarios/:id/diario', nutricaoController.diario);
  router.get('/usuarios/:id/pesagens', pesagemController.listar);
  router.get('/usuarios/:id/treinos-realizados', treinoController.listar);
  router.get('/usuarios/:id/progresso', treinoController.progresso);

  // Avaliações (questionário + plano de treino)
  router.post('/avaliacoes', avaliacaoController.criar);
  router.get('/avaliacoes/:id', avaliacaoController.buscar);
  router.patch('/avaliacoes/:id', avaliacaoController.atualizar);
  router.delete('/avaliacoes/:id', avaliacaoController.remover);

  // Alimentos e diário alimentar
  router.get('/alimentos', nutricaoController.listarAlimentos);
  router.post('/alimentos', nutricaoController.criarAlimento);
  router.get('/alimentos/:id', nutricaoController.buscarAlimento);
  router.post('/registros-alimentares', nutricaoController.criarRegistro);
  router.put('/registros-alimentares/:id', nutricaoController.atualizarRegistro);
  router.delete('/registros-alimentares/:id', nutricaoController.removerRegistro);

  // Pesagens
  router.post('/pesagens', pesagemController.criar);
  router.delete('/pesagens/:id', pesagemController.remover);

  // Treinos realizados
  router.post('/treinos-realizados', treinoController.criar);
  router.delete('/treinos-realizados/:id', treinoController.remover);

  return router;
}

module.exports = { criarRotas };
