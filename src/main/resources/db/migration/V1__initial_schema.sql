-- ============================================
-- V1: Initial Database Schema (SQLite)
-- ============================================
-- Bu migration dosyası mevcut veritabanı yapısını oluşturur
-- Testleri bozmamak için mevcut entity yapısına uygun hazırlanmıştır
-- Not: PRAGMA foreign_keys = ON; uygulama başlangıcında etkinleştirilecek

-- Users Tablosu
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone_number TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Accounts Tablosu
CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT NOT NULL UNIQUE,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    account_type TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Transactions Tablosu
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_number TEXT NOT NULL UNIQUE,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type TEXT NOT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    account_id INTEGER NOT NULL,
    to_account_id INTEGER,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

-- Indexler (Performans için)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account_id ON transactions(to_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_date ON transactions(transaction_date);

