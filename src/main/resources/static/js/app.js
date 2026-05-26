// ============================================================
//  app.js - Car Rental System Frontend Logic
// ============================================================

// Base URL cho API - Tự động nhận diện chạy từ file:// (local) hay trên Web Server
const API_URL = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';

// Hàm tiện ích format tiền tệ VNĐ
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

let bookingDepositAmount = 0;

// ============================================================
//  SEARCH PAGE - Tìm kiếm xe
// ============================================================

// Hàm xử lý tìm kiếm xe
async function searchVehicles(event) {
    if (event) event.preventDefault();

    const type = document.getElementById('type').value;
    const location = document.getElementById('location').value;

    let url = `${API_URL}/vehicles/search?`;
    if (type) url += `type=${encodeURIComponent(type)}&`;
    if (location) url += `location=${encodeURIComponent(location)}`;

    const resultsContainer = document.getElementById('results');
    resultsContainer.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 3rem;">
            <div style="display:inline-block; width:40px; height:40px; border:3px solid #f3f3f3; border-top:3px solid #4361ee; border-radius:50%; animation:spin 1s linear infinite;"></div>
            <p style="margin-top:1rem; color:#8d99ae;">Đang tải dữ liệu...</p>
        </div>
    `;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Không thể fetch dữ liệu');
        const data = await response.json();
        renderVehicles(data);
    } catch (error) {
        console.error('Error fetching vehicles:', error);
        resultsContainer.innerHTML = '<p style="text-align:center;width:100%;color:red;grid-column:1/-1;">⚠️ Đã xảy ra lỗi khi tải dữ liệu. Vui lòng kiểm tra server.</p>';
    }
}

// Hàm render danh sách xe ra HTML
function renderVehicles(vehicles) {
    const resultsContainer = document.getElementById('results');

    if (!vehicles || vehicles.length === 0) {
        resultsContainer.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: white; border-radius: 12px;">
                <h3 style="color: #64748b;">Không tìm thấy xe nào phù hợp.</h3>
                <p style="color: #94a3b8; margin-top: 0.5rem;">Hãy thử tìm kiếm với từ khóa khác.</p>
            </div>
        `;
        return;
    }

    let html = '';
    vehicles.forEach(v => {
        const isCar = v.vehicleType === 'CAR';
        const icon = isCar ? '🚗' : '🏍️';
        const vehicleName = `${v.brand} ${v.model}`;
        const typeLabel = isCar ? 'Ô Tô' : 'Xe Máy';

        let featuresHtml = `
            <span class="feature-tag">📍 ${v.address || 'Chưa cập nhật'}</span>
            <span class="feature-tag">🏢 ${v.brand}</span>
        `;

        if (isCar) {
            if (v.seatCount) featuresHtml += `<span class="feature-tag">💺 ${v.seatCount} chỗ</span>`;
            if (v.transmission) featuresHtml += `<span class="feature-tag">⚙️ ${v.transmission === 'AUTOMATIC' ? 'Tự động' : 'Số sàn'}</span>`;
        } else {
            if (v.engineCapacity) featuresHtml += `<span class="feature-tag">⚡ ${v.engineCapacity}cc</span>`;
        }

        html += `
            <div class="vehicle-card">
                <div class="vehicle-image">${icon}</div>
                <div class="vehicle-content">
                    <div class="vehicle-header">
                        <div class="vehicle-title">${vehicleName}</div>
                        <div class="vehicle-badge">${typeLabel}</div>
                    </div>
                    <div class="vehicle-price">
                        ${formatCurrency(v.pricePerDay)} <span>/ ngày</span>
                    </div>
                    <div class="vehicle-features">${featuresHtml}</div>
                    <p style="font-size:0.875rem; color:#64748b; margin-bottom: 1.5rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
                        ${v.description || 'Không có mô tả.'}
                    </p>
                    <button class="btn btn-primary" onclick="goToBooking(${v.id})">🗓️ Đặt Thuê Ngay</button>
                </div>
            </div>
        `;
    });

    resultsContainer.innerHTML = html;
}

// Điều hướng sang trang đặt xe kèm vehicleId
function goToBooking(vehicleId) {
    window.location.href = `/booking.html?vehicleId=${vehicleId}`;
}

// Tự động load khi vào search.html
if (window.location.pathname.includes('search.html')) {
    document.addEventListener('DOMContentLoaded', () => {
        searchVehicles();
    });
}

// ============================================================
//  BOOKING PAGE - Đặt xe
// ============================================================

// Load thông tin chi tiết xe để hiển thị trong trang booking
async function loadVehicleDetails(vehicleId) {
    try {
        const response = await fetch(`${API_URL}/vehicles/${vehicleId}`);
        if (!response.ok) throw new Error('Không tìm thấy xe');
        vehicleData = await response.json();
        renderVehicleSummary(vehicleData);
    } catch (error) {
        console.error('Error loading vehicle:', error);
        document.getElementById('vehicleName').textContent = 'Không tìm thấy xe!';
        document.getElementById('vehicleDetailsBody').innerHTML =
            '<p style="color:red; padding:1rem;">❌ Không thể tải thông tin xe. Vui lòng quay lại trang tìm kiếm.</p>';
    }
}

// Render thông tin xe lên card summary
function renderVehicleSummary(v) {
    const isCar = v.vehicleType === 'CAR';
    document.getElementById('vehicleIcon').textContent = isCar ? '🚗' : '🏍️';
    document.getElementById('vehicleTypeBadge').textContent = isCar ? 'Ô Tô' : 'Xe Máy';
    document.getElementById('vehicleName').textContent = `${v.brand} ${v.model}`;
    document.getElementById('vehiclePriceHero').innerHTML = `${formatCurrency(v.pricePerDay)} <span style="font-size:0.9rem;font-weight:400;opacity:0.8;">/ ngày</span>`;

    let detailsHtml = `
        <div class="detail-row"><span class="detail-icon">📍</span> <span>${v.address || 'Chưa cập nhật địa chỉ'}</span></div>
        <div class="detail-row"><span class="detail-icon">🔖</span> <span>Biển số: <strong>${v.licensePlate}</strong></span></div>
        <div class="detail-row"><span class="detail-icon">💰</span> <span>Tiền cọc: <strong>30% Tổng tiền thuê</strong></span></div>
    `;

    if (isCar && v.seatCount) {
        detailsHtml += `<div class="detail-row"><span class="detail-icon">💺</span> <span>${v.seatCount} chỗ ngồi</span></div>`;
    }
    if (isCar && v.transmission) {
        const transLabel = v.transmission === 'AUTOMATIC' ? 'Số tự động' : 'Số sàn';
        detailsHtml += `<div class="detail-row"><span class="detail-icon">⚙️</span> <span>${transLabel}</span></div>`;
    }
    if (!isCar && v.engineCapacity) {
        detailsHtml += `<div class="detail-row"><span class="detail-icon">⚡</span> <span>Dung tích: ${v.engineCapacity}cc</span></div>`;
    }
    if (v.description) {
        detailsHtml += `<div class="detail-row" style="align-items:flex-start;"><span class="detail-icon">📋</span> <span>${v.description}</span></div>`;
    }

    document.getElementById('vehicleDetailsBody').innerHTML = detailsHtml;
}

// Tính tiền động khi thay đổi ngày
function calculatePrice() {
    const startStr = document.getElementById('startDate').value;
    const endStr = document.getElementById('endDate').value;
    const btn = document.getElementById('btnSubmit');
    const breakdown = document.getElementById('priceBreakdown');
    const unavailableAlert = document.getElementById('unavailableAlert');

    if (!vehicleData || !startStr || !endStr) {
        btn.disabled = true;
        btn.innerHTML = '<span>🔒</span> Chọn ngày để đặt xe';
        breakdown.style.display = 'none';
        const paymentArea = document.getElementById('paymentArea');
        if (paymentArea) paymentArea.style.display = 'none';
        return;
    }

    const start = new Date(startStr);
    const end = new Date(endStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Validate dates
    if (start < today) {
        showDateError('Ngày nhận xe không được ở quá khứ!');
        return;
    }
    if (end < start) {
        showDateError('Ngày trả xe phải bằng hoặc sau ngày nhận xe!');
        return;
    }

    // Calculate days and price
    const diffMs = end - start;
    const numDays = Math.round(diffMs / (1000 * 60 * 60 * 24)) + 1;
    const totalPrice = vehicleData.pricePerDay * numDays;
    bookingDepositAmount = totalPrice * 0.3;

    document.getElementById('pricePerDayDisplay').textContent = formatCurrency(vehicleData.pricePerDay);
    document.getElementById('numDaysDisplay').textContent = `${numDays} ngày`;
    document.getElementById('depositDisplay').textContent = formatCurrency(bookingDepositAmount);
    document.getElementById('totalPriceDisplay').textContent = formatCurrency(totalPrice);

    breakdown.style.display = 'block';
    unavailableAlert.style.display = 'none';

    // Check availability from API
    checkAvailability(vehicleId, startStr, endStr, numDays, totalPrice);
}

async function checkAvailability(vId, startDate, endDate, numDays, totalPrice) {
    const btn = document.getElementById('btnSubmit');
    const unavailableAlert = document.getElementById('unavailableAlert');

    btn.disabled = true;
    btn.innerHTML = '<span>⏳</span> Đang kiểm tra lịch...';

    try {
        const url = `${API_URL}/bookings/check-availability?vehicleId=${vId}&startDate=${startDate}&endDate=${endDate}`;
        const response = await fetch(url);
        const isAvailable = await response.json();

        if (isAvailable) {
            btn.disabled = false;
            btn.innerHTML = `<span>💳</span> Tiếp tục thanh toán cọc - ${formatCurrency(bookingDepositAmount)}`;
            unavailableAlert.style.display = 'none';
        } else {
            btn.disabled = true;
            btn.innerHTML = '<span>🚫</span> Xe đã được đặt';
            unavailableAlert.style.display = 'block';
        }
    } catch (error) {
        btn.disabled = false;
        btn.innerHTML = `<span>💳</span> Tiếp tục thanh toán cọc - ${formatCurrency(bookingDepositAmount)}`;
    }
}

function showDateError(message) {
    const btn = document.getElementById('btnSubmit');
    btn.disabled = true;
    btn.innerHTML = `<span>⚠️</span> ${message}`;
    document.getElementById('priceBreakdown').style.display = 'none';
}

function goToPaymentScreen() {
    const renterIdVal = document.getElementById('renterId').value;
    const renterId = renterIdVal ? parseInt(renterIdVal) : null;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        showToast('⚠️ Vui lòng chọn ngày nhận và ngày trả xe!', 'error');
        return;
    }

    if (!renterId) {
        const guestFullName = document.getElementById('guestFullName').value.trim();
        const guestEmail = document.getElementById('guestEmail').value.trim();

        if (!guestFullName || !guestEmail) {
            showToast('⚠️ Vui lòng điền Họ tên và Email liên hệ trước khi thanh toán!', 'error');
            return;
        }
        if (!guestEmail.includes('@') || !guestEmail.includes('.')) {
            showToast('⚠️ Vui lòng nhập địa chỉ Email hợp lệ!', 'error');
            return;
        }
    }

    // Hiển thị thông báo tính năng đang phát triển và chặn chuyển tiếp
    showToast('⚙️ Tính năng thanh toán đang được phát triển thêm!', 'error');
}

function goToBookingForm() {
    // Hide Step 2 Payment, Show Step 1 Form
    document.getElementById('paymentScreen').style.display = 'none';
    document.getElementById('formContent').style.display = 'block';
}

// Gửi yêu cầu đặt xe lên Backend
async function submitBooking() {
    const renterIdVal = document.getElementById('renterId').value;
    const renterId = renterIdVal ? parseInt(renterIdVal) : null;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const note = document.getElementById('noteInput').value;
    const btn = document.getElementById('btnSubmit');

    let guestFullName = null;
    let guestEmail = null;
    let guestPhone = null;

    // Basic validation
    if (!startDate || !endDate) {
        showToast('⚠️ Vui lòng chọn ngày nhận và ngày trả xe!', 'error');
        return;
    }
    if (!vehicleId) {
        showToast('⚠️ Không xác định được mã xe!', 'error');
        return;
    }

    if (!renterId) {
        guestFullName = document.getElementById('guestFullName').value.trim();
        guestEmail = document.getElementById('guestEmail').value.trim();
        guestPhone = document.getElementById('guestPhone').value.trim();

        if (!guestFullName || !guestEmail) {
            showToast('⚠️ Vui lòng điền Họ tên và Email liên hệ để tiếp tục đặt xe!', 'error');
            return;
        }
        if (!guestEmail.includes('@') || !guestEmail.includes('.')) {
            showToast('⚠️ Vui lòng nhập địa chỉ Email hợp lệ!', 'error');
            return;
        }
    }

    const paymentId = document.getElementById('paymentTransactionId').value.trim();
    if (!paymentId) {
        showToast('⚠️ Vui lòng thực hiện chuyển khoản và nhập Mã giao dịch để tiếp tục!', 'error');
        return;
    }

    btn.disabled = true;
    btn.innerHTML = '<span>⏳</span> Đang xử lý...';

    const payload = {
        renterId: renterId,
        vehicleId: parseInt(vehicleId),
        startDate: startDate,
        endDate: endDate,
        note: note || null,
        guestFullName: guestFullName,
        guestEmail: guestEmail,
        guestPhone: guestPhone,
        paymentId: paymentId,
        paymentMethod: 'BANK_TRANSFER',
        paymentAmount: bookingDepositAmount
    };

    try {
        const response = await fetch(`${API_URL}/bookings`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const booking = await response.json();
            // Show success screen
            document.getElementById('formContent').style.display = 'none';
            document.getElementById('paymentScreen').style.display = 'none';
            document.getElementById('successScreen').style.display = 'block';
            document.getElementById('successBookingId').textContent = `Mã đặt: #${booking.id}`;
            showToast('🎉 Đặt xe thành công!', 'success');
        } else {
            const errorMsg = await response.text();
            showToast(`❌ ${errorMsg}`, 'error');
            btn.disabled = false;
            btn.innerHTML = '<span>✅</span> Thử lại';
        }
    } catch (error) {
        console.error('Booking error:', error);
        showToast('❌ Không thể kết nối đến server. Vui lòng thử lại.', 'error');
        btn.disabled = false;
        btn.innerHTML = '<span>⚠️</span> Thử lại';
    }
}

// Toast notification helper
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = message;
    toast.className = `toast ${type}`;
    setTimeout(() => { toast.className = 'toast'; }, 4000);
}

// ============================================================
//  AUTHENTICATION - Đăng nhập & Trạng thái người dùng
// ============================================================

async function checkLoginStatus() {
    try {
        const response = await fetch(`${API_URL}/auth/me`);
        if (!response.ok) return;
        const data = await response.json();
        
        // Cập nhật Navbar động
        const navLinks = document.querySelector('.nav-links');
        if (navLinks && data.authenticated) {
            const loginBtn = navLinks.querySelector('a[href="/login.html"]');
            if (loginBtn) {
                loginBtn.outerHTML = `
                    <span style="font-size:0.95rem; color:#4b5563; display:inline-block; margin-right:0.6rem; align-self:center;">
                        👋 Xin chào, <strong>${data.fullName}</strong>
                    </span>
                    <a href="${API_URL}/auth/logout" class="btn btn-outline" style="padding:0.4rem 1rem; text-decoration:none;">Đăng Xuất</a>
                `;
            }
        }

        // Cập nhật form booking nếu đang ở trang booking
        if (window.location.pathname.includes('booking.html')) {
            const loggedInSummary = document.getElementById('loggedInUserSummary');
            const guestForm = document.getElementById('guestDetailsForm');
            const renterIdInput = document.getElementById('renterId');

            if (data.authenticated) {
                if (loggedInSummary) loggedInSummary.style.display = 'block';
                if (guestForm) guestForm.style.display = 'none';
                if (renterIdInput) renterIdInput.value = data.id;
                
                document.getElementById('userSummaryName').textContent = data.fullName;
                document.getElementById('userSummaryEmail').textContent = data.email;
            } else {
                if (loggedInSummary) loggedInSummary.style.display = 'none';
                if (guestForm) guestForm.style.display = 'block';
                if (renterIdInput) renterIdInput.value = '';
            }
        }
    } catch (error) {
        console.error('Error checking login status:', error);
    }
}

// Khởi chạy khi DOM sẵn sàng
document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
});
