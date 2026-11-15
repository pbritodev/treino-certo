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
  };
}

/** Acesso à tabela `alimentos` (valores nutricionais por 100 g/ml). */
function criarAlimentoRepository(db) {
  return {
    listar({ busca, categoria } = {}) {
      const filtros = [];
      const params = [];
      if (busca) {
        filtros.push('nome LIKE ?');
        params.push(`%${busca}%`);
      }
      if (categoria) {
        filtros.push('categoria = ?');
        params.push(categoria);
      }
      const where = filtros.length ? `WHERE ${filtros.join(' AND ')}` : '';
      return db.prepare(`SELECT * FROM alimentos ${where} ORDER BY nome`).all(...params).map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM alimentos WHERE id = ?').get(id));
    },
  };
}

module.exports = { criarAlimentoRepository };
