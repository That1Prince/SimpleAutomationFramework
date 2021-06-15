import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class BasePage {
    private static final int TIMEOUT = 5;
    private static final int POLLING = 100;

    private final Logger log = LoggerFactory.getLogger(BasePage.class);
    protected final String testEnv = System.getProperty("testEnv").toLowerCase();

    protected WebDriver driver;
    private WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, TIMEOUT, POLLING);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, TIMEOUT), this);
    }

    protected void waitForElementToAppear(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForElementToDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForTextToDisappear(By locator, String text) {
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(locator, text)));
    }

    /**
     * Navigate to a provided url.
     * @param url the destination url.
     */
    protected void navigateTo(String url) {
        driver.get(url);
        driver.manage().window().maximize();
    }

    /**
     *  Handle a certificate error on a web page.
     */
    public void verifyCertificate() {
        if ("Certificate Error: Navigation Blocked".equalsIgnoreCase(driver.getTitle()))
            waitAndClick(driver.findElement(By.cssSelector("#overridelink")), 20);
    }

    /**
     * Checks if the element is displayed within specified seconds and logs exception if not.
     * @param ele the element to display status
     * @param time the time in seconds
     * @return boolean of is element displayed
     */
    protected boolean isDisplayed(WebElement ele, int time) {
        boolean status = false;
        try {
            waitFor(ele, time);
            status = ele.isDisplayed();
        } catch (Exception e) {
            log.info("Exception in wait for visible: " + e);
        }
        return status;
    }

    /**
     * Is the element enabled within the provided seconds?
     * @param ele the element
     * @param time the seconds
     * @return the boolean result
     */
    protected boolean isEnabled(WebElement ele, int time) {
        boolean status = false;
        try {
            waitFor(ele, time);
            status = ele.isEnabled();
        } catch (Exception e) {
            log.info("Exception in wait for visible: " + e);
        }
        return status;
    }

    /**
     * Wait for passed element to become visible and create a log entry for errors.
     * @param ele the page element
     * @param time the seconds to wait before throwing an exception
     */
    protected void waitFor(WebElement ele, int time) {
        try {
            new WebDriverWait(driver, time).until(ExpectedConditions.visibilityOf(ele));
        } catch (Exception e) {
           // log.info("Exception in wait for visible: " + e);
        }
    }

    /**
     * Wait up to 60 seconds for given element.
     * @param ele
     */
    protected void waitFor(WebElement ele) {
        try {
            new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(ele));
        } catch (Exception e) {
            log.info("Exception in wait for visible: " + e);
        }
    }

    /**
     * Mouse click simulation with a 30 second timeout to wait visibility.
     * @param ele the element to be clicked
     */
    protected void click(WebElement ele) {
        waitFor(ele, 30);
        ele.click();
    }

    /**
     * Alternative click simulation using JavaScript.
     * @param ele
     */
    protected void jsClick(WebElement ele) {
        waitFor(ele, 60);
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", ele);
    }

    /**
     * Wait for given element to be clickable and then click.
     * @param ele the element to be clicked
     * @param time the seconds to wait
     */
    protected void waitAndClick(WebElement ele, int time) {
        try {
            waitForClick(ele, time);
            click(ele);
        } catch (UnhandledAlertException e) {
            log.error(e.getMessage() + e);
        }
    }

    /**
     * Wait for the element to be clickable.
     * @param ele the element to wait for click-ability.
     * @param time the seconds to wait
     */
    protected void waitForClick(WebElement ele, int time) {
        try {
            new WebDriverWait(driver, time).until(ExpectedConditions.elementToBeClickable(ele));
        } catch (Exception e) {
            log.info("Exception in wait for clickable: " + e);
        }
    }

    /**
     * Set value of an element to given string.
     * @param ele the element
     * @param str the string to set element
     */
    protected void setValue(WebElement ele, String str) {
        waitFor(ele, 30);
        ele.sendKeys(str);
    }

    /**
     * Clear an element and then set value of string.
     * @param ele the element
     * @param str the string to set element
     */
    protected void clearAndSetValue(WebElement ele, String str) {
        waitFor(ele, 60);
        ele.clear();
        ele.sendKeys(str);
        ele.sendKeys(Keys.TAB);
    }

    /**
     * Select element by given string.
     * @param ele the element to select
     * @param str the string of element
     */
    protected void selectByText(WebElement ele, String str) {
        waitFor(ele, 30);
        Select select = new Select(ele);
        select.selectByVisibleText(str);
    }

    /**
     * Wait for element and then set value.
     * @param ele the element
     * @param str the string
     */
    protected void setValueAndWait(WebElement ele, String str) {
        waitFor(ele, 30);
        ele.sendKeys(str);
    }

    /**
     * Select a specific given index of an element.
     * @param ele the element
     * @param index the index to be selected
     */
    protected void selectByIndex(WebElement ele, int index) {
        waitFor(ele, 30);
        Select select = new Select(ele);
        select.selectByIndex(index);
    }

    /**
     * Search for specific given element within list of elements and the click it.
     * @param elements the elements to be searched
     * @param condition the string to search for
     */
    protected void elementListLoop(List<WebElement> elements, String condition) {
        for (WebElement ele : elements) {
            log.info("Available Elements: {}", ele.getText());
            if (ele.getText().contains(condition)) {
                waitAndClick(ele, 30);
                break;
            }
        }
    }

    /**
     * Select dropdown item by given value.
     * @param ele the dropdown element
     * @param value the value to be selected
     */
    protected void ddSelectionByValue(WebElement ele, String value) {
        waitFor(ele, 60);
        Select sel = new Select(ele);
        sel.selectByValue(value);
        log.info("Element selected from dropDown" + ele);
    }

    /**
     * Switches to the parent window.
     */
    public void switchToParentWindow() {
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[0]);
    }

    /**
     * Retrieves the Mac ID of this device.
     * @return the Mac ID of this device
     */
    protected String getMacID() {
        String macID = null;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macID = sb.toString().replace("-", "").toLowerCase();
        } catch (UnknownHostException | SocketException e) {
            log.error(e.getMessage() + e);
        }
        log.info("MAC ID: {}", macID);
        return macID;
    }

    /**
     * Retrieves device MacID and creates a MacId cookie.
     * @param driver
     */
    protected void setMacIDCookie(WebDriver driver) {
        String mac = getMacID();
        Cookie ck = new Cookie("MacId", mac);
        driver.manage().addCookie(ck);
        ck = new Cookie("NetID", mac);
        driver.manage().addCookie(ck);
        log.info("MAC Id: %s set.", mac);
    }

    /**
     * Sets a custom MacID cookie.
     * @param macID the MacId to be used
     * @param driver the WebDriver
     */
    public void setMacIDCookie(String macID, WebDriver driver) {
        Cookie ck = new Cookie("MacId", macID); // 00059a3c7800
        driver.manage().addCookie(ck);
        ck = new Cookie("NetID", macID); // 00059a3c7800
        driver.manage().addCookie(ck);
    }
}