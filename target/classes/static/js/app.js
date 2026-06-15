// Cấu hình URL kết nối Backend chung
window.API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    checkUserSession();
    // Nạp trang chủ mặc định khi mở ứng dụng lên
    loadPage('home');
});

// Quản lý việc click Menu chuyển mục
function initNavigation() {
    document.querySelectorAll('.menu-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = link.getAttribute('data-page');
            if (page) {
                document.querySelectorAll('.menu-link').forEach(l => l.classList.remove('active'));
                link.classList.add('active');
                loadPage(page);
            }
        });
    });
}

// Lõi chuyển đổi module giao diện động
function loadPage(pageName) {
    const mainContent = document.getElementById('main-content');
    const pageTitle = document.getElementById('page-title');

    switch(pageName) {
        case 'home':
            pageTitle.innerText = "Trang Chủ Tổng Quan";
            mainContent.innerHTML = `
                <div class="app-card text-center">
                    <h2 class="fw-bold text-dark">🚗 Chào Mừng Đến Với Hệ Thống CarRental</h2>
                    <p class="text-muted">Kiến trúc SPA (Single Page Application) kết nối Backend Spring Boot hoàn chỉnh.</p>
                </div>`;
            break;

        case 'auth':
            pageTitle.innerText = "Xác Thực Hệ Thống";
            mainContent.innerHTML = getAuthFormTemplate();
            initAuthEvents(); // Kích hoạt sự kiện đăng ký/đăng nhập từ module auth
            break;

        case 'vehicles':
            pageTitle.innerText = "Quản Lý Danh Sách Xe";
            mainContent.innerHTML = `
                <div class="app-card">
                    <h3>🚗 Module Quản Lý Xe</h3>
                    <p class="text-muted">Tính năng xem danh sách xe, thêm xe mới sẽ tự động mở rộng tại đây ở Bước tiếp theo.</p>
                </div>`;
            break;

        default:
            mainContent.innerHTML = `<div class="alert alert-warning">Mô-đun đang được phát triển...</div>`;
    }
}

// --- GIAO DIỆN FORM ĐĂNG NHẬP / ĐĂNG KÝ (ĐƯỢC GỘP GỌN GÀNG) ---
function getAuthFormTemplate() {
    return `
    <div class="row g-4 justify-content-center">
        <div class="col-md-6">
            <div class="app-card">
                <h4 class="fw-bold text-success mb-3"><i class="bi bi-person-plus"></i> Đăng Ký (Renter)</h4>
                <form id="registerForm">
                    <div class="mb-3"><label class="form-label">Họ và Tên</label><input type="text" id="regName" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Email</label><input type="email" id="regEmail" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Số Điện Thoại</label><input type="text" id="regPhone" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Mật Khẩu</label><input type="password" id="regPassword" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Số Bằng Lái Xe</label><input type="text" id="regLicense" class="form-control" required></div>
                    <button type="submit" class="btn btn-success w-100">Đăng Ký</button>
                </form>
            </div>
        </div>
        <div class="col-md-5">
            <div class="app-card">
                <h4 class="fw-bold text-primary mb-3"><i class="bi bi-shield-lock"></i> Đăng Nhập</h4>
                <form id="loginForm">
                    <div class="mb-3"><label class="form-label">Email</label><input type="email" id="loginEmail" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Mật Khẩu</label><input type="password" id="loginPassword" class="form-control" required></div>
                    <button type="submit" class="btn btn-primary w-100">Đăng Nhập</button>
                </form>
            </div>
        </div>
    </div>`;
}

// Logic điều khiển xử lý Đăng ký / Đăng nhập
function initAuthEvents() {
    // ĐĂNG KÝ
    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            fullName: document.getElementById('regName').value,
            email: document.getElementById('regEmail').value,
            phone: document.getElementById('regPhone').value,
            password: document.getElementById('regPassword').value,
            licenseNumber: document.getElementById('regLicense').value
        };
        const res = await fetch(`${window.API_BASE_URL}/auth/register/renter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if(res.ok) {
            showNotification("🎉 Đăng ký tài khoản mới thành công!");
            document.getElementById('loginEmail').value = payload.email;
        } else {
            showNotification("❌ Đăng ký thất bại! Email đã tồn tại.", false);
        }
    });

    // ĐĂNG NHẬP
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            email: document.getElementById('loginEmail').value,
            password: document.getElementById('loginPassword').value
        };
        const res = await fetch(`${window.API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if(res.ok) {
            const user = await res.json();
            localStorage.setItem('user', JSON.stringify(user)); // Lưu session vào trình duyệt
            showNotification(`🔓 Đăng nhập thành công! Chào mừng ${user.fullName}`);
            checkUserSession();
            loadPage('home'); // Quay về trang chủ
        } else {
            showNotification("❌ Sai tài khoản hoặc mật khẩu!", false);
        }
    });
}

// Kiểm tra xem User đã đăng nhập chưa để hiển thị Tên / Ẩn nút đăng nhập
function checkUserSession() {
    const user = JSON.parse(localStorage.getItem('user'));
    if(user) {
        document.getElementById('menu-auth').classList.add('d-none');
        document.getElementById('menu-user').classList.remove('d-none');
        document.getElementById('username-display').innerText = user.fullName;

        document.getElementById('btn-logout').addEventListener('click', () => {
            localStorage.removeItem('user');
            showNotification("👋 Đã đăng xuất khỏi hệ thống");
            checkUserSession();
            loadPage('home');
        });
    } else {
        document.getElementById('menu-auth').classList.remove('d-none');
        document.getElementById('menu-user').classList.add('d-none');
    }
}

// Hàm thông báo Toast toàn cục
function showNotification(message, isSuccess = true) {
    const toastEl = document.getElementById('systemToast');
    document.getElementById('toastBody').innerText = message;
    toastEl.className = `toast align-items-center text-white border-0 ${isSuccess ? 'bg-success' : 'bg-danger'}`;
    new bootstrap.Toast(toastEl).show();
}