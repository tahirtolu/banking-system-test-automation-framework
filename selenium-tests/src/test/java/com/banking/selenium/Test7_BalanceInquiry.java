package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 7: Bakiye Sorgulama
 * 
 * Test Durumu Kimliği: REQ-007
 * 
 * İlgili Gereksinimler:
 * - Kullanıcı tüm hesaplarının bakiyelerini görüntüleyebilmelidir
 * - Hesap bilgileri doğru şekilde görüntülenmelidir
 * 
 * Ön Koşullar:
 * - Kullanıcı giriş yapmış olmalıdır (REQ-002)
 * - En az bir hesap mevcut olmalıdır (REQ-003)
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Dashboard ekranında "Hesaplarım" bölümüne gidilir
 * 2. Sistem otomatik olarak hesapları yükler
 * 
 * Beklenen Sonuç:
 * - Tüm hesaplar listelenmelidir
 * - Her hesap için bakiye bilgisi görüntülenmelidir
 * 
 * Son Koşullar:
 * - Hesap bilgileri güncel olmalıdır
 */
public class Test7_BalanceInquiry extends BaseSeleniumTest {

    @Test
    public void testBalanceInquiry() {
        // Giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Hesaplarım bölümünü kontrol et
        WebElement accountsList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountsList")));
        
        // Hesap bilgilerinin görüntülendiğini kontrol et
        assertTrue(accountsList.isDisplayed(), "Hesaplar listesi görünmüyor");
        
        // Eğer hesap varsa, bakiye bilgisinin göründüğünü kontrol et
        String accountsText = accountsList.getText();
        if (!accountsText.contains("Henüz hesabınız yok")) {
            assertTrue(accountsText.contains("Hesap No") || accountsText.contains("Bakiye"),
                "Hesap bilgileri görüntülenmiyor");
        }
    }
}

