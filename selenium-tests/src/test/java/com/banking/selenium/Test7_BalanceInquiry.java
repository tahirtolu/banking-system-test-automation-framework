package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 7: Bakiye Sorgulama
 * Use Case: Bakiye sorgulama işlemi
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

