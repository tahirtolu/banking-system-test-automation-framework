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
        waitForBackend();  // Backend'i bekle (BaseSeleniumTest'teki static metod)

        // Önce kullanıcı kaydı yap (testuser kullanıcısı yoksa)
        driver.get(FRONTEND_URL);

        // Kayıt sekmesine geç
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();

        // Kullanıcı kaydı formunu doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));
        long timestamp = System.currentTimeMillis();
        String username = "testuser";  // Sabit kullanıcı adı (Test2 için)
        String email = "testuser" + timestamp + "@test.com";  // Unique email
        
        driver.findElement(By.id("regUsername")).sendKeys(username);
        driver.findElement(By.id("regPassword")).sendKeys("password123");
        driver.findElement(By.id("regEmail")).sendKeys(email);
        driver.findElement(By.id("regFirstName")).sendKeys("Test");
        driver.findElement(By.id("regLastName")).sendKeys("User");
        driver.findElement(By.id("regPhone")).sendKeys("5551234567");

        System.out.println("✓ Kullanıcı kaydı yapılıyor: " + username);

        // Kayıt butonuna tıkla (JavaScript click)
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        // Kayıt işleminin tamamlanması için bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kayıt mesajını kontrol et (opsiyonel - hata olsa bile devam et)
        try {
            WebElement registerMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("registerMessage")));
            String registerMsg = registerMessage.getText().trim();
            System.out.println("Kayıt mesajı: [" + registerMsg + "]");
        } catch (Exception e) {
            System.out.println("Kayıt mesajı kontrol edilemedi (devam ediliyor)");
        }

        // Giriş sekmesine geç
        WebElement loginTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Giriş Yap')]")));
        loginTab.click();

        // Giriş formunu doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).clear();
        driver.findElement(By.id("loginUsername")).sendKeys(username);
        driver.findElement(By.id("loginPassword")).clear();
        driver.findElement(By.id("loginPassword")).sendKeys("password123");

        System.out.println("✓ Giriş formu dolduruldu: " + username);

        // Giriş butonuna tıkla (JavaScript click)
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//form[@id='loginForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);

        // API isteğinin tamamlanması için bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Login mesajını kontrol et (hata durumunda)
        try {
            WebElement loginMessage = driver.findElement(By.id("loginMessage"));
            String loginMsg = loginMessage.getText().trim();
            if (!loginMsg.isEmpty()) {
                System.out.println("Login mesajı: [" + loginMsg + "]");
            }
        } catch (Exception e) {
            // Mesaj yoksa sorun değil
        }

        // Dashboard'un göründüğünü kontrol et
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
        String displayStyle = dashboard.getCssValue("display");
        
        System.out.println("\n=== SONUÇ ===");
        System.out.println("Dashboard display style: [" + displayStyle + "]");
        System.out.println("Dashboard isDisplayed: [" + dashboard.isDisplayed() + "]");
        
        assertTrue(dashboard.isDisplayed() && !displayStyle.equals("none"), 
            "Giriş başarısız - Dashboard görünmüyor");
        
        System.out.println("✓ Giriş başarılı - Dashboard görünüyor!");
    }
}

