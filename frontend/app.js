const API_BASE_URL = '/api';

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

        const data = await response.json();
        if (response.ok) {
            currentToken = data.token;
            document.getElementById('usernameDisplay').textContent = username;
            document.getElementById('auth-section').style.display = 'none';
            document.getElementById('dashboard-section').style.display = 'block';
            loadAccounts();
        } else {
            showMessage('loginMessage', data.error || 'Giriş başarısız', 'error');
        }
    } catch (error) {
        showMessage('loginMessage', 'Bağlantı hatası', 'error');
    }
});

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

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('registerMessage', 'Kayıt başarılı! Giriş yapabilirsiniz.', 'success');
            setTimeout(() => showLogin(), 2000);
        } else {
            showMessage('registerMessage', data.error || 'Kayıt başarısız', 'error');
        }
    } catch (error) {
        showMessage('registerMessage', 'Bağlantı hatası', 'error');
    }
});

function logout() {
    currentToken = null;
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
        }
    } catch (error) {
        console.error('Hesaplar yüklenemedi:', error);
    }
}

function displayAccounts() {
    const accountsList = document.getElementById('accountsList');
    if (currentAccounts.length === 0) {
        accountsList.innerHTML = '<p>Henüz hesabınız yok. Lütfen hesap oluşturun.</p>';
        return;
    }

    accountsList.innerHTML = currentAccounts.map(account => `
        <div class="account-item">
            <strong>Hesap No:</strong> ${account.accountNumber}<br>
            <strong>Bakiye:</strong> ${account.balance.toFixed(2)} TL<br>
            <strong>Tip:</strong> ${account.accountType === 'CHECKING' ? 'Vadesiz' : 'Vadeli'}
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
    try {
        const response = await fetch(`${API_BASE_URL}/accounts?accountType=${accountType}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        const data = await response.json();
        if (response.ok) {
            showMessage('accountMessage', 'Hesap başarıyla oluşturuldu!', 'success');
            loadAccounts();
        } else {
            showMessage('accountMessage', data.error || 'Hesap oluşturulamadı', 'error');
        }
    } catch (error) {
        showMessage('accountMessage', 'Bağlantı hatası', 'error');
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
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${accountNumber}/history`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });

        if (response.ok) {
            const transactions = await response.json();
            displayTransactionHistory(transactions);
        }
    } catch (error) {
        console.error('İşlem geçmişi yüklenemedi:', error);
    }
}

function displayTransactionHistory(transactions) {
    const historyDiv = document.getElementById('transactionHistory');
    if (transactions.length === 0) {
        historyDiv.innerHTML = '<p>İşlem geçmişi bulunamadı.</p>';
        return;
    }

    historyDiv.innerHTML = transactions.map(txn => {
        const typeClass = txn.transactionType.toLowerCase();
        const typeText = {
            'deposit': 'Para Yatırma',
            'withdrawal': 'Para Çekme',
            'transfer': 'Transfer'
        }[txn.transactionType.toLowerCase()] || txn.transactionType;

        return `
            <div class="transaction-item ${typeClass}">
                <strong>${typeText}</strong><br>
                <strong>Tutar:</strong> ${txn.amount.toFixed(2)} TL<br>
                <strong>Tarih:</strong> ${new Date(txn.transactionDate).toLocaleString('tr-TR')}<br>
                ${txn.description ? `<strong>Açıklama:</strong> ${txn.description}<br>` : ''}
                ${txn.toAccountNumber ? `<strong>Alıcı Hesap:</strong> ${txn.toAccountNumber}` : ''}
            </div>
        `;
    }).join('');
}

function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.className = `message ${type}`;
    setTimeout(() => {
        element.className = 'message';
    }, 5000);
}

