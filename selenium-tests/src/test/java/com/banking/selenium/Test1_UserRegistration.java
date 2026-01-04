package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test1_UserRegistration extends BaseSeleniumTest {

    @Test
    public void testUserRegistration() {
        waitForBackend();

        System.out.println("=== Sayfa açılıyor: " + FRONTEND_URL + " ===");
        driver.get(FRONTEND_URL);

        // Kayıt sekmesine geç
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();
        System.out.println("✓ Kayıt sekmesine geçildi");

        // Form alanlarını doldur
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));

        // DÜZELTME: Username'i kısalt (max 20 karakter)
        long timestamp = System.currentTimeMillis();
        String username = "test" + (timestamp % 100000); // test12345 formatında (max 10 karakter)
        String email = "test" + timestamp + "@test.com";

        driver.findElement(By.id("regUsername")).sendKeys(username);
        driver.findElement(By.id("regPassword")).sendKeys("password123");
        driver.findElement(By.id("regEmail")).sendKeys(email);
        driver.findElement(By.id("regFirstName")).sendKeys("Selenium");
        driver.findElement(By.id("regLastName")).sendKeys("Test");
        driver.findElement(By.id("regPhone")).sendKeys("5551234567");

        System.out.println("✓ Form dolduruldu: " + username + " / " + email);

        // Kayıt butonuna tıkla
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='registerForm']//button[@type='submit']")));

        System.out.println("✓ Kayıt butonuna tıklanıyor...");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        // API isteğinin tamamlanması için bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Başarı mesajı kontrolü
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));

        wait.until(driver -> {
            String text = message.getText().trim();
            return !text.isEmpty();
        });

        String messageText = message.getText().trim();
        System.out.println("\n=== SONUÇ ===");
        System.out.println("Register message: [" + messageText + "]");

        String lowerMessage = messageText.toLowerCase();
        assertTrue(lowerMessage.contains("başarılı") || lowerMessage.contains("success"),
                "Kayıt işlemi başarısız oldu. Mesaj: [" + messageText + "]");
    }

    private void waitForBackend() {
        String backendHealthUrl = "http://localhost:8082/api/auth/login";
        int maxAttempts = 60;
        int attempt = 0;

        System.out.println("Backend hazır olana kadar bekleniyor...");

        while (attempt < maxAttempts) {
            try {
                URL url = new URL(backendHealthUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Backend hazır! (HTTP " + responseCode + ")");
                    return;
                }

                System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") - HTTP " + responseCode);

            } catch (Exception e) {
                System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") - " + e.getMessage());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        System.err.println("⚠ UYARI: Backend 60 saniye içinde hazır olmadı, teste devam ediliyor...");
    }
}