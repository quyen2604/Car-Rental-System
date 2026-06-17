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
        if (myBookingsLink) {
            myBookingsLink.style.display = 'inline-block';
            const isOwner = (user.licenseNumber === undefined || user.licenseNumber === null);
            if (isOwner) {
                myBookingsLink.innerText = 'Quản Lý Đơn Thuê';
            }
        }
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

async function loadRenterBookings() {
    const userStr = localStorage.getItem('user');
    const bookingList = document.getElementById('bookingList');
    const bookingsSectionTitle = document.getElementById('bookingsSectionTitle');
    if (!bookingList) return;

    if (!userStr) {
        bookingList.innerHTML = '<div class="status-message">Vui lòng đăng nhập để xem lịch sử đặt xe.</div>';
        return;
    }

    const user = JSON.parse(userStr);
    const isOwner = (user.licenseNumber === undefined || user.licenseNumber === null);

    if (bookingsSectionTitle) {
        bookingsSectionTitle.innerText = isOwner ? '🛠️ Quản Lý Đơn Thuê Của Bạn' : '📂 Lịch Sử Chuyến Đi Của Bạn';
    }

    bookingList.innerHTML = '<div class="status-message">🔄 Đang tải lịch sử đặt xe...</div>';

    try {
        const endpoint = isOwner ? `/bookings/owner/${user.userId}` : `/bookings/renter/${user.userId}`;
        const response = await fetch(`${window.API_BASE_URL}${endpoint}`);
        if (!response.ok) throw new Error('Không thể tải danh sách đặt xe.');

        const bookings = await response.json();
        bookingList.innerHTML = '';

        if (bookings.length === 0) {
            bookingList.innerHTML = '<div class="status-message">📭 Hiện tại chưa có đơn đặt xe nào.</div>';
            return;
        }

        bookings.forEach(b => {
            const statusClass = b.bookingStatus.toLowerCase();
            const statusText = b.bookingStatus === 'CONFIRMED' ? 'Đã xác nhận' : 
                               (b.bookingStatus === 'PENDING' ? 'Chờ xác nhận' : 'Đã hủy');
            
            const item = document.createElement('div');
            item.className = 'booking-item';
            
            let actionButtons = '';
            let statusDisplay = statusText;

            if (b.bookingStatus === 'CONFIRMED') {
                if (isOwner) {
                    statusDisplay = 'Đã duyệt, chờ khách cọc';
                } else {
                    statusDisplay = 'Đã duyệt, hãy cọc';
                    actionButtons = `
                        <button class="btn btn-primary" onclick="payDeposit(${b.bookingId})">Thanh toán cọc</button>
                        <button class="btn btn-danger" onclick="cancelBooking(${b.bookingId}, 'RENTER')" style="margin-left: 0.5rem;">Hủy đặt xe</button>
                    `;
                }
            } else if (b.bookingStatus === 'DEPOSIT_PAID') {
                statusDisplay = 'Đã cọc';
                if (!isOwner) {
                    actionButtons = `
                        <button class="btn btn-danger" onclick="cancelBooking(${b.bookingId}, 'RENTER')">Hủy đặt xe</button>
                    `;
                } else {
                    actionButtons = `
                        <button class="btn btn-primary" onclick="pickUpVehicle(${b.bookingId})">Bàn giao xe</button>
                        <button class="btn btn-danger" onclick="cancelBooking(${b.bookingId}, 'OWNER')" style="margin-left: 0.5rem;">Hủy đặt xe (Hoàn 100%)</button>
                    `;
                }
            } else if (b.bookingStatus === 'RENTING') {
                statusDisplay = 'Đang cho thuê';
                if (isOwner) {
                    actionButtons = `
                        <button class="btn btn-primary" onclick="returnVehicle(${b.bookingId})">Xác nhận nhận lại xe</button>
                    `;
                }
            } else if (b.bookingStatus === 'RETURNED') {
                statusDisplay = 'Đã trả xe';
                if (!isOwner) {
                    actionButtons = `
                        <button class="btn btn-primary" onclick="openFinalPaymentModal(${b.bookingId})">💳 Thanh Toán & Hoàn Thành</button>
                    `;
                } else {
                    statusDisplay = 'Đã nhận xe, chờ thanh toán';
                }
            } else if (b.bookingStatus === 'COMPLETED') {
                statusDisplay = 'Đã hoàn thành';
            } else if (b.bookingStatus === 'PENDING') {
                if (isOwner) {
                    actionButtons = `
                        <button class="btn btn-primary" onclick="approveBooking(${b.bookingId})">Duyệt Đơn</button>
                        <button class="btn btn-danger" onclick="rejectBooking(${b.bookingId})" style="margin-left: 0.5rem;">Từ Chối</button>
                    `;
                } else {
                    actionButtons = `
                        <button class="btn btn-danger" onclick="cancelBooking(${b.bookingId}, 'RENTER')">Hủy đặt xe</button>
                    `;
                }
            }

            item.innerHTML = `
                <div class="booking-avatar">🚗</div>
                <div class="booking-details">
                    <h4>${b.vehicleBrand} ${b.vehicleModel}</h4>
                    <p>📍 Biển số: <strong>${b.licensePlate}</strong></p>
                    <p>👤 Người thuê: <strong>${b.renterName}</strong></p>
                    <p>📅 Thời gian: từ <strong>${formatDate(b.startDate)}</strong> đến <strong>${formatDate(b.endDate)}</strong></p>
                    <p>💰 Tổng cộng: <strong style="color:var(--primary); font-size:1.1rem;">${formatVND(b.totalAmount)}</strong> (${formatVND(b.pricePerDay)}/ngày)</p>
                    <span class="booking-badge ${statusClass}">${statusDisplay}</span>
                    ${b.refundAmount > 0 ? `<p style="color: green; margin-top: 5px;">💸 Số tiền hoàn trả: <strong>${formatVND(b.refundAmount)}</strong></p>` : ''}
                </div>
                <div>
                    ${actionButtons}
                </div>
            `;
            bookingList.appendChild(item);
        });
    } catch (err) {
        bookingList.innerHTML = `<div class="status-message" style="color:var(--danger);">${err.message}</div>`;
    }
}

// Hủy đặt xe
async function cancelBooking(id, role = 'RENTER') {
    let confirmMsg = 'Bạn có chắc chắn muốn hủy đơn đặt xe này không?';
    if (role === 'RENTER') {
        confirmMsg += '\nLưu ý: Nếu đơn đã thanh toán cọc, tiền hoàn sẽ được tính theo quy định (<24h mất cọc, >48h hoàn 100%).';
    }
    if (!confirm(confirmMsg)) return;

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${id}/cancel?role=${role}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error(await response.text());
        showToast('Đã hủy đặt xe thành công.');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ================================================================
// PAYMENT MODAL SYSTEM
// ================================================================

// State object để lưu thông tin thanh toán hiện tại
const paymentState = {
    deposit: { bookingId: null, amount: 0, method: 'MOMO', bookingData: null },
    final:   { bookingId: null, amount: 0, method: 'MOMO', bookingData: null }
};

// Mở modal thanh toán cọc
function payDeposit(bookingId) {
    // Lấy thông tin booking từ danh sách hiện tại
    fetch(`${window.API_BASE_URL}/bookings/${bookingId}`)
        .then(r => r.json())
        .then(b => {
            const depositAmt = b.totalAmount * 0.30;  // Cọc 30%
            paymentState.deposit.bookingId = bookingId;
            paymentState.deposit.amount    = depositAmt;
            paymentState.deposit.bookingData = b;

            // Điền thông tin vào modal
            document.getElementById('dep-vehicle-name').textContent =
                `${b.vehicleBrand || ''} ${b.vehicleModel || ''}`.trim() || 'Thông tin xe';
            document.getElementById('dep-booking-dates').textContent =
                `📅 Mã đơn: #${b.bookingId} | ${formatDate(b.startDate)} → ${formatDate(b.endDate)}`;
            document.getElementById('dep-amount-display').textContent = formatVND(depositAmt);
            document.getElementById('dep-total-amount').textContent   = formatVND(b.totalAmount);
            document.getElementById('dep-pay-now').textContent        = formatVND(depositAmt);

            // Reset modal về trạng thái form
            resetPaymentModal('dep');
            openPaymentModal('depositModal');
        })
        .catch(() => showToast('Không thể tải thông tin đơn đặt xe.', 'error'));
}

// Mở modal thanh toán tổng (khi booking ở trạng thái RETURNED)
function openFinalPaymentModal(bookingId) {
    fetch(`${window.API_BASE_URL}/bookings/${bookingId}`)
        .then(r => r.json())
        .then(b => {
            const deposited  = b.totalAmount * 0.30;
            const remaining  = b.totalAmount - deposited + (b.extraFee || 0);
            paymentState.final.bookingId = bookingId;
            paymentState.final.amount    = remaining;
            paymentState.final.bookingData = b;

            document.getElementById('fin-vehicle-name').textContent =
                `${b.vehicleBrand || ''} ${b.vehicleModel || ''}`.trim() || 'Thông tin xe';
            document.getElementById('fin-booking-dates').textContent =
                `📅 Mã đơn: #${b.bookingId} | ${formatDate(b.startDate)} → ${formatDate(b.endDate)}`;
            document.getElementById('fin-amount-display').textContent = formatVND(remaining);
            document.getElementById('fin-total-amount').textContent   = formatVND(b.totalAmount);
            document.getElementById('fin-deposited').textContent      = formatVND(deposited);
            document.getElementById('fin-extra-fee').textContent      = formatVND(b.extraFee || 0);
            document.getElementById('fin-pay-now').textContent        = formatVND(remaining);

            resetPaymentModal('fin');
            openPaymentModal('finalModal');
        })
        .catch(() => showToast('Không thể tải thông tin đơn đặt xe.', 'error'));
}

// Xử lý submit thanh toán cọc
async function submitDepositPayment() {
    const { bookingId, amount, method } = paymentState.deposit;
    if (!bookingId) return;

    setPaymentLoading('dep', true);
    try {
        const res = await fetch(`${window.API_BASE_URL}/payments/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookingId, amount, method, note: 'Thanh toán cọc 30%' })
        });

        if (!res.ok) throw new Error(await res.text());
        const data = await res.json();

        if (data.payUrl) {
            window.location.href = data.payUrl;
            return;
        }

        showPaymentResult('dep', true,
            '✅ Thanh Toán Thành Công!',
            `Tiền cọc ${formatVND(amount)} đã được xác nhận qua ${method}.\nĐơn đặt xe đang chờ bàn giao.`,
            data.transactionId || 'DEP-' + bookingId
        );
    } catch (err) {
        showPaymentResult('dep', false, '❌ Thanh Toán Thất Bại', err.message, null);
    } finally {
        setPaymentLoading('dep', false);
    }
}

// Xử lý submit thanh toán tổng
async function submitFinalPayment() {
    const { bookingId, amount, method } = paymentState.final;
    if (!bookingId) return;

    setPaymentLoading('fin', true);
    try {
        const res = await fetch(`${window.API_BASE_URL}/payments/final`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookingId, amount, method, note: 'Thanh toán tổng kết thúc chuyến' })
        });

        if (!res.ok) throw new Error(await res.text());
        const data = await res.json();

        if (data.payUrl) {
            window.location.href = data.payUrl;
            return;
        }

        showPaymentResult('fin', true,
            '🎉 Chuyến Đi Hoàn Thành!',
            `Cảm ơn bạn đã sử dụng CarRental!\nTổng đã thanh toán: ${formatVND(amount)}.`,
            data.transactionId || 'FIN-' + bookingId
        );
    } catch (err) {
        showPaymentResult('fin', false, '❌ Thanh Toán Thất Bại', err.message, null);
    } finally {
        setPaymentLoading('fin', false);
    }
}

// Chọn phương thức thanh toán trong modal
function selectPaymentMethod(prefix, method) {
    // Cập nhật state
    if (prefix === 'dep') paymentState.deposit.method = method;
    else paymentState.final.method = method;

    // Reset tất cả cards
    const momoCard = document.getElementById(`${prefix}-method-momo`);
    const cashCard = document.getElementById(`${prefix}-method-cash`);
    momoCard.classList.remove('selected', 'green-select');
    cashCard.classList.remove('selected', 'green-select');

    // Highlight card được chọn
    const isGreen = (prefix === 'fin');
    const selectedCard = method === 'MOMO' ? momoCard : cashCard;
    selectedCard.classList.add('selected');
    if (isGreen) selectedCard.classList.add('green-select');
}

// Hiển thị kết quả thanh toán (success / error)
function showPaymentResult(prefix, success, title, sub, txnId) {
    document.getElementById(`${prefix}-form-section`).style.display = 'none';
    const resultEl = document.getElementById(`${prefix}-result-section`);
    resultEl.classList.add('visible');

    document.getElementById(`${prefix}-result-icon`).textContent  = success ? (prefix === 'dep' ? '✅' : '🎉') : '❌';
    const titleEl = document.getElementById(`${prefix}-result-title`);
    titleEl.textContent = title;
    titleEl.className   = `pm-result-title ${success ? 'success' : 'error'}`;
    document.getElementById(`${prefix}-result-sub`).textContent   = sub;
    const txnEl = document.getElementById(`${prefix}-txn-id`);
    txnEl.style.display = txnId ? 'block' : 'none';
    if (txnId) txnEl.textContent = 'TXN: ' + txnId;

    if (success) showToast(title);
    else showToast(sub, 'error');
}

// Bật / tắt trạng thái loading trên nút thanh toán
function setPaymentLoading(prefix, loading) {
    const btn     = document.getElementById(`${prefix}-pay-btn`);
    const spinner = document.getElementById(`${prefix}-spinner`);
    const text    = document.getElementById(`${prefix}-btn-text`);
    btn.disabled          = loading;
    spinner.style.display = loading ? 'block' : 'none';
    text.style.display    = loading ? 'none'  : 'inline';
}

// Reset modal về trạng thái mặc định (form visible, result hidden)
function resetPaymentModal(prefix) {
    const formSection   = document.getElementById(`${prefix}-form-section`);
    const resultSection = document.getElementById(`${prefix}-result-section`);
    if (formSection)   formSection.style.display = 'block';
    if (resultSection) resultSection.classList.remove('visible');
    setPaymentLoading(prefix, false);
    // Reset method về MOMO mặc định
    selectPaymentMethod(prefix, 'MOMO');
}

// Mở overlay modal
function openPaymentModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) {
        el.classList.add('active');
        // Đóng khi click bên ngoài
        el.onclick = (e) => { if (e.target === el) closePaymentModal(modalId); };
    }
}

// Đóng overlay modal
function closePaymentModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('active');
}



// Bàn giao xe (Owner)
async function pickUpVehicle(id) {
    if (!confirm('Xác nhận đã bàn giao xe cho khách?')) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${id}/pick-up`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error(await response.text());
        showToast('Đã cập nhật trạng thái Bàn Giao Xe!');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Trả xe & Kiểm tra thiệt hại (Owner)
async function returnVehicle(id) {
    let lateFee = prompt("Nhập số tiền phụ phí trễ giờ (nếu không có, để trống hoặc nhập 0):", "0");
    if (lateFee === null) return; // Hủy bỏ
    
    let damageFee = prompt("Nhập số tiền bồi thường thiệt hại (nếu không có, để trống hoặc nhập 0):", "0");
    if (damageFee === null) return;

    lateFee = parseFloat(lateFee) || 0;
    damageFee = parseFloat(damageFee) || 0;

    if (!confirm(`Xác nhận nhận lại xe với Phụ phí trễ giờ: ${formatVND(lateFee)} và Phí bồi thường: ${formatVND(damageFee)}?`)) return;

    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${id}/return?lateFee=${lateFee}&damageFee=${damageFee}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error(await response.text());
        showToast('Đã xác nhận nhận lại xe thành công!');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Hoàn thành đơn & Thanh toán (Renter)
async function completeBooking(id) {
    if (!confirm('Bạn có chắc chắn muốn thanh toán hóa đơn và hoàn thành chuyến đi này?')) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${id}/complete`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error(await response.text());
        showToast('Đã thanh toán và hoàn thành chuyến đi! Cảm ơn bạn.');
        loadRenterBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Phê duyệt đặt xe (Owner)
async function approveBooking(bookingId) {
    if (!confirm('Bạn có chắc chắn muốn duyệt đơn đặt xe này không?')) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/approve`, { method: 'POST' });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Lỗi khi duyệt đơn');
        }
        showToast('Đã duyệt đơn đặt xe thành công!');
        loadRenterBookings();
    } catch (err) { showToast(err.message, 'error'); }
}

// Từ chối đặt xe (Owner)
async function rejectBooking(bookingId) {
    if (!confirm('Bạn có chắc chắn muốn từ chối đơn đặt xe này không?')) return;
    try {
        const response = await fetch(`${window.API_BASE_URL}/bookings/${bookingId}/reject`, { method: 'POST' });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Lỗi khi từ chối đơn');
        }
        showToast('Đã từ chối đơn đặt xe thành công!');
        loadRenterBookings();
    } catch (err) { showToast(err.message, 'error'); }
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

    const hasPet = document.getElementById('hasPet') && document.getElementById('hasPet').checked;
    const hasGPS = document.getElementById('hasGPS') && document.getElementById('hasGPS').checked;
    const hasBabySeat = document.getElementById('hasBabySeat') && document.getElementById('hasBabySeat').checked;
    const hasDashcam = document.getElementById('hasDashcam') && document.getElementById('hasDashcam').checked;

    let total = diffDays * vehicleData.pricePerDay;
    if (hasPet) total += 150000;
    if (hasGPS) total += 50000;
    if (hasBabySeat) total += 100000;
    if (hasDashcam) total += 80000;

    if (priceBreakdown) {
        priceBreakdown.style.display = 'block';
        document.getElementById('pricePerDayDisplay').innerText = formatVND(vehicleData.pricePerDay);
        document.getElementById('numDaysDisplay').innerText = `${diffDays} ngày`;
        
        const petFeeRow = document.getElementById('petFeeRow');
        if (petFeeRow) petFeeRow.style.display = hasPet ? 'flex' : 'none';

        const gpsFeeRow = document.getElementById('gpsFeeRow');
        if (gpsFeeRow) gpsFeeRow.style.display = hasGPS ? 'flex' : 'none';

        const babySeatFeeRow = document.getElementById('babySeatFeeRow');
        if (babySeatFeeRow) babySeatFeeRow.style.display = hasBabySeat ? 'flex' : 'none';

        const dashcamFeeRow = document.getElementById('dashcamFeeRow');
        if (dashcamFeeRow) dashcamFeeRow.style.display = hasDashcam ? 'flex' : 'none';
        
        document.getElementById('totalPriceDisplay').innerText = formatVND(total);
    }
}

async function submitBookingDirect() {
    // 1. Lấy dữ liệu từ giao diện
    const renterId = document.getElementById('renterId').value;
    const startDate = document.getElementById('startDate').value; // Trình duyệt sẽ tự lấy chuỗi "yyyy-MM-dd"
    const endDate = document.getElementById('endDate').value;     // Trình duyệt sẽ tự lấy chuỗi "yyyy-MM-dd"

    // Kiểm tra xem đã chọn ngày chưa
    if (!startDate || !endDate) {
        alert("Vui lòng chọn đầy đủ ngày nhận và ngày trả xe!");
        return;
    }

    // 2. Tính toán tiền cọc (Giả sử cọc 50% tổng chi phí - bạn có thể đổi logic này)
    // Lưu ý: Hàm calculatePrice() của bạn phải gán giá trị tổng tiền vào đâu đó,
    // ở đây mình tự tính lại dựa trên số ngày để gửi đi cho chắc chắn
    const start = new Date(startDate);
    const end = new Date(endDate);
    const timeDiff = end.getTime() - start.getTime();
    let days = Math.ceil(timeDiff / (1000 * 3600 * 24));
    if (days <= 0) days = 1;

    const hasPet = document.getElementById('hasPet') && document.getElementById('hasPet').checked;
    const hasGPS = document.getElementById('hasGPS') && document.getElementById('hasGPS').checked;
    const hasBabySeat = document.getElementById('hasBabySeat') && document.getElementById('hasBabySeat').checked;
    const hasDashcam = document.getElementById('hasDashcam') && document.getElementById('hasDashcam').checked;

    // Giả sử xe giá 700k/ngày, tiền cọc = (ngày * 700k) / 2
    // Bạn nên lấy giá xe từ biến vehicleData có sẵn trong file của bạn
    const pricePerDay = vehicleData ? vehicleData.pricePerDay : 700000;
    let totalAmount = days * pricePerDay;
    if (hasPet) totalAmount += 150000;
    if (hasGPS) totalAmount += 50000;
    if (hasBabySeat) totalAmount += 100000;
    if (hasDashcam) totalAmount += 80000;
    
    const depositAmount = totalAmount * 0.5; // Cọc 50%

    const couponCodeInput = document.getElementById('couponCode');
    const couponCode = couponCodeInput ? couponCodeInput.value.trim() : "";

    // 3. Đóng gói dữ liệu thành chuẩn JSON
    const requestBody = {
        renterId: parseInt(renterId) || 1, // Fix cứng ID=1 nếu chưa làm chức năng đăng nhập
        vehicleId: parseInt(vehicleId),    // Lấy từ URL (đã có sẵn trong html của bạn)
        startDate: startDate,              // Gửi đi chuỗi "yyyy-MM-dd"
        endDate: endDate,                  // Gửi đi chuỗi "yyyy-MM-dd"
        deposit: depositAmount,            // Gửi kèm tiền cọc
        couponCode: couponCode,            // Thêm mã giảm giá
        hasPet: hasPet,                     // Mang theo thú cưng
        hasGPS: hasGPS,                     // Định vị GPS
        hasBabySeat: hasBabySeat,           // Ghế trẻ em
        hasDashcam: hasDashcam              // Camera hành trình
    };

    // 4. Gọi API gửi xuống Backend
    try {
        const response = await fetch('http://localhost:8080/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // BẮT BUỘC để báo cho server biết đây là JSON
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            // Thành công: Hiển thị màn hình báo thành công
            document.getElementById('formContent').style.display = 'none';
            document.getElementById('successScreen').style.display = 'block';
            document.getElementById('successBookingId').innerText = 'Mã đặt xe: #' + data.bookingId;
        } else {
            // Lỗi từ server trả về (400, 500...)
            const errorMsg = await response.text();
            alert("Lỗi đặt xe: " + errorMsg);
        }
    } catch (error) {
        console.error("Lỗi kết nối:", error);
        alert("Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại server Spring Boot.");
    }
}