const roomId = location.pathname.split('/').filter(Boolean).pop();
const detail = document.getElementById('roomDetails');
async function loadRoom() {
    try {
        const room = await Aurelia.request(`/api/rooms/${roomId}`);
        document.title = `Room ${room.roomNumber} | Aurelia Hotels`;
        document.getElementById('bookingRoomId').value = room.id;
        detail.innerHTML = `<div class="section-kicker">${room.category}</div><h1>Room ${room.roomNumber}.</h1><p class="lead-muted text-white-50">A considered space for up to ${room.capacity} guests, with every detail designed for ease.</p><div class="detail-meta"><span><i class="bi bi-people me-2"></i>${room.capacity} guests</span><span><i class="bi bi-moon-stars me-2"></i>$${Number(room.nightlyRate).toFixed(2)} / night</span></div>`;
    } catch (error) { detail.innerHTML = '<h1>Room unavailable.</h1><p>This room is no longer part of the collection.</p>'; }
}
document.getElementById('bookingForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    if (!Aurelia.user()) { location.href = `/login?redirect=/rooms/${roomId}`; return; }
    const form = event.currentTarget;
    const submit = form.querySelector('button[type="submit"]');
    if (submit) submit.disabled = true;
    try {
        const booking = await Aurelia.request('/api/bookings', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({roomId:Number(roomId), checkIn:document.getElementById('bookingCheckIn').value, checkOut:document.getElementById('bookingCheckOut').value})});
        const details = booking?.id
            ? `Booking #${booking.id} · Room ${booking.roomNumber || roomId} · ${booking.checkIn} to ${booking.checkOut}`
            : 'Your stay is confirmed. We look forward to welcoming you.';
        Aurelia.message('bookingMessage', `Booking Confirmed! ${details}`, 'success');
        if (form && typeof form.reset === 'function') form.reset();
    } catch (error) {
        Aurelia.message('bookingMessage', error.status === 401 ? 'Please sign in to reserve this room.' : error.message || 'Unable to reserve these dates.');
    } finally {
        if (submit) submit.disabled = false;
    }
});
loadRoom();
