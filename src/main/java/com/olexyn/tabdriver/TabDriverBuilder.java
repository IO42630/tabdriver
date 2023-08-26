package com.olexyn.tabdriver;

import java.nio.file.Path;
import java.util.HashMap;

import com.olexyn.PropConf;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

@UtilityClass
public class TabDriverBuilder {

	private static Path CONFIG_PATH;

	public static TabDriver build(Path path) {
		CONFIG_PATH = path;
		if (CONFIG_PATH == null) {
			throw new RuntimeException("CONFIG_PATH must be set to an absolute path.");
		}
		return new TabDriver(configureService(), configureCapabilities());
	}


	private static ChromeDriverService configureService() {
		PropConf.loadProperties(CONFIG_PATH.toString());
		var path = Path.of(PropConf.get("chrome.driver.path"));
		return new ChromeDriverService.Builder()
				.usingDriverExecutable(path.toFile())
				.usingAnyFreePort()
				.build();
	}

	private static DesiredCapabilities configureCapabilities() {
		var cap = DesiredCapabilities.chrome();
		cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		if (PropConf.is("headless")) {
			options.addArguments("--window-size=1920,1080");
			options.addArguments("--headless");
		}
		// see also https://chromium.googlesource.com/chromium/src/+/master/chrome/common/pref_names.cc
		HashMap<String, Object> chromePrefs = new HashMap<>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", PropConf.get("download.dir"));
		chromePrefs.put("download.prompt_for_download", false);
		options.setExperimentalOption("prefs", chromePrefs);
		cap.setCapability(ChromeOptions.CAPABILITY, options);
		return cap;
	}




}
