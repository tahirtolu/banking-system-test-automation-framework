package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.HttpURLConnection;
import java.net.URL;

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

    /**
     * Backend'in hazır olmasını bekle (max 60 saniye)
     */
    private void waitForBackend() {
        String backendHealthUrl = "http://localhost:8082/api/auth/login";
        int maxAttempts = 60;
        int attempt = 0;

        System.out.println("Backend hazır olana kadar bekleniyor...");

        while (attempt < maxAttempts) {
            try {
                URL url = new URL(backendHealthUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                // 403, 405, 200 = Backend çalışıyor
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Backend hazır! (HTTP " + responseCode + ")");
                    return;
                }

                System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ")");

            } catch (Exception e) {
                System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ")");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        System.err.println("⚠ UYARI: Backend 60 saniye içinde hazır olmadı!");
    }

    @Test
    public void testUserRegistration() {
        waitForBackend();  // Backend'i bekle

        driver.get(FRONTEND_URL);

        // Kayıt sekmesine geç
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();

        // Form alanlarını doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));
        
        // ✅ KRİTİK DÜZELTME: Username max 20 karakter
        long timestamp = System.currentTimeMillis();
        String username = "test" + (timestamp % 100000); // test12345 formatı (9 karakter)
        String email = "test" + timestamp + "@test.com";
        
        driver.findElement(By.id("regUsername")).sendKeys(username);
        driver.findElement(By.id("regPassword")).sendKeys("password123");
        driver.findElement(By.id("regEmail")).sendKeys(email);
        driver.findElement(By.id("regFirstName")).sendKeys("Selenium");
        driver.findElement(By.id("regLastName")).sendKeys("Test");
        driver.findElement(By.id("regPhone")).sendKeys("5551234567");

        System.out.println("✓ Form dolduruldu: " + username + " / " + email);

        // Kayıt butonuna tıkla (JavaScript click - headless mod için daha güvenilir)
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        // API isteğinin tamamlanması için bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Başarı mesajı elementinin görünür olmasını bekle
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));
        
        // Metnin dolmasını bekle (Boş olmamasını sağla)
        wait.until(driver -> {
            String text = message.getText().trim();
            return !text.isEmpty();
        });

        String messageText = message.getText().trim();
        System.out.println("\n=== SONUÇ ===");
        System.out.println("Register message: [" + messageText + "]");
        
        String lowerMessage = messageText.toLowerCase();
        assertTrue(lowerMessage.contains("başarılı") || lowerMessage.contains("success"),
            "Kayıt işlemi başarısız oldu. Mesaj: [" + messageText + "]");
    }
}

