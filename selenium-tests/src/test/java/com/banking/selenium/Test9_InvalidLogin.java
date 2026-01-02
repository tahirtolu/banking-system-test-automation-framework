package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 9: Geçersiz Giriş Denemesi
 * 
 * Test Durumu Kimliği: REQ-009
 * 
 * İlgili Gereksinimler:
 * - Geçersiz kullanıcı adı veya şifre ile giriş yapılmamalıdır
 * - Hata mesajı görüntülenmelidir
 * - Dashboard ekranı görüntülenmemelidir
 * 
 * Ön Koşullar:
 * - Sistem çalışır durumda olmalıdır
 * - Frontend uygulaması erişilebilir olmalıdır
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Frontend uygulamasına gidilir
 * 2. Geçersiz kullanıcı adı ve şifre girilir
 * 3. "Giriş Yap" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Giriş işlemi başarısız olmalıdır
 * - Hata mesajı görüntülenmelidir
 * - Dashboard ekranı görüntülenmemelidir
 * 
 * Son Koşullar:
 * - Kullanıcı oturum açmamış durumda olmalıdır
 * - Sistem güvenliği korunmuş olmalıdır
 */
public class Test9_InvalidLogin extends BaseSeleniumTest {

    @Test
    public void testInvalidLogin() {
        driver.get(FRONTEND_URL);

        // Geçersiz bilgilerle giriş yapmayı dene
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("invalid_user");
        driver.findElement(By.id("loginPassword")).sendKeys("wrong_password");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        // Hata mesajının göründüğünü kontrol et
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginMessage")));
        assertTrue(message.getText().contains("hatalı") || message.getText().contains("error") ||
                   message.getText().contains("başarısız"),
            "Hata mesajı görüntülenmedi");

        // Dashboard'un görünmediğini kontrol et
        try {
            WebElement dashboard = driver.findElement(By.id("dashboard-section"));
            assertTrue(!dashboard.isDisplayed() || dashboard.getCssValue("display").equals("none"),
                "Dashboard görünmemeli");
        } catch (Exception e) {
            // Dashboard yoksa bu beklenen davranış
            assertTrue(true);
        }
    }
}

