package com.olexyn.tabdriver;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.nio.file.Path;

public interface TabDriverConfigProvider {


    Path getDriverPath();

    String getDownloadDir();


    boolean isHeadless();

    DesiredCapabilities getCapabilities();

}
