/**
 * Popula o banco com dados de demonstração (usuária, avaliações, pesagens e diário).
 * Uso: npm run seed   (apaga e recria o arquivo do banco definido em DB_PATH)
 */
const fs = require('node:fs');
const { DB_PATH } = require('../src/config/env');
const { abrirBanco } = require('../src/db/database');
const { criarUsuarioRepository } = require('../src/repositories/usuario.repository');
const { criarAvaliacaoRepository } = require('../src/repositories/avaliacao.repository');
const { criarRegistroRepository } = require('../src/repositories/registro.repository');
const { criarPesagemRepository } = require('../src/repositories/pesagem.repository');
const { criarAlimentoRepository } = require('../src/repositories/alimento.repository');
const { gerarRecomendacao } = require('../src/services/recomendacao.service');
const { dataLocal } = require('../src/utils/datas');
const { gerarHash } = require('../src/utils/senha');
const { criarTreinoRepository } = require('../src/repositories/treino.repository');
const { estimarKcal } = require('../src/services/gasto.service');

const diasAtras = (dias) => {
  const data = new Date();
  data.setDate(data.getDate() - dias);
  return dataLocal(data);
};

if (fs.existsSync(DB_PATH)) fs.rmSync(DB_PATH);
const db = abrirBanco();
const usuarios = criarUsuarioRepository(db);
const avaliacoes = criarAvaliacaoRepository(db);
const registros = criarRegistroRepository(db);
const pesagens = criarPesagemRepository(db);
const alimentos = criarAlimentoRepository(db);

const treinos = criarTreinoRepository(db);

const maria = usuarios.criar({
  nome: 'Maria Silva', email: 'maria@email.com', idade: 27, sexo: 'FEMININO', senhaHash: gerarHash('123456'),
});

const respostasAntigas = {
  usuarioId: maria.id, objetivo: 'CONDICIONAMENTO', nivel: 'INICIANTE', diasSemana: 3, minutosSessao: 45,
  local: 'CASA', possuiEquipamento: true, restricoes: ['JOELHO'], peso: 74.5, altura: 1.65,
  pesoMeta: 66, atividadeDiaria: 'SEDENTARIO', horasSono: 6,
};
const primeira = avaliacoes.criar({ ...respostasAntigas, ...gerarRecomendacao(respostasAntigas, maria) });
avaliacoes.atualizar(primeira.id, { favorita: true, observacao: 'Senti o joelho no dia 2, reduzi a amplitude.' });

const respostasAtuais = {
  usuarioId: maria.id, objetivo: 'EMAGRECIMENTO', nivel: 'INTERMEDIARIO', diasSemana: 4, minutosSessao: 60,
  local: 'ACADEMIA', possuiEquipamento: false, restricoes: ['JOELHO'], peso: 71.2, altura: 1.65,
  pesoMeta: 66, atividadeDiaria: 'LEVE', horasSono: 7,
};
const atual = avaliacoes.criar({ ...respostasAtuais, ...gerarRecomendacao(respostasAtuais, maria) });

// Treinos concluídos nas últimas semanas (alimenta a tela de Progresso)
[13, 11, 9, 6, 4, 2, 1, 0].forEach((dias, i) => {
  const dia = atual.plano[i % atual.plano.length];
  treinos.criar({
    usuarioId: maria.id, avaliacaoId: atual.id, dia: dia.dia, foco: dia.foco, duracaoMin: 60, data: diasAtras(dias),
    kcal: estimarKcal({ duracaoMin: 60, peso: 71.2 }),
  });
});

// Treinos avulsos cadastrados pela tela "Meus treinos"
[
  { tipo: 'CORRIDA', dia: 'Corrida no parque', foco: '5 km', duracaoMin: 35, intensidade: 'MODERADA', dias: 5 },
  { tipo: 'NATACAO', dia: 'Natação', foco: 'Crawl', duracaoMin: 45, intensidade: 'LEVE', dias: 3 },
  { tipo: 'YOGA', dia: 'Yoga em casa', foco: 'Alongamento', duracaoMin: 30, intensidade: 'LEVE', dias: 0 },
].forEach(({ dias, ...treino }) => {
  treinos.criar({
    usuarioId: maria.id, ...treino, data: diasAtras(dias), kcal: estimarKcal({ ...treino, peso: 71.2 }),
  });
});

[[42, 74.5], [35, 74.0], [28, 73.1], [21, 72.6], [14, 72.4], [7, 71.6], [0, 71.2]].forEach(([dias, peso]) => {
  pesagens.criar({ usuarioId: maria.id, peso, data: diasAtras(dias), observacao: dias === 0 ? 'Em jejum' : '' });
});

const idDe = (nome) => alimentos.listar({ busca: nome })[0].id;
[
  ['Aveia em flocos', 'CAFE_DA_MANHA', 30], ['Banana prata', 'CAFE_DA_MANHA', 70], ['Leite desnatado', 'CAFE_DA_MANHA', 200],
  ['Arroz integral', 'ALMOCO', 120], ['Feijão carioca', 'ALMOCO', 80], ['Peito de frango', 'ALMOCO', 130],
  ['Brócolis', 'ALMOCO', 80], ['Iogurte natural', 'LANCHE', 170], ['Morango', 'LANCHE', 100],
].forEach(([nome, refeicao, quantidade]) => {
  registros.criar({ usuarioId: maria.id, alimentoId: idDe(nome), data: diasAtras(0), refeicao, quantidade });
});

console.log(`Dados de exemplo criados em ${DB_PATH}. Login: ${maria.email} / senha 123456`);
