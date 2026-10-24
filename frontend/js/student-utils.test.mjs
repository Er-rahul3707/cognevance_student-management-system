import test from 'node:test';
import assert from 'node:assert/strict';
import { buildStudentsUrl, escapeHtml, filterAndSortStudents, formatDate, getDashboardStats, getInitials, normalizeApiBaseUrl, SessionEpoch } from './student-utils.mjs';

test('normalizes API base URLs and encodes search terms', () => {
  assert.equal(normalizeApiBaseUrl(' https://api.example.test/v1/// '), 'https://api.example.test/v1');
  assert.equal(buildStudentsUrl('http://localhost:8080/api/', 'Asha Patel & Co'),
    'http://localhost:8080/api/students?q=Asha%20Patel%20%26%20Co');
  assert.equal(buildStudentsUrl('http://localhost:8080/api'), 'http://localhost:8080/api/students');
});

test('invalidating a session prevents pending work from exposing prior-session data', () => {
  const session = new SessionEpoch();
  const pendingRequest = session.capture();
  assert.equal(session.isCurrent(pendingRequest), true);

  session.invalidate();
  assert.equal(session.isCurrent(pendingRequest), false);

  const newSessionRequest = session.capture();
  assert.equal(session.isCurrent(newSessionRequest), true);
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

test('filters students by course and sorts names without mutating source data', () => {
  const source = [
    { id: 2, fullName: 'Zara Noor', course: 'Design' },
    { id: 1, fullName: 'Asha Patel', course: 'Computer Science' },
    { id: 3, fullName: 'Bina Das', course: 'Computer Science' }
  ];
  assert.deepEqual(filterAndSortStudents(source, 'Computer Science', 'name').map(student => student.id), [1, 3]);
  assert.deepEqual(source.map(student => student.id), [2, 1, 3]);
});

test('supports newest and oldest student ordering', () => {
  const students = [{ id: 1, course: 'A', fullName: 'One' }, { id: 3, course: 'A', fullName: 'Three' }];
  assert.deepEqual(filterAndSortStudents(students).map(student => student.id), [3, 1]);
  assert.deepEqual(filterAndSortStudents(students, '', 'oldest').map(student => student.id), [1, 3]);
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
