function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    usuarioId: linha.usuario_id,
    peso: linha.peso,
    data: linha.data,
    observacao: linha.observacao,
  };
}

/** Acesso à tabela `pesagens` (evolução do peso corporal). */
function criarPesagemRepository(db) {
  return {
    listarPorUsuario(usuarioId) {
      return db.prepare('SELECT * FROM pesagens WHERE usuario_id = ? ORDER BY data, id').all(usuarioId).map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM pesagens WHERE id = ?').get(id));
    },
    criar({ usuarioId, peso, data, observacao = '' }) {
      const { lastInsertRowid } = db
        .prepare('INSERT INTO pesagens (usuario_id, peso, data, observacao) VALUES (?, ?, ?, ?)')
        .run(usuarioId, peso, data, observacao.trim());
      return this.buscarPorId(Number(lastInsertRowid));
    },
    remover(id) {
      return db.prepare('DELETE FROM pesagens WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarPesagemRepository };
