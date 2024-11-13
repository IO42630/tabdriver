package com.olexyn.tabdriver;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class TabDriverTest {


    TabDriver tabDriver;

    @BeforeEach
    public void setUp() {
        tabDriver = new TabDriver(new TestTabDriverConfig());
    }

    @Disabled
    @Test
    public void test() {
        var google = new Purpose("GOOGLE");
        var youtube = new Purpose("YOUTUBE");
        tabDriver.newTab(google);
        tabDriver.get("https://www.google.com");
        tabDriver.newTab(youtube);
        tabDriver.get("https://www.youtube.com");
        tabDriver.goToTab(google);
        int br = 0;
    }


    @AfterEach
    public void tearDown() {
        tabDriver.quit();
    }

}
