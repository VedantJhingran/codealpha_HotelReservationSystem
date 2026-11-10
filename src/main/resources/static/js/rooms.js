const roomGrid = document.getElementById('roomGrid');
function roomCard(room) {
    return `<div class="col-md-6 col-xl-4"><article class="room-card"><div class="room-image"></div><div class="room-body"><div class="room-category">${room.category}</div><h3>Room ${room.roomNumber}</h3><p>Designed for restful nights and unhurried mornings. Sleeps up to ${room.capacity} guest${room.capacity === 1 ? '' : 's'}.</p><div class="d-flex justify-content-between align-items-center mt-4"><span class="room-rate">$${Number(room.nightlyRate).toFixed(2)} <small>/ night</small></span><a class="text-link" href="/rooms/${room.id}">View room <i class="bi bi-arrow-up-right"></i></a></div></div></article></div>`;
}
async function loadRooms(category = '') {
    try {
        const rooms = await Aurelia.request(`/api/rooms${category ? `?category=${category}` : ''}`);
        roomGrid.innerHTML = rooms.length ? rooms.map(roomCard).join('') : '<div class="col-12"><div class="empty-state text-center py-5"><h3>No rooms found</h3><p class="lead-muted">Try another category.</p></div></div>';
    } catch (error) { Aurelia.message('roomsMessage', error.message || 'Unable to load rooms.'); }
}
document.querySelectorAll('#categoryFilters button').forEach(button => button.addEventListener('click', () => {
    document.querySelectorAll('#categoryFilters button').forEach(item => item.classList.remove('active'));
    button.classList.add('active'); loadRooms(button.dataset.category);
}));
loadRooms();
