const arred = (valor) => Math.round(valor * 10) / 10;

/** Converte a linha (registro + alimento) calculando os nutrientes pela quantidade ingerida. */
function mapear(linha) {
  if (!linha) return null;
  const fator = linha.quantidade / 100;
  return {
    id: linha.id,
    usuarioId: linha.usuario_id,
    alimentoId: linha.alimento_id,
    alimento: linha.nome,
    porcaoDescricao: linha.porcao_descricao,
    porcaoG: linha.porcao_g,
    data: linha.data,
    refeicao: linha.refeicao,
    quantidade: linha.quantidade,
    kcal: arred(linha.kcal * fator),
    proteina: arred(linha.proteina * fator),
    carboidrato: arred(linha.carboidrato * fator),
    gordura: arred(linha.gordura * fator),
  };
}

const SELECT_COM_ALIMENTO = `
  SELECT r.*, a.nome, a.kcal, a.proteina, a.carboidrato, a.gordura, a.porcao_g, a.porcao_descricao
  FROM registros_alimentares r
  JOIN alimentos a ON a.id = r.alimento_id
`;

/** Acesso à tabela `registros_alimentares` (diário de alimentação). */
function criarRegistroRepository(db) {
  return {
    listarPorData(usuarioId, data) {
      return db.prepare(`${SELECT_COM_ALIMENTO} WHERE r.usuario_id = ? AND r.data = ? ORDER BY r.id`)
        .all(usuarioId, data)
        .map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare(`${SELECT_COM_ALIMENTO} WHERE r.id = ?`).get(id));
    },
    criar({ usuarioId, alimentoId, data, refeicao, quantidade }) {
      const { lastInsertRowid } = db
        .prepare('INSERT INTO registros_alimentares (usuario_id, alimento_id, data, refeicao, quantidade) VALUES (?, ?, ?, ?, ?)')
        .run(usuarioId, alimentoId, data, refeicao, quantidade);
      return this.buscarPorId(Number(lastInsertRowid));
    },
    atualizar(id, { quantidade, refeicao }) {
      const alterou = db.prepare('UPDATE registros_alimentares SET quantidade = ?, refeicao = ? WHERE id = ?')
        .run(quantidade, refeicao, id).changes > 0;
      return alterou ? this.buscarPorId(id) : null;
    },
    remover(id) {
      return db.prepare('DELETE FROM registros_alimentares WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarRegistroRepository };
