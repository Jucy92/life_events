// API Base URL
const API_BASE = '/api';

// Chart instances
let yearlyChart, monthlyChart, eventTypeChart;

// ⚡ XSS 방어: HTML 이스케이핑 함수 (보안 강화 2025-11-07)
function escapeHtml(text) {
    if (text == null || text === '') return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Initialize statistics page
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (!token || !user) {
        window.location.href = '/login';
        return;
    }

    // Display user name
    try {
        const userData = JSON.parse(user);
        document.getElementById('userName').textContent = userData.name;
    } catch (e) {
        console.error('Failed to parse user data:', e);
        logout();
        return;
    }

    // Load all statistics
    loadAllStatistics();
});

// Get authentication headers
function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

// Load all statistics data
async function loadAllStatistics() {
    try {
        await Promise.all([
            loadYearlyStatistics(),
            loadMonthlyStatistics(),
            loadPersonStatistics(),
            loadEventTypeStatistics(),
            loadRelationStatistics()
        ]);
    } catch (error) {
        console.error('통계 데이터 로딩 실패:', error);
        showToast('통계 데이터를 불러오는데 실패했습니다.', 'error');
    }
}

// Load yearly statistics
async function loadYearlyStatistics() {
    try {
        const response = await fetch(`${API_BASE}/statistics/yearly`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw response;

        const data = await response.json();

        if (data.length === 0) {
            document.getElementById('yearlyChart').parentElement.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-chart-bar"></i>
                    <p>연도별 데이터가 없습니다</p>
                </div>
            `;
            return;
        }

        // Create chart
        const ctx = document.getElementById('yearlyChart').getContext('2d');

        if (yearlyChart) {
            yearlyChart.destroy();
        }

        yearlyChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(item => item.year + '년'),
                datasets: [
                    {
                        label: '받은 경조금',
                        data: data.map(item => item.receivedTotal),
                        backgroundColor: 'rgba(40, 167, 69, 0.7)',
                        borderColor: 'rgba(40, 167, 69, 1)',
                        borderWidth: 1
                    },
                    {
                        label: '보낸 경조금',
                        data: data.map(item => item.sentTotal),
                        backgroundColor: 'rgba(220, 53, 69, 0.7)',
                        borderColor: 'rgba(220, 53, 69, 1)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'top'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                label += Number(context.parsed.y).toLocaleString() + '원';
                                return label;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return Number(value).toLocaleString() + '원';
                            }
                        }
                    }
                }
            }
        });

    } catch (error) {
        console.error('연도별 통계 로딩 실패:', error);
    }
}

// Load monthly statistics
async function loadMonthlyStatistics() {
    try {
        const response = await fetch(`${API_BASE}/statistics/monthly?months=12`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw response;

        const data = await response.json();

        if (data.length === 0) {
            document.getElementById('monthlyChart').parentElement.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-chart-line"></i>
                    <p>월별 데이터가 없습니다</p>
                </div>
            `;
            return;
        }

        // Reverse to show oldest to newest
        data.reverse();

        const ctx = document.getElementById('monthlyChart').getContext('2d');

        if (monthlyChart) {
            monthlyChart.destroy();
        }

        monthlyChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.map(item => `${item.year}.${String(item.month).padStart(2, '0')}`),
                datasets: [
                    {
                        label: '받은 경조금',
                        data: data.map(item => item.receivedTotal),
                        borderColor: 'rgba(40, 167, 69, 1)',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: '보낸 경조금',
                        data: data.map(item => item.sentTotal),
                        borderColor: 'rgba(220, 53, 69, 1)',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'top'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                label += Number(context.parsed.y).toLocaleString() + '원';
                                return label;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return Number(value).toLocaleString() + '원';
                            }
                        }
                    }
                }
            }
        });

    } catch (error) {
        console.error('월별 통계 로딩 실패:', error);
    }
}

// Load person statistics
async function loadPersonStatistics() {
    try {
        const response = await fetch(`${API_BASE}/statistics/person`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw response;

        const data = await response.json();

        // Top 5 persons
        const top5 = data.slice(0, 5);
        const topContainer = document.getElementById('topPersonsContainer');

        if (top5.length === 0) {
            topContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-users"></i>
                    <p>데이터가 없습니다</p>
                </div>
            `;
        } else {
            // ⚡ XSS 방어: 사용자 입력 데이터 이스케이핑 (보안 강화 2025-11-07)
            topContainer.innerHTML = top5.map((person, index) => `
                <div class="mb-2 pb-2 ${index < top5.length - 1 ? 'border-bottom' : ''}">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${escapeHtml(person.name)}</strong>
                            <small class="text-muted">(${escapeHtml(person.relation || '미지정')})</small>
                        </div>
                        <span class="badge ${person.balance >= 0 ? 'badge-balance-positive' : 'badge-balance-negative'}">
                            ${Number(person.balance).toLocaleString()}원
                        </span>
                    </div>
                </div>
            `).join('');
        }

        // Full person table (limit to prevent memory issues)
        const tableBody = document.getElementById('personTable');
        const MAX_DISPLAY_ROWS = 500; // Limit to 500 rows to prevent memory issues

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">데이터가 없습니다</td></tr>';
        } else {
            const displayData = data.slice(0, MAX_DISPLAY_ROWS);
            const hasMore = data.length > MAX_DISPLAY_ROWS;

            // ⚡ XSS 방어: 사용자 입력 데이터 이스케이핑 (보안 강화 2025-11-07)
            tableBody.innerHTML = displayData.map(person => `
                <tr>
                    <td><strong>${escapeHtml(person.name)}</strong></td>
                    <td>${escapeHtml(person.relation || '미지정')}</td>
                    <td>
                        <span class="badge badge-received">${Number(person.receivedTotal).toLocaleString()}원</span>
                        <small class="text-muted">(${person.receivedCount}건)</small>
                    </td>
                    <td>
                        <span class="badge badge-sent">${Number(person.sentTotal).toLocaleString()}원</span>
                        <small class="text-muted">(${person.sentCount}건)</small>
                    </td>
                    <td>
                        <span class="badge ${person.balance >= 0 ? 'badge-balance-positive' : 'badge-balance-negative'}">
                            ${Number(person.balance).toLocaleString()}원
                        </span>
                    </td>
                    <td>
                        ${escapeHtml(person.lastEventDate ? person.lastEventDate : '-')}<br>
                        <small class="text-muted">${escapeHtml(person.lastEventType || '')}</small>
                    </td>
                </tr>
            `).join('');

            // Add notice if data was truncated
            if (hasMore) {
                tableBody.innerHTML += `
                    <tr class="table-warning">
                        <td colspan="6" class="text-center">
                            <i class="fas fa-info-circle"></i>
                            ${MAX_DISPLAY_ROWS}개 항목만 표시됩니다. (전체 ${data.length}개 중)
                            <br><small class="text-muted">필요시 검색 기능을 사용하세요.</small>
                        </td>
                    </tr>
                `;
            }
        }

    } catch (error) {
        console.error('인물별 통계 로딩 실패:', error);
    }
}

// Load event type statistics
async function loadEventTypeStatistics() {
    try {
        const response = await fetch(`${API_BASE}/statistics/event-type`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw response;

        const data = await response.json();

        if (data.length === 0) {
            document.getElementById('eventTypeChart').parentElement.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-gift"></i>
                    <p>데이터가 없습니다</p>
                </div>
            `;
            return;
        }

        const ctx = document.getElementById('eventTypeChart').getContext('2d');

        if (eventTypeChart) {
            eventTypeChart.destroy();
        }

        // Calculate total amount for each event type
        const totals = data.map(item => Number(item.receivedTotal) + Number(item.sentTotal));

        eventTypeChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.map(item => item.eventType),
                datasets: [{
                    data: totals,
                    backgroundColor: [
                        'rgba(102, 126, 234, 0.7)',
                        'rgba(118, 75, 162, 0.7)',
                        'rgba(40, 167, 69, 0.7)',
                        'rgba(220, 53, 69, 0.7)',
                        'rgba(255, 193, 7, 0.7)'
                    ],
                    borderColor: [
                        'rgba(102, 126, 234, 1)',
                        'rgba(118, 75, 162, 1)',
                        'rgba(40, 167, 69, 1)',
                        'rgba(220, 53, 69, 1)',
                        'rgba(255, 193, 7, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: ${Number(value).toLocaleString()}원 (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });

    } catch (error) {
        console.error('행사 유형별 통계 로딩 실패:', error);
    }
}

// Load relation statistics
async function loadRelationStatistics() {
    try {
        const response = await fetch(`${API_BASE}/statistics/relation`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw response;

        const data = await response.json();

        // Top 5 relations
        const top5 = data.slice(0, 5);
        const topContainer = document.getElementById('topRelationsContainer');

        if (top5.length === 0) {
            topContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-handshake"></i>
                    <p>데이터가 없습니다</p>
                </div>
            `;
        } else {
            // ⚡ XSS 방어: 사용자 입력 데이터 이스케이핑 (보안 강화 2025-11-07)
            topContainer.innerHTML = top5.map((relation, index) => {
                const total = Number(relation.receivedTotal) + Number(relation.sentTotal);
                return `
                    <div class="mb-2 pb-2 ${index < top5.length - 1 ? 'border-bottom' : ''}">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <strong>${escapeHtml(relation.relation)}</strong>
                                <small class="text-muted">(${relation.receivedCount + relation.sentCount}건)</small>
                            </div>
                            <span class="text-primary">
                                ${total.toLocaleString()}원
                            </span>
                        </div>
                    </div>
                `;
            }).join('');
        }

    } catch (error) {
        console.error('관계별 통계 로딩 실패:', error);
    }
}

// Toast message system
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'info'} border-0`;
    toast.setAttribute('role', 'alert');

    // ⚡ XSS 방어: 메시지 HTML 이스케이핑
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${escapeHtml(message)}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    container.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    setTimeout(() => {
        container.removeChild(toast);
    }, 5000);
}

// Logout
function logout() {
    // localStorage 정리: JWT 토큰 및 사용자 정보 (로그인 상태)
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    // sessionStorage 정리: 아이디 찾기 → 비밀번호 변경 연결용 임시 데이터
    // (원래 탭 닫으면 자동 삭제되지만, 로그아웃 시에도 명시적으로 정리)
    sessionStorage.removeItem('verifiedEmail');
    sessionStorage.removeItem('verifiedUserId');
    sessionStorage.removeItem('emailVerifiedAt');

    window.location.href = '/login';
}
