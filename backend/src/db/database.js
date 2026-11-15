const { DatabaseSync } = require('node:sqlite');
const { DB_PATH } = require('../config/env');
const { popularAlimentos } = require('./alimentos.seed');

const SCHEMA = `
  CREATE TABLE IF NOT EXISTS usuarios (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    nome       TEXT    NOT NULL,
    email      TEXT    NOT NULL UNIQUE,
    idade      INTEGER NOT NULL,
    sexo       TEXT    NOT NULL CHECK (sexo IN ('MASCULINO', 'FEMININO', 'OUTRO')),
    senha_hash TEXT,
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
    peso_meta           REAL,
    atividade_diaria    TEXT    NOT NULL,
    horas_sono          INTEGER NOT NULL,
    imc                 REAL    NOT NULL,
    classificacao_imc   TEXT    NOT NULL,
    intensidade         INTEGER NOT NULL,
    classificacao       TEXT    NOT NULL,
    perfil              TEXT    NOT NULL,
    tmb                 INTEGER NOT NULL,
    meta_calorica       INTEGER NOT NULL,
    meta_proteina       INTEGER NOT NULL,
    plano               TEXT    NOT NULL,
    dicas               TEXT    NOT NULL,
    observacao          TEXT    NOT NULL DEFAULT '',
    favorita            INTEGER NOT NULL DEFAULT 0,
    criado_em           TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE INDEX IF NOT EXISTS idx_avaliacoes_usuario ON avaliacoes(usuario_id);

  CREATE TABLE IF NOT EXISTS alimentos (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    nome              TEXT    NOT NULL UNIQUE,
    categoria         TEXT    NOT NULL,
    kcal              REAL    NOT NULL,  -- valores por 100 g/ml
    proteina          REAL    NOT NULL,
    carboidrato       REAL    NOT NULL,
    gordura           REAL    NOT NULL,
    porcao_g          REAL    NOT NULL,
    porcao_descricao  TEXT    NOT NULL,
    criado_por        INTEGER REFERENCES usuarios(id) ON DELETE CASCADE  -- NULL = alimento padrão do app
  );

  CREATE TABLE IF NOT EXISTS registros_alimentares (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id   INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    alimento_id  INTEGER NOT NULL REFERENCES alimentos(id),
    data         TEXT    NOT NULL,  -- AAAA-MM-DD
    refeicao     TEXT    NOT NULL CHECK (refeicao IN ('CAFE_DA_MANHA', 'ALMOCO', 'LANCHE', 'JANTAR')),
    quantidade   REAL    NOT NULL,  -- gramas ou ml consumidos
    criado_em    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE INDEX IF NOT EXISTS idx_registros_usuario_data ON registros_alimentares(usuario_id, data);

  CREATE TABLE IF NOT EXISTS pesagens (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id  INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    peso        REAL    NOT NULL,
    data        TEXT    NOT NULL,  -- AAAA-MM-DD
    observacao  TEXT    NOT NULL DEFAULT '',
    criado_em   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE INDEX IF NOT EXISTS idx_pesagens_usuario ON pesagens(usuario_id, data);

  CREATE TABLE IF NOT EXISTS treinos_realizados (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id    INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    avaliacao_id  INTEGER REFERENCES avaliacoes(id) ON DELETE SET NULL,
    dia           TEXT    NOT NULL,  -- ex.: "Dia 2"
    foco          TEXT    NOT NULL,  -- ex.: "Membros inferiores e core"
    duracao_min   INTEGER NOT NULL,
    data          TEXT    NOT NULL,  -- AAAA-MM-DD
    criado_em     TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
  );

  CREATE INDEX IF NOT EXISTS idx_treinos_usuario_data ON treinos_realizados(usuario_id, data);
`;

/** Atualiza bancos criados por versões anteriores (adiciona colunas novas). */
function migrar(db) {
  const colunas = db.prepare('PRAGMA table_info(usuarios)').all().map((c) => c.name);
  if (!colunas.includes('senha_hash')) db.exec('ALTER TABLE usuarios ADD COLUMN senha_hash TEXT');

  const colunasAlimentos = db.prepare('PRAGMA table_info(alimentos)').all().map((c) => c.name);
  if (colunasAlimentos.length && !colunasAlimentos.includes('criado_por')) {
    db.exec('ALTER TABLE alimentos ADD COLUMN criado_por INTEGER REFERENCES usuarios(id) ON DELETE CASCADE');
  }
}

/**
 * Abre (ou cria) o banco SQLite e garante que o schema exista.
 * @param {string} [caminho] caminho do arquivo; ':memory:' é usado nos testes.
 */
function abrirBanco(caminho = DB_PATH) {
  const db = new DatabaseSync(caminho);
  db.exec('PRAGMA foreign_keys = ON;');
  db.exec(SCHEMA);
  migrar(db);
  popularAlimentos(db);
  return db;
}

module.exports = { abrirBanco };
