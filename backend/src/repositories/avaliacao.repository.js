/** Acesso à tabela `avaliacoes`. Listas e objetos são guardados como JSON em colunas TEXT. */
function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    usuarioId: linha.usuario_id,
    objetivo: linha.objetivo,
    nivel: linha.nivel,
    diasSemana: linha.dias_semana,
    minutosSessao: linha.minutos_sessao,
    local: linha.local,
    possuiEquipamento: linha.possui_equipamento === 1,
    restricoes: JSON.parse(linha.restricoes),
    peso: linha.peso,
    altura: linha.altura,
    pesoMeta: linha.peso_meta,
    atividadeDiaria: linha.atividade_diaria,
    horasSono: linha.horas_sono,
    imc: linha.imc,
    classificacaoImc: linha.classificacao_imc,
    intensidade: linha.intensidade,
    classificacao: linha.classificacao,
    perfil: linha.perfil,
    tmb: linha.tmb,
    metaCalorica: linha.meta_calorica,
    metaProteina: linha.meta_proteina,
    plano: JSON.parse(linha.plano),
    dicas: JSON.parse(linha.dicas),
    observacao: linha.observacao,
    favorita: linha.favorita === 1,
    criadoEm: linha.criado_em,
  };
}

function criarAvaliacaoRepository(db) {
  return {
    listarPorUsuario(usuarioId) {
      return db
        .prepare('SELECT * FROM avaliacoes WHERE usuario_id = ? ORDER BY id DESC')
        .all(usuarioId)
        .map(mapear);
    },
    buscarUltimaDoUsuario(usuarioId) {
      return mapear(db.prepare('SELECT * FROM avaliacoes WHERE usuario_id = ? ORDER BY id DESC LIMIT 1').get(usuarioId));
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM avaliacoes WHERE id = ?').get(id));
    },
    criar(a) {
      const { lastInsertRowid } = db.prepare(`
        INSERT INTO avaliacoes (
          usuario_id, objetivo, nivel, dias_semana, minutos_sessao, local, possui_equipamento,
          restricoes, peso, altura, peso_meta, atividade_diaria, horas_sono, imc,
          classificacao_imc, intensidade, classificacao, perfil, tmb, meta_calorica,
          meta_proteina, plano, dicas
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(
        a.usuarioId, a.objetivo, a.nivel, a.diasSemana, a.minutosSessao, a.local,
        a.possuiEquipamento ? 1 : 0, JSON.stringify(a.restricoes), a.peso, a.altura,
        a.pesoMeta ?? null, a.atividadeDiaria, a.horasSono, a.imc, a.classificacaoImc,
        a.intensidade, a.classificacao, a.perfil, a.tmb, a.metaCalorica, a.metaProteina,
        JSON.stringify(a.plano), JSON.stringify(a.dicas),
      );
      return this.buscarPorId(Number(lastInsertRowid));
    },
    atualizar(id, { observacao, favorita }) {
      const atual = this.buscarPorId(id);
      if (!atual) return null;
      db.prepare('UPDATE avaliacoes SET observacao = ?, favorita = ? WHERE id = ?').run(
        observacao ?? atual.observacao,
        (favorita ?? atual.favorita) ? 1 : 0,
        id,
      );
      return this.buscarPorId(id);
    },
    remover(id) {
      return db.prepare('DELETE FROM avaliacoes WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarAvaliacaoRepository };
