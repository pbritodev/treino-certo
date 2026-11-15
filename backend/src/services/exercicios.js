/**
 * Biblioteca de exercícios usada pelo motor de recomendação.
 * equipamento: NENHUM (peso corporal), HALTERES ou MAQUINA (academia).
 * evitar: restrições físicas para as quais o exercício NÃO é indicado.
 */
const EXERCICIOS = [
  // Pernas
  { nome: 'Agachamento livre', grupo: 'PERNAS', equipamento: 'NENHUM', evitar: ['JOELHO'] },
  { nome: 'Ponte de glúteo', grupo: 'PERNAS', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Afundo alternado', grupo: 'PERNAS', equipamento: 'NENHUM', evitar: ['JOELHO'] },
  { nome: 'Elevação de panturrilha', grupo: 'PERNAS', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Agachamento goblet', grupo: 'PERNAS', equipamento: 'HALTERES', evitar: ['JOELHO'] },
  { nome: 'Stiff com halteres', grupo: 'PERNAS', equipamento: 'HALTERES', evitar: ['COLUNA'] },
  { nome: 'Leg press 45°', grupo: 'PERNAS', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Cadeira flexora', grupo: 'PERNAS', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Cadeira abdutora', grupo: 'PERNAS', equipamento: 'MAQUINA', evitar: [] },

  // Peito
  { nome: 'Flexão de braço', grupo: 'PEITO', equipamento: 'NENHUM', evitar: ['OMBRO'] },
  { nome: 'Flexão inclinada (mãos elevadas)', grupo: 'PEITO', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Supino com halteres', grupo: 'PEITO', equipamento: 'HALTERES', evitar: ['OMBRO'] },
  { nome: 'Crucifixo com halteres', grupo: 'PEITO', equipamento: 'HALTERES', evitar: ['OMBRO'] },
  { nome: 'Supino máquina', grupo: 'PEITO', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Peck deck', grupo: 'PEITO', equipamento: 'MAQUINA', evitar: [] },

  // Costas
  { nome: 'Superman (extensão lombar)', grupo: 'COSTAS', equipamento: 'NENHUM', evitar: ['COLUNA'] },
  { nome: 'Remada invertida na mesa', grupo: 'COSTAS', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Remada unilateral com halter', grupo: 'COSTAS', equipamento: 'HALTERES', evitar: [] },
  { nome: 'Puxada frontal', grupo: 'COSTAS', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Remada baixa', grupo: 'COSTAS', equipamento: 'MAQUINA', evitar: [] },

  // Ombros
  { nome: 'Elevação lateral com garrafas', grupo: 'OMBROS', equipamento: 'NENHUM', evitar: ['OMBRO'] },
  { nome: 'Desenvolvimento com halteres', grupo: 'OMBROS', equipamento: 'HALTERES', evitar: ['OMBRO', 'COLUNA'] },
  { nome: 'Elevação lateral com halteres', grupo: 'OMBROS', equipamento: 'HALTERES', evitar: ['OMBRO'] },
  { nome: 'Face pull no cabo', grupo: 'OMBROS', equipamento: 'MAQUINA', evitar: [] },

  // Braços
  { nome: 'Tríceps no banco', grupo: 'BRACOS', equipamento: 'NENHUM', evitar: ['OMBRO'] },
  { nome: 'Rosca direta com halteres', grupo: 'BRACOS', equipamento: 'HALTERES', evitar: [] },
  { nome: 'Tríceps francês com halter', grupo: 'BRACOS', equipamento: 'HALTERES', evitar: ['OMBRO'] },
  { nome: 'Tríceps na polia', grupo: 'BRACOS', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Rosca no cabo', grupo: 'BRACOS', equipamento: 'MAQUINA', evitar: [] },

  // Core
  { nome: 'Prancha isométrica', grupo: 'CORE', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Dead bug', grupo: 'CORE', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Abdominal bicicleta', grupo: 'CORE', equipamento: 'NENHUM', evitar: ['COLUNA'] },
  { nome: 'Prancha lateral', grupo: 'CORE', equipamento: 'NENHUM', evitar: ['OMBRO'] },

  // Cardio
  { nome: 'Caminhada rápida', grupo: 'CARDIO', equipamento: 'NENHUM', evitar: [] },
  { nome: 'Polichinelo', grupo: 'CARDIO', equipamento: 'NENHUM', evitar: ['JOELHO', 'CARDIACO'] },
  { nome: 'Corrida leve', grupo: 'CARDIO', equipamento: 'NENHUM', evitar: ['JOELHO'] },
  { nome: 'Burpee', grupo: 'CARDIO', equipamento: 'NENHUM', evitar: ['JOELHO', 'OMBRO', 'CARDIACO'] },
  { nome: 'Bicicleta ergométrica', grupo: 'CARDIO', equipamento: 'MAQUINA', evitar: [] },
  { nome: 'Elíptico', grupo: 'CARDIO', equipamento: 'MAQUINA', evitar: [] },
];

module.exports = { EXERCICIOS };
