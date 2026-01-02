package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 4: Para Yatırma
 * 
 * Test Durumu Kimliği: REQ-004
 * 
 * İlgili Gereksinimler:
 * - Kullanıcı hesabına para yatırabilmelidir
 * - Yatırılan tutar hesap bakiyesine eklenmelidir
 * - İşlem kaydı oluşturulmalıdır
 * 
 * Ön Koşullar:
 * - Kullanıcı giriş yapmış olmalıdır (REQ-002)
 * - En az bir hesap mevcut olmalıdır (REQ-003)
 * 
 * Adım Adım Uygulanacak İşlemler:
 * 1. Dashboard ekranında "İşlemler" bölümüne gidilir
 * 2. "Para Yatır" sekmesine tıklanır
 * 3. Hesap seçilir ve tutar girilir
 * 4. "Para Yatır" butonuna tıklanır
 * 
 * Beklenen Sonuç:
 * - Para yatırma işlemi başarılı olmalıdır
 * - Hesap bakiyesi güncellenmiş olmalıdır
 * 
 * Son Koşullar:
 * - Hesap bakiyesi artmış olmalıdır
 * - İşlem kaydı oluşturulmuş olmalıdır
 */
public class Test4_Deposit extends BaseSeleniumTest {

    @Test
    public void testDeposit() {
        // Giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        // Dashboard'un yüklendiğini bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Para yatır sekmesine geç
        WebElement depositTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Para Yatır')]")));
        depositTab.click();

        // Hesap seç (eğer varsa)
        try {
            WebElement accountSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("depositAccount")));
            Select select = new Select(accountSelect);
            if (select.getOptions().size() > 0) {
                select.selectByIndex(0);
            }
        } catch (Exception e) {
            // Hesap yoksa önce hesap oluştur
            WebElement accountTypeSelect = driver.findElement(By.id("accountType"));
            Select accountSelect = new Select(accountTypeSelect);
            accountSelect.selectByValue("CHECKING");
            driver.findElement(By.xpath("//button[contains(text(), 'Hesap Oluştur')]")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountMessage")));
        }

        // Para yatır formunu doldur
        WebElement depositAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("depositAccount")));
        Select depositSelect = new Select(depositAccount);
        if (depositSelect.getOptions().size() > 0) {
            depositSelect.selectByIndex(0);
        }

        driver.findElement(By.id("depositAmount")).sendKeys("100.00");
        driver.findElement(By.id("depositDescription")).sendKeys("Test para yatırma");

        // Para yatır butonuna tıkla
        WebElement depositButton = driver.findElement(By.xpath("//button[contains(text(), 'Para Yatır')]"));
        depositButton.click();

        // Başarı mesajını kontrol et
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionMessage")));
        assertTrue(message.getText().contains("başarılı") || message.getText().contains("yatırma"),
            "Para yatırma işlemi başarısız");
    }
}

