const { test } = require('node:test');
const assert = require('node:assert/strict');
const { gerarRecomendacao, calcularImc, classificarImc } = require('../src/services/recomendacao.service');

const base = {
  objetivo: 'HIPERTROFIA', nivel: 'INICIANTE', diasSemana: 3, minutosSessao: 60,
  local: 'ACADEMIA', possuiEquipamento: false, restricoes: [], peso: 70, altura: 1.75,
};

test('calcula e classifica o IMC', () => {
  assert.equal(calcularImc(70, 1.75), 22.9);
  assert.equal(classificarImc(17), 'Abaixo do peso');
  assert.equal(classificarImc(22.9), 'Peso normal');
  assert.equal(classificarImc(27), 'Sobrepeso');
  assert.equal(classificarImc(31), 'Obesidade');
});

test('gera um dia de treino para cada dia disponível', () => {
  const r = gerarRecomendacao({ ...base, diasSemana: 5 });
  assert.equal(r.plano.length, 5);
  assert.ok(r.plano.every((d) => d.exercicios.length > 0));
});

test('respeita o limite de exercícios pelo tempo da sessão', () => {
  const r = gerarRecomendacao({ ...base, minutosSessao: 30 });
  assert.ok(r.plano.every((d) => d.exercicios.length <= 4));
});

test('remove exercícios contraindicados para a restrição', () => {
  const r = gerarRecomendacao({ ...base, restricoes: ['JOELHO'], objetivo: 'EMAGRECIMENTO' });
  const nomes = r.plano.flatMap((d) => d.exercicios.map((e) => e.nome));
  assert.ok(!nomes.includes('Agachamento livre'));
  assert.ok(!nomes.includes('Corrida leve'));
});

test('treino em casa sem equipamento usa apenas peso corporal', () => {
  const r = gerarRecomendacao({ ...base, local: 'CASA' });
  const nomes = r.plano.flatMap((d) => d.exercicios.map((e) => e.nome));
  assert.ok(!nomes.some((n) => /halter|máquina|press|polia|puxada/i.test(n)));
});

test('restrição cardíaca limita a intensidade e gera dica médica', () => {
  const r = gerarRecomendacao({ ...base, nivel: 'AVANCADO', diasSemana: 6, restricoes: ['CARDIACO'] });
  assert.ok(r.intensidade <= 45);
  assert.ok(r.dicas.some((d) => d.includes('cardiologista')));
});

test('emagrecimento inclui cardio em todos os dias', () => {
  const r = gerarRecomendacao({ ...base, objetivo: 'EMAGRECIMENTO' });
  assert.ok(r.plano.every((d) => d.exercicios.some((e) => e.grupo === 'CARDIO')));
});
