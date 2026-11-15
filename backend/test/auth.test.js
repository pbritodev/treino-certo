const { test, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const { criarApp } = require('../src/app');
const { abrirBanco } = require('../src/db/database');

let app;
let usuario;
beforeEach(async () => {
  app = criarApp(abrirBanco(':memory:'));
  ({ body: usuario } = await request(app).post('/api/usuarios').send({
    nome: 'Ana Souza', email: 'Ana@Email.com', idade: 22, sexo: 'FEMININO', senha: 'minhaSenha',
  }));
});

test('cadastro exige senha e nunca devolve a senha', async () => {
  assert.equal(usuario.senha, undefined);
  assert.equal(usuario.senhaHash, undefined);
  const res = await request(app).post('/api/usuarios')
    .send({ nome: 'Sem Senha', email: 'x@x.com', idade: 30, sexo: 'OUTRO' }).expect(400);
  assert.ok(res.body.detalhes.some((d) => d.includes('senha')));
});

test('login com e-mail (sem diferenciar maiúsculas) e senha corretos', async () => {
  const res = await request(app).post('/api/auth/login')
    .send({ email: 'ana@email.com', senha: 'minhaSenha' }).expect(200);
  assert.equal(res.body.id, usuario.id);
});

test('login recusa senha errada e e-mail inexistente com a mesma mensagem', async () => {
  const errada = await request(app).post('/api/auth/login').send({ email: 'ana@email.com', senha: 'outra' }).expect(401);
  const inexistente = await request(app).post('/api/auth/login').send({ email: 'z@z.com', senha: 'outra' }).expect(401);
  assert.equal(errada.body.erro, inexistente.body.erro);
});

test('troca de senha confere a senha atual', async () => {
  await request(app).put(`/api/usuarios/${usuario.id}/senha`)
    .send({ senhaAtual: 'errada', novaSenha: 'novaSenha1' }).expect(401);
  await request(app).put(`/api/usuarios/${usuario.id}/senha`)
    .send({ senhaAtual: 'minhaSenha', novaSenha: '123' }).expect(400);
  await request(app).put(`/api/usuarios/${usuario.id}/senha`)
    .send({ senhaAtual: 'minhaSenha', novaSenha: 'novaSenha1' }).expect(204);

  await request(app).post('/api/auth/login').send({ email: 'ana@email.com', senha: 'minhaSenha' }).expect(401);
  await request(app).post('/api/auth/login').send({ email: 'ana@email.com', senha: 'novaSenha1' }).expect(200);
});

test('editar perfil (PUT) mantém a senha', async () => {
  await request(app).put(`/api/usuarios/${usuario.id}`)
    .send({ nome: 'Ana S.', email: 'ana@email.com', idade: 23, sexo: 'FEMININO' }).expect(200);
  await request(app).post('/api/auth/login').send({ email: 'ana@email.com', senha: 'minhaSenha' }).expect(200);
});
