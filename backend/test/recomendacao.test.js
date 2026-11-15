const { test } = require('node:test');
const assert = require('node:assert/strict');
const {
  gerarRecomendacao, calcularImc, classificarImc, calcularTmb, calcularMetasNutricionais,
} = require('../src/services/recomendacao.service');

const base = {
  objetivo: 'HIPERTROFIA', nivel: 'INICIANTE', diasSemana: 3, minutosSessao: 60,
  local: 'ACADEMIA', possuiEquipamento: false, restricoes: [], peso: 70, altura: 1.75,
  atividadeDiaria: 'LEVE', horasSono: 8,
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

test('calcula a TMB pela fórmula de Mifflin-St Jeor', () => {
  // 10*70 + 6.25*175 - 5*30 + 5 = 1648.75
  assert.equal(calcularTmb({ peso: 70, altura: 1.75, idade: 30, sexo: 'MASCULINO' }), 1649);
  assert.equal(calcularTmb({ peso: 70, altura: 1.75, idade: 30, sexo: 'FEMININO' }), 1483);
});

test('meta calórica considera atividade e objetivo', () => {
  const usuario = { idade: 30, sexo: 'MASCULINO' };
  const manter = calcularMetasNutricionais({ ...base, objetivo: 'SAUDE' }, usuario);
  const emagrecer = calcularMetasNutricionais({ ...base, objetivo: 'EMAGRECIMENTO' }, usuario);
  const ganhar = calcularMetasNutricionais({ ...base, objetivo: 'HIPERTROFIA' }, usuario);
  assert.equal(manter.metaCalorica, Math.round(1649 * 1.375));
  assert.ok(emagrecer.metaCalorica < manter.metaCalorica);
  assert.ok(ganhar.metaCalorica > manter.metaCalorica);
  assert.equal(ganhar.metaProteina, 140);
});

test('dicas de sono e de peso meta', () => {
  const r = gerarRecomendacao({ ...base, horasSono: 5, pesoMeta: 65 });
  assert.ok(r.dicas.some((d) => d.includes('5h')));
  assert.ok(r.dicas.some((d) => d.includes('perder 5.0 kg')));
});
