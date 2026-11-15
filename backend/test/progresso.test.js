const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');
const { calcularSequencia, inicioDaSemana } = require('../src/services/progresso.service');
const { dataLocal } = require('../src/utils/datas');

let app;
let usuarioId;
beforeEach(async () => {
  app = criarApp(abrirBanco(':memory:'));
  ({ body: { id: usuarioId } } = await request(app).post('/api/usuarios').send({
    nome: 'Leo', email: 'leo@email.com', idade: 35, sexo: 'MASCULINO', senha: '123456',
  }));
});

test('sequência de dias seguidos com treino', () => {
  assert.equal(calcularSequencia(['2026-11-14', '2026-11-15', '2026-11-16'], '2026-11-16'), 3);
  assert.equal(calcularSequencia(['2026-11-14', '2026-11-15'], '2026-11-16'), 2); // ainda não treinou hoje
  assert.equal(calcularSequencia(['2026-11-12'], '2026-11-16'), 0);
});

test('semana começa na segunda-feira', () => {
  assert.equal(inicioDaSemana('2026-11-15'), '2026-11-09'); // domingo
  assert.equal(inicioDaSemana('2026-11-16'), '2026-11-16'); // segunda
});

test('registra treinos e calcula o progresso', async () => {
  const hoje = dataLocal();
  const treino = { usuarioId, dia: 'Dia 1', foco: 'Corpo inteiro', duracaoMin: 45, data: hoje };
  const { body: t1 } = await request(app).post('/api/treinos-realizados').send(treino).expect(201);
  await request(app).post('/api/treinos-realizados').send({ ...treino, dia: 'Dia 2', duracaoMin: 30 }).expect(201);

  const { body: p } = await request(app).get(`/api/usuarios/${usuarioId}/progresso`).expect(200);
  assert.equal(p.usuario.nome, 'Leo');
  assert.equal(p.totalTreinos, 2);
  assert.equal(p.minutosTotais, 75);
  assert.equal(p.sequenciaDias, 1);
  assert.equal(p.ultimos7Dias.length, 7);
  assert.equal(p.ultimos7Dias[6].treinos, 2);
  assert.equal(p.planoAtual, null);

  await request(app).delete(`/api/treinos-realizados/${t1.id}`).expect(204);
  const { body: lista } = await request(app).get(`/api/usuarios/${usuarioId}/treinos-realizados`).expect(200);
  assert.equal(lista.length, 1);
});

test('valida o treino realizado', async () => {
  const res = await request(app).post('/api/treinos-realizados')
    .send({ usuarioId, dia: '', foco: 'x', duracaoMin: 1, data: 'ontem' }).expect(400);
  assert.equal(res.body.detalhes.length, 3);
});
