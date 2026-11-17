const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');
const { estimarKcal } = require('../src/services/gasto.service');
const { dataLocal } = require('../src/utils/datas');

let app;
let usuarioId;
beforeEach(async () => {
  app = criarApp(abrirBanco(':memory:'));
  ({ body: { id: usuarioId } } = await request(app).post('/api/usuarios').send({
    nome: 'Rui', email: 'rui@email.com', idade: 40, sexo: 'MASCULINO', senha: '123456',
  }));
});

test('estimativa de calorias pelo MET', () => {
  // Corrida (MET 9,8) por 30 min com 80 kg = 9,8 x 80 x 0,5 = 392 kcal
  assert.equal(estimarKcal({ tipo: 'CORRIDA', duracaoMin: 30, peso: 80 }), 392);
  assert.equal(estimarKcal({ tipo: 'CORRIDA', intensidade: 'INTENSA', duracaoMin: 30, peso: 80 }), 490);
  assert.equal(estimarKcal({ tipo: 'YOGA', duracaoMin: 60 }), 175); // peso padrão 70 kg
});

test('cadastra treino avulso usando o peso atual para estimar as calorias', async () => {
  await request(app).post('/api/pesagens').send({ usuarioId, peso: 80, data: dataLocal() });
  const { body: treino } = await request(app).post('/api/treinos-realizados').send({
    usuarioId, tipo: 'CORRIDA', dia: 'Corrida no parque', foco: '5 km', duracaoMin: 30,
    intensidade: 'MODERADA', observacao: 'Ritmo bom', data: dataLocal(),
  }).expect(201);
  assert.equal(treino.tipo, 'CORRIDA');
  assert.equal(treino.kcal, 392);
  assert.equal(treino.observacao, 'Ritmo bom');
});

test('treinos avulsos podem se repetir no mesmo dia; os do plano não', async () => {
  const corrida = { usuarioId, tipo: 'CORRIDA', dia: 'Corrida', duracaoMin: 20, data: dataLocal() };
  await request(app).post('/api/treinos-realizados').send(corrida).expect(201);
  await request(app).post('/api/treinos-realizados').send(corrida).expect(201);

  const doPlano = { usuarioId, dia: 'Dia 1', foco: 'Corpo inteiro', duracaoMin: 45, data: dataLocal() };
  const { body } = await request(app).post('/api/treinos-realizados').send(doPlano).expect(201);
  assert.equal(body.tipo, 'PLANO');
  await request(app).post('/api/treinos-realizados').send(doPlano).expect(409);
});

test('valida tipo e intensidade do treino', async () => {
  const res = await request(app).post('/api/treinos-realizados').send({
    usuarioId, tipo: 'PARAQUEDAS', dia: 'Salto', duracaoMin: 30, intensidade: 'EXTREMA', data: dataLocal(),
  }).expect(400);
  assert.equal(res.body.detalhes.length, 2);
});
