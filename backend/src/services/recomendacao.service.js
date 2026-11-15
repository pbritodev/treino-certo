/**
 * Motor de recomendação: transforma as respostas do questionário em um
 * plano de treino personalizado. Funções puras (sem acesso a banco),
 * o que facilita os testes automatizados.
 */
const { EXERCICIOS } = require('./exercicios');

const NOME_PERFIL = {
  EMAGRECIMENTO: 'Queimador de Calorias',
  HIPERTROFIA: 'Construtor Muscular',
  CONDICIONAMENTO: 'Atleta Resistente',
  SAUDE: 'Bem-Estar em Movimento',
};

const NOME_NIVEL = { INICIANTE: 'Iniciante', INTERMEDIARIO: 'Intermediário', AVANCADO: 'Avançado' };

const MAX_EXERCICIOS_POR_MINUTOS = { 30: 4, 45: 5, 60: 6, 90: 8 };

const DIVISAO_CORPO_INTEIRO = [{ foco: 'Corpo inteiro', grupos: ['PERNAS', 'PEITO', 'COSTAS', 'CORE'] }];
const DIVISAO_AB = [
  { foco: 'Membros superiores', grupos: ['PEITO', 'COSTAS', 'OMBROS', 'BRACOS'] },
  { foco: 'Membros inferiores e core', grupos: ['PERNAS', 'CORE'] },
];
const DIVISAO_ABC = [
  { foco: 'Peito, ombros e tríceps', grupos: ['PEITO', 'OMBROS', 'BRACOS'] },
  { foco: 'Costas e bíceps', grupos: ['COSTAS', 'BRACOS'] },
  { foco: 'Pernas e core', grupos: ['PERNAS', 'CORE'] },
];

function calcularImc(peso, altura) {
  return Math.round((peso / (altura * altura)) * 10) / 10;
}

function classificarImc(imc) {
  if (imc < 18.5) return 'Abaixo do peso';
  if (imc < 25) return 'Peso normal';
  if (imc < 30) return 'Sobrepeso';
  return 'Obesidade';
}

/** Intensidade recomendada de 10 a 100, considerando nível, volume e limitações. */
function calcularIntensidade({ nivel, diasSemana, minutosSessao, objetivo, restricoes, imc }) {
  const base = { INICIANTE: 30, INTERMEDIARIO: 55, AVANCADO: 75 }[nivel];
  const bonusMinutos = { 30: 0, 45: 3, 60: 6, 90: 10 }[minutosSessao];
  const ajusteObjetivo = { EMAGRECIMENTO: 0, HIPERTROFIA: 3, CONDICIONAMENTO: 5, SAUDE: -10 }[objetivo];
  const limitacoesFisicas = restricoes.filter((r) => r !== 'CARDIACO').length;

  let intensidade = base + (diasSemana - 2) * 3 + bonusMinutos + ajusteObjetivo - limitacoesFisicas * 5;
  if (imc >= 30) intensidade -= 10;
  if (restricoes.includes('CARDIACO')) intensidade = Math.min(intensidade, 45);
  return Math.max(10, Math.min(100, intensidade));
}

function classificarIntensidade(intensidade) {
  if (intensidade < 40) return 'Leve';
  if (intensidade < 70) return 'Moderada';
  return 'Intensa';
}

function equipamentosPermitidos(local, possuiEquipamento) {
  if (local === 'ACADEMIA') return ['NENHUM', 'HALTERES', 'MAQUINA'];
  if (local === 'CASA' && possuiEquipamento) return ['NENHUM', 'HALTERES'];
  return ['NENHUM'];
}

function escolherDivisao(objetivo, diasSemana) {
  if (objetivo === 'SAUDE' || objetivo === 'CONDICIONAMENTO' || diasSemana <= 3) return DIVISAO_CORPO_INTEIRO;
  return diasSemana === 4 ? DIVISAO_AB : DIVISAO_ABC;
}

function prescrever(exercicio, { objetivo, nivel, minutosSessao }) {
  const indiceNivel = { INICIANTE: 0, INTERMEDIARIO: 1, AVANCADO: 2 }[nivel];

  if (exercicio.grupo === 'CARDIO') {
    return { series: 1, repeticoes: `${Math.round(minutosSessao * 0.25)} min` };
  }
  if (exercicio.nome.startsWith('Prancha')) {
    return { series: [2, 3, 3][indiceNivel], repeticoes: ['20s', '30s', '45s'][indiceNivel] };
  }
  switch (objetivo) {
    case 'HIPERTROFIA':
      return { series: [3, 4, 4][indiceNivel], repeticoes: '8-12' };
    case 'EMAGRECIMENTO':
      return { series: 3, repeticoes: '15-20' };
    case 'CONDICIONAMENTO':
      return { series: [2, 3, 4][indiceNivel], repeticoes: '40s ativo / 20s pausa' };
    default:
      return { series: [2, 2, 3][indiceNivel], repeticoes: '12-15' };
  }
}

/** Seleciona exercícios para um dia, alternando entre os grupos musculares. */
function montarDia(indiceDia, divisao, respostas, disponiveis) {
  const { objetivo, minutosSessao } = respostas;
  const incluiCardio = objetivo === 'EMAGRECIMENTO' || objetivo === 'CONDICIONAMENTO';
  const limiteForca = MAX_EXERCICIOS_POR_MINUTOS[minutosSessao] - (incluiCardio ? 1 : 0);

  const porGrupo = (grupo) => disponiveis.filter((e) => e.grupo === grupo);
  const filas = divisao.grupos.map((grupo) => {
    const lista = porGrupo(grupo);
    // Desloca o início da lista conforme o dia para variar os exercícios ao longo da semana.
    const deslocamento = lista.length ? indiceDia % lista.length : 0;
    return lista.slice(deslocamento).concat(lista.slice(0, deslocamento));
  });

  const escolhidos = [];
  let adicionou = true;
  while (escolhidos.length < limiteForca && adicionou) {
    adicionou = false;
    for (const fila of filas) {
      if (escolhidos.length >= limiteForca) break;
      const proximo = fila.shift();
      if (proximo) {
        escolhidos.push(proximo);
        adicionou = true;
      }
    }
  }

  if (incluiCardio) {
    const cardios = porGrupo('CARDIO');
    if (cardios.length) escolhidos.push(cardios[indiceDia % cardios.length]);
  }

  return {
    dia: `Dia ${indiceDia + 1}`,
    foco: divisao.foco,
    exercicios: escolhidos.map((e) => ({ nome: e.nome, grupo: e.grupo, ...prescrever(e, respostas) })),
  };
}

function montarPlano(respostas) {
  const { objetivo, diasSemana, local, possuiEquipamento, restricoes } = respostas;
  const equipamentos = equipamentosPermitidos(local, possuiEquipamento);
  const disponiveis = EXERCICIOS.filter(
    (e) => equipamentos.includes(e.equipamento) && !e.evitar.some((r) => restricoes.includes(r)),
  );
  const divisao = escolherDivisao(objetivo, diasSemana);

  return Array.from({ length: diasSemana }, (_, i) => montarDia(i, divisao[i % divisao.length], respostas, disponiveis));
}

const FATOR_ATIVIDADE = { SEDENTARIO: 1.2, LEVE: 1.375, MODERADO: 1.55, INTENSO: 1.725 };
const AJUSTE_CALORICO = { EMAGRECIMENTO: -500, HIPERTROFIA: 300, CONDICIONAMENTO: 0, SAUDE: 0 };
const PROTEINA_POR_KG = { EMAGRECIMENTO: 1.8, HIPERTROFIA: 2.0, CONDICIONAMENTO: 1.6, SAUDE: 1.2 };

/** Taxa metabólica basal pela equação de Mifflin-St Jeor (kcal/dia). */
function calcularTmb({ peso, altura, idade, sexo }) {
  const base = 10 * peso + 6.25 * (altura * 100) - 5 * idade;
  const ajusteSexo = { MASCULINO: 5, FEMININO: -161 }[sexo] ?? -78; // OUTRO: média entre os dois
  return Math.round(base + ajusteSexo);
}

/**
 * Meta diária de calorias e proteínas: gasto total (TMB x fator de atividade)
 * ajustado ao objetivo. Nunca fica abaixo da TMB, por segurança.
 */
function calcularMetasNutricionais(respostas, usuario) {
  const tmb = calcularTmb({ ...respostas, ...usuario });
  const gastoTotal = tmb * FATOR_ATIVIDADE[respostas.atividadeDiaria];
  const metaCalorica = Math.round(Math.max(tmb, gastoTotal + AJUSTE_CALORICO[respostas.objetivo]));
  const metaProteina = Math.round(respostas.peso * PROTEINA_POR_KG[respostas.objetivo]);
  return { tmb, metaCalorica, metaProteina };
}

function gerarDicas({ objetivo, nivel, diasSemana, restricoes, horasSono, peso, pesoMeta }, classificacaoImc) {
  const dicas = [];
  const porObjetivo = {
    EMAGRECIMENTO: 'Combine o treino com déficit calórico moderado e boa ingestão de proteínas.',
    HIPERTROFIA: 'Aumente as cargas progressivamente e consuma cerca de 1,6 g de proteína por kg de peso.',
    CONDICIONAMENTO: 'Controle os intervalos de descanso com cronômetro e registre sua evolução semanal.',
    SAUDE: 'Constância vale mais que intensidade: mantenha a rotina mesmo nos dias corridos.',
  };
  dicas.push(porObjetivo[objetivo]);

  if (nivel === 'INICIANTE') dicas.push('Priorize a execução correta antes de aumentar cargas ou velocidade.');
  if (diasSemana >= 5) dicas.push('Com 5 ou mais treinos semanais, durma bem e respeite sinais de fadiga.');
  if (classificacaoImc === 'Obesidade' || classificacaoImc === 'Sobrepeso') {
    dicas.push('Prefira exercícios de baixo impacto no início para proteger as articulações.');
  }
  if (classificacaoImc === 'Abaixo do peso') dicas.push('Garanta superávit calórico para dar suporte ao treino.');
  if (restricoes.includes('CARDIACO')) {
    dicas.push('Procure liberação de um cardiologista antes de iniciar e mantenha intensidade leve.');
  }
  if (restricoes.some((r) => r !== 'CARDIACO')) {
    dicas.push('Exercícios que sobrecarregam suas regiões sensíveis foram removidos do plano.');
  }
  if (horasSono < 7) {
    dicas.push(`Você dorme ${horasSono}h por noite: tente chegar a 7-9h, pois a recuperação muscular acontece no sono.`);
  }
  if (pesoMeta && Math.abs(pesoMeta - peso) >= 1) {
    const diferenca = Math.abs(pesoMeta - peso);
    const semanas = Math.ceil(diferenca / 0.5);
    const verbo = pesoMeta < peso ? 'perder' : 'ganhar';
    dicas.push(`Para ${verbo} ${diferenca.toFixed(1)} kg com segurança (~0,5 kg/semana), conte com cerca de ${semanas} semanas.`);
  }
  dicas.push('Hidrate-se e faça 5 minutos de aquecimento antes de cada sessão.');
  return dicas;
}

/**
 * Ponto de entrada do motor: recebe as respostas do questionário e
 * devolve o resultado completo da avaliação.
 */
function gerarRecomendacao(entrada, usuario = { idade: 30, sexo: 'OUTRO' }) {
  const respostas = {
    possuiEquipamento: false, restricoes: [], atividadeDiaria: 'LEVE', horasSono: 7, pesoMeta: null, ...entrada,
  };
  const imc = calcularImc(respostas.peso, respostas.altura);
  const classificacaoImc = classificarImc(imc);
  const intensidade = calcularIntensidade({ ...respostas, imc });

  return {
    imc,
    classificacaoImc,
    intensidade,
    classificacao: classificarIntensidade(intensidade),
    perfil: `${NOME_PERFIL[respostas.objetivo]} · ${NOME_NIVEL[respostas.nivel]}`,
    ...calcularMetasNutricionais(respostas, usuario),
    plano: montarPlano(respostas),
    dicas: gerarDicas(respostas, classificacaoImc),
  };
}

module.exports = {
  gerarRecomendacao, calcularImc, classificarImc, calcularIntensidade, calcularTmb, calcularMetasNutricionais,
};
