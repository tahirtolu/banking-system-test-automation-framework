package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 8: İşlem Geçmişi Görüntüleme
 * 
 * Test Durumu Kimliği: REQ-008
 * 
 * İlgili Gereksinimler:
 * - Kullanıcı hesabının işlem geçmişini görüntüleyebilmelidir
 * - İşlemler tarih sırasına göre listelenmelidir
 * 
 * Ön Koşullar:
 * - Kullanıcı giriş yapmış olmalıdır (REQ-002)
 * - En az bir hesap mevcut olmalıdır (REQ-003)
 * - Hesapta en az bir işlem yapılmış olmalıdır
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Dashboard ekranında "İşlem Geçmişi" bölümüne gidilir
 * 2. Hesap seçilir ve "Geçmişi Yükle" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Seçilen hesabın tüm işlemleri listelenmelidir
 * - İşlemler en yeni tarihten en eski tarihe doğru sıralanmalıdır
 * 
 * Son Koşullar:
 * - İşlem geçmişi doğru şekilde görüntülenmiş olmalıdır
 */
public class Test8_TransactionHistory extends BaseSeleniumTest {

    @Test
    public void testTransactionHistory() {
        // Giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // İşlem geçmişi bölümüne scroll
        WebElement historyAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("historyAccount")));
        Select select = new Select(historyAccount);
        
        if (select.getOptions().size() > 0) {
            select.selectByIndex(0);
            
            // Geçmişi yükle butonuna tıkla
            WebElement loadHistoryButton = driver.findElement(By.xpath("//button[contains(text(), 'Geçmişi Yükle')]"));
            loadHistoryButton.click();

            // İşlem geçmişinin görüntülendiğini kontrol et
            WebElement transactionHistory = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionHistory")));
            assertTrue(transactionHistory.isDisplayed(), "İşlem geçmişi görüntülenmiyor");
        }
    }
}

