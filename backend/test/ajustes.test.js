const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');
const { dataLocal } = require('../src/utils/datas');

let app;
let usuarioId;
beforeEach(async () => {
  app = criarApp(abrirBanco(':memory:'));
  ({ body: { id: usuarioId } } = await request(app).post('/api/usuarios').send({
    nome: 'Bia', email: 'bia@email.com', idade: 29, sexo: 'FEMININO', senha: '123456',
  }));
});

const novoAlimento = {
  nome: 'Bolo de cenoura da vó', categoria: 'OUTROS', kcal: 350, proteina: 5, carboidrato: 50, gordura: 14,
  porcaoG: 60, porcaoDescricao: '1 fatia',
};

test('lista inclui os novos alimentos (manteiga, requeijão, presunto, óleo)', async () => {
  for (const nome of ['Manteiga', 'Requeijão', 'Presunto', 'Óleo de soja', 'Azeite']) {
    const { body } = await request(app).get('/api/alimentos').query({ busca: nome });
    assert.ok(body.length >= 1, `não encontrou ${nome}`);
  }
});

test('cria alimento personalizado visível só para quem criou', async () => {
  const { body: criado } = await request(app).post('/api/alimentos').send({ ...novoAlimento, usuarioId }).expect(201);
  assert.equal(criado.personalizado, true);
  assert.equal(criado.kcal, 350);

  const meus = await request(app).get('/api/alimentos').query({ busca: 'Bolo', usuarioId });
  assert.equal(meus.body.length, 1);
  const deOutros = await request(app).get('/api/alimentos').query({ busca: 'Bolo' });
  assert.equal(deOutros.body.length, 0);

  // Pode ser usado no diário normalmente: 120 g = 2 fatias = 420 kcal
  const { body: registro } = await request(app).post('/api/registros-alimentares').send({
    usuarioId, alimentoId: criado.id, data: dataLocal(), refeicao: 'LANCHE', quantidade: 120,
  }).expect(201);
  assert.equal(registro.kcal, 420);
});

test('valida o alimento e impede nome repetido', async () => {
  await request(app).post('/api/alimentos').send({ ...novoAlimento, usuarioId, nome: 'manteiga com sal' }).expect(409);
  const res = await request(app).post('/api/alimentos')
    .send({ ...novoAlimento, usuarioId, proteina: 60, carboidrato: 50, kcal: 1000 }).expect(400);
  assert.ok(res.body.detalhes.some((d) => d.includes('kcal')));
  assert.ok(res.body.detalhes.some((d) => d.includes('soma')));
});

test('o mesmo treino não é registrado duas vezes no mesmo dia', async () => {
  const treino = { usuarioId, dia: 'Dia 1', foco: 'Corpo inteiro', duracaoMin: 45, data: dataLocal() };
  await request(app).post('/api/treinos-realizados').send(treino).expect(201);
  const repetido = await request(app).post('/api/treinos-realizados').send(treino).expect(409);
  assert.match(repetido.body.erro, /já foi registrado/);
  await request(app).post('/api/treinos-realizados').send({ ...treino, dia: 'Dia 2' }).expect(201);

  const hoje = await request(app).get(`/api/usuarios/${usuarioId}/treinos-realizados`).query({ data: dataLocal() });
  assert.deepEqual(hoje.body.map((t) => t.dia).sort(), ['Dia 1', 'Dia 2']);
});

test('peso do questionário vira o peso atual e uma pesagem por dia', async () => {
  await request(app).post('/api/avaliacoes').send({
    usuarioId, objetivo: 'SAUDE', nivel: 'INICIANTE', diasSemana: 3, minutosSessao: 30, local: 'CASA',
    peso: 64.5, altura: 1.62, atividadeDiaria: 'LEVE', horasSono: 8,
  }).expect(201);
  let { body: lista } = await request(app).get(`/api/usuarios/${usuarioId}/pesagens`);
  assert.deepEqual(lista.map((p) => p.peso), [64.5]);

  // Nova pesagem no mesmo dia substitui a anterior (200), em outro dia cria (201)
  await request(app).post('/api/pesagens').send({ usuarioId, peso: 64.1, data: dataLocal() }).expect(200);
  await request(app).post('/api/pesagens').send({ usuarioId, peso: 65, data: '2026-01-10' }).expect(201);
  ({ body: lista } = await request(app).get(`/api/usuarios/${usuarioId}/pesagens`));
  assert.deepEqual(lista.map((p) => p.peso), [65, 64.1]);
});
