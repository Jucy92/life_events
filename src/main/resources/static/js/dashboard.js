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

// ⚡ XSS 방어: HTML 이스케이핑 함수 (보안 강화 2025-11-07)
function escapeHtml(text) {
    if (text == null || text === '') return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Toast 메시지 시스템
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    };

    // ⚡ XSS 방어: 메시지 HTML 이스케이핑
    toast.innerHTML = `
        <div class="toast-icon">${icons[type] || icons.info}</div>
        <div class="toast-message">${escapeHtml(message)}</div>
    `;

    container.appendChild(toast);

    // 3초 후 자동 제거
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    }, 3000);
}

// slideOut 애니메이션 추가 (CSS에 없으므로 동적으로 추가)
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

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

    // Setup date validation (미래 날짜 경고)
    setupDateValidation();

    // Setup form validation
    setupFormValidation();
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

// Setup custom date input (yyyy.mm.dd 형식)
function setupDateValidation() {
    const eventDateInput = document.getElementById('eventDate');
    let dateValue = { year: '', month: '', day: '' };

    // 날짜 입력 처리
    eventDateInput.addEventListener('input', function(e) {
        let value = this.value.replace(/[^\d]/g, ''); // 숫자만 추출

        if (value.length === 0) {
            dateValue = { year: '', month: '', day: '' };
            this.value = '';
            document.getElementById('eventDateValue').value = '';
            document.getElementById('eventDateWarning').style.display = 'none';
            return;
        }

        // 앞자리부터 채우기 (1992 입력 시: 1 -> 19 -> 199 -> 1992)
        if (value.length <= 4) {
            dateValue.year = value.padEnd(4, '_').substring(0, 4);
            dateValue.month = '';
            dateValue.day = '';
        } else if (value.length <= 6) {
            dateValue.year = value.substring(0, 4);
            dateValue.month = value.substring(4, 6).padEnd(2, '_').substring(0, 2);
            dateValue.day = '';
        } else {
            dateValue.year = value.substring(0, 4);
            dateValue.month = value.substring(4, 6);
            dateValue.day = value.substring(6, 8);
        }

        // 포맷팅된 값 표시
        let formatted = dateValue.year;
        if (dateValue.month) {
            formatted += '.' + dateValue.month;
        }
        if (dateValue.day) {
            formatted += '.' + dateValue.day;
        }

        this.value = formatted;

        // 완전한 날짜인 경우 검증
        if (value.length === 8) {
            const year = dateValue.year;
            const month = dateValue.month;
            const day = dateValue.day;

            // YYYY-MM-DD 형식으로 변환 (백엔드 전송용)
            const isoDate = `${year}-${month}-${day}`;
            document.getElementById('eventDateValue').value = isoDate;

            // 미래 날짜 경고 (저장은 가능)
            try {
                const inputDate = new Date(isoDate);
                const today = new Date();
                today.setHours(0, 0, 0, 0);

                const warningElement = document.getElementById('eventDateWarning');
                if (inputDate > today) {
                    warningElement.style.display = 'block';
                } else {
                    warningElement.style.display = 'none';
                }
            } catch (e) {
                document.getElementById('eventDateWarning').style.display = 'none';
            }
        }
    });

    // 백스페이스 처리 (일 -> 월 -> 년 순서로 지우기)
    eventDateInput.addEventListener('keydown', function(e) {
        if (e.key === 'Backspace') {
            e.preventDefault();

            let value = this.value.replace(/[^\d]/g, '');

            if (value.length > 0) {
                // 마지막 숫자 하나 제거
                value = value.substring(0, value.length - 1);

                // input 이벤트 트리거하여 다시 포맷팅
                this.value = value;
                const inputEvent = new Event('input', { bubbles: true });
                this.dispatchEvent(inputEvent);
            }
        } else if (e.key === 'Delete') {
            e.preventDefault();
            this.value = '';
            dateValue = { year: '', month: '', day: '' };
            document.getElementById('eventDateValue').value = '';
            document.getElementById('eventDateWarning').style.display = 'none';
        } else if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
            // Ctrl+A 처리는 기본 동작 허용
            return;
        } else if (!e.ctrlKey && !e.metaKey && !/^\d$/.test(e.key) &&
                   !['ArrowLeft', 'ArrowRight', 'Home', 'End', 'Tab'].includes(e.key)) {
            // 숫자와 허용된 키만 입력 가능
            e.preventDefault();
        }
    });

    // 붙여넣기 처리
    eventDateInput.addEventListener('paste', function(e) {
        e.preventDefault();
        const pastedText = (e.clipboardData || window.clipboardData).getData('text');
        const numbers = pastedText.replace(/[^\d]/g, '');

        if (numbers.length > 0) {
            this.value = numbers.substring(0, 8); // 최대 8자리
            const inputEvent = new Event('input', { bubbles: true });
            this.dispatchEvent(inputEvent);
        }
    });

    // 날짜 유효성 검사 (월: 1-12, 일: 1-31)
    function validateDate(year, month, day) {
        const monthNum = parseInt(month, 10);
        const dayNum = parseInt(day, 10);
        const yearNum = parseInt(year, 10);

        // 월 검사
        if (monthNum < 1 || monthNum > 12) {
            return { valid: false, message: '월은 1~12 사이여야 합니다.' };
        }

        // 일 검사 (간단한 검증: 1-31)
        if (dayNum < 1 || dayNum > 31) {
            return { valid: false, message: '일은 1~31 사이여야 합니다.' };
        }

        // 월별 최대 일수 검증
        const daysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

        // 윤년 계산
        const isLeapYear = (yearNum % 4 === 0 && yearNum % 100 !== 0) || (yearNum % 400 === 0);
        if (isLeapYear) {
            daysInMonth[1] = 29;
        }

        if (dayNum > daysInMonth[monthNum - 1]) {
            return { valid: false, message: `${monthNum}월은 최대 ${daysInMonth[monthNum - 1]}일까지입니다.` };
        }

        return { valid: true };
    }

    // 완전한 날짜 입력 시 유효성 검사
    eventDateInput.addEventListener('blur', function() {
        const value = this.value.replace(/[^\d]/g, '');

        if (value.length === 8) {
            const year = value.substring(0, 4);
            const month = value.substring(4, 6);
            const day = value.substring(6, 8);

            const validation = validateDate(year, month, day);

            if (!validation.valid) {
                showToast(validation.message, 'error');
                this.value = '';
                document.getElementById('eventDateValue').value = '';
                document.getElementById('eventDateWarning').style.display = 'none';
            }
        }
    });

    // 달력 버튼 클릭 시 달력 표시
    const calendarBtn = document.getElementById('calendarBtn');
    const calendarPicker = document.getElementById('calendarPicker');

    if (calendarBtn && calendarPicker) {
        calendarBtn.addEventListener('click', function() {
            calendarPicker.showPicker();
        });

        // 달력에서 날짜 선택 시 텍스트 입력으로 동기화
        calendarPicker.addEventListener('change', function() {
            if (this.value) {
                const [year, month, day] = this.value.split('-');
                const formattedDate = `${year}.${month}.${day}`;

                eventDateInput.value = formattedDate;
                document.getElementById('eventDateValue').value = this.value;

                // 미래 날짜 경고 확인
                const inputDate = new Date(this.value);
                const today = new Date();
                today.setHours(0, 0, 0, 0);

                const warningElement = document.getElementById('eventDateWarning');
                if (inputDate > today) {
                    warningElement.style.display = 'block';
                } else {
                    warningElement.style.display = 'none';
                }
            }
        });
    }
}

// Setup form validation
function setupFormValidation() {
    const form = document.getElementById('giftMoneyForm');
    const requiredFields = ['eventDate', 'eventType', 'transactionType', 'name', 'amount'];

    form.addEventListener('submit', function(e) {
        e.preventDefault();
    });

    // 필수 항목 검증 함수
    window.validateRequiredFields = function() {
        let isValid = true;
        let firstInvalidField = null;

        requiredFields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            const value = field.value.trim();

            if (!value || value === '') {
                isValid = false;
                field.classList.add('is-invalid');
                field.style.borderColor = '#dc3545';

                if (!firstInvalidField) {
                    firstInvalidField = field;
                }

                // 이벤트 리스너 추가 (값 입력 시 빨간 테두리 제거)
                field.addEventListener('input', function() {
                    this.classList.remove('is-invalid');
                    this.style.borderColor = '';
                }, { once: true });
            } else {
                field.classList.remove('is-invalid');
                field.style.borderColor = '';
            }
        });

        if (!isValid) {
            if (firstInvalidField) {
                firstInvalidField.focus();
            }
            showToast('필수 항목을 모두 입력해주세요.', 'warning');
        }

        return isValid;
    };
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
                    'name': '이름',
                    'relation': '관계',
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

        // ⚡ XSS 방어: 사용자 입력 데이터 이스케이핑 (보안 강화 2025-11-07)
        return `
        <tr>
            <td class="text-center">
                ${typeIcon}
                <span class="badge ${badgeClass} ms-1">${typeText}</span>
            </td>
            <td>${escapeHtml(item.eventDate)}</td>
            <td>${escapeHtml(item.eventType)}</td>
            <td>${escapeHtml(item.name)}</td>
            <td>${escapeHtml(item.relation || '-')}</td>
            <td><span class="badge-amount">${Number(item.amount).toLocaleString()}원</span></td>
            <td>${escapeHtml(item.contact || '-')}</td>
            <td>${escapeHtml(item.memo || '-')}</td>
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

// Display pagination (최대 5개 페이지 버튼 표시)
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

    // Calculate page range (최대 5개 버튼)
    const maxButtons = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

    // Adjust start if we're near the end
    if (endPage - startPage < maxButtons - 1) {
        startPage = Math.max(0, endPage - maxButtons + 1);
    }

    // First page button (if not in range)
    if (startPage > 0) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(0); return false;">1</a></li>`;
        if (startPage > 1) {
            html += `<li class="page-item disabled"><a class="page-link" href="#">...</a></li>`;
        }
    }

    // Page numbers
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            html += `<li class="page-item active"><a class="page-link" href="#">${i + 1}</a></li>`;
        } else {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${i}); return false;">${i + 1}</a></li>`;
        }
    }

    // Last page button (if not in range)
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            html += `<li class="page-item disabled"><a class="page-link" href="#">...</a></li>`;
        }
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${totalPages - 1}); return false;">${totalPages}</a></li>`;
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

    // 오늘 날짜로 기본값 설정 (yyyy.mm.dd 형식)
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const formattedDate = `${year}.${month}.${day}`;
    const isoDate = `${year}-${month}-${day}`;

    document.getElementById('eventDate').value = formattedDate;
    document.getElementById('eventDateValue').value = isoDate;
    document.getElementById('eventDateWarning').style.display = 'none';

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

        // 날짜 형식 변환 (YYYY-MM-DD -> yyyy.mm.dd)
        const dateParts = item.eventDate.split('-');
        const formattedDate = dateParts.join('.');
        document.getElementById('eventDate').value = formattedDate;
        document.getElementById('eventDateValue').value = item.eventDate;

        // 미래 날짜 경고 확인
        try {
            const inputDate = new Date(item.eventDate);
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const warningElement = document.getElementById('eventDateWarning');
            if (inputDate > today) {
                warningElement.style.display = 'block';
            } else {
                warningElement.style.display = 'none';
            }
        } catch (e) {
            document.getElementById('eventDateWarning').style.display = 'none';
        }

        document.getElementById('eventType').value = item.eventType;
        document.getElementById('transactionType').value = item.transactionType;
        document.getElementById('name').value = item.name;
        document.getElementById('relation').value = item.relation || '';

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
    // 필수 항목 검증
    if (!validateRequiredFields()) {
        return;
    }

    const id = document.getElementById('giftMoneyId').value;

    // 금액은 hidden input에서 가져오기 (콤마 제거된 실제 값)
    const amountValue = document.getElementById('amountValue').value ||
                        document.getElementById('amount').value.replace(/[^\d]/g, '');

    // 날짜는 hidden input에서 가져오기 (YYYY-MM-DD 형식)
    const eventDateValue = document.getElementById('eventDateValue').value ||
                           document.getElementById('eventDate').value.replace(/\./g, '-');

    const transactionType = document.getElementById('transactionType').value;

    const data = {
        eventDate: eventDateValue,
        eventType: document.getElementById('eventType').value,
        transactionType: transactionType,
        name: document.getElementById('name').value,
        relation: document.getElementById('relation').value,
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

        showToast(id ? '수정되었습니다.' : '추가되었습니다.', 'success');

        const modal = bootstrap.Modal.getInstance(document.getElementById('giftMoneyModal'));
        modal.hide();

        // 추가/수정한 거래 유형에 맞는 탭으로 자동 이동
        if (!id) { // 새로 추가한 경우에만 탭 이동
            currentTransactionType = transactionType;

            // 해당 탭 활성화
            const tabId = transactionType === 'RECEIVED' ? 'received-tab' :
                          transactionType === 'SENT' ? 'sent-tab' : 'all-tab';
            const tabButton = document.getElementById(tabId);

            if (tabButton) {
                const tab = new bootstrap.Tab(tabButton);
                tab.show();
            }
        }

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

        showToast('삭제되었습니다.', 'success');

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
        showToast('파일을 선택해주세요.', 'warning');
        return;
    }

    // 파일 크기 제한 (5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
        showToast('파일 크기는 5MB 이하여야 합니다.', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    const uploadButton = document.querySelector('#uploadModal button.btn-primary');
    const originalText = uploadButton.textContent;
    uploadButton.disabled = true;
    uploadButton.textContent = '업로드 중...';

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
        showToast(`업로드 완료! 성공: ${result.successCount}건`, 'success');

        const modal = bootstrap.Modal.getInstance(document.getElementById('uploadModal'));
        modal.hide();

        // 데이터 다시 로드
        await loadGiftMoney();
        await loadStatistics();

    } catch (error) {
        await handleError(error, '업로드에 실패했습니다.');
    } finally {
        uploadButton.disabled = false;
        uploadButton.textContent = originalText;
    }
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
