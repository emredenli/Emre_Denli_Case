package com.insider.imp;

import com.thoughtworks.gauge.Step;
import com.insider.steps.BaseSteps;
import lombok.extern.log4j.Log4j;
import org.openqa.selenium.support.ui.Select;

@Log4j
public class StepImps {
    private final BaseSteps baseSteps;
    private final LoggerImps loggerImps = LoggerImps.getInstance(log);
    public static int DEFAULT_SECOND_WAIT_AMOUNT = 20;

    public StepImps() {
        baseSteps = BaseSteps.getInstance();
    }

    @Step({"Click the <key> element"})
    public void click(String key) {
        baseSteps.waitIfNotExistWithTime(key, DEFAULT_SECOND_WAIT_AMOUNT);
        baseSteps.click(key);
    }

    @Step({"<text> textini <key> elemente yaz"})
    public void sendKeys(String text, String keyword) {
        baseSteps.sendKeys(keyword, text);
    }

    @Step({"Verify that the <key> element is visible"})
    public void waitIfNotExist(String key) {
        baseSteps.waitIfNotExist(key);
    }

    @Step({"<key> elementinin gorunur olmasi bekle"})
    public void waitIfNotExistWithTime(String key) {
        baseSteps.waitIfNotExistWithTime(key, DEFAULT_SECOND_WAIT_AMOUNT);
    }

    @Step({"Wait for <int> seconds"})
    public void waitBySeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Hover over the <key> element")
    public void hover(String key) {
        baseSteps.mouseHover(key);
    }

    @Step({"Verify that the current URL the value <url>"})
    public void checkURLRepeat(String expectedURL) {
        baseSteps.waitForPageToLoad(15);
        baseSteps.checkUrl(expectedURL);
    }

    @Step({"Verify that the current URL contains the value <url>"})
    public void checkURLContainsRepeat(String expectedURL) {
        baseSteps.waitForPageToLoad(15);
        baseSteps.checkContainsUrl(expectedURL);
    }

    @Step({"Go to <url>"})
    public void goToUrl(String url) {
        baseSteps.goToUrl(url);
        baseSteps.waitForPageToLoad(15);
    }

    @Step({"Select <text> from the <key> dropdown"})
    public void selectFromDropDown(String text, String key) {
        baseSteps.selectFromDropDown(text, key);
    }

    @Step({"Verify that all results with key <key> contain <expectedText>"})
    public void checkAllResultsContain(String key, String text) {
        baseSteps.verifyAllResultsContain(key, text);
    }

    @Step({"Wait until the text of element with key <key> becomes stable for <stableSeconds> seconds within <timeoutSeconds> seconds"})
    public void waitForTextToBeStable(String key, int stableSeconds, int timeoutSeconds) {
        baseSteps.waitForStableText(key, stableSeconds, timeoutSeconds);
    }

    @Step("Scroll to the element with key <key>")
    public void scrollToElementWithKey(String key) {
        baseSteps.scrollToElement(key);
    }

    @Step("Switch to the newly opened tab")
    public void switchToNewTabStep() {
        baseSteps.switchToNewTab();
    }





}