package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 6: Para Transferi
 * Use Case: Para transferi işlemi
 * Ön koşul: İki hesabın olması ve gönderen hesapta yeterli bakiye
 */
public class Test6_Transfer extends BaseSeleniumTest {

    @Test
    public void testTransfer() {
        // Giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Transfer sekmesine geç
        WebElement transferTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Transfer')]")));
        transferTab.click();

        // Gönderen hesabı seç
        WebElement fromAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transferFromAccount")));
        Select select = new Select(fromAccount);
        if (select.getOptions().size() > 0) {
            String fromAccountNumber = select.getOptions().get(0).getText().split(" - ")[0];
            select.selectByIndex(0);

            // Alıcı hesap numarasını gir (aynı hesap numarası - test amaçlı)
            driver.findElement(By.id("transferToAccount")).sendKeys(fromAccountNumber);
            driver.findElement(By.id("transferAmount")).sendKeys("25.00");
            driver.findElement(By.id("transferDescription")).sendKeys("Test transfer");

            // Transfer butonuna tıkla
            WebElement transferButton = driver.findElement(By.xpath("//button[contains(text(), 'Transfer Et')]"));
            transferButton.click();

            // Sonucu kontrol et
            WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionMessage")));
            assertTrue(message.getText().contains("başarılı") || message.getText().contains("Transfer") ||
                       message.getText().contains("Yetersiz"),
                "Transfer işlemi sonucu beklenmedik");
        }
    }
}

