const path = require('node:path');

module.exports = {
  PORT: Number(process.env.PORT) || 3000,
  DB_PATH: process.env.DB_PATH || path.join(__dirname, '..', '..', 'treino_certo.db'),
};
