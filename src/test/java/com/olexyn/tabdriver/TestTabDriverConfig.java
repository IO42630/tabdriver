package com.olexyn.tabdriver;

import java.nio.file.Path;

public class TestTabDriverConfig extends DefaultTabDriverConfig {

    @Override
    public Path getDriverPath() {
        return Path.of(System.getProperties().getProperty("user.home"), "/home/apps/chrome_155/chromedriver");
    }

    @Override
    public String getDownloadDir() {
        return "";
    }

    @Override
    public boolean isHeadless() {
        return false;
    }
}
