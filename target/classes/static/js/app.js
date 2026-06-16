// Cấu hình API URL Backend
window.API_BASE_URL = 'http://localhost:8080/api';

let vehicleData = null; // Biến global lưu thông tin xe khi vào trang chi tiết

document.addEventListener('DOMContentLoaded', () => {
    checkUserSession();
    initGlobalEvents();

    // 🔥 ĐÃ SỬA: Tự động chuyển tab và đồng bộ cho cả Admin lẫn Owner khi tải lại trang (F5)
    const userJson = localStorage.getItem('user');
    if (userJson) {
        const user = JSON.parse(userJson);
        const rawRole = String(user.role || '').toUpperCase().trim();
        const lowerName = String(user.fullName || '').toLowerCase();

        if (rawRole === 'ADMIN' || lowerName.includes('admin')) {
            const adminManagementSection = document.getElementById('adminManagementSection');
            if (adminManagementSection && adminManagementSection.style.display !== 'none') {
                loadAdminVehicleApproval();
            }
        }
        else if (rawRole === 'OWNER' || lowerName.includes("trần xe") || lowerName.includes("xe")) {
            const navOwnerMenu = document.getElementById('navOwnerMenu');
            if (navOwnerMenu) {
                navOwnerMenu.style.display = 'inline-block';
            }
            checkPendingBookingsAlert(user.id || user.userId || 2);
        }
    }
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
    const navOwnerMenu = document.getElementById('navOwnerMenu');
    const navRegisterVehicle = document.getElementById('navRegisterVehicle');
    const navAdminMenu = document.getElementById('navAdminMenu');

    const ownerManagementSection = document.getElementById('ownerManagementSection');
    const ownerRegisterVehicleSection = document.getElementById('ownerRegisterVehicleSection');
    const adminManagementSection = document.getElementById('adminManagementSection');

    if (userStr) {
        const user = JSON.parse(userStr);
        if (authBtn) authBtn.style.display = 'none';
        if (userDropdown) userDropdown.style.display = 'flex';
        if (usernameDisplay) usernameDisplay.innerText = `👤 ${user.fullName}`;

        const rawRole = String(user.role || '').toUpperCase().trim();
        const lowerName = String(user.fullName || '').toLowerCase();

        // -----------------------------------------------------------------
        // 🚀 ĐOẠN KHẮC PHỤC CHÍNH: KIỂM TRA BẪY QUYỀN AN TOÀN
        // -----------------------------------------------------------------
        if (rawRole === 'ADMIN' || lowerName.includes('admin')) {
            user.role = 'ADMIN';

            // 🛡️ HIỂN THỊ CHO ADMIN
            if (myBookingsLink) myBookingsLink.style.display = 'none';
            if (navOwnerMenu) navOwnerMenu.style.display = 'none';
            if (navRegisterVehicle) navRegisterVehicle.style.display = 'none';
            if (navAdminMenu) navAdminMenu.style.display = 'inline-block'; // 🔥 Chắc chắn hiện nút màu vàng!

            if (adminManagementSection && adminManagementSection.style.display !== 'none') {
                loadAdminVehicleApproval();
            }
        }
        else if (rawRole === 'OWNER' || lowerName.includes('trần xe') || lowerName.includes('xe')) {
            user.role = 'OWNER';

            // ➕ HIỂN THỊ CHO CHỦ XE (OWNER)
            if (myBookingsLink) myBookingsLink.style.display = 'none';
            if (navOwnerMenu) navOwnerMenu.style.display = 'inline-block';
            if (navRegisterVehicle) navRegisterVehicle.style.display = 'inline-block';
            if (navAdminMenu) navAdminMenu.style.display = 'none';

            if (typeof checkPendingBookingsAlert === "function") {
                checkPendingBookingsAlert(user.id || user.userId);
            }
        }
        else {
            user.role = 'RENTER';

            // 👤 HIỂN THỊ CHO KHÁCH THUÊ (RENTER)
            if (myBookingsLink) myBookingsLink.style.display = 'inline-block';
            if (navOwnerMenu) navOwnerMenu.style.display = 'none';
            if (navRegisterVehicle) navRegisterVehicle.style.display = 'none';
            if (navAdminMenu) navAdminMenu.style.display = 'none';
        }
    } else {
        // NẾU CHƯA ĐĂNG NHẬP -> Ẩn sạch các nút
        if (authBtn) authBtn.style.display = 'inline-flex';
        if (userDropdown) userDropdown.style.display = 'none';
        if (myBookingsLink) myBookingsLink.style.display = 'none';
        if (navOwnerMenu) navOwnerMenu.style.display = 'none';
        if (navRegisterVehicle) navRegisterVehicle.style.display = 'none';
        if (navAdminMenu) navAdminMenu.style.display = 'none';

        if (ownerManagementSection) ownerManagementSection.style.display = 'none';
        if (ownerRegisterVehicleSection) ownerRegisterVehicleSection.style.display = 'none';
        if (adminManagementSection) adminManagementSection.style.display = 'none';
    }
}

// Khởi tạo các sự kiện chung
function initGlobalEvents() {
    const authBtn = document.getElementById('navAuthBtn');
    if (authBtn) {
        authBtn.addEventListener('click', () => openAuthModal('login'));
    }

    const modalOverlay = document.getElementById('authModal');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) closeAuthModal();
        });
    }

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

                const navOwnerMenu = document.getElementById('navOwnerMenu');
                if (navOwnerMenu) {
                    navOwnerMenu.style.display = 'none';
                    navOwnerMenu.style.backgroundColor = '#4b5563';
                    navOwnerMenu.innerHTML = '🔔 Duyệt Đơn';
                }

                checkUserSession();
                switchTab('search');
            }
        });
    }
}

function openAuthModal(tab = 'login') {
    const modal = document.getElementById('authModal');
    if (!modal) return;
    modal.classList.add('active');
    switchAuthTab(tab);
}

function closeAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.classList.remove('active');
}

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

        const rawRole = String(user.role || '').toUpperCase().trim();
        const lowerName = String(user.fullName || '').toLowerCase();
        const lowerEmail = String(user.email || '').toLowerCase(); // Lấy thêm email

// Ép quyền ADMIN nếu email chứa chữ 'admin' hoặc role chuẩn
        if (rawRole === 'ADMIN' || lowerName.includes('admin') || lowerEmail.includes('admin')) {
            user.role = 'ADMIN';
        } else if (rawRole === 'OWNER' || lowerName.includes('trần xe') || lowerName.includes('xe')) {
            user.role = 'OWNER';
        } else {
            user.role = 'RENTER';
        }

        localStorage.setItem('user', JSON.stringify(user));
        closeAuthModal();

        checkUserSession();

        if (user.role === 'ADMIN') {
            showToast("🛡️ Chào mừng Quản trị viên hệ thống!");
            switchTab('admin-management');
        } else if (user.role === 'OWNER') {
            showToast(`Chào mừng chủ xe, ${user.fullName}!`);
            switchTab('owner-register-vehicle');
        } else {
            showToast(`Chào mừng trở lại, ${user.fullName}!`);
            switchTab('search');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

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

function bookVehicle(vehicleId) {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        showToast('Vui lòng đăng nhập để tiến hành đặt xe!', 'error');
        openAuthModal('login');
        return;
    }
    window.location.href = `/booking.html?vehicleId=${vehicleId}`;
}

function switchTab(tabName) {
    const searchSec = document.getElementById('searchSection');
    const bookingSec = document.getElementById('bookingsSection');
    const ownerManagementSec = document.getElementById('ownerManagementSection');
    const ownerRegisterVehicleSec = document.getElementById('ownerRegisterVehicleSection');
    const adminManagementSec = document.getElementById('adminManagementSection');

    const navSearch = document.getElementById('navSearch');
    const navMyBookings = document.getElementById('navMyBookings');
    const navOwnerMenu = document.getElementById('navOwnerMenu');
    const navRegisterVehicle = document.getElementById('navRegisterVehicle');
    const navAdminMenu = document.getElementById('navAdminMenu');

    if (searchSec) searchSec.style.display = 'none';
    if (bookingSec) bookingSec.style.display = 'none';
    if (ownerManagementSec) ownerManagementSec.style.display = 'none';
    if (ownerRegisterVehicleSec) ownerRegisterVehicleSec.style.display = 'none';
    if (adminManagementSec) adminManagementSec.style.display = 'none';

    if (navSearch) navSearch.classList.remove('active');
    if (navMyBookings) navMyBookings.classList.remove('active');
    if (navOwnerMenu) navOwnerMenu.classList.remove('active');
    if (navRegisterVehicle) navRegisterVehicle.classList.remove('active');
    if (navAdminMenu) navAdminMenu.classList.remove('active');

    if (tabName === 'search') {
        if (searchSec) searchSec.style.display = 'block';
        if (navSearch) navSearch.classList.add('active');
    } else if (tabName === 'bookings') {
        if (bookingSec) bookingSec.style.display = 'block';
        if (navMyBookings) navMyBookings.classList.add('active');
        loadRenterBookings();
    } else if (tabName === 'owner-management') {
        if (ownerManagementSec) ownerManagementSec.style.display = 'block';
        if (navOwnerMenu) navOwnerMenu.classList.add('active');
        loadOwnerBookings();
    } else if (tabName === 'owner-register-vehicle') {
        if (ownerRegisterVehicleSec) ownerRegisterVehicleSec.style.display = 'block';
        if (navRegisterVehicle) navRegisterVehicle.classList.add('active');
    } else if (tabName === 'admin-management') {
        if (adminManagementSec) adminManagementSec.style.display = 'block';
        if (navAdminMenu) navAdminMenu.classList.add('active');
        loadAdminVehicleApproval();
    }
}

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
            bookingList.innerHTML = '<div class="status-message">📭 Bạn chưa có đơn đặt xe nào.</div>';
            return;
        }

        bookings.forEach(b => {
            const statusClass = b.bookingStatus.toLowerCase();
            const statusText = b.bookingStatus === 'CONFIRMED' ? 'Đã xác nhận' : (b.bookingStatus === 'PENDING' ? 'Chờ xác nhận' : 'Đã hủy');

            const item = document.createElement('div');
            item.className = 'booking-item';
            item.innerHTML = `
                <div class="booking-avatar">🚗</div>
                <div class="booking-details">
                    <h4>${b.vehicleBrand || 'Phương tiện'} ${b.vehicleModel || ''}</h4>
                    <p>📍 Biển số: <strong>${b.licensePlate || '---'}</strong></p>
                    <p>📅 Thời gian: từ <strong>${formatDate(b.startDate)}</strong> đến <strong>${formatDate(b.endDate)}</strong></p>
                    <p>💰 Tổng cộng: <strong style="color:var(--primary); font-size:1.1rem;">${formatVND(b.totalAmount)}</strong></p>
                    <span class="booking-badge ${statusClass}">${statusText}</span>
                </div>
                <div>
                    ${b.bookingStatus === 'PENDING' ? `<button class="btn btn-danger" onclick="cancelBooking(${b.bookingId})">Hủy đặt xe</button>` : ''}
                </div>
            `;
            bookingList.appendChild(item);
        });
    } catch (err) {
        bookingList.innerHTML = `<div class="status-message" style="color:var(--danger);">${err.message}</div>`;
    }
}

async function cancelBooking(bookingId) {
    if (!confirm('Bạn có chắc chắn muốn hủy đơn đặt xe này không?')) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/cancel`, { method: 'POST' });
        if (!response.ok) throw new Error('Không thể hủy đơn đặt xe.');
        showToast('Đã hủy đặt xe thành công!');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function checkPendingBookingsAlert(ownerId) {
    const navOwnerMenu = document.getElementById('navOwnerMenu');
    if (!navOwnerMenu || !ownerId) return;

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/owner/${ownerId}`);
        if (response.ok) {
            const bookings = await response.json();
            const hasPending = bookings.some(b => b.bookingStatus === 'PENDING');

            if (hasPending) {
                navOwnerMenu.style.backgroundColor = '#ef4444';
                navOwnerMenu.innerHTML = '🔴 Duyệt Đơn Mới!';
            } else {
                navOwnerMenu.style.backgroundColor = '#4b5563';
                navOwnerMenu.innerHTML = 'Duyệt Đơn';
            }
        }
    } catch (e) {
        console.error("Không thể quét trạng thái đơn hàng: ", e);
    }
}

async function loadOwnerBookings() {
    const userStr = localStorage.getItem('user');
    const container = document.getElementById('ownerBookingList');
    if (!container) return;

    if (!userStr) {
        container.innerHTML = '<div class="status-message">❌ Vui lòng đăng nhập tài khoản Owner!</div>';
        return;
    }

    const user = JSON.parse(userStr);
    const ownerId = user.userId || user.id;

    container.innerHTML = '<div class="status-message">🔄 Đang tải danh sách đơn hàng...</div>';

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/owner/${ownerId}`);
        if (!response.ok) throw new Error('Không thể kết nối dữ liệu.');

        const bookings = await response.json();
        bookings.sort((a, b) => (b.bookingId || b.id) - (a.bookingId || a.id));

        container.innerHTML = '';

        if (bookings.length === 0) {
            container.innerHTML = '<div class="status-message">📭 Hiện tại không có yêu cầu thuê xe nào.</div>';
            return;
        }

        bookings.forEach(b => {
            const statusClass = b.bookingStatus.toLowerCase();
            let actionButtons = '';

            if (b.bookingStatus === 'PENDING') {
                actionButtons = `
                    <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
                        <button class="btn btn-primary" style="background-color: #10b981; color: white; border:none; padding: 0.5rem 1rem;" onclick="handleApprove(${b.bookingId || b.id})">✅ Xác Nhận</button>
                        <button class="btn btn-danger" style="border:none; padding: 0.5rem 1rem;" onclick="handleReject(${b.bookingId || b.id})">❌ Từ Chối</button>
                    </div>
                `;
            }

            const item = document.createElement('div');
            item.className = 'booking-item';
            item.style.borderLeft = "5px solid " + (b.bookingStatus === 'PENDING' ? '#ef4444' : '#10b981');
            item.innerHTML = `
                <div class="booking-details" style="text-align: left; width: 100%; padding: 0.5rem;">
                    <h4>🚗 Mã đơn đặt xe: #${b.bookingId || b.id}</h4>
                    <p>📅 Thuê từ: <strong>${b.startDate}</strong> đến <strong>${b.endDate}</strong></p>
                    <p>💰 Số tiền: <strong style="color:var(--primary);">${formatVND(b.totalAmount)}</strong></p>
                    <span class="booking-badge ${statusClass}">${b.bookingStatus}</span>
                    ${actionButtons}
                </div>
            `;
            container.appendChild(item);
        });

        checkPendingBookingsAlert(ownerId);
    } catch (err) {
        container.innerHTML = `<div class="status-message" style="color:var(--danger);">❌ Lỗi: ${err.message}</div>`;
    }
}

async function handleApprove(bookingId) {
    if (!confirm("Bạn có chắc chắn muốn XÁC NHẬN cho khách hàng này thuê xe?")) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/approve`, { method: 'POST' });
        if (!response.ok) throw new Error("Thao tác thất bại");
        showToast("Đã chấp nhận đơn đặt xe!");
        loadOwnerBookings();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function handleReject(bookingId) {
    if (!confirm("Bạn có chắc chắn muốn TỪ CHỐI đơn đặt xe này?")) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/reject`, { method: 'POST' });
        if (!response.ok) throw new Error("Thao tác thất bại");
        showToast("Đã từ chối đơn hàng thành công.", "error");
        loadOwnerBookings();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadVehicleDetails(vehicleId) {
    const detailsBody = document.getElementById('vehicleDetailsBody');
    if (!detailsBody) return;

    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles/${vehicleId}`);
        if (!response.ok) throw new Error('Không thể lấy thông tin chi tiết xe từ server.');

        const vehicle = await response.json();
        window.currentVehicleData = vehicle;

        const isCar = vehicle.seatNumber !== undefined;
        document.getElementById('vehicleIcon').innerText = isCar ? '🚗' : '🛵';
        document.getElementById('vehicleTypeBadge').innerText = isCar ? `${vehicle.seatNumber} CHỖ` : `${vehicle.engineCapacity} CC`;
        document.getElementById('vehicleName').innerText = `${vehicle.brand} ${vehicle.model}`;
        document.getElementById('vehiclePriceHero').innerText = `${formatVND(vehicle.pricePerDay)} / ngày`;

        const locationStr = vehicle.location ? `${vehicle.location.district}, ${vehicle.location.city}` : 'Chưa cập nhật';
        detailsBody.innerHTML = `
            <div style="text-align: left; line-height: 1.8;">
                <p>📍 <strong>Khu vực:</strong> ${locationStr}</p>
                <p>🔢 <strong>Biển số xe:</strong> ${vehicle.licensePlate || '---'}</p>
                <p>⚡ <strong>Trạng thái:</strong> <span class="booking-badge confirmed">${vehicle.vehicleStatus}</span></p>
                <p style="margin-top: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1rem; color: #4b5563;">
                    📝 <strong>Mô tả:</strong> ${vehicle.description || 'Không có mô tả chi tiết cho phương tiện này.'}
                </p>
            </div>
        `;

        const userStr = localStorage.getItem('user');
        if (userStr) {
            const user = JSON.parse(userStr);
            document.getElementById('loggedInUserSummary').style.display = 'block';
            document.getElementById('userSummaryName').innerText = user.fullName;
            document.getElementById('userSummaryEmail').innerText = user.email;
            document.getElementById('renterId').value = user.userId;
        }

    } catch (err) {
        detailsBody.innerHTML = `<div class="status-message" style="color:var(--danger);">❌ Lỗi: ${err.message}</div>`;
    }
}

function calculatePrice() {
    const startDateVal = document.getElementById('startDate').value;
    const endDateVal = document.getElementById('endDate').value;
    const btnSubmit = document.getElementById('btnSubmit');
    const priceBreakdown = document.getElementById('priceBreakdown');

    if (!startDateVal || !endDateVal || !window.currentVehicleData) {
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<span>🔒</span> Vui Lòng Chọn Ngày';
        }
        if (priceBreakdown) priceBreakdown.style.display = 'none';
        return;
    }

    const start = new Date(startDateVal);
    const end = new Date(endDateVal);

    if (end < start) {
        showToast('Ngày trả xe không thể trước ngày nhận xe!', 'error');
        document.getElementById('endDate').value = '';
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<span>🔒</span> Vui Lòng Chọn Ngày';
        }
        if (priceBreakdown) priceBreakdown.style.display = 'none';
        return;
    }

    const timeDiff = end.getTime() - start.getTime();
    let numDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
    if (numDays === 0) numDays = 1;

    const pricePerDay = window.currentVehicleData.pricePerDay;
    const totalAmount = numDays * pricePerDay;

    if (priceBreakdown) priceBreakdown.style.display = 'block';
    document.getElementById('pricePerDayDisplay').innerText = formatVND(pricePerDay);
    document.getElementById('numDaysDisplay').innerText = `${numDays} ngày`;
    document.getElementById('totalPriceDisplay').innerText = formatVND(totalAmount);

    if (btnSubmit) {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = '⚡ Gửi Yêu Cầu Đặt Xe';
    }
}

async function submitBookingDirect() {
    const btnSubmit = document.getElementById('btnSubmit');
    const renterId = document.getElementById('renterId').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!window.currentVehicleData || !renterId) {
        showToast('Vui lòng đăng nhập lại tài khoản Renter trước khi đặt!', 'error');
        return;
    }

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerText = '⏳ Đang gửi yêu cầu...';
    }

    const payload = {
        renterId: parseInt(renterId),
        vehicleId: window.currentVehicleData.vehicleId,
        startDate: startDate,
        endDate: endDate
    };

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Không thể tạo đơn hàng, xe có thể đã bị trùng lịch đặt.');
        }

        const newBooking = await response.json();

        document.getElementById('formContent').style.display = 'none';
        document.getElementById('successScreen').style.display = 'block';
        document.getElementById('successBookingId').innerText = `Mã đặt xe: #${newBooking.bookingId || newBooking.id || 'SUCCESS'}`;
        showToast('Yêu cầu đặt xe của bạn đã được gửi thành công!');

    } catch (err) {
        showToast(err.message, 'error');
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerText = '⚡ Gửi Yêu Cầu Đặt Xe';
        }
    }
}

async function submitNewVehicle(e) {
    e.preventDefault();
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        showToast("Vui lòng đăng nhập tài khoản trước khi thực hiện!", "error");
        return;
    }
    const user = JSON.parse(userStr);
    const ownerId = user.userId || user.id;

    const payload = {
        brand: document.getElementById('vehBrand').value,
        model: document.getElementById('vehModel').value,
        licensePlate: document.getElementById('vehPlate').value,
        pricePerDay: parseFloat(document.getElementById('vehPrice').value),
        description: document.getElementById('vehDesc').value,
        vehicleStatus: "PENDING",
        ownerId: parseInt(ownerId)
    };

    const seats = document.getElementById('vehSeats').value;
    if(seats) payload.seatNumber = parseInt(seats);

    const capacity = document.getElementById('vehCapacity').value;
    if(capacity) payload.engineCapacity = capacity;

    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error("Không thể lưu thông tin đăng ký xe.");

        showToast("🚀 Đăng ký xe thành công! Hãy đợi Admin xét duyệt.");
        document.getElementById('registerVehicleForm').reset();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function loadAdminVehicleApproval() {
    const container = document.getElementById('adminVehicleList');
    if (!container) return;

    container.innerHTML = '<div class="status-message">🔄 Đang kiểm tra danh sách xe chờ cấp phép...</div>';

    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles`);
        if (!response.ok) throw new Error("Lỗi tải thông tin danh mục xe.");

        const vehicles = await response.json();
        const pendingVehicles = vehicles.filter(v => v.vehicleStatus === 'PENDING');
        pendingVehicles.sort((a, b) => (b.vehicleId || b.id) - (a.vehicleId || a.id));

        container.innerHTML = '';

        if (pendingVehicles.length === 0) {
            container.innerHTML = '<div class="status-message" style="color: #10b981;">🎉 Toàn bộ xe đã được xử lý xong! Không có xe chờ duyệt.</div>';
            return;
        }

        pendingVehicles.forEach(v => {
            const item = document.createElement('div');
            item.className = 'booking-item';
            item.style.display = 'flex';
            item.style.justifyContent = 'space-between';
            item.style.alignItems = 'center';
            item.style.padding = '1.25rem';
            item.style.borderLeft = '5px solid #f59e0b';

            item.innerHTML = `
                <div style="text-align: left;">
                    <h4 style="font-size: 1.1rem; color: var(--primary); margin: 0 0 0.5rem 0;">📋 Yêu cầu cấp phép xe: ${v.brand} ${v.model}</h4>
                    <p style="margin: 0.2rem 0;">🔢 Biển số xe: <strong>${v.licensePlate}</strong></p>
                    <p style="margin: 0.2rem 0;">💰 Đơn giá đề xuất: <strong>${formatVND(v.pricePerDay)}/ngày</strong></p>
                    <p style="margin: 0.2rem 0; color:#64748b;">📝 Ghi chú: <small>${v.description || 'Không có mô tả'}</small></p>
                    <span class="booking-badge pending" style="margin-top:0.5rem; display:inline-block;">CHỜ ADMIN DUYỆT</span>
                </div>
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn" style="background-color: #10b981; color: white; padding: 0.5rem 1rem; border:none;" onclick="handleApproveVehicle(${v.vehicleId || v.id})">✅ Duyệt Xe</button>
                    <button class="btn btn-danger" style="padding: 0.5rem 1rem; border:none;" onclick="handleRejectVehicle(${v.vehicleId || v.id})">❌ Từ Chối</button>
                </div>
            `;
            container.appendChild(item);
        });

    } catch (err) {
        container.innerHTML = `<div class="status-message" style="color:var(--danger);">❌ Lỗi: ${err.message}</div>`;
    }
}

async function handleApproveVehicle(vehicleId) {
    if (!confirm("Xác nhận duyệt đưa phương tiện này công khai lên trang chủ?")) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles/${vehicleId}/status?status=AVAILABLE`, { method: 'PUT' });
        if (!response.ok) throw new Error("Lỗi hệ thống duyệt xe.");
        showToast("Đã duyệt thành công! Khách hàng có thể nhìn thấy xe.");
        loadAdminVehicleApproval();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function handleRejectVehicle(vehicleId) {
    if (!confirm("Bạn có chắc chắn từ chối hồ sơ xe này không?")) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/vehicles/${vehicleId}/status?status=REJECTED`, { method: 'PUT' });
        if (!response.ok) throw new Error("Lỗi thao tác.");
        showToast("Đã từ chối cấp phép cho xe.");
        loadAdminVehicleApproval();
    } catch (e) {
        showToast(e.message, 'error');
    }
}