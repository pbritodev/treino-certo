function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    usuarioId: linha.usuario_id,
    avaliacaoId: linha.avaliacao_id,
    tipo: linha.tipo,
    dia: linha.dia,
    foco: linha.foco,
    duracaoMin: linha.duracao_min,
    intensidade: linha.intensidade,
    kcal: linha.kcal,
    observacao: linha.observacao,
    data: linha.data,
  };
}

/** Acesso à tabela `treinos_realizados` (treinos que o usuário marcou como concluídos). */
function criarTreinoRepository(db) {
  return {
    /** Treinos do usuário (mais recentes primeiro); com `data`, só os daquele dia. */
    listarPorUsuario(usuarioId, data) {
      const filtroData = data ? 'AND data = ?' : '';
      const parametros = data ? [usuarioId, data] : [usuarioId];
      return db.prepare(`SELECT * FROM treinos_realizados WHERE usuario_id = ? ${filtroData} ORDER BY data DESC, id DESC`)
        .all(...parametros)
        .map(mapear);
    },
    /** Verdadeiro se o mesmo dia do plano já foi registrado nesta data (evita duplicar sem querer). */
    jaRegistrado({
      usuarioId, avaliacaoId = null, dia, data,
    }) {
      return Boolean(db.prepare(`
        SELECT 1 FROM treinos_realizados
        WHERE usuario_id = ? AND tipo = 'PLANO' AND avaliacao_id IS ? AND dia = ? AND data = ?
      `).get(usuarioId, avaliacaoId, dia.trim(), data));
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM treinos_realizados WHERE id = ?').get(id));
    },
    criar({
      usuarioId, avaliacaoId = null, tipo = 'PLANO', dia, foco = '', duracaoMin,
      intensidade = null, kcal = null, observacao = '', data,
    }) {
      const { lastInsertRowid } = db.prepare(`
        INSERT INTO treinos_realizados
          (usuario_id, avaliacao_id, tipo, dia, foco, duracao_min, intensidade, kcal, observacao, data)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(
        usuarioId, avaliacaoId, tipo, dia.trim(), foco.trim(), duracaoMin, intensidade, kcal, observacao.trim(), data,
      );
      return this.buscarPorId(Number(lastInsertRowid));
    },
    remover(id) {
      return db.prepare('DELETE FROM treinos_realizados WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarTreinoRepository };
