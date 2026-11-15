const { DatabaseSync } = require('node:sqlite');
const { DB_PATH } = require('../config/env');

const SCHEMA = `
  CREATE TABLE IF NOT EXISTS usuarios (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    nome       TEXT    NOT NULL,
    email      TEXT    NOT NULL UNIQUE,
    idade      INTEGER NOT NULL,
    sexo       TEXT    NOT NULL CHECK (sexo IN ('MASCULINO', 'FEMININO', 'OUTRO')),
    criado_em  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE TABLE IF NOT EXISTS avaliacoes (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id          INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    objetivo            TEXT    NOT NULL,
    nivel               TEXT    NOT NULL,
    dias_semana         INTEGER NOT NULL,
    minutos_sessao      INTEGER NOT NULL,
    local               TEXT    NOT NULL,
    possui_equipamento  INTEGER NOT NULL DEFAULT 0,
    restricoes          TEXT    NOT NULL DEFAULT '[]',
    peso                REAL    NOT NULL,
    altura              REAL    NOT NULL,
    imc                 REAL    NOT NULL,
    classificacao_imc   TEXT    NOT NULL,
    intensidade         INTEGER NOT NULL,
    classificacao       TEXT    NOT NULL,
    perfil              TEXT    NOT NULL,
    plano               TEXT    NOT NULL,
    dicas               TEXT    NOT NULL,
    observacao          TEXT    NOT NULL DEFAULT '',
    favorita            INTEGER NOT NULL DEFAULT 0,
    criado_em           TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE INDEX IF NOT EXISTS idx_avaliacoes_usuario ON avaliacoes(usuario_id);
`;

/**
 * Abre (ou cria) o banco SQLite e garante que o schema exista.
 * @param {string} [caminho] caminho do arquivo; ':memory:' é usado nos testes.
 */
function abrirBanco(caminho = DB_PATH) {
  const db = new DatabaseSync(caminho);
  db.exec('PRAGMA foreign_keys = ON;');
  db.exec(SCHEMA);
  return db;
}

module.exports = { abrirBanco };
