const { criarApp } = require('./app');
const { abrirBanco } = require('./db/database');
const { PORT, DB_PATH } = require('./config/env');

const app = criarApp(abrirBanco());

app.listen(PORT, '0.0.0.0', () => {
  console.log(`TreinoCerto API rodando em http://localhost:${PORT}`);
  console.log(`Swagger: http://localhost:${PORT}/docs`);
  console.log(`Banco SQLite: ${DB_PATH}`);
});
