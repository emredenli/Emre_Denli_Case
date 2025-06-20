package com.insider.driver;

import com.thoughtworks.gauge.ExecutionContext;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.insider.imp.ConfigFileReader;
import com.insider.imp.LoggerImps;
import com.insider.steps.BaseSteps;
import lombok.extern.log4j.Log4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j
public class DriverManager {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static DriverManager driverManager;
    private final ConfigFileReader configFileReader;
    private final LoggerImps loggerImps;
    private final boolean IS_MAC = (OS.contains("mac"));
    private final boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    private final String ORANGE = "orange";
    public boolean IS_WINDOWS = (OS.contains("win"));
    protected WebDriver webDriver;
    protected WebDriverWait webWait;
    private boolean IS_CHROME = false;
    private boolean IS_EDGE = false;
    private boolean IS_FIREFOX = false;
    private boolean IS_PROD = true;
    private boolean IS_HEADLESS = false;
    private boolean IS_FULLSCREEN = false;
    private boolean IS_TEST = false;
    private List<String> tags;

    private DriverManager() {
        configFileReader = ConfigFileReader.getInstance();
        loggerImps = LoggerImps.getInstance(log);
    }

    public static DriverManager getInstance() {
        if (driverManager == null) {
            driverManager = new DriverManager();
        }
        return driverManager;
    }

    private static String getScenarioStatusColor(ExecutionContext context) {
        if (context.getCurrentScenario().getIsFailing()) return "red";
        return "green";
    }

    public WebDriver getDriver() {
        return webDriver;
    }

    public WebDriverWait getWebWait() {
        return webWait;
    }

    public JavascriptExecutor getJSExecutor() {
        return (JavascriptExecutor) webDriver;
    }

    protected void initializeDriver(ExecutionContext context) {
            setDriverEnvironment(context);
            setLocalDriver();
            beforeScenarioLog(context);
    }

    private void setLocalDriver() {
        try {
            loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);
            if (IS_CHROME) setChromeDriver();
            else if (IS_FIREFOX) setFirefoxDriver();
            else if (IS_EDGE) setEdgeDriver();
            setWebWaitAndDriver();
        } catch (Exception e) {
            loggerImps.errorAndFail("BROWSER | UI testi başlatılamadı." + e.getMessage(), "red");
        }
        loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);

    }

    private void setWebWaitAndDriver() {
        int waitSeconds = configFileReader.getInteger("WaitSeconds");
        int waitMilis = configFileReader.getInteger("WaitMilliseconds");
        int webWaitSeconds = configFileReader.getInteger("WebWaitSeconds");
        String testUrl = configFileReader.getString("TestUrl");
        String prodUrl = configFileReader.getString("ProdUrl");
        int scriptLoadTime = configFileReader.getInteger("ScriptLoadMinutes");
        int pageLoadTime = configFileReader.getInteger("PageLoadMinutes");

        webDriver.manage().timeouts().scriptTimeout(Duration.ofMinutes(scriptLoadTime));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(pageLoadTime));

        webWait = new WebDriverWait(webDriver, Duration.ofSeconds(webWaitSeconds));
        webWait.withTimeout(Duration.ofSeconds(waitSeconds))
                .pollingEvery(Duration.ofMillis(waitMilis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        webDriver.manage().window().maximize();
        if (IS_TEST) webDriver.get(testUrl);
        else if (IS_PROD) webDriver.get(prodUrl);
    }

    public void beforeScenarioLog(ExecutionContext context) {
        loggerImps.info("╔═══════════════════════════════════════════════════════════════════════╗");
        loggerImps.info("║═══════════════════ Browser | UI testi başlatıldı ═════════════════════╣");
        loggerImps.info("╚═══════════════════════════════════════════════════════════════════════╝");
        loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);
        loggerImps.info("Specification tags => " + tags, ORANGE);
        String scenarioName = context.getCurrentScenario().getName();
        loggerImps.info("Scenario name => " + scenarioName, ORANGE);
        loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);
    }

    public List<String> getTags(ExecutionContext context) {
        return Stream.concat(
                        context.getCurrentScenario().getTags().stream(),
                        context.getCurrentSpecification().getTags().stream())
                .collect(Collectors.toList())
                .stream().map(String::toLowerCase).collect(Collectors.toList()
                );
    }

    public void setDriverEnvironment(ExecutionContext context) {
        tags = getTags(context);
        setDriverType();
        setDriverLink();
        setDriverWindowType();
    }

    public void setDriverType() {
        if (tags.contains("firefox")) IS_FIREFOX = true;
        else if (tags.contains("edge")) IS_EDGE = true;
        else IS_CHROME = true;
    }

    public void setDriverLink() {
        if (tags.contains("prod")) IS_PROD = true;
        else if (tags.contains("test")) IS_TEST = true;
        else {
            String text = "Desteklenen bir test ortam linki girilmemiştir. Lütfen senaryo taglerinizi kontrol ediniz.";
            loggerImps.errorAndFail(text, "red");
        }
    }

    public void setDriverWindowType() {
        if (tags.contains("fullscreen")) IS_FULLSCREEN = true;
        if (tags.contains("headless")) IS_HEADLESS = true;
    }

    private void setChromeDriver() {
        loggerImps.info("-> Driver chrome olarak seçilmiştir.İşletim sistemine göre ayarlamalar yapılıyor...", ORANGE);
        if (IS_WINDOWS) WebDriverManager.chromedriver().win().setup();
        else if (IS_MAC) WebDriverManager.chromedriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.chromedriver().linux().setup();
        webDriver = new ChromeDriver(getChromeOptions());
    }

    public ChromeOptions getChromeOptions() {
        loggerImps.info("Chrome driver ayarlamaları yapılıyor.", ORANGE);
        ChromeOptions options = new ChromeOptions();
        if (IS_FULLSCREEN) options.addArguments("--start-fullscreen");
        if (IS_HEADLESS) options.addArguments("--headless");
        options.addArguments("--ignore-ssl-errors=yes");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-translate");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        return options;
    }

    private void setFirefoxDriver() {
        loggerImps.info("-> Driver firefox olarak seçilmiştir.İşletim sistemine göre ayarlamalar yapılıyor...", ORANGE);
        if (IS_WINDOWS) WebDriverManager.firefoxdriver().win().setup();
        else if (IS_MAC) WebDriverManager.firefoxdriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.firefoxdriver().linux().setup();
        webDriver = new FirefoxDriver(getFirefoxOptions());
    }

    public FirefoxOptions getFirefoxOptions() {
        loggerImps.info("Firefox driver ayarlamaları yapılıyor.", ORANGE);
        FirefoxProfile profile = new FirefoxProfile();
        FirefoxOptions options = new FirefoxOptions();
        profile.setPreference("browser.download.folderList", 1);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.manager.focusWhenStarting", false);
        profile.setPreference("browser.download.useDownloadDir", true);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        profile.setPreference("browser.download.manager.closeWhenDone", true);
        profile.setPreference("browser.download.manager.showAlertOnComplete", false);
        profile.setPreference("browser.download.manager.useWindow", false);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
        options.setProfile(profile);
        return options;
    }

    private void setEdgeDriver() {
        loggerImps.info("-> Driver edge olarak seçilmiştir.İşletim sistemine göre ayarlamalar yapılıyor...", ORANGE);
        if (IS_WINDOWS) WebDriverManager.edgedriver().win().setup();
        else if (IS_MAC) WebDriverManager.edgedriver().mac().setup();
        else if (IS_UNIX) WebDriverManager.edgedriver().linux().setup();
        webDriver = new EdgeDriver(getEdgeOptions());
    }

    public EdgeOptions getEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        loggerImps.info("Edge driver ayarlamaları yapılıyor.", ORANGE);
        options.setCapability("acceptSslCerts", true);
        options.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "eager");
        return options;
    }

    public void afterScenario(ExecutionContext context) {
        closeDriver();
        deleteExeOut();
        afterScenarioLog(getScenarioStatusColor(context));
        waitAfterScenario();
    }

    public void closeDriver() {
        if (webDriver != null) webDriver.quit();
    }

    public void deleteExeOut() {
        File dir = new File("logs/");
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".out"));
        if (files != null) {
            for (File logs : files) {
                logs.deleteOnExit();
            }
        }
    }

    private void afterScenarioLog(String color) {
        loggerImps.info("╔═══════════════════════════════════════════════════════════════════════╗", color);
        loggerImps.info("║═══════════════════ Browser | UI testi kapatıldı. ═════════════════════║", color);
        if (color.equals("red")) {
            loggerImps.info("║══════════════════════════ TEST BAŞARISIZ ═════════════════════════════║", color);
        } else {
            loggerImps.info("║═══════════════════════════ TEST BAŞARILI ═════════════════════════════║", color);
        }
        loggerImps.info("╚═══════════════════════════════════════════════════════════════════════╝", color);
    }

    private void waitAfterScenario() {
        if (configFileReader.getBoolean("WaitAfter")) {
            BaseSteps.getInstance().waitSecondsWithoutLog(configFileReader.getLong("WaitAfterSeconds"));
        }
    }

    public void beforeEachStep(ExecutionContext context) {
        loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);
        loggerImps.info("Step Name => " + context.getCurrentStep().getText(), "orange");
        loggerImps.info("════════════════════════════════════════════════════════════════════════", ORANGE);

    }
}