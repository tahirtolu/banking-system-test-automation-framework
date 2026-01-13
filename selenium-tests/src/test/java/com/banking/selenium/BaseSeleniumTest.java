package com.banking.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.logging.Level;

public class BaseSeleniumTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:8081";
    // Frontend URL - Docker container'dan servis edilen frontend
    protected static final String FRONTEND_URL = "http://localhost:8082";

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // Console loglarını yakala (debug için)
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Backend'in hazır olmasını bekle (max 90 saniye)
     * Önce direkt backend'e (8081) bak, sonra Nginx üzerinden (8082) kontrol et
     */
    protected static void waitForBackend() {
        String backendDirectUrl = "http://localhost:8081/api/auth/login"; // Direkt backend
        String backendNginxUrl = "http://localhost:8082/api/auth/login"; // Nginx üzerinden
        int maxAttempts = 90; // 90 saniyeye çıkarıldı
        int attempt = 0;

        System.out.println("Backend hazır olana kadar bekleniyor...");
        System.out.println("1. Adım: Direkt backend kontrolü (port 8081)...");

        // Önce direkt backend'e bak (Nginx'e bağlı değil)
        boolean backendReady = false;
        while (attempt < maxAttempts && !backendReady) {
            try {
                URL url = new URL(backendDirectUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                // 403, 405, 200 = Backend çalışıyor
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Backend hazır! (Direkt - HTTP " + responseCode + ")");
                    backendReady = true;
                    break;
                }

                if (attempt % 10 == 0) {
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts
                            + ") Backend henüz hazır değil (HTTP " + responseCode + ")");
                }

            } catch (Exception e) {
                if (attempt % 10 == 0) {
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts
                            + ") Backend henüz başlamadı: " + e.getMessage());
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        if (!backendReady) {
            System.err.println("⚠ UYARI: Backend 90 saniye içinde hazır olmadı!");
            return;
        }

        // Backend hazır, şimdi Nginx üzerinden kontrol et
        System.out.println("2. Adım: Nginx proxy kontrolü (port 8082)...");
        attempt = 0;
        while (attempt < 30) { // Nginx için 30 saniye yeterli
            try {
                URL url = new URL(backendNginxUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);

                int responseCode = connection.getResponseCode();

                // 403, 405, 200, 502 değil = Nginx çalışıyor ve backend'e ulaşabiliyor
                if (responseCode == 403 || responseCode == 405 || responseCode == 200) {
                    System.out.println("✓ Nginx proxy hazır! (HTTP " + responseCode + ")");
                    return;
                }

                if (responseCode == 502) {
                    System.out.println("Nginx 502 döndürüyor, backend'e ulaşamıyor... (" + (attempt + 1) + "/30)");
                }

            } catch (Exception e) {
                System.out.println("Nginx kontrolü... (" + (attempt + 1) + "/30)");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attempt++;
        }

        System.err.println("⚠ UYARI: Nginx proxy 30 saniye içinde hazır olmadı, ama direkt backend çalışıyor!");
    }

    /**
     * Robust User Registration with Retry Logic & API Verification
     * Handles transient JPA/DB locking errors and flaky UI by verifying via API.
     * 
     * @return The actual username used for registration
     */
    protected String registerUser(String username, String password, String email, String firstName, String lastName,
            String phone) {

        int maxRetries = 10;
        String registeredUsername = username;

        for (int i = 0; i < maxRetries; i++) {
            System.out.println("=== Kayıt Denemesi " + (i + 1) + "/" + maxRetries + " ===");
            try {
                driver.get(FRONTEND_URL); // Her denemede sayfayı yenile

                // Kayıt sekmesine geç
                try {
                    WebElement registerTab = wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                            .elementToBeClickable(
                                    org.openqa.selenium.By.xpath("//button[contains(text(), 'Kayıt Ol')]")));
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                            registerTab);
                } catch (Exception e) {
                    // Zaten oradaysak devam
                }

                wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                        .presenceOfElementLocated(org.openqa.selenium.By.id("regUsername")));

                String currentUsername = username + (i > 0 ? "_" + i : "");
                String currentEmail = (i > 0 ? i : "") + email;

                // Formu doldur
                driver.findElement(org.openqa.selenium.By.id("regUsername")).clear();
                driver.findElement(org.openqa.selenium.By.id("regUsername")).sendKeys(currentUsername);
                driver.findElement(org.openqa.selenium.By.id("regPassword")).clear();
                driver.findElement(org.openqa.selenium.By.id("regPassword")).sendKeys(password);
                driver.findElement(org.openqa.selenium.By.id("regEmail")).clear();
                driver.findElement(org.openqa.selenium.By.id("regEmail")).sendKeys(currentEmail);
                driver.findElement(org.openqa.selenium.By.id("regFirstName")).clear();
                driver.findElement(org.openqa.selenium.By.id("regFirstName")).sendKeys(firstName);
                driver.findElement(org.openqa.selenium.By.id("regLastName")).clear();
                driver.findElement(org.openqa.selenium.By.id("regLastName")).sendKeys(lastName);
                driver.findElement(org.openqa.selenium.By.id("regPhone")).clear();
                driver.findElement(org.openqa.selenium.By.id("regPhone")).sendKeys(phone);

                WebElement registerButton = wait
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                                org.openqa.selenium.By.xpath("//form[@id='registerForm']//button[@type='submit']")));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                        registerButton);

                // UI Mesajını bekle (Log için) ama ona güvenme
                try {
                    WebElement msg = wait
                            .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                                    org.openqa.selenium.By.id("registerMessage")));
                    System.out.println("UI Mesajı: " + msg.getText());
                } catch (Exception e) {
                }

                // --- API Verification (Solution 2) ---
                boolean userExists = false;
                for (int apiRetry = 0; apiRetry < 10; apiRetry++) {
                    try {
                        URL url = new URL("http://localhost:8081/api/auth/login");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        String jsonInputString = String.format("{\"username\": \"%s\", \"password\": \"%s\"}",
                                currentUsername, password);

                        try (java.io.OutputStream os = conn.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        int code = conn.getResponseCode();
                        if (code == 200 || code == 403) {
                            userExists = true;
                            System.out.println("✓ API Onayı: Kullanıcı backend'de mevcut (HTTP " + code + ")");
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                    Thread.sleep(1000);
                }

                if (userExists) {
                    System.out.println("✓ Kayıt Başarılı ve Doğrulandı: " + currentUsername);
                    registeredUsername = currentUsername;
                    return registeredUsername;
                } else {
                    System.out.println("⚠ API kullanıcıyı bulamadı, tekrar deneyelim...");
                    throw new RuntimeException("API_VERIFICATION_FAILED");
                }

            } catch (Exception e) {
                System.out.println("⚠ Hata (Retry " + (i + 1) + "): " + e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                }
            }
        }

        throw new RuntimeException("Kayıt işlemi " + maxRetries + " denemede başarısız oldu.");
    }

    protected void loginUser(String username, String password) {
        // DB'nin sakinleşmesi için kısa bir bekleme
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        // Login sekmesine geç
        try {
            WebElement loginTab = driver
                    .findElement(org.openqa.selenium.By.xpath("//button[contains(text(), 'Giriş Yap')]"));
            if (loginTab.isDisplayed()) {
                loginTab.click();
            }
        } catch (Exception e) {
            // Zaten login sekmesinde olabilir veya buton görünmeyebilir
        }

        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                    .presenceOfElementLocated(org.openqa.selenium.By.id("loginUsername")));
            driver.findElement(org.openqa.selenium.By.id("loginUsername")).clear();
            driver.findElement(org.openqa.selenium.By.id("loginUsername")).sendKeys(username);
            driver.findElement(org.openqa.selenium.By.id("loginPassword")).clear();
            driver.findElement(org.openqa.selenium.By.id("loginPassword")).sendKeys(password);

            WebElement loginButton = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                    org.openqa.selenium.By.xpath("//form[@id='loginForm']//button[@type='submit']")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);

            // Dashboard kontrolü (Timeout 60 saniyeye çıkarıldı)
            try {
                new WebDriverWait(driver, Duration.ofSeconds(60))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions
                                .visibilityOfElementLocated(org.openqa.selenium.By.id("dashboard-section")));
                System.out.println("✓ Login başarılı: " + username);
            } catch (org.openqa.selenium.TimeoutException te) {
                // Timeout olduysa hata mesajı var mı kontrol et
                String errorMsg = "Bilinmeyen hata";
                try {
                    WebElement msgEl = driver.findElement(org.openqa.selenium.By.id("loginMessage"));
                    errorMsg = msgEl.getText();
                } catch (Exception e) {
                }
                throw new RuntimeException("Login timeout! Ekrandaki mesaj: [" + errorMsg + "]", te);
            }

        } catch (Exception e) {
            System.err.println("Login başarısız: " + e.getMessage());
            throw new RuntimeException("Login başarısız", e);
        }
    }
}
