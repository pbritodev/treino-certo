function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    nome: linha.nome,
    categoria: linha.categoria,
    kcal: linha.kcal,
    proteina: linha.proteina,
    carboidrato: linha.carboidrato,
    gordura: linha.gordura,
    porcaoG: linha.porcao_g,
    porcaoDescricao: linha.porcao_descricao,
    personalizado: linha.criado_por !== null,
  };
}

/** Acesso à tabela `alimentos` (valores nutricionais por 100 g/ml). */
function criarAlimentoRepository(db) {
  return {
    /** Lista os alimentos padrão e, se informado o usuário, também os que ele criou. */
    listar({ busca, categoria, usuarioId } = {}) {
      const filtros = [usuarioId ? '(criado_por IS NULL OR criado_por = ?)' : 'criado_por IS NULL'];
      const parametros = usuarioId ? [usuarioId] : [];
      if (busca) {
        filtros.push('nome LIKE ?');
        parametros.push(`%${busca}%`);
      }
      if (categoria) {
        filtros.push('categoria = ?');
        parametros.push(categoria);
      }
      return db.prepare(`SELECT * FROM alimentos WHERE ${filtros.join(' AND ')} ORDER BY nome`)
        .all(...parametros)
        .map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM alimentos WHERE id = ?').get(id));
    },
    buscarPorNome(nome) {
      return mapear(db.prepare('SELECT * FROM alimentos WHERE lower(nome) = lower(?)').get(nome.trim()));
    },
    criar({
      nome, categoria, kcal, proteina, carboidrato, gordura, porcaoG, porcaoDescricao, usuarioId,
    }) {
      const { lastInsertRowid } = db.prepare(`
        INSERT INTO alimentos (nome, categoria, kcal, proteina, carboidrato, gordura, porcao_g, porcao_descricao, criado_por)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(nome.trim(), categoria, kcal, proteina, carboidrato, gordura, porcaoG, porcaoDescricao.trim(), usuarioId);
      return this.buscarPorId(Number(lastInsertRowid));
    },
  };
}

module.exports = { criarAlimentoRepository };
