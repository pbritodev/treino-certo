const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');

let app;
let usuarioId;
beforeEach(async () => {
  app = criarApp(abrirBanco(':memory:'));
  const { body } = await request(app).post('/api/usuarios')
    .send({
      nome: 'João', email: 'joao@email.com', idade: 30, sexo: 'MASCULINO', senha: '123456',
    });
  usuarioId = body.id;
});

async function alimentoPorNome(nome) {
  const { body } = await request(app).get('/api/alimentos').query({ busca: nome });
  return body[0];
}

test('lista e filtra alimentos', async () => {
  const todos = await request(app).get('/api/alimentos').expect(200);
  assert.ok(todos.body.length >= 40);
  const frutas = await request(app).get('/api/alimentos').query({ categoria: 'FRUTAS' });
  assert.ok(frutas.body.every((a) => a.categoria === 'FRUTAS'));
  const arroz = await alimentoPorNome('Arroz branco');
  assert.equal(arroz.kcal, 128);
});

test('calcula calorias pela quantidade ingerida e monta o diário', async () => {
  const arroz = await alimentoPorNome('Arroz branco');
  const frango = await alimentoPorNome('Peito de frango');
  const data = '2026-11-16';

  const r1 = await request(app).post('/api/registros-alimentares')
    .send({ usuarioId, alimentoId: arroz.id, data, refeicao: 'ALMOCO', quantidade: 150 }).expect(201);
  assert.equal(r1.body.kcal, 192); // 128 * 1,5

  await request(app).post('/api/registros-alimentares')
    .send({ usuarioId, alimentoId: frango.id, data, refeicao: 'ALMOCO', quantidade: 120 }).expect(201);

  const { body: diario } = await request(app).get(`/api/usuarios/${usuarioId}/diario`).query({ data }).expect(200);
  assert.equal(diario.metaCalorica, 2000); // sem avaliação: meta padrão
  assert.equal(diario.totais.kcal, 382.8); // 192 + 159 * 1,2
  assert.equal(diario.refeicoes.find((r) => r.refeicao === 'ALMOCO').itens.length, 2);
  assert.equal(diario.restante, 1617);

  const editado = await request(app).put(`/api/registros-alimentares/${r1.body.id}`)
    .send({ quantidade: 100, refeicao: 'JANTAR' }).expect(200);
  assert.equal(editado.body.kcal, 128);

  await request(app).delete(`/api/registros-alimentares/${r1.body.id}`).expect(204);
  await request(app).delete(`/api/registros-alimentares/${r1.body.id}`).expect(404);
});

test('diário usa a meta calórica da última avaliação', async () => {
  const { body: avaliacao } = await request(app).post('/api/avaliacoes').send({
    usuarioId, objetivo: 'EMAGRECIMENTO', nivel: 'INICIANTE', diasSemana: 3, minutosSessao: 45,
    local: 'CASA', peso: 90, altura: 1.8, atividadeDiaria: 'SEDENTARIO', horasSono: 7,
  });
  const { body: diario } = await request(app).get(`/api/usuarios/${usuarioId}/diario`).query({ data: '2026-11-16' });
  assert.equal(diario.metaCalorica, avaliacao.metaCalorica);
  assert.equal(diario.metaDefinidaPorAvaliacao, true);
});

test('valida registro alimentar', async () => {
  const res = await request(app).post('/api/registros-alimentares')
    .send({ usuarioId, alimentoId: 1, data: '16/11/2026', refeicao: 'CEIA', quantidade: -5 }).expect(400);
  assert.equal(res.body.detalhes.length, 3);
});

test('registra, lista e remove pesagens', async () => {
  await request(app).post('/api/pesagens').send({ usuarioId, peso: 82.4, data: '2026-11-10' }).expect(201);
  const { body: p2 } = await request(app).post('/api/pesagens')
    .send({ usuarioId, peso: 81.9, data: '2026-11-16', observacao: 'Em jejum' }).expect(201);

  const { body: lista } = await request(app).get(`/api/usuarios/${usuarioId}/pesagens`).expect(200);
  assert.deepEqual(lista.map((p) => p.peso), [82.4, 81.9]);

  await request(app).delete(`/api/pesagens/${p2.id}`).expect(204);
  await request(app).post('/api/pesagens').send({ usuarioId, peso: 10, data: 'x' }).expect(400);
});
