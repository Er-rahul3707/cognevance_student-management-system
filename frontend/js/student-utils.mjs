export function normalizeApiBaseUrl(value) {
  const base = String(value || 'http://localhost:8080/api').trim();
  return base.replace(/\/+$/, '');
}

export function buildStudentsUrl(baseUrl, query = '') {
  const base = normalizeApiBaseUrl(baseUrl);
  return `${base}/students${query ? `?q=${encodeURIComponent(query)}` : ''}`;
}

export function getInitials(fullName) {
  return String(fullName || '').trim().split(/\s+/).filter(Boolean)
    .slice(0, 2).map(part => part[0].toLocaleUpperCase()).join('');
}

export function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

export function formatDate(value, options = { dateStyle: 'medium' }) {
  if (!value) return '—';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.valueOf()) ? '—' : new Intl.DateTimeFormat(undefined, options).format(date);
}

export function getDashboardStats(students, now = new Date()) {
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  return {
    total: students.length,
    courses: new Set(students.map(student => String(student.course || '').trim().toLowerCase()).filter(Boolean)).size,
    newThisMonth: students.filter(student => String(student.enrollmentDate || '').startsWith(month)).length
  };
}
