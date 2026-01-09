-- ============================================
-- V2: Cleanup Old Tables (HTE_ prefix)
-- ============================================
-- Bu migration dosyası eski tabloları temizler
-- HTE_ prefix'li tablolar eski migration'lardan kalmış olabilir

-- Eski tabloları sil (eğer varsa)
DROP TABLE IF EXISTS HTE_users;
DROP TABLE IF EXISTS HTE_accounts;
DROP TABLE IF EXISTS HTE_transactions;
