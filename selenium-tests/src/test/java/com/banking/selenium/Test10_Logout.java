package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 10: Çıkış Yapma
 * 
 * Test Durumu Kimliği: REQ-010
 * 
 * İlgili Gereksinimler:
 * - Kullanıcı sistemden çıkış yapabilmelidir
 * - Çıkış sonrası oturum kapatılmalıdır
 * - Kullanıcı giriş ekranına yönlendirilmelidir
 * 
 * Ön Koşullar:
 * - Kullanıcı giriş yapmış olmalıdır (REQ-002)
 * - Dashboard ekranı görüntüleniyor olmalıdır
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Dashboard ekranında "Çıkış Yap" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Çıkış işlemi başarılı olmalıdır
 * - Kullanıcı giriş ekranına yönlendirilmelidir
 * 
 * Son Koşullar:
 * - Kullanıcı oturum kapatılmış durumda olmalıdır
 * - JWT token geçersiz hale gelmelidir
 */
public class Test10_Logout extends BaseSeleniumTest {

    @Test
    public void testLogout() {
        // Önce giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        // Dashboard'un yüklendiğini bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Çıkış yap butonuna tıkla
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Çıkış Yap')]")));
        logoutButton.click();

        // Auth section'un göründüğünü kontrol et
        WebElement authSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("auth-section")));
        assertTrue(authSection.isDisplayed(), "Çıkış işlemi başarısız - Auth section görünmüyor");

        // Dashboard'un görünmediğini kontrol et
        WebElement dashboard = driver.findElement(By.id("dashboard-section"));
        assertTrue(!dashboard.isDisplayed() || dashboard.getCssValue("display").equals("none"),
            "Dashboard hala görünüyor");
    }
}

