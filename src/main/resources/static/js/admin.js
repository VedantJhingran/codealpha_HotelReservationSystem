const adminList = document.getElementById('adminRoomList');
let editingRoomId = null;

function adminRow(room) {
    return `<div class="admin-room-row"><div><h3>Room ${room.roomNumber}</h3><small>${room.category} · $${Number(room.nightlyRate).toFixed(2)} / night · ${room.capacity} guests</small></div><div class="d-flex gap-2"><a class="btn btn-sm btn-outline-secondary" href="/rooms/${room.id}">View</a><button class="btn btn-sm btn-outline-gold" data-edit-room="${room.id}">Edit</button><button class="btn btn-sm btn-outline-danger" data-delete-room="${room.id}">Remove</button></div></div>`;
}
async function loadAdminRooms() {
    try {
        const rooms = await Aurelia.request('/api/rooms');
        adminList.innerHTML = rooms.length ? rooms.map(adminRow).join('') : '<p class="lead-muted">No active rooms.</p>';
    }
    catch (error) { Aurelia.message('adminMessage', error.status === 401 ? 'Please sign in as an administrator.' : error.message); }
}
document.getElementById('roomForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const room = {
        roomNumber: document.getElementById('adminRoomNumber').value,
        category: document.getElementById('adminCategory').value,
        nightlyRate: Number(document.getElementById('adminRate').value),
        capacity: Number(document.getElementById('adminCapacity').value)
    };
    const isEditing = editingRoomId !== null;
    try {
        const endpoint = isEditing ? `/api/rooms/${editingRoomId}` : '/api/rooms';
        const method = isEditing ? 'PUT' : 'POST';
        await Aurelia.request(endpoint, {method, headers:{'Content-Type':'application/json'}, body:JSON.stringify(room)});
        resetRoomForm();
        Aurelia.message('adminMessage', isEditing ? 'Room updated.' : 'Room added to the collection.', 'success');
        await loadAdminRooms();
    } catch (error) { Aurelia.message('adminMessage', error.message || (isEditing ? 'Unable to update room.' : 'Unable to add room.')); }
});
adminList?.addEventListener('click', async event => {
    const editButton = event.target.closest('[data-edit-room]');
    if (editButton) {
        editButton.disabled = true;
        try {
            const room = await Aurelia.request(`/api/rooms/${editButton.dataset.editRoom}`);
            beginRoomEdit(room);
        } catch (error) {
            Aurelia.message('adminMessage', error.message || 'Unable to load room details.');
        } finally {
            editButton.disabled = false;
        }
        return;
    }
    const button = event.target.closest('[data-delete-room]');
    if (!button || !confirm('Remove this room from the active collection?')) return;
    try { await Aurelia.request(`/api/rooms/${button.dataset.deleteRoom}`, {method:'DELETE'}); await loadAdminRooms(); }
    catch (error) { Aurelia.message('adminMessage', error.message || 'Unable to remove room.'); }
});
function beginRoomEdit(room) {
    const formElements = getRoomFormElements();
    if (!formElements) return;
    editingRoomId = room.id;
    if (formElements.kicker) formElements.kicker.textContent = 'EDIT ROOM';
    if (formElements.title) formElements.title.textContent = `Edit room ${room.roomNumber}`;
    formElements.submit.textContent = 'Save changes';
    if (formElements.cancel) formElements.cancel.classList.remove('d-none');
    formElements.roomNumber.value = room.roomNumber;
    formElements.category.value = room.category;
    formElements.rate.value = room.nightlyRate;
    formElements.capacity.value = room.capacity;
    formElements.roomNumber.focus();
}
function resetRoomForm() {
    const formElements = getRoomFormElements();
    if (!formElements) return;
    editingRoomId = null;
    formElements.form.reset();
    if (formElements.kicker) formElements.kicker.textContent = 'NEW ROOM';
    if (formElements.title) formElements.title.textContent = 'Add a room';
    formElements.submit.textContent = 'Add to collection';
    if (formElements.cancel) formElements.cancel.classList.add('d-none');
}
function getRoomFormElements() {
    const form = document.getElementById('roomForm');
    const elements = {
        form,
        roomNumber: document.getElementById('adminRoomNumber'),
        category: document.getElementById('adminCategory'),
        rate: document.getElementById('adminRate'),
        capacity: document.getElementById('adminCapacity'),
        kicker: document.getElementById('roomFormKicker'),
        title: document.getElementById('roomFormTitle'),
        submit: document.getElementById('roomFormSubmit'),
        cancel: document.getElementById('roomFormCancel')
    };
    const required = ['form', 'roomNumber', 'category', 'rate', 'capacity', 'submit', 'cancel'];
    const missing = required.filter(name => !elements[name]);
    if (missing.length) {
        const error = new Error(`Admin room form is missing required element(s): ${missing.join(', ')}`);
        console.error(error);
        Aurelia.message('adminMessage', 'The admin room form is incomplete. Reload the page and try again.');
        return null;
    }
    return elements;
}
document.getElementById('roomFormCancel')?.addEventListener('click', resetRoomForm);
if (Aurelia.user()?.role !== 'ADMIN') Aurelia.message('adminMessage', 'This area is reserved for administrators.');
loadAdminRooms();
