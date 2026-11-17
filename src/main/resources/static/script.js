/**
 * CineBook - Cinema Booking & Payment System
 * Frontend Controller & API Integration
 */

// Application State
const state = {
    currentUser: null,
    movies: [],
    selectedMovie: null,
    movieShows: [],
    selectedDate: null,
    selectedShow: null,
    screenSeats: [],
    selectedSeats: new Map(), // seatId -> { seatNumber, seatType, price }
    currentBooking: null,
    selectedPaymentMethod: "UPI",
    selectedBank: "State Bank of India",
    pendingPaymentPayload: null
};

// DOM Elements
const movieGrid = document.getElementById("movieGrid");
const searchInput = document.getElementById("searchInput");
const genrePills = document.querySelectorAll(".genre-pill");
const globalStatusMsg = document.getElementById("globalStatusMsg");

// Modals
const showtimeModal = document.getElementById("showtimeModal");
const seatModal = document.getElementById("seatModal");
const paymentModal = document.getElementById("paymentModal");
const otpModal = document.getElementById("otpModal");
const ticketModal = document.getElementById("ticketModal");
const myBookingsModal = document.getElementById("myBookingsModal");
const processingOverlay = document.getElementById("processingOverlay");

// Close buttons
document.getElementById("closeShowtimeModal").onclick = () => closeModal(showtimeModal);
document.getElementById("closeSeatModal").onclick = () => closeModal(seatModal);
document.getElementById("closePaymentModal").onclick = () => closeModal(paymentModal);
document.getElementById("closeTicketModal").onclick = () => closeModal(ticketModal);
document.getElementById("closeMyBookingsModal").onclick = () => closeModal(myBookingsModal);

// ========================================================
// INITIALIZATION
// ========================================================
document.addEventListener("DOMContentLoaded", async () => {
    await refreshCsrfToken();
    await loadCurrentUser();
    await loadMovies();
    setupEventListeners();
    updateBookingBadge();
    const pendingMovieId = new URLSearchParams(window.location.search).get("movieId");
    if (pendingMovieId && state.currentUser) {
        const movieId = Number(pendingMovieId);
        setTimeout(() => window.openShowtimeModal(movieId), 250);
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

async function loadCurrentUser() {
    try {
        const response = await fetch("/auth/me", { credentials: "same-origin" });
        if (!response.ok) throw new Error("Guest session");
        state.currentUser = await response.json();
        document.getElementById("currentUserName").textContent = state.currentUser.name;
        document.getElementById("currentUserRole").textContent = "Customer account";
        document.getElementById("loginNavLink").hidden = true;
        document.getElementById("logoutNavButton").hidden = false;
    } catch (_) {
        state.currentUser = null;
        document.getElementById("currentUserName").textContent = "Guest";
        document.getElementById("currentUserRole").textContent = "Sign in to book tickets";
        document.getElementById("loginNavLink").hidden = false;
        document.getElementById("logoutNavButton").hidden = true;
    }
}

function setupEventListeners() {
    // Search input
    searchInput.addEventListener("input", filterMovies);

    // Genre filter pills
    genrePills.forEach(pill => {
        pill.addEventListener("click", () => {
            genrePills.forEach(p => p.classList.remove("active"));
            pill.classList.add("active");
            filterMovies();
        });
    });

    // Navigation buttons
    document.getElementById("openMyBookingsBtn").addEventListener("click", openMyBookings);
    document.getElementById("logoutNavButton").addEventListener("click", async () => {
        try { await cineBookFetch("/auth/logout", { method: "POST" }); } catch (e) { console.warn(e); }
        state.currentUser = null;
        window.location.reload();
    });

    // Seat proceed button
    document.getElementById("proceedToPayBtn").addEventListener("click", proceedToPaymentGate);

    // Payment method tabs
    const payTabs = document.querySelectorAll(".pay-tab");
    payTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            payTabs.forEach(t => t.classList.remove("active"));
            document.querySelectorAll(".pay-tab-content").forEach(c => c.classList.remove("active"));

            tab.classList.add("active");
            const tabName = tab.dataset.tab;

            if (tabName === "upi") {
                state.selectedPaymentMethod = "UPI";
                document.getElementById("tabContentUpi").classList.add("active");
            } else if (tabName === "card") {
                state.selectedPaymentMethod = "CARD";
                document.getElementById("tabContentCard").classList.add("active");
            } else if (tabName === "netbanking") {
                state.selectedPaymentMethod = "NET_BANKING";
                document.getElementById("tabContentNetbanking").classList.add("active");
            } else if (tabName === "wallet") {
                state.selectedPaymentMethod = "WALLET";
                document.getElementById("tabContentWallet").classList.add("active");
            }
        });
    });

    // Bank buttons in Net Banking
    const bankBtns = document.querySelectorAll(".bank-btn");
    bankBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            bankBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            state.selectedBank = btn.dataset.bank;
        });
    });

    // Auto-fill presets
    document.getElementById("useFastUpiPreset").addEventListener("click", () => {
        document.getElementById("virtualUpiInput").value = "dhiraj@okaxis";
    });

    document.getElementById("fillDemoCardBtn").addEventListener("click", fillDemoCard);

    // Interactive Card Preview Inputs
    const cardNumInput = document.getElementById("cardNumInput");
    const cardHolderInput = document.getElementById("cardHolderInput");
    const cardExpiryInput = document.getElementById("cardExpiryInput");

    cardNumInput.addEventListener("input", (e) => {
        let val = e.target.value.replace(/\D/g, '').substring(0, 16);
        let formatted = val.match(/.{1,4}/g)?.join(' ') || val;
        e.target.value = formatted;
        document.getElementById("cardPreviewNumber").textContent = formatted || "•••• •••• •••• 4242";
    });

    cardHolderInput.addEventListener("input", (e) => {
        document.getElementById("cardPreviewHolder").textContent = (e.target.value || "DHIRAJ CHAUHAN").toUpperCase();
    });

    cardExpiryInput.addEventListener("input", (e) => {
        let val = e.target.value.replace(/\D/g, '').substring(0, 4);
        if (val.length >= 3) {
            val = val.substring(0, 2) + '/' + val.substring(2);
        }
        e.target.value = val;
        document.getElementById("cardPreviewExpiry").textContent = val || "12/28";
    });

    // Pay Confirmation Button
    document.getElementById("confirmVirtualPayBtn").addEventListener("click", initiatePaymentFlow);

    // OTP Modal buttons
    document.getElementById("autoFillOtpBtn").addEventListener("click", () => {
        document.getElementById("otpCodeInput").value = "123456";
    });

    document.getElementById("cancelOtpBtn").addEventListener("click", () => {
        closeModal(otpModal);
    });

    document.getElementById("submitOtpBtn").addEventListener("click", () => {
        closeModal(otpModal);
        executePaymentRequest(state.pendingPaymentPayload);
    });

    // Ticket Actions
    document.getElementById("printTicketBtn").addEventListener("click", () => {
        // Temporarily reset body overflow to guarantee zero blank pages in browser print preview
        const originalOverflow = document.body.style.overflow;
        document.body.style.overflow = "visible";
        document.body.classList.add("printing-ticket-active");
        
        // Trigger clean print dialog
        window.print();
        
        // Restore standard scroll state after print dialog opens
        setTimeout(() => {
            document.body.style.overflow = originalOverflow || "hidden";
            document.body.classList.remove("printing-ticket-active");
        }, 300);
    });

    document.getElementById("ticketViewAllBookingsBtn").addEventListener("click", () => {
        closeModal(ticketModal);
        openMyBookings();
    });
}

function fillDemoCard() {
    document.getElementById("cardNumInput").value = "4242 4242 4242 4242";
    document.getElementById("cardHolderInput").value = "Demo Customer";
    document.getElementById("cardExpiryInput").value = "12/28";
    document.getElementById("cardCvvInput").value = "123";
    document.getElementById("cardPreviewNumber").textContent = "4242 4242 4242 4242";
    document.getElementById("cardPreviewHolder").textContent = "DEMO CUSTOMER";
    document.getElementById("cardPreviewExpiry").textContent = "12/28";
}

// ========================================================
// MOVIES LOADING & RENDERING
// ========================================================
async function loadMovies() {
    try {
        const response = await fetch("/movies");
        if (!response.ok) throw new Error("Backend offline");
        const data = await response.json();

        if (Array.isArray(data) && data.length > 0) {
            state.movies = data;
            renderMovies(state.movies);
            return;
        }
    } catch (err) {
        console.warn("Using sample catalog as fallback:", err);
    }

    state.movies = [
        { id: 1, title: "Interstellar", genre: "Sci-Fi / Adventure", duration: 169, rating: 8.7, posterUrl: "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg" },
        { id: 2, title: "Inception", genre: "Action / Sci-Fi", duration: 148, rating: 8.8, posterUrl: "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg" },
        { id: 3, title: "The Dark Knight", genre: "Action / Crime", duration: 152, rating: 9.0, posterUrl: "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg" },
        { id: 4, title: "Avatar: The Way of Water", genre: "Adventure / Sci-Fi", duration: 192, rating: 7.8, posterUrl: "https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg" },
        { id: 5, title: "Oppenheimer", genre: "Biography / Drama", duration: 180, rating: 8.9, posterUrl: "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg" },
        { id: 6, title: "Avengers: Endgame", genre: "Action / Sci-Fi", duration: 181, rating: 8.4, posterUrl: "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg" }
    ];
    renderMovies(state.movies);
}

function renderMovies(movieList) {
    movieGrid.innerHTML = "";

    if (movieList.length === 0) {
        movieGrid.innerHTML = `
            <div class="loading-state">
                <p>No movies found matching your search.</p>
            </div>`;
        return;
    }

    movieList.forEach(movie => {
        const posterSrc = movie.posterUrl || movie.poster || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80";

        const card = document.createElement("article");
        card.className = "movie-card";
        card.innerHTML = `
            <div class="card-poster-wrap">
                <img src="${posterSrc}" alt="${movie.title}" class="movie-poster" loading="lazy" onerror="this.src='https://placehold.co/500x750/1a1a24/ffffff?text=${encodeURIComponent(movie.title)}'">
                <span class="card-rating-badge">★ ${movie.rating ?? '8.5'}</span>
            </div>
            <div class="movie-info">
                <h3>${movie.title}</h3>
                <p class="movie-meta">${movie.genre || 'Action / Drama'} • ${movie.duration || '150'} min</p>
                <button class="book-btn" onclick="openShowtimeModal(${movie.id})">Book Tickets</button>
            </div>
        `;
        movieGrid.appendChild(card);
    });
}

function filterMovies() {
    const query = searchInput.value.trim().toLowerCase();
    const activeGenrePill = document.querySelector(".genre-pill.active");
    const activeGenre = activeGenrePill ? activeGenrePill.dataset.genre : "ALL";

    const filtered = state.movies.filter(movie => {
        const matchesQuery = (movie.title || "").toLowerCase().includes(query) ||
                             (movie.genre || "").toLowerCase().includes(query);
        const matchesGenre = activeGenre === "ALL" || (movie.genre || "").toLowerCase().includes(activeGenre.toLowerCase());
        return matchesQuery && matchesGenre;
    });

    renderMovies(filtered);
}

// ========================================================
// STEP 1: SHOWTIMES MODAL
// ========================================================
window.openShowtimeModal = async function(movieId) {
    if (!state.currentUser) {
        const query = new URLSearchParams({ redirect: "index.html", movieId: String(movieId) });
        window.location.href = "login.html?" + query.toString();
        return;
    }
    const movie = state.movies.find(m => m.id === movieId);
    if (!movie) return;

    state.selectedMovie = movie;
    state.selectedShow = null;
    state.selectedSeats.clear();

    document.getElementById("showtimeMovieTitle").textContent = movie.title;
    document.getElementById("showtimeMovieMeta").textContent = `${movie.genre} • ${movie.duration} min • ★ ${movie.rating}`;

    openModal(showtimeModal);

    const showsListContainer = document.getElementById("showsListContainer");
    showsListContainer.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Fetching scheduled showtimes...</p>
        </div>`;

    try {
        const response = await fetch(`/shows/movie/${movieId}`);
        if (!response.ok) throw new Error("Could not load shows");
        const shows = await response.json();

        if (Array.isArray(shows) && shows.length > 0) {
            state.movieShows = shows;
            renderShowDatePills(shows);
            return;
        }
    } catch (err) {
        console.warn("Could not load backend shows:", err);
    }

    // Default message if no upcoming shows returned
    document.getElementById("showDatePills").innerHTML = "";
    showsListContainer.innerHTML = `<p class="loading-state">No upcoming showtimes currently scheduled for this movie.</p>`;
};

function renderShowDatePills(shows) {
    // Only upcoming dates
    const todayStr = new Date().toISOString().split('T')[0];
    const datesSet = new Set(shows.map(s => s.showDate).filter(d => d >= todayStr));
    const dates = Array.from(datesSet).sort();
    const datePillsRow = document.getElementById("showDatePills");
    datePillsRow.innerHTML = "";

    if (dates.length === 0) {
        document.getElementById("showsListContainer").innerHTML = `<p class="loading-state">No upcoming shows found for this movie.</p>`;
        return;
    }

    dates.forEach((dateStr, idx) => {
        const d = new Date(dateStr + "T00:00:00");
        const dayName = d.toLocaleDateString('en-US', { weekday: 'short' });
        const monthDay = d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

        const pill = document.createElement("button");
        pill.className = `date-pill ${idx === 0 ? 'active' : ''}`;
        pill.innerHTML = `<strong>${dayName}</strong><span>${monthDay}</span>`;
        pill.onclick = () => {
            document.querySelectorAll(".date-pill").forEach(p => p.classList.remove("active"));
            pill.classList.add("active");
            state.selectedDate = dateStr;
            renderShowsForDate(dateStr);
        };
        datePillsRow.appendChild(pill);
    });

    state.selectedDate = dates[0];
    renderShowsForDate(dates[0]);
}

function renderShowsForDate(dateStr) {
    const container = document.getElementById("showsListContainer");
    const matchingShows = state.movieShows.filter(s => s.showDate === dateStr);

    if (matchingShows.length === 0) {
        container.innerHTML = `<p class="loading-state">No shows available for this date.</p>`;
        return;
    }

    container.innerHTML = "";
    matchingShows.forEach(show => {
        const screenName = show.screen?.name || "Screen 1";
        const theatreName = show.screen?.theatre?.name || "CineBook Multiplex";
        const formattedTime = formatTime(show.showTime);

        const card = document.createElement("div");
        card.className = "show-item-card";
        card.innerHTML = `
            <div>
                <div class="show-screen-name">${screenName}</div>
                <div class="show-theatre-name">${theatreName}</div>
            </div>
            <button class="show-timing-btn" onclick="openSeatSelectionModal(${show.id})">
                <span>${formattedTime}</span>
                <span class="show-price-tag">₹${show.ticketPrice}</span>
            </button>
        `;
        container.appendChild(card);
    });
}

// ========================================================
// STEP 2: SEAT SELECTION MODAL
// ========================================================
window.openSeatSelectionModal = async function(showId) {
    const show = state.movieShows.find(s => s.id === showId);
    if (!show) return;

    state.selectedShow = show;
    state.selectedSeats.clear();
    updateSeatSummary();

    closeModal(showtimeModal);
    openModal(seatModal);

    document.getElementById("seatModalTitle").textContent = `${state.selectedMovie.title} - Select Seats`;
    document.getElementById("seatModalShowInfo").textContent =
        `${show.screen?.name || 'Screen 1'} • ${show.showDate} at ${formatTime(show.showTime)} • ₹${show.ticketPrice} / ticket`;

    const gridContainer = document.getElementById("seatGridContainer");
    gridContainer.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading real-time seat availability...</p>
        </div>`;

    try {
        const response = await fetch(`/shows/${showId}/seats`);
        if (!response.ok) throw new Error("Seat fetch failed");
        const seats = await response.json();

        if (Array.isArray(seats) && seats.length > 0) {
            state.screenSeats = seats;
            renderSeatGrid(seats);
            return;
        } else {
            gridContainer.innerHTML = `<p class="loading-state">No seats configured for this screen. Please select another show.</p>`;
        }
    } catch (err) {
        console.error("Could not load seats:", err);
        gridContainer.innerHTML = `<p class="loading-state">Could not connect to seat server. Please try again.</p>`;
    }
};

function renderSeatGrid(seats) {
    const gridContainer = document.getElementById("seatGridContainer");
    gridContainer.innerHTML = "";

    // Group seats by Row letter (e.g. "A", "B", "C", "D")
    const rowsMap = new Map();
    seats.forEach(seat => {
        const rowLetter = seat.seatNumber.charAt(0);
        if (!rowsMap.has(rowLetter)) {
            rowsMap.set(rowLetter, []);
        }
        rowsMap.get(rowLetter).push(seat);
    });

    rowsMap.forEach((rowSeats, rowLetter) => {
        const rowDiv = document.createElement("div");
        rowDiv.className = "seat-row";

        const label = document.createElement("span");
        label.className = "seat-row-label";
        label.textContent = rowLetter;

        const seatsWrap = document.createElement("div");
        seatsWrap.className = "seat-row-seats";

        rowSeats.forEach(seat => {
            const btn = document.createElement("button");
            btn.className = `seat-btn ${seat.seatType === 'VIP' ? 'seat-vip' : (seat.seatType === 'PREMIUM' ? 'seat-premium' : '')}`;
            if (!seat.available) {
                btn.classList.add("booked");
                btn.disabled = true;
            }
            btn.textContent = seat.seatNumber;
            btn.title = `${seat.seatNumber} (${seat.seatType || 'Standard'}) - ${seat.available ? 'Available' : 'Booked'}`;

            btn.onclick = () => toggleSeatSelection(seat, btn);
            seatsWrap.appendChild(btn);
        });

        rowDiv.appendChild(label);
        rowDiv.appendChild(seatsWrap);
        gridContainer.appendChild(rowDiv);
    });
}

function toggleSeatSelection(seat, btnElement) {
    if (!seat.available) return;

    if (state.selectedSeats.has(seat.seatId)) {
        state.selectedSeats.delete(seat.seatId);
        btnElement.classList.remove("selected");
    } else {
        if (state.selectedSeats.size >= 8) {
            alert("You can select up to 8 seats per booking.");
            return;
        }
        state.selectedSeats.set(seat.seatId, {
            seatId: seat.seatId,
            seatNumber: seat.seatNumber,
            seatType: seat.seatType
        });
        btnElement.classList.add("selected");
    }

    updateSeatSummary();
}

function updateSeatSummary() {
    const count = state.selectedSeats.size;
    const pricePerTicket = state.selectedShow?.ticketPrice || 250;
    const total = count * pricePerTicket;

    const seatsArray = Array.from(state.selectedSeats.values()).map(s => s.seatNumber);
    document.getElementById("selectedSeatsLabel").textContent = seatsArray.length > 0 ? seatsArray.join(", ") : "None";
    document.getElementById("ticketPriceLabel").textContent = `₹${pricePerTicket}`;
    document.getElementById("ticketCountLabel").textContent = count;
    document.getElementById("totalAmountLabel").textContent = `₹${total.toFixed(2)}`;

    const proceedBtn = document.getElementById("proceedToPayBtn");
    proceedBtn.disabled = count === 0;
}

// ========================================================
// STEP 3: PAYMENT GATEWAY (REAL RESERVATION CALL)
// ========================================================
async function proceedToPaymentGate() {
    if (state.selectedSeats.size === 0) return;

    const seatIds = Array.from(state.selectedSeats.keys());
    const seatsArray = Array.from(state.selectedSeats.values()).map(s => s.seatNumber);
    const totalAmount = state.selectedSeats.size * state.selectedShow.ticketPrice;

    const proceedBtn = document.getElementById("proceedToPayBtn");
    proceedBtn.disabled = true;
    proceedBtn.textContent = "Reserving Seats...";

    try {
        const payload = {
            showId: state.selectedShow.id,
            seatIds: seatIds
        };

        const response = await cineBookFetch("/bookings/with-seats", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            const errorMsg = data.error || data.message || "Could not reserve selected seats. Please choose another seat.";
            alert("Booking Notice: " + errorMsg);
            proceedBtn.disabled = false;
            proceedBtn.textContent = "Proceed to Payment →";
            return;
        }

        // Successfully created booking
        state.currentBooking = {
            id: data.bookingId || data.id,
            totalAmount: data.totalAmount || totalAmount,
            status: data.status || "PENDING"
        };

        // Populate Payment Gateway Modal
        document.getElementById("payModalAmount").textContent = `₹${totalAmount.toFixed(2)}`;
        document.getElementById("payBtnAmount").textContent = `₹${totalAmount.toFixed(2)}`;
        document.getElementById("paySummaryMovie").textContent = state.selectedMovie.title;
        document.getElementById("paySummaryShow").textContent =
            `${state.selectedShow.screen?.name || 'Screen 1'} • ${state.selectedShow.showDate} at ${formatTime(state.selectedShow.showTime)}`;
        document.getElementById("paySummarySeats").textContent = seatsArray.join(", ");
        document.getElementById("paymentErrorBanner").style.display = "none";

        closeModal(seatModal);
        openModal(paymentModal);

    } catch (err) {
        console.error("Booking error:", err);
        alert("Server communication error while creating booking: " + err.message);
    } finally {
        proceedBtn.disabled = false;
        proceedBtn.textContent = "Proceed to Payment →";
    }
}

function initiatePaymentFlow() {
    const isSimulateFailure = document.getElementById("simulateFailureCheckbox")?.checked || false;

    const paymentPayload = {
        bookingId: state.currentBooking?.id,
        paymentMethod: state.selectedPaymentMethod,
        simulateFailure: isSimulateFailure
    };

    if (!paymentPayload.bookingId) {
        alert("No active booking found. Please select your seats again.");
        return;
    }

    if (state.selectedPaymentMethod === "UPI") {
        paymentPayload.upiId = document.getElementById("virtualUpiInput").value.trim() || "dhiraj@okaxis";
    } else if (state.selectedPaymentMethod === "CARD") {
        paymentPayload.cardNumber = document.getElementById("cardNumInput").value.trim();
        paymentPayload.cardHolderName = document.getElementById("cardHolderInput").value.trim();
        paymentPayload.expiryDate = document.getElementById("cardExpiryInput").value.trim();
        paymentPayload.cvv = document.getElementById("cardCvvInput").value.trim();
    } else if (state.selectedPaymentMethod === "NET_BANKING") {
        paymentPayload.bankName = state.selectedBank;
    } else if (state.selectedPaymentMethod === "WALLET") {
        paymentPayload.walletName = "CinePay Fast Wallet";
    }

    // If card or net banking, show 2-factor OTP verification
    if (state.selectedPaymentMethod === "CARD" || state.selectedPaymentMethod === "NET_BANKING") {
        state.pendingPaymentPayload = paymentPayload;
        openModal(otpModal);
        return;
    }

    executePaymentRequest(paymentPayload);
}

async function executePaymentRequest(payload) {
    closeModal(paymentModal);
    showProcessingOverlay("Processing Payment...", "Connecting with secure banking network and confirming seat reservation...");

    const bar = document.getElementById("processingBar");
    bar.style.width = "30%";

    setTimeout(() => {
        document.getElementById("processingStepTitle").textContent = "Finalizing Booking...";
        document.getElementById("processingStepSubtitle").textContent = "Allocating tickets and generating cinema pass...";
        bar.style.width = "75%";
    }, 700);

    try {
        const response = await cineBookFetch("/payments/virtual/process", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await response.json().catch(() => ({}));

        setTimeout(() => {
            bar.style.width = "100%";
            setTimeout(() => {
                hideProcessingOverlay();

                if (response.ok) {
                    saveBookingToLocalStorage(data);
                    updateBookingBadge();
                    renderTicket(data);
                } else {
                    const errMsg = data.error || data.message || "Payment declined by issuing bank.";
                    showPaymentFailure(errMsg);
                }
            }, 400);
        }, 1200);

    } catch (err) {
        hideProcessingOverlay();
        showPaymentFailure("Could not connect to payment server. Please check your internet connection.");
    }
}

function showPaymentFailure(msg) {
    openModal(paymentModal);
    const banner = document.getElementById("paymentErrorBanner");
    banner.textContent = `❌ ${msg}`;
    banner.style.display = "block";
}

// ========================================================
// STEP 4: DIGITAL TICKET RENDERING
// ========================================================
function renderTicket(data) {
    document.getElementById("ticketMovieTitle").textContent = data.movieTitle;
    document.getElementById("ticketMovieGenre").textContent = `${data.movieGenre || 'Cinema'} • ${data.duration || '150'} min`;
    document.getElementById("ticketTheatreScreen").textContent = `${data.theatreName || 'CineBook Cinema'} • ${data.screenName || 'Screen 1'}`;
    document.getElementById("ticketDateTime").textContent = `${data.showDate} • ${formatTime(data.showTime)}`;
    document.getElementById("ticketNumberDisplay").textContent = data.ticketNumber || `TKT-${data.bookingId}`;
    document.getElementById("ticketBarcodeText").textContent = data.ticketNumber || `TKT-${data.bookingId}`;
    document.getElementById("ticketPayMethodBadge").textContent = `PAID VIA ${formatPaymentMethod(data.paymentMethod)}`;
    document.getElementById("ticketTxnRefDisplay").textContent = data.transactionId || "TXN-9835482012";
    document.getElementById("ticketTotalPaidDisplay").textContent = `₹${(data.totalAmount || 0).toFixed(2)}`;

    const poster = data.moviePoster || state.selectedMovie?.posterUrl || "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80";
    document.getElementById("ticketPosterImg").src = poster;

    // Badges for seats
    const seatWrap = document.getElementById("ticketSeatBadges");
    seatWrap.innerHTML = "";
    const seats = data.seatNumbers || [];
    seats.forEach(s => {
        const pill = document.createElement("span");
        pill.className = "seat-pill";
        pill.textContent = s;
        seatWrap.appendChild(pill);
    });

    openModal(ticketModal);
}

// ========================================================
// STEP 5: MY BOOKINGS
// ========================================================
async function openMyBookings() {
    openModal(myBookingsModal);
    const container = document.getElementById("bookingsHistoryList");
    container.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Fetching your booking history...</p>
        </div>`;

    if (!state.currentUser) {
        window.location.href = "login.html?redirect=index.html";
        return;
    }
    let bookings = [];
    try {
        const res = await fetch("/bookings/me/detailed", { credentials: "same-origin" });
        if (res.ok) bookings = await res.json();
    } catch (e) {
        console.warn("Could not load account bookings:", e);
    }

    if (bookings.length === 0) {
        container.innerHTML = `
            <div class="loading-state">
                <p>No bookings found yet. Select a movie and complete checkout to see your tickets here!</p>
            </div>`;
        return;
    }

    container.innerHTML = "";
    bookings.forEach(b => {
        const seatsStr = (b.seatNumbers && b.seatNumbers.length > 0) ? b.seatNumbers.join(", ") : `${b.numberOfTickets || 1} Tickets`;
        const card = document.createElement("div");
        card.className = "history-card";
        card.innerHTML = `
            <div class="history-main-info">
                <h4>${b.movieTitle || 'Movie'}</h4>
                <p class="history-sub">${b.theatreName || 'CineBook'} • ${b.showDate} at ${formatTime(b.showTime)}</p>
                <div class="history-seats-row">
                    <span class="seat-pill">${seatsStr}</span>
                    <span class="badge-tag">${b.status || 'CONFIRMED'}</span>
                </div>
            </div>
            <div class="history-actions">
                <span class="history-amount">₹${(b.totalAmount || 0).toFixed(2)}</span>
                <button class="view-ticket-btn" onclick='viewStoredTicket(${JSON.stringify(b).replace(/'/g, "&#39;")})'>
                    🎟️ View Ticket
                </button>
            </div>
        `;
        container.appendChild(card);
    });
}

window.viewStoredTicket = function(bookingData) {
    closeModal(myBookingsModal);
    renderTicket(bookingData);
};

// ========================================================
// STORAGE & HELPERS
// ========================================================
function saveBookingToLocalStorage(booking) {
    try {
        const key = "cinebook_bookings_" + (state.currentUser?.id || "guest");
        const stored = JSON.parse(localStorage.getItem(key) || "[]");
        stored.unshift(booking);
        localStorage.setItem(key, JSON.stringify(stored));
    } catch (e) {
        console.warn("Could not write to localStorage:", e);
    }
}

function getStoredBookings() {
    try {
        const key = "cinebook_bookings_" + (state.currentUser?.id || "guest");
        const raw = localStorage.getItem(key);
        return raw ? JSON.parse(raw) : [];
    } catch (e) {
        return [];
    }
}

function updateBookingBadge() {
    const badge = document.getElementById("bookingBadge");
    const count = getStoredBookings().length;
    badge.textContent = count;
}

function formatTime(timeStr) {
    if (!timeStr) return "07:30 PM";
    const parts = timeStr.split(':');
    let h = parseInt(parts[0], 10);
    const m = parts[1] || "00";
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12;
    h = h ? h : 12;
    return `${h}:${m} ${ampm}`;
}

function formatPaymentMethod(m) {
    if (!m) return "UPI";
    if (m === "VIRTUAL_UPI" || m === "UPI") return "UPI";
    if (m === "VIRTUAL_CARD" || m === "CARD" || m === "CREDIT_DEBIT_CARD") return "CARD";
    if (m === "VIRTUAL_NET_BANKING" || m === "NET_BANKING") return "NET BANKING";
    if (m === "VIRTUAL_WALLET" || m === "WALLET") return "WALLET";
    return m.replace(/_/g, ' ');
}

function openModal(modal) {
    modal.classList.add("open");
    document.body.style.overflow = "hidden";
}

function closeModal(modal) {
    modal.classList.remove("open");
    document.body.style.overflow = "auto";
}

function showProcessingOverlay(title, subtitle) {
    document.getElementById("processingStepTitle").textContent = title;
    document.getElementById("processingStepSubtitle").textContent = subtitle;
    document.getElementById("processingBar").style.width = "10%";
    processingOverlay.classList.add("open");
}

function hideProcessingOverlay() {
    processingOverlay.classList.remove("open");
}
