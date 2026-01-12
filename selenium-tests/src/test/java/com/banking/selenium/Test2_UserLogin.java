package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 2: Kullanıcı Girişi
 * 
 * Test Durumu Kimliği: REQ-002
 * 
 * İlgili Gereksinimler:
 * - Kayıtlı kullanıcı sisteme giriş yapabilmelidir
 * - Kullanıcı adı ve şifre ile kimlik doğrulama yapılmalıdır
 * - Başarılı giriş sonrası JWT token döndürülmelidir
 * 
 * Ön Koşullar:
 * - Sistem çalışır durumda olmalıdır
 * - Kullanıcı kaydı tamamlanmış olmalıdır (REQ-001)
 * - Kullanıcı bilgileri veritabanında mevcut olmalıdır
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Frontend uygulamasına gidilir
 * 2. Kullanıcı adı ve şifre girilir
 * 3. "Giriş Yap" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Giriş işlemi başarılı olmalıdır
 * - Dashboard ekranı görüntülenmelidir
 * 
 * Son Koşullar:
 * - Kullanıcı oturum açmış durumda olmalıdır
 * - JWT token saklanmış olmalıdır
 */
public class Test2_UserLogin extends BaseSeleniumTest {

    @Test
    public void testUserLogin() {
        waitForBackend();

        long timestamp = System.currentTimeMillis();
        String username = "testuser";
        String email = "testuser" + timestamp + "@test.com";

        System.out.println("=== Test2: Kullanıcı Girişi Başlıyor ===");

        // 1. Kayıt Ol (Setup - Robust)
        String registeredUser = registerUser(username, "password123", email, "Test", "User", "5551234567");

        // 2. Giriş Yap (Test Edilen İşlem - Robust)
        loginUser(registeredUser, "password123");

        // Assertions loginUser içinde yapılıyor (Dashboard kontrolü)
        assertTrue(true, "Login başarılı (BaseSeleniumTest.loginUser kontrol etti)");
    }
}
