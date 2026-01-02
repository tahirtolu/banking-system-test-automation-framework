package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
        driver.get(FRONTEND_URL);

        // Giriş formunu doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");

        // Giriş butonuna tıkla
        WebElement loginButton = driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']"));
        loginButton.click();

        // Dashboard'un göründüğünü kontrol et
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
        assertTrue(dashboard.isDisplayed(), "Giriş başarısız - Dashboard görünmüyor");
    }
}

