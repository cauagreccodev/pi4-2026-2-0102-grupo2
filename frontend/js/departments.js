// Shared department names for registration, assignment and permission checks.
const DEPARTMENTS = Object.freeze([
  'Financeiro', 'Compras', 'TI', 'Produção', 'Recursos Humanos',
  'Marketing', 'Vendas', 'Logística', 'Administrativo'
]);

function normalizeDepartment(value) {
  const name = String(value || '').trim();
  const key = name.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    .toUpperCase().replace(/[^A-Z0-9]/g, '');
  if (['TI', 'TECNOLOGIA', 'TECNOLOGIADAINFORMACAO', 'TECNOLOGIADEINFORMACAO'].includes(key)) return 'TI';
  return DEPARTMENTS.find(department => department.normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '').toUpperCase().replace(/[^A-Z0-9]/g, '') === key) || name;
}
