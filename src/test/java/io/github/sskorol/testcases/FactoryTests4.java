package io.github.sskorol.testcases;

import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests4 {

    @Test
    public void test13() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test14() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test15() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test16() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }
}
