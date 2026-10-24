import test from 'node:test';
import assert from 'node:assert/strict';
import { buildStudentsUrl, escapeHtml, formatDate, getDashboardStats, getInitials, normalizeApiBaseUrl } from './student-utils.mjs';

test('normalizes API base URLs and encodes search terms', () => {
  assert.equal(normalizeApiBaseUrl(' https://api.example.test/v1/// '), 'https://api.example.test/v1');
  assert.equal(buildStudentsUrl('http://localhost:8080/api/', 'Asha Patel & Co'),
    'http://localhost:8080/api/students?q=Asha%20Patel%20%26%20Co');
  assert.equal(buildStudentsUrl('http://localhost:8080/api'), 'http://localhost:8080/api/students');
});

test('escapes HTML-sensitive characters before inserting student values into markup', () => {
  assert.equal(escapeHtml(`<script title="x">&'`), '&lt;script title=&quot;x&quot;&gt;&amp;&#39;');
  assert.equal(escapeHtml(null), '');
});

test('builds initials from the first and last-name positions shown in the table', () => {
  assert.equal(getInitials('  Asha   Patel  '), 'AP');
  assert.equal(getInitials('Cher'), 'C');
  assert.equal(getInitials(''), '');
});

test('calculates dashboard totals and current-month enrollments', () => {
  const now = new Date(2026, 9, 24);
  const result = getDashboardStats([
    { course: 'Computer Science', enrollmentDate: '2026-10-01' },
    { course: ' computer science ', enrollmentDate: '2026-09-30' },
    { course: 'Design', enrollmentDate: '2026-10-24' }
  ], now);
  assert.deepEqual(result, { total: 3, courses: 2, newThisMonth: 2 });
});

test('formats absent or malformed dates safely', () => {
  assert.equal(formatDate(''), '—');
  assert.equal(formatDate('not-a-date'), '—');
});
