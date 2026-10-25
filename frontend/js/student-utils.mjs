export function normalizeApiBaseUrl(value) {
  const base = String(value || 'http://localhost:8080/api').trim();
  return base.replace(/\/+$/, '');
}

export function defaultApiBaseUrl(location) {
  const localPreview = ['localhost', '127.0.0.1'].includes(location.hostname) && location.port === '5500';
  return localPreview ? 'http://localhost:8080/api' : `${String(location.origin).replace(/\/+$/, '')}/api`;
}

export class SessionEpoch {
  #value = 0;

  capture() { return this.#value; }
  invalidate() { this.#value += 1; }
  isCurrent(value) { return value === this.#value; }
}

export function buildStudentsUrl(baseUrl, query = '') {
  const base = normalizeApiBaseUrl(baseUrl);
  return `${base}/students${query ? `?q=${encodeURIComponent(query)}` : ''}`;
}

export function getInitials(fullName) {
  return String(fullName || '').trim().split(/\s+/).filter(Boolean)
    .slice(0, 2).map(part => part[0].toLocaleUpperCase()).join('');
}

export function filterAndSortStudents(students, course = '', sortOrder = 'newest') {
  return students.filter(student => !course || student.course === course).slice().sort((a, b) => {
    if (sortOrder === 'name') return a.fullName.localeCompare(b.fullName, undefined, { sensitivity: 'base' });
    if (sortOrder === 'course') return a.course.localeCompare(b.course, undefined, { sensitivity: 'base' }) || a.fullName.localeCompare(b.fullName);
    return sortOrder === 'oldest' ? a.id - b.id : b.id - a.id;
  });
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
