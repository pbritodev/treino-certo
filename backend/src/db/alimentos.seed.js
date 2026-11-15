/**
 * Lista inicial de alimentos (valores aproximados por 100 g ou 100 ml,
 * baseados na Tabela Brasileira de Composição de Alimentos - TACO).
 * porcao: quantidade sugerida em gramas/ml e sua descrição caseira.
 */
// [nome, categoria, kcal, proteina, carboidrato, gordura, porcaoG, porcaoDescricao]
const ALIMENTOS = [
  ['Arroz branco cozido', 'CEREAIS', 128, 2.5, 28.1, 0.2, 100, '4 colheres de sopa'],
  ['Arroz integral cozido', 'CEREAIS', 124, 2.6, 25.8, 1.0, 100, '4 colheres de sopa'],
  ['Macarrão cozido', 'CEREAIS', 157, 5.8, 30.9, 0.9, 110, '1 prato raso'],
  ['Pão francês', 'CEREAIS', 300, 8.0, 58.6, 3.1, 50, '1 unidade'],
  ['Pão integral', 'CEREAIS', 253, 9.4, 49.9, 3.7, 25, '1 fatia'],
  ['Tapioca (goma)', 'CEREAIS', 240, 0.0, 60.0, 0.0, 50, '1 tapioca média'],
  ['Aveia em flocos', 'CEREAIS', 394, 13.9, 66.6, 8.5, 30, '3 colheres de sopa'],
  ['Batata-doce cozida', 'CEREAIS', 77, 0.6, 18.4, 0.1, 150, '1 unidade média'],
  ['Cuscuz de milho cozido', 'CEREAIS', 113, 2.2, 25.3, 0.7, 120, '1 fatia média'],
  ['Feijão carioca cozido', 'LEGUMINOSAS', 76, 4.8, 13.6, 0.5, 80, '1 concha'],
  ['Feijão preto cozido', 'LEGUMINOSAS', 77, 4.5, 14.0, 0.5, 80, '1 concha'],
  ['Lentilha cozida', 'LEGUMINOSAS', 93, 6.3, 16.3, 0.5, 80, '1 concha'],
  ['Peito de frango grelhado', 'PROTEINAS', 159, 32.0, 0.0, 2.5, 100, '1 filé médio'],
  ['Patinho bovino grelhado', 'PROTEINAS', 219, 35.9, 0.0, 7.3, 100, '1 bife médio'],
  ['Carne moída refogada', 'PROTEINAS', 212, 26.7, 0.0, 10.9, 100, '4 colheres de sopa'],
  ['Tilápia grelhada', 'PROTEINAS', 128, 26.0, 0.0, 2.7, 120, '1 filé'],
  ['Ovo cozido', 'PROTEINAS', 146, 13.3, 0.6, 9.5, 50, '1 unidade'],
  ['Atum em água (lata)', 'PROTEINAS', 116, 25.5, 0.0, 0.8, 60, '1/2 lata'],
  ['Whey protein', 'PROTEINAS', 400, 80.0, 8.0, 6.0, 30, '1 scoop'],
  ['Leite integral', 'LATICINIOS', 61, 3.2, 4.7, 3.3, 200, '1 copo (200 ml)'],
  ['Leite desnatado', 'LATICINIOS', 35, 3.4, 4.9, 0.1, 200, '1 copo (200 ml)'],
  ['Iogurte natural', 'LATICINIOS', 51, 4.1, 1.9, 3.0, 170, '1 pote'],
  ['Queijo minas frescal', 'LATICINIOS', 264, 17.4, 3.2, 20.2, 30, '1 fatia'],
  ['Queijo muçarela', 'LATICINIOS', 330, 22.6, 3.0, 25.2, 20, '1 fatia'],
  ['Banana prata', 'FRUTAS', 98, 1.3, 26.0, 0.1, 70, '1 unidade'],
  ['Maçã', 'FRUTAS', 56, 0.3, 15.2, 0.0, 130, '1 unidade'],
  ['Mamão papaia', 'FRUTAS', 40, 0.5, 10.4, 0.1, 150, '1/2 unidade'],
  ['Morango', 'FRUTAS', 30, 0.9, 6.8, 0.3, 100, '8 unidades'],
  ['Laranja', 'FRUTAS', 37, 1.0, 8.9, 0.1, 140, '1 unidade'],
  ['Abacate', 'FRUTAS', 96, 1.2, 6.0, 8.4, 100, '2 colheres de sopa'],
  ['Alface', 'VEGETAIS', 11, 1.3, 1.7, 0.2, 50, '1 prato de sobremesa'],
  ['Tomate', 'VEGETAIS', 15, 1.1, 3.1, 0.2, 100, '1 unidade'],
  ['Brócolis cozido', 'VEGETAIS', 25, 2.1, 4.4, 0.5, 100, '1 xícara'],
  ['Cenoura crua', 'VEGETAIS', 34, 1.3, 7.7, 0.2, 50, '1/2 unidade'],
  ['Azeite de oliva', 'GORDURAS', 884, 0.0, 0.0, 100.0, 13, '1 colher de sopa'],
  ['Pasta de amendoim', 'GORDURAS', 588, 25.0, 20.0, 50.0, 15, '1 colher de sopa'],
  ['Castanha-do-pará', 'GORDURAS', 643, 14.5, 15.1, 63.5, 10, '2 unidades'],
  ['Café sem açúcar', 'BEBIDAS', 2, 0.1, 0.3, 0.0, 50, '1 xícara'],
  ['Suco de laranja natural', 'BEBIDAS', 37, 0.7, 8.6, 0.1, 200, '1 copo (200 ml)'],
  ['Refrigerante de cola', 'BEBIDAS', 42, 0.0, 10.6, 0.0, 350, '1 lata'],
  ['Chocolate ao leite', 'OUTROS', 540, 7.2, 59.6, 30.3, 25, '1 barra pequena'],
  ['Pizza de muçarela', 'OUTROS', 280, 12.0, 30.0, 12.0, 110, '1 fatia'],
  ['Hambúrguer (sanduíche)', 'OUTROS', 254, 13.0, 30.0, 9.0, 200, '1 unidade'],
  ['Pão de queijo', 'OUTROS', 363, 5.1, 34.2, 24.6, 40, '2 unidades médias'],
];

function popularAlimentos(db) {
  const { total } = db.prepare('SELECT COUNT(*) AS total FROM alimentos').get();
  if (total > 0) return;
  const inserir = db.prepare(`
    INSERT INTO alimentos (nome, categoria, kcal, proteina, carboidrato, gordura, porcao_g, porcao_descricao)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `);
  db.exec('BEGIN');
  for (const alimento of ALIMENTOS) inserir.run(...alimento);
  db.exec('COMMIT');
}

module.exports = { popularAlimentos };
