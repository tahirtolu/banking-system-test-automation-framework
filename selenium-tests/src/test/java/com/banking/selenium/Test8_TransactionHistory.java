package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test8_TransactionHistory extends BaseSeleniumTest {

    @Test
    public void testTransactionHistory() {
        waitForBackend();
        System.out.println("=== Test8: İşlem Geçmişi Başlıyor ===");

        driver.get(FRONTEND_URL);

        // Kayıt + Login + Hesap + Para Yatır
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));

        long timestamp = System.currentTimeMillis();
        String username = "test" + (timestamp % 100000);
        String password = "password123";
        String email = "test" + timestamp + "@test.com";

        driver.findElement(By.id("regUsername")).sendKeys(username);
        driver.findElement(By.id("regPassword")).sendKeys(password);
        driver.findElement(By.id("regEmail")).sendKeys(email);
        driver.findElement(By.id("regFirstName")).sendKeys("Test");
        driver.findElement(By.id("regLastName")).sendKeys("User");
        driver.findElement(By.id("regPhone")).sendKeys("5551234567");

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Login
        try {
            WebElement loginTab = driver.findElement(By.xpath("//button[contains(text(), 'Giriş Yap')]"));
            if (loginTab.isDisplayed()) loginTab.click();
        } catch (Exception e) { }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).clear();
        driver.findElement(By.id("loginUsername")).sendKeys(username);
        driver.findElement(By.id("loginPassword")).clear();
        driver.findElement(By.id("loginPassword")).sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='loginForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        // Hesap oluştur
        WebElement accountTypeSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountType")));
        Select accountSelect = new Select(accountTypeSelect);
        accountSelect.selectByValue("CHECKING");
        WebElement createAccountButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Hesap Oluştur')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", createAccountButton);
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Para yatır (işlem geçmişi için)
        WebElement depositTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Para Yatır')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositTab);
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        WebElement depositAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("depositAccount")));
        Select depositSelect = new Select(depositAccount);
        if (depositSelect.getOptions().size() > 0) depositSelect.selectByIndex(0);

        driver.findElement(By.id("depositAmount")).clear();
        driver.findElement(By.id("depositAmount")).sendKeys("100.00");
        driver.findElement(By.id("depositDescription")).clear();
        driver.findElement(By.id("depositDescription")).sendKeys("Test işlem");

        WebElement depositButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='deposit-tab']//button[contains(text(), 'Para Yatır')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositButton);
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // İşlem geçmişi
        WebElement historyAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("historyAccount")));
        Select historySelect = new Select(historyAccount);

        if (historySelect.getOptions().size() > 0) {
            historySelect.selectByIndex(0);

            WebElement loadHistoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Geçmişi Yükle')]")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loadHistoryButton);
            try { Thread.sleep(2000); } catch (InterruptedException e) { }

            WebElement transactionHistory = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionHistory")));
            assertTrue(transactionHistory.isDisplayed(), "İşlem geçmişi görüntülenmiyor");

            System.out.println("=== SONUÇ ===");
            System.out.println("İşlem geçmişi yüklendi");
        }
    }

    private void waitForBackend() {
        String backendHealthUrl = "http://localhost:8082/api/auth/login";
        int maxAttempts = 60;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                URL url = new URL(backendHealthUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                int responseCode = connection.getResponseCode();
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) return;
            } catch (Exception e) { }
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            attempt++;
        }
    }
}