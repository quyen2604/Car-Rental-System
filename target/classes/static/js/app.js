// Cấu hình API URL Backend
window.API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    checkUserSession();
    initGlobalEvents();
});

// Toast notification helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : '❌'}</span>
        <div>${message}</div>
    `;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'toastIn 0.3s reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Định dạng tiền tệ VND
function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// Định dạng ngày hiển thị
function formatDate(dateString) {
    if (!dateString) return '--';
    const d = new Date(dateString);
    return d.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

// Kiểm tra phiên đăng nhập của người dùng
function checkUserSession() {
    const userStr = localStorage.getItem('user');
    const authBtn = document.getElementById('navAuthBtn');
    const userDropdown = document.getElementById('navUserDropdown');
    const usernameDisplay = document.getElementById('navUsername');
    const myBookingsLink = document.getElementById('navMyBookings');

    if (userStr) {
        const user = JSON.parse(userStr);
        if (authBtn) authBtn.style.display = 'none';
        if (userDropdown) userDropdown.style.display = 'flex';
        if (usernameDisplay) usernameDisplay.innerText = `👤 ${user.fullName}`;
        if (myBookingsLink) myBookingsLink.style.display = 'inline-block';
    } else {
        if (authBtn) authBtn.style.display = 'inline-flex';
        if (userDropdown) userDropdown.style.display = 'none';
        if (myBookingsLink) myBookingsLink.style.display = 'none';
    }
}

// Khởi tạo các sự kiện chung
function initGlobalEvents() {
    // Sự kiện mở Modal Đăng nhập/Đăng ký
    const authBtn = document.getElementById('navAuthBtn');
    if (authBtn) {
        authBtn.addEventListener('click', () => openAuthModal('login'));
    }

    // Sự kiện đóng Modal khi click bên ngoài
    const modalOverlay = document.getElementById('authModal');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) closeAuthModal();
        });
    }

    // Xử lý nút Đăng xuất
    const logoutBtn = document.getElementById('btnLogout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            const user = JSON.parse(localStorage.getItem('user'));
            if (user) {
                try {
                    await fetch(`${window.API_BASE_URL}/auth/logout/${user.userId}`, { method: 'POST' });
                } catch (e) {
                    console.error('Lỗi khi gọi API logout:', e);
                }
                localStorage.removeItem('user');
                showToast('Đã đăng xuất thành công!');
                checkUserSession();
                // Nếu đang ở trang lịch sử thì reload về trang chủ
                if (document.getElementById('bookingsSection') && document.getElementById('bookingsSection').classList.contains('active')) {
                    switchTab('search');
                } else {
                    window.location.reload();
                }
            }
        });
    }
}

// Mở Modal Xác thực
function openAuthModal(tab = 'login') {
    const modal = document.getElementById('authModal');
    if (!modal) return;
    modal.classList.add('active');
    switchAuthTab(tab);
}

// Đóng Modal Xác thực
function closeAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.classList.remove('active');
}

// Chuyển tab trong Modal Đăng nhập/Đăng ký
function switchAuthTab(tab) {
    const loginTab = document.getElementById('tabLogin');
    const registerTab = document.getElementById('tabRegister');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (tab === 'login') {
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.classList.add('active');
        registerForm.classList.remove('active');
    } else {
        loginTab.classList.remove('active');
        registerTab.classList.add('active');
        loginForm.classList.remove('active');
        registerForm.classList.add('active');
    }
}

// Đăng nhập
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch(`${window.API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errMsg = await response.text();
            throw new Error(errMsg || 'Sai tài khoản hoặc mật khẩu!');
        }

        const user = await response.json();
        localStorage.setItem('user', JSON.stringify(user));
        showToast(`Chào mừng trở lại, ${user.fullName}!`);
        closeAuthModal();
        checkUserSession();
        
        // Reload if on booking page or dashboard to update data
        if (window.location.pathname.includes('booking.html')) {
            window.location.reload();
        }
        
        // Cập nhật tab đang hiển thị
        if (document.getElementById('bookingsSection') && document.getElementById('bookingsSection').classList.contains('active')) {
            loadRenterBookings();
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Đăng ký renter
async function handleRegister(e) {
    e.preventDefault();
    const fullName = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const phone = document.getElementById('regPhone').value;
    const password = document.getElementById('regPassword').value;
    const licenseNumber = document.getElementById('regLicense').value;

    try {
        const response = await fetch(`${window.API_BASE_URL}/auth/register/renter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, email, phone, password, licenseNumber, active: true })
        });

        if (!response.ok) {
            const errMsg = await response.text();
            throw new Error(errMsg || 'Đăng ký thất bại. Email có thể đã tồn tại!');
        }

        showToast('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
        switchAuthTab('login');
        document.getElementById('loginEmail').value = email;
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Hàm Tìm kiếm xe (Trang chủ)
async function handleSearch() {
    const city = document.getElementById('citySelect').value;
    const type = document.getElementById('typeSelect').value;
    const carGrid = document.getElementById('carGrid');

    if (!carGrid) return;
    carGrid.innerHTML = '<div class="status-message">🔄 Đang tải danh sách xe phù hợp...</div>';

    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles/search-vehicle?city=${city}&type=${type}`);
        if (!response.ok) throw new Error('Không thể kết nối đến máy chủ.');
        
        const vehicles = await response.json();
        carGrid.innerHTML = '';

        if (vehicles.length === 0) {
            carGrid.innerHTML = '<div class="status-message">❌ Không tìm thấy xe nào khả dụng tại khu vực này!</div>';
            return;
        }

        vehicles.forEach(v => {
            const isCar = v.seatNumber !== undefined;
            const specialDetail = isCar ? `🪑 ${v.seatNumber} Chỗ` : `⚡ ${v.engineCapacity} CC`;
            const icon = isCar ? '🚗' : '🛵';
            const locationStr = v.location ? `${v.location.district}, ${v.location.city}` : 'Chưa cập nhật';
            
            const card = document.createElement('div');
            card.className = 'vehicle-card';
            card.innerHTML = `
                <div class="vehicle-hero ${!isCar ? 'vehicle-hero-motorbike' : ''}">
                    <div class="vehicle-avatar">${icon}</div>
                    <div class="vehicle-badge">${v.vehicleStatus}</div>
                </div>
                <div class="vehicle-body">
                    <h3 class="vehicle-name">${v.brand} ${v.model}</h3>
                    <p class="vehicle-desc">${v.description || 'Không có mô tả chi tiết cho xe này.'}</p>
                    <div class="vehicle-meta">
                        <div class="meta-item">📍 ${locationStr}</div>
                        <div class="meta-item">${specialDetail}</div>
                    </div>
                    <div class="vehicle-footer">
                        <div class="vehicle-price">
                            <span class="price-num">${formatVND(v.pricePerDay)}</span>
                            <span class="price-unit">/ ngày</span>
                        </div>
                        <button class="btn btn-primary" onclick="bookVehicle(${v.vehicleId})">Đặt xe ngay</button>
                    </div>
                </div>
            `;
            carGrid.innerHTML += card.outerHTML;
        });
    } catch (err) {
        carGrid.innerHTML = `<div class="status-message" style="color: var(--danger);">${err.message}</div>`;
    }
}

// Bắt đầu luồng đặt xe
function bookVehicle(vehicleId) {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        showToast('Vui lòng đăng nhập để tiến hành đặt xe!', 'error');
        openAuthModal('login');
        return;
    }
    window.location.href = `/booking.html?vehicleId=${vehicleId}`;
}

// Quản lý chuyển đổi Tab trên Trang chủ
function switchTab(tab) {
    const searchSec = document.getElementById('searchSection');
    const bookingsSec = document.getElementById('bookingsSection');
    const navSearch = document.getElementById('navSearch');
    const navMyBookings = document.getElementById('navMyBookings');

    if (tab === 'search') {
        if (searchSec) searchSec.classList.add('active');
        if (bookingsSec) bookingsSec.classList.remove('active');
        if (navSearch) navSearch.classList.add('active');
        if (navMyBookings) navMyBookings.classList.remove('active');
    } else {
        if (searchSec) searchSec.classList.remove('active');
        if (bookingsSec) bookingsSec.classList.add('active');
        if (navSearch) navSearch.classList.remove('active');
        if (navMyBookings) navMyBookings.classList.add('active');
        loadRenterBookings();
    }
}

// Load lịch sử đặt xe của renter
async function loadRenterBookings() {
    const userStr = localStorage.getItem('user');
    const bookingList = document.getElementById('bookingList');
    if (!bookingList) return;

    if (!userStr) {
        bookingList.innerHTML = '<div class="status-message">Vui lòng đăng nhập để xem lịch sử đặt xe.</div>';
        return;
    }

    const user = JSON.parse(userStr);
    bookingList.innerHTML = '<div class="status-message">🔄 Đang tải lịch sử đặt xe...</div>';

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/renter/${user.userId}`);
        if (!response.ok) throw new Error('Không thể tải danh sách đặt xe.');

        const bookings = await response.json();
        bookingList.innerHTML = '';

        if (bookings.length === 0) {
            bookingList.innerHTML = '<div class="status-message">📭 Bạn chưa có đơn đặt xe nào. Hãy tìm kiếm xe và đặt ngay nhé!</div>';
            return;
        }

        bookings.forEach(b => {
            const statusClass = b.bookingStatus.toLowerCase();
            const statusText = b.bookingStatus === 'CONFIRMED' ? 'Đã xác nhận' : 
                               (b.bookingStatus === 'PENDING' ? 'Chờ xác nhận' : 'Đã hủy');
            
            const item = document.createElement('div');
            item.className = 'booking-item';
            item.innerHTML = `
                <div class="booking-avatar">🚗</div>
                <div class="booking-details">
                    <h4>${b.vehicleBrand} ${b.vehicleModel}</h4>
                    <p>📍 Biển số: <strong>${b.licensePlate}</strong></p>
                    <p>📅 Thời gian: từ <strong>${formatDate(b.startDate)}</strong> đến <strong>${formatDate(b.endDate)}</strong></p>
                    <p>💰 Tổng cộng: <strong style="color:var(--primary); font-size:1.1rem;">${formatVND(b.totalAmount)}</strong> (${formatVND(b.pricePerDay)}/ngày)</p>
                    <span class="booking-badge ${statusClass}">${statusText}</span>
                </div>
                <div>
                    ${b.bookingStatus !== 'CANCELLED' ? `
                        <button class="btn btn-danger" onclick="cancelBooking(${b.bookingId})">Hủy đặt xe</button>
                    ` : ''}
                </div>
            `;
            bookingList.appendChild(item);
        });
    } catch (err) {
        bookingList.innerHTML = `<div class="status-message" style="color:var(--danger);">${err.message}</div>`;
    }
}

// Hủy đặt xe
async function cancelBooking(bookingId) {
    if (!confirm('Bạn có chắc chắn muốn hủy đơn đặt xe này không?')) return;

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/cancel`, {
            method: 'POST'
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Không thể hủy đơn đặt xe.');
        }

        showToast('Đã hủy đặt xe thành công!');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// --- LOGIC TRÊN TRANG CHI TIẾT ĐẶT XE (booking.html) ---
async function loadVehicleDetails(id) {
    const vehicleIcon = document.getElementById('vehicleIcon');
    const vehicleTypeBadge = document.getElementById('vehicleTypeBadge');
    const vehicleName = document.getElementById('vehicleName');
    const vehiclePriceHero = document.getElementById('vehiclePriceHero');
    const vehicleDetailsBody = document.getElementById('vehicleDetailsBody');

    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles/${id}`);
        if (!response.ok) throw new Error('Không tìm thấy thông tin xe.');

        vehicleData = await response.json();

        // Cập nhật giao diện
        const isCar = vehicleData.seatNumber !== undefined;
        if (vehicleIcon) vehicleIcon.innerText = isCar ? '🚗' : '🛵';
        if (vehicleTypeBadge) {
            vehicleTypeBadge.innerText = isCar ? 'Ô TÔ TỰ LÁI' : 'XE MÁY TỰ LÁI';
            vehicleTypeBadge.style.background = isCar ? 'var(--primary)' : 'var(--accent)';
        }
        if (vehicleName) vehicleName.innerText = `${vehicleData.brand} ${vehicleData.model}`;
        if (vehiclePriceHero) vehiclePriceHero.innerHTML = `${formatVND(vehicleData.pricePerDay)} <span>/ ngày</span>`;

        if (vehicleDetailsBody) {
            const specialDetail = isCar ? `🪑 Số chỗ ngồi: <strong>${vehicleData.seatNumber} chỗ</strong>` : `⚡ Dung tích xi lanh: <strong>${vehicleData.engineCapacity} cc</strong>`;
            const locationStr = vehicleData.location ? `${vehicleData.location.addressDetail}, ${vehicleData.location.district}, ${vehicleData.location.city}` : 'Chưa cập nhật';
            
            vehicleDetailsBody.innerHTML = `
                <div class="detail-row">
                    <span class="detail-icon">📍</span>
                    <div>Địa điểm nhận xe: <strong>${locationStr}</strong></div>
                </div>
                <div class="detail-row">
                    <span class="detail-icon">💳</span>
                    <div>${specialDetail}</div>
                </div>
                <div class="detail-row">
                    <span class="detail-icon">🆔</span>
                    <div>Biển số xe: <strong>${vehicleData.licensePlate}</strong></div>
                </div>
                <div class="detail-row">
                    <span class="detail-icon">📝</span>
                    <div>Mô tả: ${vehicleData.description || 'Không có mô tả chi tiết.'}</div>
                </div>
            `;
        }

        // Cập nhật thông tin renter
        setupRenterInfo();
    } catch (err) {
        showToast(err.message, 'error');
        if (vehicleDetailsBody) {
            vehicleDetailsBody.innerHTML = `<div class="status-message" style="color:var(--danger);">${err.message}</div>`;
        }
    }
}

// Hiển thị thông tin renter trong trang booking
function setupRenterInfo() {
    const userStr = localStorage.getItem('user');
    const loggedInSummary = document.getElementById('loggedInUserSummary');
    const userSummaryName = document.getElementById('userSummaryName');
    const userSummaryEmail = document.getElementById('userSummaryEmail');
    const renterIdInput = document.getElementById('renterId');
    const btnSubmit = document.getElementById('btnSubmit');

    if (userStr) {
        const user = JSON.parse(userStr);
        if (loggedInSummary) loggedInSummary.style.display = 'block';
        if (userSummaryName) userSummaryName.innerText = user.fullName;
        if (userSummaryEmail) userSummaryEmail.innerText = user.email;
        if (renterIdInput) renterIdInput.value = user.userId;
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<span>🔒</span> Xác nhận Đặt xe Ngay';
        }
    } else {
        if (loggedInSummary) loggedInSummary.style.display = 'none';
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '🔑 Đăng nhập để Đặt xe';
            btnSubmit.onclick = () => {
                alert('Vui lòng đăng nhập trước khi thực hiện đặt xe.');
                window.location.href = '/index.html';
            };
        }
    }
}

// Tính toán giá tiền thực tế
function calculatePrice() {
    if (!vehicleData) return;
    
    const startDateVal = document.getElementById('startDate').value;
    const endDateVal = document.getElementById('endDate').value;
    const priceBreakdown = document.getElementById('priceBreakdown');
    
    if (!startDateVal || !endDateVal) {
        if (priceBreakdown) priceBreakdown.style.display = 'none';
        return;
    }

    const start = new Date(startDateVal);
    const end = new Date(endDateVal);

    if (end < start) {
        showToast('Ngày kết thúc không thể trước ngày bắt đầu!', 'error');
        document.getElementById('endDate').value = '';
        if (priceBreakdown) priceBreakdown.style.display = 'none';
        return;
    }

    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // Tính cả ngày đầu tiên

    const total = diffDays * vehicleData.pricePerDay;

    if (priceBreakdown) {
        priceBreakdown.style.display = 'block';
        document.getElementById('pricePerDayDisplay').innerText = formatVND(vehicleData.pricePerDay);
        document.getElementById('numDaysDisplay').innerText = `${diffDays} ngày`;
        document.getElementById('totalPriceDisplay').innerText = formatVND(total);
    }
}

// Đặt xe trực tiếp
async function submitBookingDirect() {
    const renterId = document.getElementById('renterId').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        showToast('Vui lòng chọn ngày nhận và ngày trả xe!', 'error');
        return;
    }

    const btnSubmit = document.getElementById('btnSubmit');
    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerText = '⌛ Đang xử lý đặt xe...';
    }

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                renterId: parseInt(renterId),
                vehicleId: vehicleData.vehicleId,
                startDate: new Date(startDate),
                endDate: new Date(endDate)
            })
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Không thể tạo yêu cầu đặt xe.');
        }

        const bookingRes = await response.json();
        
        // Hiển thị màn hình đặt xe thành công
        const formContent = document.getElementById('formContent');
        const successScreen = document.getElementById('successScreen');
        const successBookingId = document.getElementById('successBookingId');

        if (formContent) formContent.style.display = 'none';
        if (successScreen) successScreen.style.display = 'block';
        if (successBookingId) successBookingId.innerText = `Mã đặt xe: #${bookingRes.bookingId}`;
        
        showToast('Đặt xe thành công!');
    } catch (err) {
        showToast(err.message, 'error');
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<span>🔒</span> Xác nhận Đặt xe Ngay';
        }
    }
}