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
                // 1. & 2. ADIM: KAYIT OLMA ve GİRİŞ (Robust Helpers)
                String password = "password123";
                long timestamp = System.currentTimeMillis();
                String username = "test" + (timestamp % 100000);
                String email = "test" + timestamp + "@test.com";

                // Helper metodumuz retry ve unique username yönetimini kendi içinde yapıyor
                String registeredUser = registerUser(username, password, email, "Test", "User", "5551234567");
                loginUser(registeredUser, password);
                // Dashboard yüklenene kadar bekle (30 saniye timeout)
                org.openqa.selenium.support.ui.WebDriverWait dashboardWait = new org.openqa.selenium.support.ui.WebDriverWait(
                                driver, java.time.Duration.ofSeconds(30));
                dashboardWait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
                dashboardWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
                try {
                        Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                // Hesap oluştur
                WebElement accountTypeSelect = wait
                                .until(ExpectedConditions.presenceOfElementLocated(By.id("accountType")));
                Select accountSelect = new Select(accountTypeSelect);
                accountSelect.selectByValue("CHECKING");
                WebElement createAccountButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(), 'Hesap Oluştur')]")));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                createAccountButton);
                try {
                        Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                // Para yatır (işlem geçmişi için)
                WebElement depositTab = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(), 'Para Yatır')]")));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositTab);
                try {
                        Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                WebElement depositAccount = wait
                                .until(ExpectedConditions.presenceOfElementLocated(By.id("depositAccount")));
                Select depositSelect = new Select(depositAccount);
                if (depositSelect.getOptions().size() > 0)
                        depositSelect.selectByIndex(0);

                driver.findElement(By.id("depositAmount")).clear();
                driver.findElement(By.id("depositAmount")).sendKeys("100.00");
                driver.findElement(By.id("depositDescription")).clear();
                driver.findElement(By.id("depositDescription")).sendKeys("Test işlem");

                WebElement depositButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[@id='deposit-tab']//button[contains(text(), 'Para Yatır')]")));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositButton);
                try {
                        Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                // İşlem geçmişi
                WebElement historyAccount = wait
                                .until(ExpectedConditions.presenceOfElementLocated(By.id("historyAccount")));
                Select historySelect = new Select(historyAccount);

                if (historySelect.getOptions().size() > 0) {
                        historySelect.selectByIndex(0);

                        WebElement loadHistoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                                        By.xpath("//button[contains(text(), 'Geçmişi Yükle')]")));
                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        loadHistoryButton);
                        try {
                                Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }

                        WebElement transactionHistory = wait.until(
                                        ExpectedConditions.presenceOfElementLocated(By.id("transactionHistory")));
                        assertTrue(transactionHistory.isDisplayed(), "İşlem geçmişi görüntülenmiyor");

                        System.out.println("=== SONUÇ ===");
                        System.out.println("İşlem geçmişi yüklendi");
                }
        }

}