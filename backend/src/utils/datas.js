/** Data local no formato AAAA-MM-DD (toISOString usaria UTC e erraria o dia à noite no Brasil). */
function dataLocal(data = new Date()) {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

module.exports = { dataLocal };
