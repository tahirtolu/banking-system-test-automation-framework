// API Base URL - Otomatik algılama
let API_BASE_URL;
if (window.location.protocol === 'file:') {
    // HTML dosyası direkt açıldıysa
    API_BASE_URL = 'http://localhost:8081/api';
    console.log('📁 HTML dosyası olarak açıldı, API URL:', API_BASE_URL);
} else if (window.location.hostname === 'localhost' && window.location.port === '8082') {
    // Nginx proxy üzerinden (port 8082)
    API_BASE_URL = '/api';
    console.log('🐳 Docker Nginx üzerinden, API URL:', API_BASE_URL);
} else {
    // Direkt backend erişimi (port 8081)
    API_BASE_URL = 'http://localhost:8081/api';
    console.log('🔌 Direkt backend erişimi, API URL:', API_BASE_URL);
}
console.log('✅ API Base URL:', API_BASE_URL);

let currentToken = null;
let currentAccounts = [];

// Auth Functions
function showLogin() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
    document.querySelectorAll('.auth-tabs .tab-btn')[0].classList.add('active');
    document.querySelectorAll('.auth-tabs .tab-btn')[1].classList.remove('active');
}

function showRegister() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
    document.querySelectorAll('.auth-tabs .tab-btn')[0].classList.remove('active');
    document.querySelectorAll('.auth-tabs .tab-btn')[1].classList.add('active');
}

// Login Form Event Listener
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        // Response'un boş olup olmadığını kontrol et
        const text = await response.text();
        let data;
        if (text && text.trim()) {
            try {
                data = JSON.parse(text);
            } catch (parseError) {
                console.error('JSON parse error:', parseError);
                showMessage('loginMessage', `Backend hatası: Geçersiz JSON yanıtı. Status: ${response.status}`, 'error');
                return;
            }
        } else {
            showMessage('loginMessage', `❌ Boş yanıt. HTTP Status: ${response.status}`, 'error');
            return;
        }

        if (response.ok) {
            currentToken = data.token;
            localStorage.setItem('authToken', data.token);
            document.getElementById('usernameDisplay').textContent = username;
            document.getElementById('auth-section').style.display = 'none';
            document.getElementById('dashboard-section').style.display = 'block';
            loadAccounts();
        } else {
            showMessage('loginMessage', data.error || `❌ Giriş başarısız. Status: ${response.status}`, 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        if (error.message.includes('fetch')) {
            showMessage('loginMessage', `❌ Backend'e bağlanılamıyor. ${API_BASE_URL} adresini kontrol edin.`, 'error');
        } else {
            showMessage('loginMessage', '❌ Bağlantı hatası: ' + error.message, 'error');
        }
    }
});

// Register Form Event Listener
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = {
        username: document.getElementById('regUsername').value,
        password: document.getElementById('regPassword').value,
        email: document.getElementById('regEmail').value,
        firstName: document.getElementById('regFirstName').value,
        lastName: document.getElementById('regLastName').value,
        phoneNumber: document.getElementById('regPhone').value
    };

    const messageEl = document.getElementById('registerMessage');
    messageEl.innerHTML = '<div class="loading">Kayıt yapılıyor...</div>';
    messageEl.className = 'message loading';
    messageEl.style.display = 'block';

    try {
        console.log('API URL:', `${API_BASE_URL}/auth/register`);
        console.log('Request data:', formData);

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);

        // Response'un boş olup olmadığını kontrol et
        const text = await response.text();
        console.log('Response text:', text);

        let data;
        if (text && text.trim()) {
            try {
                data = JSON.parse(text);
            } catch (parseError) {
                console.error('JSON parse error:', parseError);
                showMessage('registerMessage', `Backend hatası: Geçersiz JSON yanıtı. Status: ${response.status}`, 'error');
                return;
            }
        } else {
            // Boş response
            if (response.ok || response.status === 201) {
                showMessage('registerMessage', '✅ Kayıt başarılı! Giriş yapabilirsiniz.', 'success');
                setTimeout(() => showLogin(), 2000);
                return;
            } else {
                showMessage('registerMessage', `❌ Kayıt başarısız. HTTP Status: ${response.status}`, 'error');
                return;
            }
        }

        console.log('Parsed data:', data);
        console.log('Response OK?', response.ok);
        console.log('Response status:', response.status);
        
        if (response.ok || response.status === 201) {
            console.log('✅ Registration successful!');
            showMessage('registerMessage', '✅ Kayıt başarılı! Giriş yapabilirsiniz.', 'success');
            // Formu temizle
            document.getElementById('registerForm').reset();
            setTimeout(() => showLogin(), 2000);
        } else {
            console.error('❌ Registration failed:', data);
            showMessage('registerMessage', data.error || `❌ Kayıt başarısız. Status: ${response.status}`, 'error');
        }
    } catch (error) {
        console.error('Register error:', error);
        console.error('API URL:', `${API_BASE_URL}/auth/register`);
        
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showMessage('registerMessage', `❌ Backend'e bağlanılamıyor!\n\nLütfen kontrol edin:\n1. Backend çalışıyor mu? (http://localhost:8081)\n2. Tarayıcı konsolunu açın (F12) ve hataları kontrol edin\n3. API URL: ${API_BASE_URL}`, 'error');
        } else if (error.message.includes('JSON')) {
            showMessage('registerMessage', `❌ Backend yanıt vermiyor!\n\nLütfen kontrol edin:\n1. Backend loglarını kontrol edin\n2. http://localhost:8081/api/auth/register adresini tarayıcıda test edin\n3. CORS hatası olabilir`, 'error');
        } else {
            showMessage('registerMessage', '❌ Hata: ' + error.message, 'error');
        }
    }
});

function logout() {
    currentToken = null;
    localStorage.removeItem('authToken'); // Token'ı temizle
    currentAccounts = [];
    document.getElementById('auth-section').style.display = 'block';
    document.getElementById('dashboard-section').style.display = 'none';
    document.getElementById('loginForm').reset();
    document.getElementById('registerForm').reset();
}

// Account Functions
async function loadAccounts() {
    try {
        const response = await fetch(`${API_BASE_URL}/accounts`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        if (response.ok) {
            currentAccounts = await response.json();
            displayAccounts();
            updateAccountSelects();
        } else if (response.status === 401 || response.status === 403) {
            // Token geçersiz, çıkış yap
            logout();
            showMessage('accountMessage', 'Oturum süreniz doldu. Lütfen tekrar giriş yapın.', 'error');
        }
    } catch (error) {
        console.error('Hesaplar yüklenemedi:', error);
        showMessage('accountMessage', 'Hesaplar yüklenirken hata oluştu: ' + error.message, 'error');
    }
}

function displayAccounts() {
    const accountsList = document.getElementById('accountsList');
    if (currentAccounts.length === 0) {
        accountsList.innerHTML = '<div class="empty-state"><p>📭 Henüz hesabınız yok. Lütfen hesap oluşturun.</p></div>';
        return;
    }

    accountsList.innerHTML = currentAccounts.map(account => `
        <div class="account-item">
            <div class="account-header">
                <h3>💳 ${account.accountNumber}</h3>
                <span class="account-type-badge ${account.accountType === 'CHECKING' ? 'checking' : 'savings'}">
                    ${account.accountType === 'CHECKING' ? 'Vadesiz' : 'Vadeli'}
                </span>
            </div>
            <div class="account-balance">
                <span class="balance-label">Bakiye:</span>
                <span class="balance-amount">${parseFloat(account.balance).toFixed(2)} TL</span>
            </div>
            <div class="account-actions">
                <button class="btn btn-sm btn-info" onclick="viewAccountDetails('${account.accountNumber}')">Detaylar</button>
                <button class="btn btn-sm btn-primary" onclick="selectAccountForTransaction('${account.accountNumber}')">İşlem Yap</button>
            </div>
        </div>
    `).join('');
}

function updateAccountSelects() {
    const selects = ['depositAccount', 'withdrawAccount', 'transferFromAccount', 'historyAccount'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        select.innerHTML = currentAccounts.map(acc => 
            `<option value="${acc.accountNumber}">${acc.accountNumber} - ${acc.balance.toFixed(2)} TL</option>`
        ).join('');
    });
}

async function createAccount() {
    const accountType = document.getElementById('accountType').value;
    const accountMessageEl = document.getElementById('accountMessage');
    accountMessageEl.innerHTML = '<div class="loading">Hesap oluşturuluyor</div>';
    accountMessageEl.className = 'message loading';
    accountMessageEl.style.display = 'block';
    
    try {
        const response = await fetch(`${API_BASE_URL}/accounts?accountType=${accountType}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('accountMessage', `✅ ${data.message || 'Hesap başarıyla oluşturuldu!'} Hesap No: ${data.accountNumber}`, 'success');
            loadAccounts();
        } else {
            showMessage('accountMessage', `❌ ${data.error || 'Hesap oluşturulamadı'}`, 'error');
        }
    } catch (error) {
        showMessage('accountMessage', '❌ Bağlantı hatası: ' + error.message, 'error');
    }
}

// Transaction Functions
function showTransactionTab(tab) {
    ['deposit', 'withdraw', 'transfer'].forEach(t => {
        document.getElementById(`${t}-tab`).style.display = t === tab ? 'block' : 'none';
    });
    document.querySelectorAll('.transaction-tabs .tab-btn').forEach((btn, idx) => {
        btn.classList.toggle('active', ['deposit', 'withdraw', 'transfer'][idx] === tab);
    });
}

async function deposit() {
    const accountNumber = document.getElementById('depositAccount').value;
    const amount = parseFloat(document.getElementById('depositAmount').value);
    const description = document.getElementById('depositDescription').value;

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${accountNumber}/deposit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ amount, description })
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('transactionMessage', 'Para yatırma başarılı!', 'success');
            document.getElementById('depositAmount').value = '';
            document.getElementById('depositDescription').value = '';
            loadAccounts();
        } else {
            showMessage('transactionMessage', data.error || 'İşlem başarısız', 'error');
        }
    } catch (error) {
        showMessage('transactionMessage', 'Bağlantı hatası', 'error');
    }
}

async function withdraw() {
    const accountNumber = document.getElementById('withdrawAccount').value;
    const amount = parseFloat(document.getElementById('withdrawAmount').value);
    const description = document.getElementById('withdrawDescription').value;

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${accountNumber}/withdraw`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ amount, description })
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('transactionMessage', 'Para çekme başarılı!', 'success');
            document.getElementById('withdrawAmount').value = '';
            document.getElementById('withdrawDescription').value = '';
            loadAccounts();
        } else {
            showMessage('transactionMessage', data.error || 'İşlem başarısız', 'error');
        }
    } catch (error) {
        showMessage('transactionMessage', 'Bağlantı hatası', 'error');
    }
}

async function transfer() {
    const fromAccountNumber = document.getElementById('transferFromAccount').value;
    const toAccountNumber = document.getElementById('transferToAccount').value;
    const amount = parseFloat(document.getElementById('transferAmount').value);
    const description = document.getElementById('transferDescription').value;

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${fromAccountNumber}/transfer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ toAccountNumber, amount, description })
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('transactionMessage', 'Transfer başarılı!', 'success');
            document.getElementById('transferAmount').value = '';
            document.getElementById('transferToAccount').value = '';
            document.getElementById('transferDescription').value = '';
            loadAccounts();
        } else {
            showMessage('transactionMessage', data.error || 'Transfer başarısız', 'error');
        }
    } catch (error) {
        showMessage('transactionMessage', 'Bağlantı hatası', 'error');
    }
}

async function loadTransactionHistory() {
    const accountNumber = document.getElementById('historyAccount').value;
    if (!accountNumber) {
        showMessage('transactionMessage', 'Lütfen bir hesap seçin', 'error');
        return;
    }
    
    const historyDiv = document.getElementById('transactionHistory');
    historyDiv.innerHTML = '<div class="loading">İşlem geçmişi yükleniyor</div>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${accountNumber}/history`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        if (response.ok) {
            const transactions = await response.json();
            displayTransactionHistory(transactions);
        } else if (response.status === 404) {
            historyDiv.innerHTML = '<div class="empty-state"><p>Bu hesap için işlem geçmişi bulunamadı.</p></div>';
        } else {
            showMessage('transactionMessage', 'İşlem geçmişi yüklenemedi', 'error');
            historyDiv.innerHTML = '';
        }
    } catch (error) {
        console.error('İşlem geçmişi yüklenemedi:', error);
        showMessage('transactionMessage', 'Bağlantı hatası: ' + error.message, 'error');
        historyDiv.innerHTML = '';
    }
}

function displayTransactionHistory(transactions) {
    const historyDiv = document.getElementById('transactionHistory');
    if (transactions.length === 0) {
        historyDiv.innerHTML = '<div class="empty-state"><p>📋 Bu hesap için işlem geçmişi bulunamadı.</p></div>';
        return;
    }

    historyDiv.innerHTML = transactions.map(txn => {
        const typeClass = txn.transactionType.toLowerCase();
        const typeText = {
            'deposit': '💰 Para Yatırma',
            'withdrawal': '💸 Para Çekme',
            'transfer': '🔄 Transfer'
        }[txn.transactionType.toLowerCase()] || txn.transactionType;

        const amountClass = typeClass === 'deposit' ? 'amount-positive' : typeClass === 'withdrawal' ? 'amount-negative' : 'amount-neutral';
        const amountPrefix = typeClass === 'deposit' ? '+' : typeClass === 'withdrawal' ? '-' : '';

        return `
            <div class="transaction-item ${typeClass}">
                <div class="transaction-header">
                    <span class="transaction-type">${typeText}</span>
                    <span class="transaction-amount ${amountClass}">${amountPrefix}${parseFloat(txn.amount).toFixed(2)} TL</span>
                </div>
                <div class="transaction-details">
                    <div class="transaction-date">📅 ${new Date(txn.transactionDate).toLocaleString('tr-TR')}</div>
                    ${txn.description ? `<div class="transaction-description">📝 ${txn.description}</div>` : ''}
                    ${txn.toAccountNumber ? `<div class="transaction-target">➡️ Alıcı: ${txn.toAccountNumber}</div>` : ''}
                    ${txn.transactionNumber ? `<div class="transaction-id">🔖 İşlem No: ${txn.transactionNumber}</div>` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.className = `message ${type}`;
    element.style.display = 'block';
    setTimeout(() => {
        element.className = 'message';
        element.style.display = 'none';
    }, 5000);
}

// Yeni yardımcı fonksiyonlar
function viewAccountDetails(accountNumber) {
    // Hesap detaylarını göster (gelecekte modal eklenebilir)
    alert(`Hesap Detayları:\nHesap No: ${accountNumber}\nDetaylı bilgi için API çağrısı yapılabilir.`);
}

function selectAccountForTransaction(accountNumber) {
    // İşlem sekmesine geç ve hesabı seç
    if (document.getElementById('depositAccount').options.length > 0) {
        document.getElementById('depositAccount').value = accountNumber;
        showTransactionTab('deposit');
    }
}

// Sayfa yüklendiğinde hesapları otomatik yükle (eğer giriş yapıldıysa)
window.addEventListener('DOMContentLoaded', () => {
    // Token kontrolü
    const savedToken = localStorage.getItem('authToken');
    if (savedToken) {
        currentToken = savedToken;
        // Dashboard'u göster ve hesapları yükle
        document.getElementById('auth-section').style.display = 'none';
        document.getElementById('dashboard-section').style.display = 'block';
        loadAccounts();
    }
});



