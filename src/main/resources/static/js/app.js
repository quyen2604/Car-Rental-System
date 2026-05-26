// Base URL cho API
const API_URL = 'http://localhost:8080/api';

// Hàm tiện ích format tiền tệ VNĐ
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Hàm xử lý tìm kiếm xe
async function searchVehicles(event) {
    if(event) event.preventDefault();
    
    const type = document.getElementById('type').value;
    const location = document.getElementById('location').value;
    
    let url = `${API_URL}/vehicles/search?`;
    if (type) url += `type=${encodeURIComponent(type)}&`;
    if (location) url += `location=${encodeURIComponent(location)}`;
    
    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Không thể fetch dữ liệu');
        
        const data = await response.json();
        renderVehicles(data);
    } catch (error) {
        console.error('Error fetching vehicles:', error);
        document.getElementById('results').innerHTML = '<p style="text-align:center;width:100%;color:red;">Đã xảy ra lỗi khi tải dữ liệu.</p>';
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
        const icon = v.type === 'car' ? '🚗' : '🏍️';
        html += `
            <div class="vehicle-card">
                <div class="vehicle-image">
                    ${icon}
                </div>
                <div class="vehicle-content">
                    <div class="vehicle-header">
                        <div class="vehicle-title">${v.name}</div>
                        <div class="vehicle-badge">${v.type === 'car' ? 'Ô Tô' : 'Xe Máy'}</div>
                    </div>
                    <div class="vehicle-price">
                        ${formatCurrency(v.pricePerDay)} <span>/ ngày</span>
                    </div>
                    <div class="vehicle-features">
                        <span class="feature-tag">📍 ${v.location}</span>
                        <span class="feature-tag">🏢 ${v.brand}</span>
                        ${v.seats ? `<span class="feature-tag">💺 ${v.seats} chỗ</span>` : ''}
                        <span class="feature-tag">⛽ ${v.fuelType === 'gasoline' ? 'Xăng' : 'Điện'}</span>
                    </div>
                    <p style="font-size:0.875rem; color:#64748b; margin-bottom: 1.5rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
                        ${v.description}
                    </p>
                    <button class="btn btn-primary" onclick="alert('Tính năng đang phát triển!')">Đặt Thuê Ngay</button>
                </div>
            </div>
        `;
    });
    
    resultsContainer.innerHTML = html;
}

// Tự động load dữ liệu khi vào trang search
if (window.location.pathname.includes('search.html')) {
    document.addEventListener('DOMContentLoaded', () => {
        searchVehicles(); // Load all on init
    });
}
