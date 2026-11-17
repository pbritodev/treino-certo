/**
 * Estimativa de calorias gastas em um treino pela fórmula do MET
 * (equivalente metabólico): kcal = MET x peso (kg) x duração (horas).
 * Valores de MET aproximados do Compêndio de Atividades Físicas (intensidade moderada).
 */
const MET_POR_TIPO = {
  PLANO: 5.0,
  MUSCULACAO: 5.0,
  CORRIDA: 9.8,
  CAMINHADA: 3.8,
  CICLISMO: 7.5,
  NATACAO: 7.0,
  FUNCIONAL: 8.0,
  YOGA: 2.5,
  ESPORTE: 7.0,
  OUTRO: 5.0,
};

const AJUSTE_INTENSIDADE = { LEVE: 0.75, MODERADA: 1, INTENSA: 1.25 };

const PESO_PADRAO = 70;

function estimarKcal({
  tipo = 'PLANO', intensidade = 'MODERADA', duracaoMin, peso,
}) {
  const met = MET_POR_TIPO[tipo] * AJUSTE_INTENSIDADE[intensidade ?? 'MODERADA'];
  return Math.round(met * (peso ?? PESO_PADRAO) * (duracaoMin / 60));
}

module.exports = { estimarKcal, MET_POR_TIPO, PESO_PADRAO };
