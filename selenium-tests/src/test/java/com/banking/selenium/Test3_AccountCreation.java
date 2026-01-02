package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 3: Hesap Oluşturma
 * Use Case: Hesap açma işlemi
 * Ön koşul: Oturum açma işleminin tamamlanması
 */
public class Test3_AccountCreation extends BaseSeleniumTest {

    @Test
    public void testAccountCreation() {
        // Önce giriş yap
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");
        driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']")).click();

        // Dashboard'un yüklendiğini bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));

        // Hesap tipini seç
        WebElement accountTypeSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountType")));
        Select select = new Select(accountTypeSelect);
        select.selectByValue("CHECKING");

        // Hesap oluştur butonuna tıkla
        WebElement createAccountButton = driver.findElement(By.xpath("//button[contains(text(), 'Hesap Oluştur')]"));
        createAccountButton.click();

        // Başarı mesajını kontrol et
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountMessage")));
        assertTrue(message.getText().contains("başarıyla") || message.getText().contains("oluşturuldu"),
            "Hesap oluşturma başarısız");
    }
}

