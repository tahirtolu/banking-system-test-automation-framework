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
 * Test Senaryosu 3: Hesap Oluşturma
 *
 * Test Durumu Kimliği: REQ-003
 *
 * İlgili Gereksinimler:
 * - Oturum açmış kullanıcı hesap oluşturabilmelidir
 * - Vadesiz (CHECKING) veya Vadeli (SAVINGS) hesap tipi seçilebilmelidir
 * - Her hesaba benzersiz hesap numarası atanmalıdır
 *
 * Ön Koşullar:
 * - Kullanıcı giriş yapmış olmalıdır (REQ-002)
 * - Dashboard ekranı görüntüleniyor olmalıdır
 *
 * Adım Adım Uygulanacak İşlemler:
 * 1. Dashboard ekranında "Hesap İşlemleri" bölümüne gidilir
 * 2. Hesap tipi seçilir
 * 3. "Hesap Oluştur" butonuna tıklanır
 *
 * Beklenen Sonuç:
 * - Hesap başarıyla oluşturulmalıdır
 * - Hesap numarası görüntülenmelidir
 *
 * Son Koşullar:
 * - Yeni hesap veritabanında kayıtlı olmalıdır
 * - Hesap numarası benzersiz olmalıdır
 */
public class Test3_AccountCreation extends BaseSeleniumTest {

    @Test
    public void testAccountCreation() {
        // Backend'in hazır olmasını bekle
        waitForBackend();

        System.out.println("=== Test3: Hesap Oluşturma Başlıyor ===");

        // Önce kullanıcı kaydı yap
        driver.get(FRONTEND_URL);

        // Kayıt sekmesine geç
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
        registerTab.click();
        System.out.println("✓ Kayıt sekmesine geçildi");

        // Kullanıcı kaydı yap
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

        // Kayıt butonuna tıkla
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='registerForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);

        // Kayıt başarı mesajını bekle
        WebElement registerMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerMessage")));
        wait.until(driver -> !registerMessage.getText().trim().isEmpty());
        System.out.println("✓ Kayıt mesajı: " + registerMessage.getText());

        // Kayıt başarılı ise login sekmesine geç
        try {
            Thread.sleep(2000); // Otomatik geçiş için bekle veya manuel geç
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Login sekmesine geç (eğer otomatik geçmediyse)
        try {
            WebElement loginTab = driver.findElement(By.xpath("//button[contains(text(), 'Giriş Yap')]"));
            if (loginTab.isDisplayed()) {
                loginTab.click();
                System.out.println("✓ Login sekmesine geçildi");
            }
        } catch (Exception e) {
            System.out.println("Login sekmesi zaten aktif");
        }

        // Login yap
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUsername")));
        driver.findElement(By.id("loginUsername")).clear();
        driver.findElement(By.id("loginUsername")).sendKeys(username);
        driver.findElement(By.id("loginPassword")).clear();
        driver.findElement(By.id("loginPassword")).sendKeys(password);

        System.out.println("✓ Login formu dolduruldu");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[@id='loginForm']//button[@type='submit']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);

        // Dashboard'un yüklendiğini bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-section")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        System.out.println("✓ Dashboard yüklendi");

        // Biraz bekle (dashboard içeriğinin yüklenmesi için)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Hesap tipini seç
        WebElement accountTypeSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountType")));
        Select select = new Select(accountTypeSelect);
        select.selectByValue("CHECKING");
        System.out.println("✓ Hesap tipi seçildi: CHECKING");

        // Hesap oluştur butonuna tıkla
        WebElement createAccountButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Hesap Oluştur')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", createAccountButton);
        System.out.println("✓ Hesap oluştur butonuna tıklandı");

        // API isteğinin tamamlanması için bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Başarı mesajını kontrol et
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountMessage")));

        // Mesajın dolmasını bekle
        wait.until(driver -> !message.getText().trim().isEmpty());

        String messageText = message.getText();
        System.out.println("=== SONUÇ ===");
        System.out.println("Hesap oluşturma mesajı: [" + messageText + "]");

        assertTrue(messageText.toLowerCase().contains("başarı") ||
                        messageText.toLowerCase().contains("oluşturuldu") ||
                        messageText.toLowerCase().contains("success"),
                "Hesap oluşturma başarısız. Mesaj: [" + messageText + "]");
    }


}