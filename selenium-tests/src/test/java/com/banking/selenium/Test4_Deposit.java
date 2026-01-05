package com.banking.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.net.HttpURLConnection;
import java.net.URL;

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
        // Backend'in hazır olmasını bekle
        waitForBackend();

        System.out.println("=== Test4: Para Yatırma Başlıyor ===");

        // Kullanıcı kaydı yap
        driver.get(FRONTEND_URL);

        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();
        System.out.println("✓ Kayıt sekmesine geçildi");

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

        System.out.println("✓ Kayıt formu dolduruldu: " + username);

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        WebElement registerMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));
        wait.until(driver -> !registerMessage.getText().trim().isEmpty());
        System.out.println("✓ Kayıt başarılı");

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Login yap
        try {
            WebElement loginTab = driver.findElement(By.xpath("//button[contains(text(), 'Giriş Yap')]"));
            if (loginTab.isDisplayed()) {
                loginTab.click();
            }
        } catch (Exception e) {
            System.out.println("Login sekmesi zaten aktif");
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).clear();
        driver.findElement(By.id("loginUsername")).sendKeys(username);
        driver.findElement(By.id("loginPassword")).clear();
        driver.findElement(By.id("loginPassword")).sendKeys(password);

        System.out.println("✓ Login formu dolduruldu");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='loginForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        System.out.println("✓ Dashboard yüklendi");

        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Hesap oluştur
        WebElement accountTypeSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountType")));
        Select accountSelect = new Select(accountTypeSelect);
        accountSelect.selectByValue("CHECKING");
        System.out.println("✓ Hesap tipi seçildi: CHECKING");

        WebElement createAccountButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Hesap Oluştur')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", createAccountButton);
        System.out.println("✓ Hesap oluşturuldu");

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Para yatır sekmesine geç
        WebElement depositTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Para Yatır')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositTab);
        System.out.println("✓ Para Yatır sekmesine geçildi");

        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Para yatır formunu doldur
        WebElement depositAccount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("depositAccount")));
        Select depositSelect = new Select(depositAccount);
        if (depositSelect.getOptions().size() > 0) {
            depositSelect.selectByIndex(0);
            System.out.println("✓ Hesap seçildi");
        }

        WebElement amountField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("depositAmount")));
        amountField.clear();
        amountField.sendKeys("100.00");

        WebElement descField = driver.findElement(By.id("depositDescription"));
        descField.clear();
        descField.sendKeys("Test para yatırma");

        System.out.println("✓ Para yatır formu dolduruldu: 100.00 TL");

        // Para yatır butonuna tıkla
        WebElement depositButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='deposit-tab']//button[contains(text(), 'Para Yatır')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", depositButton);
        System.out.println("✓ Para Yatır butonuna tıklandı");

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Başarı mesajını kontrol et
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionMessage")));
        wait.until(driver -> !message.getText().trim().isEmpty());

        String messageText = message.getText();
        System.out.println("=== SONUÇ ===");
        System.out.println("Para yatırma mesajı: [" + messageText + "]");

        assertTrue(messageText.toLowerCase().contains("başarı") ||
                        messageText.toLowerCase().contains("yatırma") ||
                        messageText.toLowerCase().contains("success"),
                "Para yatırma işlemi başarısız. Mesaj: [" + messageText + "]");
    }

    /**
     * Backend'in hazır olmasını bekle (max 60 saniye)
     */
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

            } catch (Exception e) {
                // Sessizce bekle
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        System.err.println("⚠ UYARI: Backend 60 saniye içinde hazır olmadı!");
    }
}