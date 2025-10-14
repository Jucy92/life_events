// API Base URL
const API_BASE = '/api';

// Current page state
let currentPage = 0;
let pageSize = 10;
let searchKeyword = '';
let currentTransactionType = 'RECEIVED'; // 기본값: 받은 경조금

// Global flags to prevent race conditions
let isRedirecting = false;
let isInitializing = false;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (!token || !user) {
        if (!isRedirecting) {
            isRedirecting = true;
            window.location.href = '/login';
        }
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

    // Load initial data safely
    initializeDashboard();

    // Setup search with auto-search on input (debounced)
    let searchTimeout;
    document.getElementById('searchInput').addEventListener('input', function(e) {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            searchKeyword = this.value.trim();
            currentPage = 0;
            loadGiftMoney();
        }, 500); // 500ms 후 자동 검색
    });

    // Keep Enter key functionality as well
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            clearTimeout(searchTimeout);
            searchKeyword = this.value.trim();
            currentPage = 0;
            loadGiftMoney();
        }
    });

    // Setup amount input formatting (천 단위 콤마)
    setupAmountFormatting();
});

// Initialize dashboard with safe concurrent API calls
async function initializeDashboard() {
    if (isInitializing || isRedirecting) {
        return;
    }

    isInitializing = true;

    try {
        // Use Promise.allSettled to prevent cascading failures
        const results = await Promise.allSettled([
            loadGiftMoney(),
            loadStatistics()
        ]);

        // Check if any failed due to auth issues
        const authFailed = results.some(result =>
            result.status === 'rejected' && isRedirecting
        );

        if (authFailed) {
            console.log('Authentication failed during initialization');
            return;
        }

        // Log any other failures
        results.forEach((result, index) => {
            if (result.status === 'rejected') {
                console.error(`Initialization task ${index} failed:`, result.reason);
            }
        });
    } catch (error) {
        console.error('Dashboard initialization error:', error);
        if (!isRedirecting) {
            alert('대시보드 초기화 중 오류가 발생했습니다.');
        }
    } finally {
        isInitializing = false;
    }
}

// Get authentication headers
function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

// Setup amount input formatting (천 단위 콤마 자동 추가)
function setupAmountFormatting() {
    const amountInput = document.getElementById('amount');

    amountInput.addEventListener('input', function(e) {
        let value = this.value.replace(/[^\d]/g, ''); // 숫자만 추출

        if (value === '') {
            this.value = '';
            document.getElementById('amountValue').value = '';
            return;
        }

        // 천 단위 콤마 추가
        const formattedValue = Number(value).toLocaleString();
        this.value = formattedValue;

        // hidden input에 실제 숫자 값 저장
        document.getElementById('amountValue').value = value;
    });

    // 붙여넣기 이벤트 처리
    amountInput.addEventListener('paste', function(e) {
        e.preventDefault();
        const pastedText = (e.clipboardData || window.clipboardData).getData('text');
        const value = pastedText.replace(/[^\d]/g, '');

        if (value !== '') {
            this.value = Number(value).toLocaleString();
            document.getElementById('amountValue').value = value;
        }
    });
}

// 오늘 날짜를 YYYY-MM-DD 형식으로 반환
function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 탭 필터링
function filterByType(type) {
    currentTransactionType = type;
    currentPage = 0;
    loadGiftMoney();
}

// Handle API errors with detailed messages
async function handleError(response, defaultMessage) {
    console.error('API Error:', response);

    // Prevent multiple redirects
    if (response.status === 401) {
        if (!isRedirecting) {
            isRedirecting = true;
            alert('인증이 만료되었습니다. 다시 로그인해주세요.');
            logout();
        }
        return;
    }

    try {
        // Check content type before parsing
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('Non-JSON error response');
            alert(defaultMessage || '서버 오류가 발생했습니다.');
            return;
        }

        const errorData = await response.json();

        // 유효성 검사 에러 처리
        if (errorData.errors) {
            let errorMessage = '입력 오류:\n';
            for (const [field, message] of Object.entries(errorData.errors)) {
                const fieldNames = {
                    'eventDate': '행사 날짜',
                    'eventType': '행사 유형',
                    'transactionType': '거래 유형',
                    'giverName': '보낸 사람',
                    'giverRelation': '관계',
                    'amount': '금액',
                    'contact': '연락처',
                    'memo': '메모'
                };
                errorMessage += `• ${fieldNames[field] || field}: ${message}\n`;
            }
            alert(errorMessage);
        } else {
            alert(errorData.message || defaultMessage);
        }
    } catch (e) {
        console.error('Error parsing response:', e);
        alert(defaultMessage || '오류가 발생했습니다.');
    }
}

// Load gift money list
async function loadGiftMoney() {
    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });

        if (searchKeyword) {
            params.append('search', searchKeyword);
        }

        // 탭 필터링 추가
        if (currentTransactionType) {
            params.append('transactionType', currentTransactionType);
        }

        const response = await fetch(`${API_BASE}/gift-money?${params}`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw response;
        }

        const data = await response.json();
        displayGiftMoneyList(data.content);
        displayPagination(data);

    } catch (error) {
        await handleError(error, '데이터를 불러오는데 실패했습니다.');
    }
}

// Display gift money list
function displayGiftMoneyList(items) {
    const tbody = document.getElementById('giftMoneyTable');

    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">데이터가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = items.map(item => {
        // 거래 유형 아이콘 및 배지 스타일
        const isReceived = item.transactionType === 'RECEIVED';
        const typeIcon = isReceived
            ? '<i class="fas fa-arrow-down text-success"></i>'
            : '<i class="fas fa-arrow-up text-danger"></i>';
        const typeText = isReceived ? '받음' : '보냄';
        const badgeClass = isReceived ? 'bg-success' : 'bg-danger';

        return `
        <tr>
            <td class="text-center">
                ${typeIcon}
                <span class="badge ${badgeClass} ms-1">${typeText}</span>
            </td>
            <td>${item.eventDate}</td>
            <td>${item.eventType}</td>
            <td>${item.giverName}</td>
            <td>${item.giverRelation || '-'}</td>
            <td><span class="badge-amount">${Number(item.amount).toLocaleString()}원</span></td>
            <td>${item.contact || '-'}</td>
            <td>${item.memo || '-'}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="editGiftMoney(${item.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteGiftMoney(${item.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
        `;
    }).join('');
}

// Display pagination
function displayPagination(pageData) {
    const pagination = document.getElementById('pagination');
    const totalPages = pageData.totalPages;

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let html = '';

    // Previous button
    if (currentPage > 0) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${currentPage - 1}); return false;">이전</a></li>`;
    }

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
            html += `<li class="page-item active"><a class="page-link" href="#">${i + 1}</a></li>`;
        } else {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${i}); return false;">${i + 1}</a></li>`;
        }
    }

    // Next button
    if (currentPage < totalPages - 1) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${currentPage + 1}); return false;">다음</a></li>`;
    }

    pagination.innerHTML = html;
}

// Change page
function changePage(page) {
    currentPage = page;
    loadGiftMoney();
}

// Load statistics
async function loadStatistics() {
    try {
        const response = await fetch(`${API_BASE}/gift-money/statistics`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw response;
        }

        const stats = await response.json();

        // 받은 경조금 통계
        document.getElementById('receivedAmount').textContent =
            Number(stats.receivedTotalAmount).toLocaleString() + '원';
        document.getElementById('receivedCount').textContent = stats.receivedCount;

        // 보낸 경조금 통계
        document.getElementById('sentAmount').textContent =
            Number(stats.sentTotalAmount).toLocaleString() + '원';
        document.getElementById('sentCount').textContent = stats.sentCount;

        // 차액 계산 (받은 - 보낸)
        const difference = Number(stats.receivedTotalAmount) - Number(stats.sentTotalAmount);
        const differenceElement = document.getElementById('differenceAmount');
        differenceElement.textContent = difference.toLocaleString() + '원';

        // 차액이 음수면 빨간색, 양수면 초록색
        if (difference < 0) {
            differenceElement.style.color = '#dc3545';
        } else if (difference > 0) {
            differenceElement.style.color = '#28a745';
        } else {
            differenceElement.style.color = 'white';
        }

        // 전체 건수
        document.getElementById('totalCount').textContent = stats.totalCount;

    } catch (error) {
        console.error('통계 로딩 실패:', error);
    }
}

// Show add modal
function showAddModal() {
    document.getElementById('modalTitle').textContent = '경조금 추가';
    document.getElementById('giftMoneyForm').reset();
    document.getElementById('giftMoneyId').value = '';

    // 오늘 날짜로 기본값 설정
    document.getElementById('eventDate').value = getTodayDate();

    // 현재 탭에 맞는 거래 유형 기본값 설정
    document.getElementById('transactionType').value = currentTransactionType || 'RECEIVED';

    const modal = new bootstrap.Modal(document.getElementById('giftMoneyModal'));
    modal.show();
}

// Edit gift money
async function editGiftMoney(id) {
    try {
        const response = await fetch(`${API_BASE}/gift-money/${id}`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw response;
        }

        const item = await response.json();

        document.getElementById('modalTitle').textContent = '경조금 수정';
        document.getElementById('giftMoneyId').value = item.id;
        document.getElementById('eventDate').value = item.eventDate;
        document.getElementById('eventType').value = item.eventType;
        document.getElementById('transactionType').value = item.transactionType;
        document.getElementById('giverName').value = item.giverName;
        document.getElementById('giverRelation').value = item.giverRelation || '';

        // 금액 포맷팅하여 표시
        const amountValue = String(item.amount).replace(/[^\d]/g, '');
        document.getElementById('amount').value = Number(amountValue).toLocaleString();
        document.getElementById('amountValue').value = amountValue;

        document.getElementById('contact').value = item.contact || '';
        document.getElementById('memo').value = item.memo || '';

        const modal = new bootstrap.Modal(document.getElementById('giftMoneyModal'));
        modal.show();

    } catch (error) {
        await handleError(error, '데이터를 불러오는데 실패했습니다.');
    }
}

// Save gift money
async function saveGiftMoney() {
    const id = document.getElementById('giftMoneyId').value;

    // 금액은 hidden input에서 가져오기 (콤마 제거된 실제 값)
    const amountValue = document.getElementById('amountValue').value ||
                        document.getElementById('amount').value.replace(/[^\d]/g, '');

    const data = {
        eventDate: document.getElementById('eventDate').value,
        eventType: document.getElementById('eventType').value,
        transactionType: document.getElementById('transactionType').value,
        giverName: document.getElementById('giverName').value,
        giverRelation: document.getElementById('giverRelation').value,
        amount: parseInt(amountValue),
        contact: document.getElementById('contact').value,
        memo: document.getElementById('memo').value
    };

    try {
        const url = id ? `${API_BASE}/gift-money/${id}` : `${API_BASE}/gift-money`;
        const method = id ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: getAuthHeaders(),
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw response;
        }

        alert(id ? '수정되었습니다.' : '추가되었습니다.');

        const modal = bootstrap.Modal.getInstance(document.getElementById('giftMoneyModal'));
        modal.hide();

        // 데이터 다시 로드
        await loadGiftMoney();
        await loadStatistics();

    } catch (error) {
        await handleError(error, '저장에 실패했습니다.');
    }
}

// Delete gift money
async function deleteGiftMoney(id) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/gift-money/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw response;
        }

        alert('삭제되었습니다.');

        // 데이터 다시 로드
        await loadGiftMoney();
        await loadStatistics();

    } catch (error) {
        await handleError(error, '삭제에 실패했습니다.');
    }
}

// Show upload modal
function showUploadModal() {
    document.getElementById('fileInput').value = '';
    const modal = new bootstrap.Modal(document.getElementById('uploadModal'));
    modal.show();
}

// Upload file
async function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        alert('파일을 선택해주세요.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`${API_BASE}/gift-money/upload`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: formData
        });

        if (!response.ok) {
            throw response;
        }

        const result = await response.json();
        alert(`업로드 완료!\n성공: ${result.successCount}건\n실패: ${result.failCount}건`);

        const modal = bootstrap.Modal.getInstance(document.getElementById('uploadModal'));
        modal.hide();

        // 데이터 다시 로드
        await loadGiftMoney();
        await loadStatistics();

    } catch (error) {
        await handleError(error, '업로드에 실패했습니다.');
    }
}

// Logout
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
}
