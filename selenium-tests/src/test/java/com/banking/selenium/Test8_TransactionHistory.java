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
                // 1. ADIM: KAYIT OLMA (Retry Mekanizmalı)
                WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
                registerTab.click();

                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));

                // Retry mekanizması değişkenleri
                int maxRetries = 3;
                boolean registrationSuccess = false;
                String username = "";
                String password = "password123";

                for (int i = 0; i < maxRetries; i++) {
                        try {
                                long timestamp = System.currentTimeMillis();
                                username = "test" + (timestamp % 100000) + "_" + i; // Unique username for attempt
                                String email = "test" + timestamp + "_" + i + "@test.com";

                                System.out.println("Kayıt Denemesi " + (i + 1) + "/" + maxRetries + " - Kullanıcı: "
                                                + username);

                                // Formu temizle ve doldur
                                driver.findElement(By.id("regUsername")).clear();
                                driver.findElement(By.id("regUsername")).sendKeys(username);

                                driver.findElement(By.id("regPassword")).clear();
                                driver.findElement(By.id("regPassword")).sendKeys(password);

                                driver.findElement(By.id("regEmail")).clear();
                                driver.findElement(By.id("regEmail")).sendKeys(email);

                                driver.findElement(By.id("regFirstName")).clear();
                                driver.findElement(By.id("regFirstName")).sendKeys("Test");

                                driver.findElement(By.id("regLastName")).clear();
                                driver.findElement(By.id("regLastName")).sendKeys("User");

                                driver.findElement(By.id("regPhone")).clear();
                                driver.findElement(By.id("regPhone")).sendKeys("5551234567");

                                WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                                                By.xpath("//form[@id='registerForm']//button[@type='submit']")));
                                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                                registerButton);

                                // Bekleme ve Kontrol
                                WebElement registerMessage = wait.until(
                                                ExpectedConditions.presenceOfElementLocated(By.id("registerMessage")));
                                int attempts = 0;
                                while (attempts < 60) { // 30 saniye
                                        String msg = registerMessage.getText().trim();
                                        String msgLower = msg.toLowerCase();

                                        if (!msgLower.contains("yapılıyor") && !msg.isEmpty()) {
                                                // Hata kontrolü
                                                if (msgLower.contains("hata") || msgLower.contains("error") ||
                                                                msgLower.contains("fail")
                                                                || msgLower.contains("could not") ||
                                                                msgLower.contains("exception")) {

                                                        // Egier JPA/DB hatası ise loop devam etsin (retry)
                                                        if (msgLower.contains("jpa")
                                                                        || msgLower.contains("entitymanager")
                                                                        || msgLower.contains("transaction")) {
                                                                System.err.println(
                                                                                "⚠ DB Hatası algılandı, tekrar deneniyor... ("
                                                                                                + msg + ")");
                                                                throw new RuntimeException("DB_RETRY");
                                                        }
                                                        throw new RuntimeException(
                                                                        "❌ Kayıt başarısız (Retry edilmeyecek hata): ["
                                                                                        + msg + "]");
                                                }

                                                System.out.println("✓ Kayıt işlemi tamamlandı: [" + msg + "]");
                                                registrationSuccess = true;
                                                break;
                                        }
                                        try {
                                                Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                        }
                                        attempts++;
                                }

                                if (registrationSuccess)
                                        break;

                        } catch (RuntimeException e) {
                                if ("DB_RETRY".equals(e.getMessage())) {
                                        // Sadece DB hatasında bekle ve döngüye devam et
                                        try {
                                                Thread.sleep(5000);
                                        } catch (InterruptedException ie) {
                                        }
                                        continue;
                                }
                                // Diğer hatalarda (veya son denemeyse) fırlat
                                if (i == maxRetries - 1)
                                        throw e;
                        } catch (Exception e) {
                                if (i == maxRetries - 1)
                                        throw new RuntimeException("Kayıt işlemi 3 denemede de başarısız oldu!", e);
                        }
                }

                if (!registrationSuccess) {
                        throw new RuntimeException("Kayıt işlemi tamamlanamadı (Success flag false kaldı).");
                }

                // 2. ADIM: GİRİŞ YAPMA
                try {
                        WebElement loginTab = driver.findElement(By.xpath("//button[contains(text(), 'Giriş Yap')]"));
                        if (loginTab.isDisplayed())
                                loginTab.click();
                } catch (Exception e) {
                }

                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
                driver.findElement(By.id("loginUsername")).clear();
                driver.findElement(By.id("loginUsername")).sendKeys(username);
                driver.findElement(By.id("loginPassword")).clear();
                driver.findElement(By.id("loginPassword")).sendKeys(password);

                WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//form[@id='loginForm']//button[@type='submit']")));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
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