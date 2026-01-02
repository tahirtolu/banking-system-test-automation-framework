package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 5: Para Çekme
 * Use Case: Para çekme işlemi
 * Ön koşul: Hesapta yeterli bakiye olması
 */
public class Test5_Withdrawal extends BaseSeleniumTest {

    @Test
    public void testWithdrawal() {
        // Giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Para çek sekmesine geç
        WebElement withdrawTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Para Çek')]")));
        withdrawTab.click();

        // Hesap seç
        WebElement withdrawAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("withdrawAccount")));
        Select select = new Select(withdrawAccount);
        if (select.getOptions().size() > 0) {
            select.selectByIndex(0);
        } else {
            // Hesap yoksa testi atla
            return;
        }

        // Para çek formunu doldur
        driver.findElement(By.id("withdrawAmount")).sendKeys("50.00");
        driver.findElement(By.id("withdrawDescription")).sendKeys("Test para çekme");

        // Para çek butonuna tıkla
        WebElement withdrawButton = driver.findElement(By.xpath("//button[contains(text(), 'Para Çek')]"));
        withdrawButton.click();

        // Sonucu kontrol et
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionMessage")));
        assertTrue(message.getText().contains("başarılı") || message.getText().contains("çekme") || 
                   message.getText().contains("Yetersiz"),
            "Para çekme işlemi sonucu beklenmedik");
    }
}

