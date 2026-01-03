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
        driver.get(FRONTEND_URL);

        // Kayıt sekmesine geç
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();

        // Form alanlarını doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));
        driver.findElement(By.id("regUsername")).sendKeys("selenium_user_" + System.currentTimeMillis());
        driver.findElement(By.id("regPassword")).sendKeys("password123");
        driver.findElement(By.id("regEmail")).sendKeys("selenium" + System.currentTimeMillis() + "@test.com");
        driver.findElement(By.id("regFirstName")).sendKeys("Selenium");
        driver.findElement(By.id("regLastName")).sendKeys("Test");
        driver.findElement(By.id("regPhone")).sendKeys("5551234567");

        // Kayıt butonuna tıkla (JavaScript click - headless mod için daha güvenilir)
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        // Başarı mesajı elementinin görünür olmasını bekle
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));
        
        // Metnin dolmasını bekle (Boş olmamasını sağla)
        wait.until(driver -> {
            String text = message.getText().trim();
            return !text.isEmpty();
        });

        String messageText = message.getText().trim();
        System.out.println("Register message: [" + messageText + "]");
        
        // Hata durumunda sayfa kaynağını yazdır (Sorunu anlamak için)
        if (messageText.isEmpty()) {
            System.out.println("SAYFA KAYNAĞI: " + driver.getPageSource());
        }
        
        String lowerMessage = messageText.toLowerCase();
        assertTrue(lowerMessage.contains("başarılı") || lowerMessage.contains("success"),
            "Kayıt işlemi başarısız oldu. Mesaj: [" + messageText + "]");
    }
}

