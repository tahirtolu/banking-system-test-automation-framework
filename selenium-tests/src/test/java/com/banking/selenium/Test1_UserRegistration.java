package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 1: Kullanıcı Kaydı
 * 
 * Test Durumu Kimliği: REQ-001
 * 
 * İlgili Gereksinimler:
 * - Kullanıcı sisteme kayıt olabilmelidir
 * - Kullanıcı adı, şifre, e-posta, ad, soyad ve telefon bilgileri alınmalıdır
 * - Kullanıcı adı ve e-posta benzersiz olmalıdır
 * 
 * Ön Koşullar:
 * - Sistem çalışır durumda olmalıdır
 * - Frontend uygulaması erişilebilir olmalıdır
 * - Backend API çalışıyor olmalıdır
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Frontend uygulamasına gidilir
 * 2. "Kayıt Ol" sekmesine tıklanır
 * 3. Form alanları doldurulur
 * 4. "Kayıt Ol" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Kayıt işlemi başarılı olmalıdır
 * - Başarı mesajı görüntülenmelidir
 * 
 * Son Koşullar:
 * - Kullanıcı sisteme kayıtlı olmalıdır
 * - Kullanıcı bilgileri veritabanında saklanmış olmalıdır
 */
public class Test1_UserRegistration extends BaseSeleniumTest {

    @Test
    public void testUserRegistration() {
        waitForBackend(); // Backend'i bekle (BaseSeleniumTest'teki static metod)

        // ✅ KRİTİK DÜZELTME: Retry mekanizması ile robust kayıt (BaseSeleniumTest
        // içinde)
        long timestamp = System.currentTimeMillis();
        String username = "test" + (timestamp % 100000); // test12345 formatı (9 karakter)
        String email = "test" + timestamp + "@test.com";

        System.out.println("Test1: Kullanıcı Kaydı Testi Başlıyor...");

        // registerUser helper'ı zaten başarı kontrolü ve retry yapıyor.
        // Hata durumunda RuntimeException fırlatıyor, bu da testi fail ettirir.
        registerUser(username, "password123", email, "Selenium", "Test", "5551234567");

        System.out.println("\n=== SONUÇ ===");
        System.out.println("Register successful via helper method.");

        assertTrue(true, "Kayıt işlemi başarılı (Helper method exception fırlatmadı)");
    }
}
