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
     * Backend'in hazır olmasını bekle (max 90 saniye)
     * Önce direkt backend'e (8081) bak, sonra Nginx üzerinden (8082) kontrol et
     */
    private void waitForBackend() {
        String backendDirectUrl = "http://localhost:8081/api/auth/login";  // Direkt backend
        String backendNginxUrl = "http://localhost:8082/api/auth/login";   // Nginx üzerinden
        int maxAttempts = 90;  // 90 saniyeye çıkarıldı
        int attempt = 0;

        System.out.println("Backend hazır olana kadar bekleniyor...");
        System.out.println("1. Adım: Direkt backend kontrolü (port 8081)...");

        // Önce direkt backend'e bak (Nginx'e bağlı değil)
        boolean backendReady = false;
        while (attempt < maxAttempts && !backendReady) {
            try {
                URL url = new URL(backendDirectUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                // 403, 405, 200 = Backend çalışıyor
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Backend hazır! (Direkt - HTTP " + responseCode + ")");
                    backendReady = true;
                    break;
                }

                if (attempt % 10 == 0) {
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") Backend henüz hazır değil (HTTP " + responseCode + ")");
                }

            } catch (Exception e) {
                if (attempt % 10 == 0) {
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") Backend henüz başlamadı: " + e.getMessage());
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        if (!backendReady) {
            System.err.println("⚠ UYARI: Backend 90 saniye içinde hazır olmadı!");
            return;
        }

        // Backend hazır, şimdi Nginx üzerinden kontrol et
        System.out.println("2. Adım: Nginx proxy kontrolü (port 8082)...");
        attempt = 0;
        while (attempt < 30) {  // Nginx için 30 saniye yeterli
            try {
                URL url = new URL(backendNginxUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                // 403, 405, 200, 502 değil = Nginx çalışıyor ve backend'e ulaşabiliyor
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Nginx proxy hazır! (HTTP " + responseCode + ")");
                    return;
                }
                
                if (responseCode == 502) {
                    System.out.println("Nginx 502 döndürüyor, backend'e ulaşamıyor... (" + (attempt + 1) + "/30)");
                }

            } catch (Exception e) {
                System.out.println("Nginx kontrolü... (" + (attempt + 1) + "/30)");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        System.err.println("⚠ UYARI: Nginx proxy 30 saniye içinde hazır olmadı, ama direkt backend çalışıyor!");
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

