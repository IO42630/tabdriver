package com.olexyn.tabdriver;

import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Path;

public interface TabDriverConfigProvider {


    Path getDriverPath();

    String getDownloadDir();


    boolean isHeadless();

    ChromeOptions getOptions();

}
