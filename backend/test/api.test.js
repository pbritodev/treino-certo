const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');

let app;
beforeEach(() => {
  app = criarApp(abrirBanco(':memory:'));
});

const usuario = { nome: 'Maria Silva', email: 'maria@email.com', idade: 25, sexo: 'FEMININO' };
const respostas = {
  objetivo: 'HIPERTROFIA', nivel: 'INTERMEDIARIO', diasSemana: 4, minutosSessao: 60,
  local: 'ACADEMIA', restricoes: ['OMBRO'], peso: 62, altura: 1.65,
  atividadeDiaria: 'MODERADO', horasSono: 6, pesoMeta: 58,
};

test('CRUD de usuário', async () => {
  const criado = await request(app).post('/api/usuarios').send(usuario).expect(201);
  const { id } = criado.body;

  await request(app).post('/api/usuarios').send(usuario).expect(409);
  await request(app).get(`/api/usuarios/${id}`).expect(200);

  const atualizado = await request(app).put(`/api/usuarios/${id}`).send({ ...usuario, idade: 26 }).expect(200);
  assert.equal(atualizado.body.idade, 26);

  await request(app).delete(`/api/usuarios/${id}`).expect(204);
  await request(app).get(`/api/usuarios/${id}`).expect(404);
});

test('valida dados do usuário', async () => {
  const res = await request(app).post('/api/usuarios').send({ nome: 'A', email: 'x', idade: 5 }).expect(400);
  assert.equal(res.body.detalhes.length, 4);
});

test('fluxo de avaliação: criar, listar, atualizar e remover', async () => {
  const { body: u } = await request(app).post('/api/usuarios').send(usuario);

  const { body: a } = await request(app).post('/api/avaliacoes').send({ ...respostas, usuarioId: u.id }).expect(201);
  assert.equal(a.plano.length, 4);
  assert.deepEqual(a.restricoes, ['OMBRO']);
  assert.equal(a.favorita, false);
  assert.ok(a.metaCalorica > a.tmb);
  assert.equal(a.pesoMeta, 58);

  const lista = await request(app).get(`/api/usuarios/${u.id}/avaliacoes`).expect(200);
  assert.equal(lista.body.length, 1);

  const patch = await request(app).patch(`/api/avaliacoes/${a.id}`).send({ favorita: true, observacao: 'Ótimo' }).expect(200);
  assert.equal(patch.body.favorita, true);
  assert.equal(patch.body.observacao, 'Ótimo');

  await request(app).delete(`/api/avaliacoes/${a.id}`).expect(204);
  await request(app).get(`/api/avaliacoes/${a.id}`).expect(404);
});

test('avaliação para usuário inexistente retorna 404', async () => {
  await request(app).post('/api/avaliacoes').send({ ...respostas, usuarioId: 999 }).expect(404);
});

test('remover usuário remove suas avaliações (ON DELETE CASCADE)', async () => {
  const { body: u } = await request(app).post('/api/usuarios').send(usuario);
  const { body: a } = await request(app).post('/api/avaliacoes').send({ ...respostas, usuarioId: u.id });
  await request(app).delete(`/api/usuarios/${u.id}`).expect(204);
  await request(app).get(`/api/avaliacoes/${a.id}`).expect(404);
});

test('documentação Swagger disponível', async () => {
  const res = await request(app).get('/openapi.json').expect(200);
  assert.equal(res.body.info.title, 'TreinoCerto API');
  await request(app).get('/docs/').expect(200);
});
