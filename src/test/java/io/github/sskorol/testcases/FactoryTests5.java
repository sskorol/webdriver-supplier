package io.github.sskorol.testcases;

import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests5 {

    @Test
    public void test17() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test18() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test19() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test20() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }
}
