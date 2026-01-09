package com.banking.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        String backendDirectUrl = "http://localhost:8081/api/auth/login";  // Direkt backend
        String backendNginxUrl = "http://localhost:8082/api/auth/login";   // Nginx üzerinden
        int maxAttempts = 90;  // 90 saniyeye çıkarıldı
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
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") Backend henüz hazır değil (HTTP " + responseCode + ")");
                }

            } catch (Exception e) {
                if (attempt % 10 == 0) {
                    System.out.println("Bekleniyor... (" + (attempt + 1) + "/" + maxAttempts + ") Backend henüz başlamadı: " + e.getMessage());
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
        while (attempt < 30) {  // Nginx için 30 saniye yeterli
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
}

