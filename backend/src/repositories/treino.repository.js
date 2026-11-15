function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    usuarioId: linha.usuario_id,
    avaliacaoId: linha.avaliacao_id,
    dia: linha.dia,
    foco: linha.foco,
    duracaoMin: linha.duracao_min,
    data: linha.data,
  };
}

/** Acesso à tabela `treinos_realizados` (treinos que o usuário marcou como concluídos). */
function criarTreinoRepository(db) {
  return {
    listarPorUsuario(usuarioId) {
      return db.prepare('SELECT * FROM treinos_realizados WHERE usuario_id = ? ORDER BY data DESC, id DESC')
        .all(usuarioId)
        .map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM treinos_realizados WHERE id = ?').get(id));
    },
    criar({
      usuarioId, avaliacaoId = null, dia, foco, duracaoMin, data,
    }) {
      const { lastInsertRowid } = db.prepare(`
        INSERT INTO treinos_realizados (usuario_id, avaliacao_id, dia, foco, duracao_min, data)
        VALUES (?, ?, ?, ?, ?, ?)
      `).run(usuarioId, avaliacaoId, dia.trim(), foco.trim(), duracaoMin, data);
      return this.buscarPorId(Number(lastInsertRowid));
    },
    remover(id) {
      return db.prepare('DELETE FROM treinos_realizados WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarTreinoRepository };
