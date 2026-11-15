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
    /** Devolve o hash da senha (usado só no login e na troca de senha; nunca vai para a resposta). */
    buscarHashSenha(id) {
      return db.prepare('SELECT senha_hash FROM usuarios WHERE id = ?').get(id)?.senha_hash ?? null;
    },
    criar({
      nome, email, idade, sexo, senhaHash,
    }) {
      const { lastInsertRowid } = db
        .prepare('INSERT INTO usuarios (nome, email, idade, sexo, senha_hash) VALUES (?, ?, ?, ?, ?)')
        .run(nome.trim(), email.trim().toLowerCase(), idade, sexo, senhaHash);
      return this.buscarPorId(Number(lastInsertRowid));
    },
    atualizar(id, { nome, email, idade, sexo }) {
      db.prepare('UPDATE usuarios SET nome = ?, email = ?, idade = ?, sexo = ? WHERE id = ?')
        .run(nome.trim(), email.trim().toLowerCase(), idade, sexo, id);
      return this.buscarPorId(id);
    },
    atualizarSenha(id, senhaHash) {
      db.prepare('UPDATE usuarios SET senha_hash = ? WHERE id = ?').run(senhaHash, id);
    },
    remover(id) {
      return db.prepare('DELETE FROM usuarios WHERE id = ?').run(id).changes > 0;
    },
  };
}

module.exports = { criarUsuarioRepository };
