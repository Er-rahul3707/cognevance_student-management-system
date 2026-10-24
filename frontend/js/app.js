import { buildStudentsUrl, escapeHtml, formatDate, getDashboardStats, getInitials, normalizeApiBaseUrl } from './student-utils.mjs';

const API_BASE_URL = normalizeApiBaseUrl(window.STUDENT_API_BASE_URL);
const rows = document.getElementById('studentRows');
const dialog = document.getElementById('studentDialog');
const detailsDialog = document.getElementById('detailsDialog');
const form = document.getElementById('studentForm');
let students = [];
let searchTimer;
let latestLoad = 0;

document.getElementById('today').textContent = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date());
document.getElementById('addStudent').addEventListener('click', () => openForm());
document.getElementById('closeDialog').addEventListener('click', closeForm);
document.getElementById('cancelDialog').addEventListener('click', closeForm);
document.getElementById('refreshButton').addEventListener('click', () => loadStudents(document.getElementById('searchInput').value.trim()));
document.getElementById('searchInput').addEventListener('input', () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadStudents(document.getElementById('searchInput').value.trim()), 250);
});
dialog.addEventListener('click', event => { if (event.target === dialog) closeForm(); });
document.getElementById('closeDetails').addEventListener('click', () => detailsDialog.close());
document.getElementById('closeDetailsFooter').addEventListener('click', () => detailsDialog.close());
detailsDialog.addEventListener('click', event => { if (event.target === detailsDialog) detailsDialog.close(); });
form.addEventListener('submit', saveStudent);
loadStudents();

async function loadStudents(query = '') {
  const requestId = ++latestLoad;
  rows.innerHTML = '<tr><td colspan="6" class="loading-cell">Loading students…</td></tr>';
  rows.setAttribute('aria-busy', 'true');
  try {
    const [response, allResponse] = await Promise.all([
      fetch(buildStudentsUrl(API_BASE_URL, query)),
      query ? fetch(buildStudentsUrl(API_BASE_URL)) : null
    ]);
    if (requestId !== latestLoad) return;
    if (!response.ok) throw await apiError(response);
    const visibleStudents = await response.json();
    if (allResponse) {
      if (!allResponse.ok) throw await apiError(allResponse);
      students = await allResponse.json();
    } else students = visibleStudents;
    renderStudents(visibleStudents);
  } catch (error) {
    if (requestId !== latestLoad) return;
    rows.innerHTML = `<tr><td colspan="6" class="empty-cell"><strong>Could not load students</strong><span>${escapeHtml(error.message)}. Check the API and database connection, then refresh.</span></td></tr>`;
  } finally {
    if (requestId === latestLoad) rows.setAttribute('aria-busy', 'false');
  }
}

function renderStudents(list) {
  document.getElementById('resultCount').textContent = list.length;
  document.getElementById('tableSummary').textContent = `Showing ${list.length} ${list.length === 1 ? 'student' : 'students'}`;
  updateStats(students);
  if (!list.length) {
    const searched = document.getElementById('searchInput').value.trim();
    rows.innerHTML = `<tr><td colspan="6" class="empty-cell"><strong>${searched ? 'No matching students' : 'No students yet'}</strong><span>${searched ? 'Try another name, email, course or student ID.' : 'Add a student to start building your directory.'}</span></td></tr>`;
    return;
  }
  rows.innerHTML = list.map(student => {
    const initials = getInitials(student.fullName);
    return `<tr><td><div class="student-cell"><span class="student-avatar">${escapeHtml(initials)}</span><span><span class="student-name">${escapeHtml(student.fullName)}</span><span class="student-email">${escapeHtml(student.email)}</span></span></div></td><td><span class="id-chip">STU-${String(student.id).padStart(4, '0')}</span></td><td><span class="course-label">${escapeHtml(student.course)}</span></td><td>${escapeHtml(student.phone || '—')}</td><td>${formatDate(student.enrollmentDate)}</td><td><div class="row-actions"><button class="row-button" data-action="view" data-id="${student.id}" aria-label="View ${escapeHtml(student.fullName)}">View</button><button class="row-button" data-action="edit" data-id="${student.id}" aria-label="Edit ${escapeHtml(student.fullName)}">Edit</button><button class="row-button delete" data-action="delete" data-id="${student.id}" aria-label="Delete ${escapeHtml(student.fullName)}">Delete</button></div></td></tr>`;
  }).join('');
  rows.querySelectorAll('button[data-action]').forEach(button => button.addEventListener('click', () => actOnStudent(button.dataset.action, Number(button.dataset.id))));
}

function updateStats(list) {
  const stats = getDashboardStats(list);
  document.getElementById('totalCount').textContent = stats.total;
  document.getElementById('courseCount').textContent = stats.courses;
  document.getElementById('newCount').textContent = stats.newThisMonth;
}

async function actOnStudent(action, id) {
  try {
    const response = await fetch(`${API_BASE_URL}/students/${id}`);
    if (!response.ok) throw await apiError(response);
    const student = await response.json();
    if (action === 'view') {
      showDetails(student);
    } else if (action === 'edit') openForm(student);
    else if (action === 'delete' && window.confirm(`Delete ${student.fullName}? This action cannot be undone.`)) {
      const deletion = await fetch(`${API_BASE_URL}/students/${id}`, { method: 'DELETE' });
      if (!deletion.ok) throw await apiError(deletion);
      toast('Student deleted');
      await loadStudents(document.getElementById('searchInput').value.trim());
    }
  } catch (error) { toast(error.message, true); }
}

function showDetails(student) {
  const values = {
    detailId: `STU-${String(student.id).padStart(4, '0')}`,
    detailName: student.fullName,
    detailEmail: student.email,
    detailPhone: student.phone || 'Not provided',
    detailCourse: student.course,
    detailEnrollmentDate: formatDate(student.enrollmentDate),
    detailCreatedAt: student.createdAt ? new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(student.createdAt)) : '—'
  };
  for (const [id, value] of Object.entries(values)) document.getElementById(id).textContent = value;
  detailsDialog.showModal();
  document.getElementById('closeDetails').focus();
}

function openForm(student) {
  form.reset();
  document.getElementById('formError').textContent = '';
  document.getElementById('studentId').value = student?.id || '';
  document.getElementById('dialogTitle').textContent = student ? 'Edit student' : 'Add student';
  document.getElementById('saveButton').textContent = student ? 'Save changes' : 'Save student';
  if (student) for (const field of ['fullName', 'email', 'phone', 'course', 'enrollmentDate']) document.getElementById(field).value = student[field] || '';
  else {
    const localToday = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    document.getElementById('enrollmentDate').value = localToday;
  }
  dialog.showModal();
  document.getElementById('fullName').focus();
}
function closeForm() { dialog.close(); }

async function saveStudent(event) {
  event.preventDefault();
  const id = document.getElementById('studentId').value;
  const payload = Object.fromEntries(new FormData(form).entries());
  delete payload.studentId;
  payload.phone = payload.phone.trim();
  const button = document.getElementById('saveButton');
  button.disabled = true; button.textContent = 'Saving…';
  try {
    const response = await fetch(`${API_BASE_URL}/students${id ? `/${id}` : ''}`, {
      method: id ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });
    if (!response.ok) throw await apiError(response);
    closeForm(); toast(id ? 'Student updated' : 'Student added');
    await loadStudents(document.getElementById('searchInput').value.trim());
  } catch (error) { document.getElementById('formError').textContent = error.message; }
  finally { button.disabled = false; button.textContent = id ? 'Save changes' : 'Save student'; }
}

async function apiError(response) {
  let body;
  try { body = await response.json(); } catch { return new Error(`Request failed (${response.status})`); }
  const fields = body.fieldErrors ? Object.entries(body.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(' · ') : '';
  return new Error(fields || body.message || `Request failed (${response.status})`);
}
function toast(message, isError = false) {
  const element = document.createElement('div'); element.className = `toast${isError ? ' error' : ''}`;
  element.textContent = message; document.getElementById('toastRegion').append(element);
  setTimeout(() => element.remove(), 4200);
}
