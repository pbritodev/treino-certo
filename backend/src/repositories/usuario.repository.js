/** Acesso à tabela `usuarios`. Converte snake_case do banco para camelCase da API. */
function mapear(linha) {
  if (!linha) return null;
  return {
    id: linha.id,
    nome: linha.nome,
    email: linha.email,
    idade: linha.idade,
    sexo: linha.sexo,
    criadoEm: linha.criado_em,
  };
}

function criarUsuarioRepository(db) {
  return {
    listar() {
      return db.prepare('SELECT * FROM usuarios ORDER BY nome').all().map(mapear);
    },
    buscarPorId(id) {
      return mapear(db.prepare('SELECT * FROM usuarios WHERE id = ?').get(id));
    },
    buscarPorEmail(email) {
      return mapear(db.prepare('SELECT * FROM usuarios WHERE email = ?').get(email));
    },
    criar({ nome, email, idade, sexo }) {
      const { lastInsertRowid } = db
        .prepare('INSERT INTO usuarios (nome, email, idade, sexo) VALUES (?, ?, ?, ?)')
        .run(nome.trim(), email.trim().toLowerCase(), idade, sexo);
      return this.buscarPorId(Number(lastInsertRowid));
    },
    atualizar(id, { nome, email, idade, sexo }) {
      db.prepare('UPDATE usuarios SET nome = ?, email = ?, idade = ?, sexo = ? WHERE id = ?')
        .run(nome.trim(), email.trim().toLowerCase(), idade, sexo, id);
      return this.buscarPorId(id);
    },
    remover(id) {
      return db.prepare('DELETE FROM usuarios WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarUsuarioRepository };
