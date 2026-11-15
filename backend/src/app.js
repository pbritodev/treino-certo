const fs = require('node:fs');
const path = require('node:path');
const express = require('express');
const cors = require('cors');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yaml');

const { criarUsuarioRepository } = require('./repositories/usuario.repository');
const { criarAvaliacaoRepository } = require('./repositories/avaliacao.repository');
const { criarAlimentoRepository } = require('./repositories/alimento.repository');
const { criarRegistroRepository } = require('./repositories/registro.repository');
const { criarPesagemRepository } = require('./repositories/pesagem.repository');
const { criarTreinoRepository } = require('./repositories/treino.repository');
const { criarUsuarioController } = require('./controllers/usuario.controller');
const { criarAvaliacaoController } = require('./controllers/avaliacao.controller');
const { criarNutricaoController } = require('./controllers/nutricao.controller');
const { criarPesagemController } = require('./controllers/pesagem.controller');
const { criarAuthController } = require('./controllers/auth.controller');
const { criarTreinoController } = require('./controllers/treino.controller');
const { criarRotas } = require('./routes');
const { rotaNaoEncontrada, tratarErros } = require('./middlewares/erros');

const openapi = YAML.parse(fs.readFileSync(path.join(__dirname, 'docs', 'openapi.yaml'), 'utf8'));

/** Monta a aplicação Express recebendo o banco por injeção (facilita os testes). */
function criarApp(db) {
  const usuarios = criarUsuarioRepository(db);
  const avaliacoes = criarAvaliacaoRepository(db);
  const alimentos = criarAlimentoRepository(db);
  const registros = criarRegistroRepository(db);
  const pesagens = criarPesagemRepository(db);
  const treinos = criarTreinoRepository(db);

  const app = express();
  app.use(cors());
  app.use(express.json());

  app.get('/', (req, res) => res.redirect('/docs'));
  app.get('/openapi.json', (req, res) => res.json(openapi));
  app.use('/docs', swaggerUi.serve, swaggerUi.setup(openapi, { customSiteTitle: 'TreinoCerto API' }));

  app.use('/api', criarRotas({
    authController: criarAuthController(usuarios),
    usuarioController: criarUsuarioController(usuarios, avaliacoes),
    avaliacaoController: criarAvaliacaoController(avaliacoes, usuarios),
    nutricaoController: criarNutricaoController({
      alimentos, registros, usuarios, avaliacoes,
    }),
    pesagemController: criarPesagemController({ pesagens, usuarios }),
    treinoController: criarTreinoController({
      treinos, usuarios, avaliacoes, pesagens,
    }),
  }));

  app.use(rotaNaoEncontrada);
  app.use(tratarErros);
  return app;
}

module.exports = { criarApp };
