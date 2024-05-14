package com.olexyn.tabdriver;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.HashMap;

public abstract class DefaultTabDriverConfig  implements TabDriverConfigProvider {


    @Override
    public ChromeOptions getOptions() {

        ChromeOptions options = new ChromeOptions();
        options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        options.addArguments("--start-maximized");
        if (isHeadless()) {
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--headless");
        }
        // see also https://chromium.googlesource.com/chromium/src/+/master/chrome/common/pref_names.cc
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", getDownloadDir());
        chromePrefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", chromePrefs);
        return options;
    }
}
