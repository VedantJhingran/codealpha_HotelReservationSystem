const bookingList = document.getElementById('bookingList');
async function loadBookings() {
    try {
        const bookings = await Aurelia.request('/api/bookings');
        if (!bookings.length) { bookingList.innerHTML = '<div class="empty-state text-center py-5"><i class="bi bi-calendar-heart display-5 text-gold"></i><h3 class="h2-serif mt-3">Nothing booked yet.</h3><p class="lead-muted">Your next beautiful stay is waiting.</p><a href="/rooms" class="btn btn-gold mt-3">Explore rooms</a></div>'; return; }
        bookingList.innerHTML = bookings.map(booking => `<article class="booking-item"><div><div class="status-badge">${booking.status}</div><h3>Room ${booking.roomNumber}</h3><div class="booking-dates">${booking.checkIn} <span class="mx-2">—</span> ${booking.checkOut}</div></div>${booking.status === 'CONFIRMED' ? `<button class="btn btn-outline-danger btn-sm" data-cancel="${booking.id}">Cancel booking</button>` : ''}</article>`).join('');
    } catch (error) { Aurelia.message('bookingsMessage', error.status === 401 ? 'Please sign in to see your bookings.' : error.message || 'Unable to load bookings.'); bookingList.innerHTML = '<div class="text-center"><a href="/login" class="btn btn-gold">Sign in</a></div>'; }
}
bookingList?.addEventListener('click', async event => {
    const button = event.target.closest('[data-cancel]');
    if (!button || !confirm('Cancel this reservation?')) return;
    try { await Aurelia.request(`/api/bookings/${button.dataset.cancel}/cancel`, {method:'POST'}); loadBookings(); }
    catch (error) { Aurelia.message('bookingsMessage', error.message || 'Unable to cancel booking.'); }
});
loadBookings();
