package com.olexyn.tabdriver;

import java.nio.file.Path;

public class TestTabDriverConfig extends DefaultTabDriverConfig {

    @Override
    public Path getDriverPath() {
        return Path.of(System.getProperties().getProperty("user.dir"), "/src/test/resources/chromedriver_124");
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
