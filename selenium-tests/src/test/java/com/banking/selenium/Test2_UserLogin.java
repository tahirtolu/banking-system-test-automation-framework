package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Senaryosu 2: Kullanıcı Girişi
 * Use Case: Oturum açma işlemi
 * Ön koşul: Kullanıcı kaydının tamamlanması
 */
public class Test2_UserLogin extends BaseSeleniumTest {

    @Test
    public void testUserLogin() {
        driver.get(FRONTEND_URL);

        // Giriş formunu doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).sendKeys("testuser");
        driver.findElement(By.id("loginPassword")).sendKeys("password123");

        // Giriş butonuna tıkla
        WebElement loginButton = driver.findElement(By.xpath("//form[@id='loginForm']//button[@type='submit']"));
        loginButton.click();

        // Dashboard'un göründüğünü kontrol et
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
        assertTrue(dashboard.isDisplayed(), "Giriş başarısız - Dashboard görünmüyor");
    }
}

