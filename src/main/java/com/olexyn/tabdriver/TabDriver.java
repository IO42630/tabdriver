package com.olexyn.tabdriver;


import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@SuppressWarnings("unused")
public class TabDriver implements JavascriptExecutor, ITabDriver {

    private final Map<String, Tab> tabs = new HashMap<>();
    private final ChromeDriver chromeDriver;

    @SuppressWarnings("deprecation")
    public TabDriver(TabDriverConfigProvider configProvider) {
        var path = configProvider.getDriverPath();
        var service = new ChromeDriverService.Builder()
            .usingDriverExecutable(path.toFile())
            .usingAnyFreePort()
            .build();

        chromeDriver = new ChromeDriver(service, configProvider.getOptions());
        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
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

    private List<WebElement> findElements(By by) {
        return chromeDriver.findElements(by);
    }



    private synchronized Tab getCurrentTab() {
        return tabs.get(getWindowHandle());
    }

    @Override
    public synchronized void goToTab(Purpose purpose) {
        tabs.values().stream()
            .filter(x -> Objects.equals(x.getPurpose(), purpose))
            .findFirst()
            .ifPresent(this::switchToTab);
    }

    private synchronized void switchToTab(Tab tab) {
        switchTo().window(tab.getHandle());
    }

    @Override
    public synchronized void newTab(Purpose purpose) {
        if (tabs.values().stream().anyMatch(x -> Objects.equals(x.getPurpose(), purpose))) {
            return;
        }
        switchTo().newWindow(WindowType.TAB);
        String handle = getWindowHandle();
        Tab tab = new Tab(handle, purpose);
        tabs.put(handle, tab);
    }


    public synchronized void refresh() {
        navigate().refresh();
    }

    public synchronized void get(String url) {
        chromeDriver.get(url);
    }

    private synchronized Optional<WebElement> findElement(By by) {
        try {
            return Optional.of(chromeDriver.findElement(by));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized void executeScript(String script) {
        ((JavascriptExecutor) this).executeScript(script);
    }

    public synchronized void sendDeleteKeys(WebElement element, int n) {
        for (int i = 0; i < n; i++) {
            element.sendKeys(Keys.BACK_SPACE);
        }
    }



    private static final String FRAME_ID_DEFAULT_CONTENT = "defaultContent";
    private static final String FRAME_ID_NONE_FOUND = "noneFound";



    public synchronized String findFrameContainingCharSeq(Map<String, String> mapOfCollectedSources, String string) {
        for (Entry<String, String> entry : mapOfCollectedSources.entrySet()) {
            if (entry.getValue().contains(string)) {
                return entry.getKey();
            }
        }
        return FRAME_ID_NONE_FOUND;
    }

    @Override
    public @Nullable Object executeScript(String script, Object... args) {
        return null;
    }

    @Override
    public @Nullable Object executeAsyncScript(String script, Object... args) {
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

    public synchronized void followContainedLink(@NonNull WebElement element) {
        String link = element.getAttribute("href");
        if (link != null) { navigate().to(link); }
    }


    public synchronized void setRadio(WebElement element, boolean checked) {
        ((JavascriptExecutor) this).executeScript("arguments[0].checked = " + checked + ';', element);
    }

    public synchronized void setComboByDataValue(@NonNull WebElement combo , String dataValue) {
        combo.click();
        combo.findElement(By.cssSelector("li[data-value='" + dataValue + "']")).click();
    }


    public synchronized Optional<WebElement> findByCss(String css) {
        return findElement(By.cssSelector(css));
    }

    public synchronized Optional<WebElement> findByCssAndText(String css, String text) {
        return findElements(By.cssSelector(css))
                .stream()
                .filter(elem -> {
                    var textContent = elem.getAttribute("textContent");
                    return textContent != null && textContent.contains(text);
                })
                .findFirst();
    }

    public synchronized Optional<WebElement> findByCss(WebElement context, String css) {
        try {
            return Optional.of(context.findElement(By.cssSelector(css)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized List<WebElement> findAllByCss(String css) {
        try {
            return findElements(By.cssSelector(css));
        } catch (Exception e) {
            return List.of();
        }
    }

    public synchronized List<WebElement> findAllByCss(WebElement context, String css) {
        try {
            return context.findElements(By.cssSelector(css));
        } catch (Exception e) {
            return List.of();
        }
    }


    public synchronized Optional<WebElement> getByText(String text) {
        return findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
    }

    public synchronized void click(WebElement we) {
        executeScript("arguments[0].click();", we);
    }

}
