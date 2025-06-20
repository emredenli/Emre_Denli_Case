package com.insider.steps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.insider.driver.DriverManager;
import com.insider.imp.ConfigFileReader;
import com.insider.imp.LoggerImps;
import com.insider.info.ElementInfo;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j
public class BaseSteps {

    private static BaseSteps baseSteps;
    private final ConfigFileReader config = ConfigFileReader.getInstance();
    private final LoggerImps loggerImps = LoggerImps.getInstance(log);
    public static JavascriptExecutor jsExecutor;
    protected static WebDriver webDriver;
    protected static WebDriverWait webWait;
    public Map<String, Object> elementsMap;
    public Map<String, String> dataStorage;
    private final int waitMillis = config.getInteger("WaitMilliseconds");
    private final int waitSeconds = config.getInteger("WaitSeconds");

    private BaseSteps() {
        dataStorage = new HashMap<>();
        if (elementsMap == null) initElementMap(getFileList());
    }

    public static BaseSteps getInstance() {
        DriverManager driverManager = DriverManager.getInstance();
        webDriver = driverManager.getDriver();
        webWait = driverManager.getWebWait();
        if (jsExecutor == null) jsExecutor = driverManager.getJSExecutor();
        if (baseSteps == null) baseSteps = new BaseSteps();
        return baseSteps;
    }

    public By getByWithKey(String keyword) {
        try {
            ElementInfo elements = (ElementInfo) elementsMap.get(keyword);
            return getBy(elements);
        } catch (NullPointerException e) {
            String format = String.format("%s ile kayıtlı bir element bulunmamaktadır. Json dosyalarınızı kontrol ediniz.", keyword);
            loggerImps.errorAndFail(format);
            return null;
        }
    }

    public By getBy(ElementInfo element) {
        String locatorValue = element.getValue();
        String locatorType = element.getType();
        switch (locatorType) {
            case "id":
                return By.id(locatorValue);
            case "css":
                return By.cssSelector(locatorValue);
            case "xpath":
                return By.xpath(locatorValue);
            case "class":
                return By.className(locatorValue);
            case "linktext":
                return By.linkText(locatorValue);
            case "name":
                return By.name(locatorValue);
            case "partial":
                return By.partialLinkText(locatorValue);
            case "tagName":
                return By.tagName(locatorValue);
            default:
                loggerImps.errorAndFail("Desteklenen locator tipi girilmediği için test durduruldu.");
                return null;
        }
    }

    public void initElementMap(File[] fileList) {
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList;
        elementsMap = new ConcurrentHashMap<>();
        for (File file : fileList) {
            try {
                elementInfoList = gson.fromJson(new FileReader(file), elementType);
                elementInfoList.parallelStream().forEach(elementInfo -> elementsMap.put(elementInfo.getKey(), elementInfo));
            } catch (FileNotFoundException e) {
                loggerImps.warn("{} not found "+ e,"orange");
            }
        }
    }

    public File[] getFileList() {
        String elementsPath = config.getString("ElementsPath");
        try {
            return new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(elementsPath)).getFile()).listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".json"));
        } catch (Exception e) {
            String msg = "Belirtilen dosya bulunamadı. Dosya yolu = " + elementsPath;
            loggerImps.warn(msg,"orange");
            loggerImps.errorAndFail(e.getMessage());
            return null;
        }
    }

    public void moveToElementWithJS(WebElement element) {
        webWait.until(ExpectedConditions.visibilityOf(element));
        jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
        loggerImps.info(element.getText() + " elemente JS ile scroll edildi");
    }

    public WebElement findElement(String keyword) {
        WebElement element = null;
        By by = getByWithKey(keyword);
        try {
            element = webWait.until(ExpectedConditions.presenceOfElementLocated(by));
            if (!element.isDisplayed()) moveToElementWithJS(element);
        } catch (TimeoutException | NoSuchElementException e) {
            String format = String.format("Keyword %s locator %s olan element %s saniye boyunca her %s milisaniyede arandı bulunamadı.", keyword, by, waitSeconds, waitMillis);
            loggerImps.errorAndFail(format);
        } catch (InvalidElementStateException e) {
            waitSecondsWithoutLog(1);
            element = webWait.until(ExpectedConditions.presenceOfElementLocated(by));
        }
        return element;
    }

    public boolean isElementVisibleWithoutLog(String keyword) {
        try {
            webDriver.findElement(getByWithKey(keyword));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void setWebWait(int seconds, int milis) {
        webWait.withTimeout(Duration.ofSeconds(seconds)).pollingEvery(Duration.ofMillis(milis)).ignoring(NoSuchElementException.class);
    }

    public void waitSecondsWithoutLog(long second) {
        if (second <= 0) second = 1;
        try {
            Thread.sleep(1000 * second);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitMillisecondsWithoutLog(long miliSeconds) {
        try {
            Thread.sleep(miliSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitIfNotExist(String keyword) {
        if (!isElementVisibleWithoutLog(keyword)) {
            webWait.until(ExpectedConditions.visibilityOfElementLocated(getByWithKey(keyword)));
        }
    }

    public void waitIfNotExistWithTime(String keyword, int seconds) {
        setWebWait(seconds, waitMillis);
        if (!isElementVisibleWithoutLog(keyword)) {
            webWait.until(ExpectedConditions.visibilityOfElementLocated(getByWithKey(keyword)));
        }
        setWebWait(waitSeconds, waitMillis);
    }

    public void sendKeys(String keyword, String text) {
        String format = String.format("%s elementine %s text değeri girildi.", keyword, text);
        try {
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            loggerImps.info(format);
        } catch (StaleElementReferenceException e) {
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            loggerImps.info(format);
        } catch (InvalidElementStateException e) {
            waitMillisecondsWithoutLog(250);
            WebElement element = findElement(keyword);
            if (element.getText() != null) element.clear();
            element.sendKeys(text);
            loggerImps.info(format);
        } catch (Exception e) {
            e.printStackTrace();
            format = String.format("%s elementine %s text değeri girilemedi.", keyword, text);
            loggerImps.errorAndFail(format);
        }
    }

    public void click(String keyword) {
        try {
            findElement(keyword).click();
            loggerImps.info(keyword + " elementine tıklandı.","cyan");
        } catch (ElementClickInterceptedException | InvalidSelectorException e ) {
            clickWithJS(findElement(keyword));
            loggerImps.info(keyword + " elemente JS ile tıklandı.","cyan");
        } catch (StaleElementReferenceException e) {
             
            findElement(keyword).click();
            loggerImps.info(keyword + "  elementine tıklandı.","cyan");
        } catch (ElementNotInteractableException e) {
            findElement(keyword).click();
            loggerImps.info(keyword + "  elementine tıklandı.","cyan");
        } catch (Exception e) {
            loggerImps.errorAndFail(keyword + " elementine tıklanamadı." + e.getMessage());
        }
    }

    public void clickWithJS(WebElement element) {
        jsExecutor.executeScript("arguments[0].click();", element);
        loggerImps.info(element.getText() + " elementine JavaScript ile tıklandı.","blue");

    }

    public void mouseHover(String keyword) {
        Actions actions = new Actions(webDriver);
        WebElement element = findElement(keyword);
        actions.moveToElement(element).perform();
    }

    public void checkUrl(String expectedURL) {
        String currentUrl = webDriver.getCurrentUrl();
        Assertions.assertEquals(expectedURL, currentUrl,"Url adresi:" + expectedURL + "olması gerekirken Url:" + currentUrl + " 'dir.");
    }

    public void checkContainsUrl(String expectedURL) {
        String currentUrl = webDriver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedURL),"Url adresi:" + expectedURL + " olması gerekirken Url:" + currentUrl + " 'dir.");
    }

    public static void waitForPageToLoad(int timeoutInSeconds) {
        new WebDriverWait(webDriver, Duration.ofSeconds(timeoutInSeconds)).until(
                (ExpectedCondition<Boolean>) wd ->
                        ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete")
        );
    }

    public void goToUrl(String url) {
        webDriver.get(url);
    }

    public void selectFromDropDown(String text, String key) {
        Select select = new Select(findElement(key));
        select.selectByVisibleText(text);
    }

    public void verifyAllResultsContain(String key, String expectedText) {
        List<WebElement> results = webDriver.findElements(getByWithKey(key));

        for (WebElement result : results) {
            String text = result.getText();
            if (!text.contains(expectedText)) {
                throw new AssertionError("The result '" + text + "' does not contain the expected text: '" + expectedText + "'");
            }
        }

        System.out.println("All results contain the expected text: '" + expectedText + "'");
    }

    public void waitForStableText(String key, int stableDurationSeconds, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeoutSeconds));

        try {
            wait.until(driver -> {
                String previousText = "";
                String currentText = driver.findElement(getByWithKey(key)).getText();
                long start = System.currentTimeMillis();

                while ((System.currentTimeMillis() - start) < (stableDurationSeconds * 1000L)) {
                    try {
                        Thread.sleep(500); // yarım saniyede bir kontrol
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // thread flag restore edilir
                        throw new RuntimeException("Thread was interrupted while waiting for text to stabilize", e);
                    }

                    previousText = currentText;
                    currentText = driver.findElement(getByWithKey(key)).getText();

                    if (!previousText.equals(currentText)) {
                        // Text changed → reset the stability timer
                        start = System.currentTimeMillis();
                    }
                }

                // Metin stabil kaldı → başarıyla çıktı
                return true;
            });

            System.out.println("Element text is now stable.");
        } catch (TimeoutException e) {
            throw new RuntimeException("Element text did not stabilize within " + timeoutSeconds + " seconds.", e);
        }
    }
    public void scrollToElement(String key) {
        WebElement element = webDriver.findElement(getByWithKey(key));
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        System.out.println("Scrolled to element");
    }

    public void switchToNewTab() {
        String originalWindow = webDriver.getWindowHandle();
        Set<String> allWindows = webDriver.getWindowHandles();

        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                webDriver.switchTo().window(window);
                System.out.println("Switched to new tab.");
                return;
            }
        }

        throw new RuntimeException("No new tab found to switch to.");
    }


    public void setValueToStorage(String key, String value) {
        dataStorage.put(key, value);
    }

    public String getValueFromStorage(String key) {
        return dataStorage.get(key);
    }
}