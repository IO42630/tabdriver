package com.olexyn.tabdriver;

import com.olexyn.min.log.LogU;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.olexyn.tabdriver.Constants.ABOUT_BLANK;

@SuppressWarnings("unused")
public class TabDriver implements JavascriptExecutor {

    private final Map<String, Tab> tabs = new HashMap<>();
    private final ChromeDriver  chromeDriver;

    @SuppressWarnings("deprecation")
    public TabDriver(TabDriverConfigProvider configProvider) {
        var path = configProvider.getDriverPath();
        var service = new ChromeDriverService.Builder()
            .usingDriverExecutable(path.toFile())
            .usingAnyFreePort()
            .build();

        chromeDriver = new ChromeDriver(service, configProvider.getOptions());
        chromeDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
    }

    public WebDriver.Navigation navigate() {
        return chromeDriver.navigate();
    }

    public WebDriver.TargetLocator switchTo() {
        return chromeDriver.switchTo();
    }

    public String getWindowHandle() {
        return chromeDriver.getWindowHandle();
    }

    public Set<String> getWindowHandles() {
        return chromeDriver.getWindowHandles();
    }

    public String getTitle() {
        return chromeDriver.getTitle();
    }

    public String getCurrentUrl() {
        return chromeDriver.getCurrentUrl();
    }

    public String getPageSource() {
        return chromeDriver.getPageSource();
    }

    public void close() {
        chromeDriver.close();
    }

    public void quit() {
        chromeDriver.quit();
    }

    public List<WebElement> findElements(By by) {
        return chromeDriver.findElements(by);
    }

    public synchronized void registerCurrentTab(String purpose) {
        Tab tab = new Tab(getWindowHandle());
        tab.setName(getTitle());
        tab.setUrl(getCurrentUrl());
        tab.setPurpose(purpose);
        tabs.put(tab.getHandle(), tab);
    }

    public synchronized Tab getCurrentTab() {
        return tabs.get(getWindowHandle());
    }

    public synchronized List<Tab> getTabByPurpose(String purpose) {
        return tabs.values().stream()
            .filter(x -> Objects.equals(x.getPurpose(), purpose))
            .toList();
    }

    public synchronized String registerBlankTab(String purpose) {
        Set<String> openTabHandles = getWindowHandles();
        for (String openTabHandle : openTabHandles) {
            if (!tabs.containsKey(openTabHandle)) {
                Tab blankTab = new Tab(openTabHandle);
                blankTab.setName(ABOUT_BLANK);
                blankTab.setUrl(ABOUT_BLANK);
                blankTab.setPurpose(purpose);
                tabs.put(openTabHandle, blankTab);
                return openTabHandle;
            }
        }
        LogU.warnPlain("Not unregistered tab found.");
        return null;
    }

    public synchronized String registerExistingTab(String purpose) {
        String handle = getWindowHandle();
        Tab tab = new Tab(handle);
        tab.setName(getTitle());
        tab.setUrl(getCurrentUrl());
        tab.setPurpose(purpose);
        tabs.put(handle, tab);
        return handle;
    }

    public synchronized void switchToTab(String handle) {
        for (Entry<String, Tab> entry : tabs.entrySet()) {
            String tabHandle = entry.getKey();
            if (tabHandle.equals(handle)) {
                switchTo().window(tabHandle);
            }
        }
    }

    public synchronized void switchToTab(Tab tab) {
        switchTo().window(tab.getHandle());
    }

    /**
     * Opens a new tab, and "moves" the WebDriver to the new tab.
     * If the current tab is empty, it is registered - this happens usually only with the initial tab of the session.
     */
    public synchronized void newTab(String purpose) {
        String currentUrl = getCurrentUrl(); // TODO this throws error, just get a new window
        if (currentUrl.isEmpty()
            || currentUrl.equals("data:,")
            || currentUrl.equals(ABOUT_BLANK)) {
            registerExistingTab(purpose);
        } else {
            executeScript("window.open(arguments[0])");
            Optional.ofNullable(registerBlankTab(purpose))
                .ifPresent(this::switchToTab);
        }
    }

    public synchronized void switchToTabByPurpose(String purpose) {
        List<Tab> existingTabs = getTabByPurpose(purpose);
        if (!existingTabs.isEmpty()) {
            switchToTab(existingTabs.get(0));
        } else {
            Tab currentTab = getCurrentTab();

        }
    }

    public synchronized void refresh() {
        navigate().refresh();
    }

    public synchronized void get(String url) {
        chromeDriver.get(url);
    }

    public synchronized WebElement findElement(By by) {
        return chromeDriver.findElement(by);
    }

    public synchronized void executeScript(String script) {
        ((JavascriptExecutor) this).executeScript(script);
    }

    public synchronized void sendDeleteKeys(WebElement element, int n) {
        for (int i = 0; i < n; i++) {
            element.sendKeys(Keys.BACK_SPACE);
        }
    }

    public synchronized WebElement filterElementListBy(List<WebElement> list, CRITERIA criteria, String text) {
        for (WebElement element : list) {
            String toEvaluate = switch (criteria) {
                case CLASS -> element.getClass().getName();
                case TEXT -> element.getText();
                case TAG -> element.getTagName();
                case HREF -> element.getAttribute("href");
                case ID -> element.getAttribute("id");
                case TITLE -> element.getAttribute("title");
                case NONE -> text;
            };
            if (toEvaluate != null && toEvaluate.contains(text)) { return element; }
        }
        return null;
    }

    private static final String FRAME_ID_DEFAULT_CONTENT = "defaultContent";
    private static final String FRAME_ID_NONE_FOUND = "noneFound";

    /**
     * Collects all frames accessible to WebDriver.
     */
    public synchronized Map<String, String> collectAllFrames() throws NoSuchFrameException {
        Map<String, String> mapOfCollectedSources = new HashMap<>();
        switchTo().defaultContent();
        mapOfCollectedSources.put(FRAME_ID_DEFAULT_CONTENT, getPageSource());
        for (int i = 0; i < 10; i++) {
            try {
                switchTo().defaultContent();
                switchTo().frame(i);
                mapOfCollectedSources.put(String.valueOf(i), getPageSource());
            } catch (NoSuchFrameException e) {
                return mapOfCollectedSources;
            }
        }
        return mapOfCollectedSources;
    }

    public synchronized String findFrameContainingCharSeq(Map<String, String> mapOfCollectedSources, String string) {
        for (Entry<String, String> entry : mapOfCollectedSources.entrySet()) {
            if (entry.getValue().contains(string)) {
                return entry.getKey();
            }
        }
        return FRAME_ID_NONE_FOUND;
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return null;
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return null;
    }


    public enum CRITERIA {
        CLASS,
        TEXT,
        TAG,
        HREF,
        NONE,
        ID,
        TITLE
    }

    public synchronized void switchToFrameContainingCharSeq(String charSeq) {
        switchTo().defaultContent();
        sleep(500);
        final String frameId = findFrameContainingCharSeq(collectAllFrames(), charSeq);
        sleep(400);
        switch (frameId) {
            case FRAME_ID_DEFAULT_CONTENT:
                switchTo().defaultContent();
                break;
            case FRAME_ID_NONE_FOUND:
                break;
            default:
                switchTo().frame(Integer.parseInt(frameId));
        }
    }

    public static void sleep(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            LogU.warnPlain("SLEEP was INTERRUPED.");
        }
    }

    /**
     * Return the first occurrence of specified class that has specified label.
     */
    public synchronized WebElement getWhere(String className, CRITERIA criteria, String text) {
        switchToFrameContainingCharSeq(text);
        List<WebElement> elements = findElements(By.className(className));
        return filterElementListBy(elements, criteria, text);
    }

    public synchronized WebElement getWhere(String className) {
        switchToFrameContainingCharSeq(className);
        List<WebElement> elements = findElements(By.className(className));
        return filterElementListBy(elements, CRITERIA.NONE, Constants.EMPTY);
    }

    public synchronized void followContainedLink(WebElement element) {
        String link = element.getAttribute("href");
        if (link != null) { navigate().to(link); }
    }


    public synchronized void setRadio(WebElement element, boolean checked) {
        ((JavascriptExecutor) this).executeScript("arguments[0].checked = " + checked + ";", element);
    }

    public synchronized void setComboByDataValue(By comboBy, String dataValue) {
        WebElement combo = findElement(comboBy);
        combo.click();
        combo.findElement(By.cssSelector("li[data-value='" + dataValue + "']")).click();
    }


    public synchronized Optional<WebElement> findByCss(String css) {
        try {
            return Optional.of(findElement(By.cssSelector(css)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized Optional<WebElement> findByCss(WebElement context, String css) {
        try {
            return Optional.of(context.findElement(By.cssSelector(css)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Any-Match.
     */
    public synchronized WebElement getByFieldValue(String type, String field, String value) {
        return findElement(By.cssSelector(type + "[" + field + "*='" + value + "']"));
    }

    public synchronized void clickByFieldValue(String type, String field, String value) {
        var we = getByFieldValue(type, field, value);
        click(we);
    }

    /**
     * @param field can contain wildcards like "class*" or "class^"
     * @param value can contain partial matches like "last-"
     */
    public synchronized WebElement getByFieldValue(SearchContext searchContext, String type, String field, String value) {

        return searchContext.findElement(By.cssSelector(type + "[" + field + "='" + value + "']"));
    }

    public synchronized WebElement getByText(String text) {
        return findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
    }

    public synchronized void click(WebElement we) {
        executeScript("arguments[0].click();", we);
    }

}
