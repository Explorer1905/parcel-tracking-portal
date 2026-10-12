package com.parcel.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class ParcelPortalSeleniumTest {
    private static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:8081/parcel-tracking-portal");
    private static WebDriver driver;
    private static final Path FAILURE_DIR = Path.of("target", "selenium-failures");

    @RegisterExtension
    static final TestWatcher FAILURE_SCREENSHOT = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            if (driver != null) {
                captureFailure(context.getDisplayName());
            }
        }
    };

    @BeforeAll
    static void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @AfterAll
    static void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void registerParcelShowsSuccessMessage() {
        String parcelId = uniqueParcelId();
        open("/parcels");
        fill("parcelId", parcelId);
        fill("sender", "Alice");
        fill("receiver", "Bob");
        fill("origin", "Mumbai");
        fill("destination", "Pune");
        driver.findElement(By.id("addBtn")).click();

        assertTrue(driver.findElement(By.id("successMsg")).getText().contains(parcelId));
    }

    @Test
    void dashboardSearchFindsRegisteredParcel() {
        String parcelId = registerParcel();
        open("/dashboard");
        fill("searchBox", parcelId);
        driver.findElement(By.id("searchBtn")).click();

        assertTrue(driver.findElement(By.id("parcelTable")).getText().contains(parcelId));
    }

    @Test
    void parcelDetailAllowsAddingTrackingEvent() {
        String parcelId = registerParcel();
        open("/parcel?id=" + parcelId);
        driver.findElement(By.id("eventStatus")).sendKeys("In Transit");
        fill("eventLocation", "Nashik");
        driver.findElement(By.id("addEventBtn")).click();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.textToBePresentInElementLocated(
                        By.id("eventTable"), "In Transit"));
        assertTrue(driver.findElement(By.id("eventTable")).getText().contains("Nashik"));
    }

    @Test
    void alertsPageIsReachable() {
        open("/alerts");
        assertTrue(driver.findElement(By.id("alertTable")).isDisplayed());
    }

    private static String registerParcel() {
        String parcelId = uniqueParcelId();
        open("/parcels");
        fill("parcelId", parcelId);
        fill("sender", "Selenium Sender");
        fill("receiver", "Selenium Receiver");
        fill("origin", "Mumbai");
        fill("destination", "Pune");
        driver.findElement(By.id("addBtn")).click();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("added="));
        return parcelId;
    }

    private static void open(String path) {
        driver.get(BASE_URL + path);
    }

    private static void fill(String id, String value) {
        driver.findElement(By.id(id)).clear();
        driver.findElement(By.id(id)).sendKeys(value);
    }

    private static String uniqueParcelId() {
        return "SEL-" + System.currentTimeMillis();
    }

    private static void captureFailure(String testName) {
        try {
            Files.createDirectories(FAILURE_DIR);
            String safeName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path output = FAILURE_DIR.resolve(safeName + ".png");
            Files.write(output, ((org.openqa.selenium.TakesScreenshot) driver)
                    .getScreenshotAs(org.openqa.selenium.OutputType.BYTES));
        } catch (IOException | RuntimeException screenshotError) {
            System.err.println("Could not save Selenium failure screenshot: " + screenshotError.getMessage());
        }
    }
}
