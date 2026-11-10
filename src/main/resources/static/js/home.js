document.getElementById('availabilityForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const checkIn = document.getElementById('homeCheckIn').value, checkOut = document.getElementById('homeCheckOut').value;
    try {
        const rooms = await Aurelia.request('/api/rooms/availability', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({checkIn, checkOut, guests:Number(document.getElementById('homeGuests').value)})});
        sessionStorage.setItem('aureliaAvailability', JSON.stringify({checkIn, checkOut}));
        location.href = `/rooms?checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}&available=${rooms.length}`;
    } catch (error) { Aurelia.message('availabilityMessage', error.message || 'Please choose valid dates.'); }
});
